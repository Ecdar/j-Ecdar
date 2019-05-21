package logic;

import models.Automaton;
import models.Transition;
import parser.JSONParser;
import parser.XMLParser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Controller {
    private static final List<String> Queries = new ArrayList<>();
    private static List<SimpleTransitionSystem> transitionSystems = new ArrayList<>();
    private static final int FEATURE_REFINEMENT = 0;
    private static final int FEATURE_COMPOSITION = 1;
    private static final int FEATURE_CONJUNCTION = 2;
    private static final int FEATURE_QUOTIENT = 3;

    public static List<String> handleRequest(String locQuery, boolean trace) throws Exception {
        Queries.clear();

        // Separates location and Queries
        ArrayList<String> temp = new ArrayList<>(Arrays.asList(locQuery.split(" ")));
        boolean isJson = temp.get(0).equals("-json");
        String folderLoc = temp.get(1);

        temp.remove(1);
        temp.remove(0);
        Queries.addAll(temp);

        parseComponents(folderLoc, isJson); // Parses components and adds them to local variable cmpt

        return runQueries(trace);
    }

    public static void parseComponents(String folderLocation, boolean isJson) {
        Automaton[] cmpt = isJson ? JSONParser.parse(folderLocation, true) : XMLParser.parse(folderLocation, true);
        for (Automaton automaton : cmpt) {
            transitionSystems.add(new SimpleTransitionSystem(automaton));
        }
    }

    private static List<String> runQueries(boolean trace) throws Exception {
        List<String> returnlist = new ArrayList<>();

        for (int i = 0; i < Queries.size(); i++) {
            isQueryValid(Queries.get(i));
            Queries.set(i, Queries.get(i).replaceAll("\\s+", ""));
            if (Queries.get(i).contains("refinement")) {
                List<String> refSplit = Arrays.asList(Queries.get(i).replace("refinement:", "").split("<="));
                Refinement ref = new Refinement(runQuery(refSplit.get(0)), runQuery(refSplit.get(1)));
                boolean refCheck;
                if(trace) {
                    refCheck = ref.check(true);
                    returnlist.add(refCheck ? "true " + JSONParser.writeRefinement(ref.getTree()) : "false ");
                }
                else {
                    refCheck = ref.check();
                    returnlist.add(refCheck ? "true" : "false");
                }

                if (i == Queries.size()-1 && refCheck) continue;
                returnlist.add("\n");
                if (!refCheck) returnlist.add(ref.getErrMsg());
            }
            if (Queries.get(i).contains("consistency")) {
                String cons = Queries.get(i).replace("consistency:", "");
                TransitionSystem ts = runQuery(cons);
                boolean passed = ts.isLeastConsistent();
                returnlist.add(String.valueOf(passed));
                if(!passed) returnlist.add("\n" + ts.getLastErr());
            }
            if (Queries.get(i).contains("implementation")) {
                String impl = Queries.get(i).replace("implementation:", "");
                TransitionSystem ts = runQuery(impl);
                boolean passed = ts.isImplementation();
                returnlist.add(String.valueOf(passed));
                if(!passed) returnlist.add("\n" + ts.getLastErr());
            }
            if (Queries.get(i).contains("determinism")) {
                String impl = Queries.get(i).replace("determinism:", "");
                TransitionSystem ts = runQuery(impl);
                boolean passed = ts.isDeterministic();
                returnlist.add(String.valueOf(passed));
                if(!passed) returnlist.add("\n" + ts.getLastErr());
            }
            //add if contains specification or smth else
        }

        return returnlist;
    }

    public static TransitionSystem runQuery(String part) {
        ArrayList<TransitionSystem> transitionSystems = new ArrayList<>();

        if (part.charAt(0) == '(') part = part.substring(1);
        int feature = -1;

        outerLoop:
        for (int i = 0; i < part.length(); i++) {
            if (part.charAt(i) == '(') {
                int tempPosition = checkParentheses(part);
                transitionSystems.add(runQuery(part.substring(i, tempPosition)));
                part = part.substring(tempPosition);
                i = 0;
            }

            if (Character.isLetter(part.charAt(i)) || Character.isDigit(part.charAt(i))) {
                int j = 0;
                boolean check = true;
                while (check) {
                    if (i + j < part.length()) {
                        if (!Character.isLetter(part.charAt(i + j)) && !Character.isDigit(part.charAt(i + j))) {
                            transitionSystems.add(findComponent(part.substring(i, j + i)));
                            j--;
                            check = false;
                        }
                    } else {
                        transitionSystems.add(findComponent(part.substring(i, j + i)));
                        break outerLoop;
                    }
                    j++;
                }
                i += j;
            }

            if (feature == -1) feature = setFeature(part.charAt(i));
        }

        return getTransitionSystem(feature, transitionSystems);
    }

    private static TransitionSystem getTransitionSystem(int feature, List<TransitionSystem> transitionSystems) {
        switch (feature) {
            case FEATURE_COMPOSITION:
                return new Composition(transitionSystems.toArray(new TransitionSystem[0]));
            case FEATURE_CONJUNCTION:
                return new Conjunction(transitionSystems.toArray(new TransitionSystem[0]));
            case FEATURE_QUOTIENT:
                break;
            default:
                break;
        }

        return transitionSystems.get(0);
    }

    private static int setFeature(char x) {
        switch (x) {
            case '|':
                return FEATURE_COMPOSITION;
            case '&':
                return FEATURE_CONJUNCTION;
            case '/':
                return FEATURE_QUOTIENT;
            default:
                return -1;
        }
    }

    // Finds and returns Automaton given the name of that component
    private static TransitionSystem findComponent(String str) {
        for (SimpleTransitionSystem ts : transitionSystems)
            if (ts.getName().equalsIgnoreCase(str)) return ts;

        System.out.println("Automaton does not exist  " + str);
        return null;
    }

    // Returns the index for string at which the first encountered parenthesis closes
    private static int checkParentheses(String smth) {
        int balanced = 0;
        boolean seePar = false;
        for (int i = 0; i < smth.length(); i++) {
            if (smth.charAt(i) == '(') {
                balanced++;
                seePar = true;
            } else if (smth.charAt(i) == ')' && seePar) {
                balanced--;
            }
            if (balanced == 0 && seePar) {
                return i;
            }
        }
        return -1;
    }

    public static void isQueryValid(String query) throws Exception {
        checkRefinementSyntax(query);
        isParBalanced(query);
        beforeAfterParantheses(query);
        checkSyntax(query);
    }

    private static void checkRefinementSyntax(String query) throws Exception {
        if (query.contains("<=") && !query.contains("refinement:")) throw new Exception("Expected: \"refinement:\"");

        if (query.matches(".*<=.*<=.*")) throw new Exception("There can only be one refinement");
    }

    private static void isParBalanced(String query) throws Exception {
        int counter = 0;

        for (int i = 0; i < query.length(); i++) {
            if (query.charAt(i) == '(') {
                counter++;
            }
            if (query.charAt(i) == ')') {
                counter--;
            }
        }

        if (counter != 0) throw new Exception("Parentheses are not balanced");
    }

    private static void beforeAfterParantheses(String query) throws Exception {
        String testString = "/=|&:(";

        for (int i = 0; i < query.length(); i++) {
            if (query.charAt(i) == '(') {
                if (i != 0) {
                    if (testString.indexOf(query.charAt(i - 1)) == -1)
                        throw new Exception("Before opening Parentheses can be either operator or second Parentheses");
                }
                if (i + 1 < query.length()) {
                    if (!(query.charAt(i + 1) == '(' || Character.isLetter(query.charAt(i + 1)) || Character.isDigit(query.charAt(i + 1))))
                        throw new Exception("After opening Parentheses can be either other Parentheses or component");
                }
            }
        }
    }

    private static void checkSyntax(String query) throws Exception {
        String testString = "/=|&:";
        for (int i = 0; i < query.length(); i++) {
            if (testString.indexOf(query.charAt(i)) != -1) {
                return;
            }
        }
        throw new Exception("Incorrect syntax, does not contain any feature");
    }
}
