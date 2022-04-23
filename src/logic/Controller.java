package logic;

import Exceptions.InvalidQueryException;
import models.Automaton;
import org.json.simple.parser.ParseException;
import parser.JSONParser;
import parser.JsonFileWriter;
import parser.XMLParser;

import models.Clock;
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
    private static List<Clock> clocksInCurrentQuery = new ArrayList<>();

    public static List<String> handleRequest(String location, String query, boolean trace) throws Exception {
        addQueries(query);

        // Separates location and Queries
        ArrayList<String> temp = new ArrayList<>(Arrays.asList(location.split(" ")));
        boolean isJson = temp.get(0).equals("-json");
        String folderLoc = temp.get(1);

        parseComponents(folderLoc, isJson); // Parses components and adds them to local variable cmpt

        return runQueries(trace);
    }

    public static String handleRequest(String query) throws Exception {
        addQueries(query);
        List<String> responseList = runQueries(false);

        return String.join(" ", responseList);
    }

    private static void addQueries(String query){
        Queries.clear();
        Queries.addAll(Arrays.asList(query.split(";")));
    }

    public static void parseComponents(String folderLocation, boolean isJson) {
        Automaton[] cmpt = isJson ? JSONParser.parse(folderLocation, true) : XMLParser.parse(folderLocation, true);
        for (Automaton automaton : cmpt) {
            transitionSystems.add(new SimpleTransitionSystem(automaton));
        }
    }

    public static void parseComponentJson(String json) throws ParseException {
        Automaton automaton = JSONParser.parseJsonString(json, true);
        transitionSystems.add(new SimpleTransitionSystem(automaton));
    }

    public static void parseComponentXml(String xml) {
        Automaton[] automatons = XMLParser.parseXmlString(xml, true);
        for (Automaton automaton : automatons) {
            transitionSystems.add(new SimpleTransitionSystem(automaton));
        }
    }


    private static List<String> runQueries(boolean trace) throws Exception {
        List<String> returnlist = new ArrayList<>();

        for (int i = 0; i < Queries.size(); i++) {
            Queries.set(i, Queries.get(i).replaceAll("\\s+", ""));
            isQueryValid(Queries.get(i));
            String componentName = null;
            if(Queries.get(i).contains("save-as")){
                String[] saveQuery = Queries.get(i).split("save-as");
                Queries.set(i, saveQuery[0]);
                componentName = saveQuery[1];
            }
            if (Queries.get(i).contains("refinement")) {
                List<String> refSplit = Arrays.asList(Queries.get(i).replace("refinement:", "").split("<="));
                TransitionSystem left = runQuery(refSplit.get(0));
                TransitionSystem right = runQuery(refSplit.get(1));

                Refinement ref = new Refinement(left, right);

                boolean refCheck;
                if (trace) {
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
                clocksInCurrentQuery = new ArrayList<>();
                TransitionSystem ts = runQuery(cons);
                boolean passed = ts.isLeastConsistent();
                returnlist.add(String.valueOf(passed));
                if(!passed) returnlist.add("\n" + ts.getLastErr());
            }
            if (Queries.get(i).contains("implementation")) {
                String impl = Queries.get(i).replace("implementation:", "");
                clocksInCurrentQuery = new ArrayList<>();
                TransitionSystem ts = runQuery(impl);

                boolean passed = ts.isImplementation();
                returnlist.add(String.valueOf(passed));
                if(!passed) returnlist.add("\n" + ts.getLastErr());
            }
            if (Queries.get(i).contains("determinism")) {
                String impl = Queries.get(i).replace("determinism:", "");
                clocksInCurrentQuery = new ArrayList<>();
                TransitionSystem ts = runQuery(impl);
                boolean passed = ts.isDeterministic();
                returnlist.add(String.valueOf(passed));
                if(!passed) returnlist.add("\n" + ts.getLastErr());
            }
            if(Queries.get(i).contains("get-component")){
                String query = Queries.get(i).replace("get-component:", "");
                clocksInCurrentQuery= new ArrayList<>();
                TransitionSystem ts = runQuery(query);
                saveAutomaton(ts.getAutomaton(), componentName);
            }
            if(Queries.get(i).contains("bisim-minim")){
                String impl = Queries.get(i).replace("bisim-minim:", "");
                clocksInCurrentQuery=new ArrayList<>();
                TransitionSystem ts = runQuery(impl);

                Automaton aut = ts.getAutomaton();

                aut = Bisimilarity.checkBisimilarity(aut);

                saveAutomaton(aut, componentName);
            }
            if(Queries.get(i).contains("prune")){
                String impl = Queries.get(i).replace("prune:", "");
                clocksInCurrentQuery = new ArrayList<>();
                TransitionSystem ts = runQuery(impl);
                Automaton aut = ts.getAutomaton();

                SimpleTransitionSystem simp = Pruning.pruneIncTimed(new SimpleTransitionSystem(aut));
                aut = simp.pruneReachTimed().getAutomaton();

                saveAutomaton(aut, componentName);
            }
            //add if contains specification or smth else
        }

        return returnlist;
    }

    public static void saveToDisk(String location){
        for(TransitionSystem system : transitionSystems){
            JsonFileWriter.writeToJson(system.getAutomaton(), location);
        }
    }

    private static void saveAutomaton(Automaton aut, String name){
        if(name != null){
            aut.setName(name);
            SimpleTransitionSystem system = new SimpleTransitionSystem(aut);
            transitionSystems.add(system);
        }
    }
/*
    public static List<Automaton> getAutomata(List<String> parts) {
        ArrayList<Automaton> automata = new ArrayList<>();
        for (String part: parts) {
            if (part.charAt(0) == '(') part = part.substring(1);
            int feature = -1;

            outerLoop:
            for (int i = 0; i < part.length(); i++) {
                if (part.charAt(i) == '(') {
                    int tempPosition = checkParentheses(part);
                    ArrayList newList = new ArrayList<>();
                    newList.add(part.substring(i, tempPosition));
                    automata.addAll(getAutomata(newList));
                    part = part.substring(tempPosition);
                    i = 0;
                }

                if (Character.isLetter(part.charAt(i)) || Character.isDigit(part.charAt(i))) {
                    int j = 0;
                    boolean check = true;
                    while (check) {
                        if (i + j < part.length()) {
                            if (!Character.isLetter(part.charAt(i + j)) && !Character.isDigit(part.charAt(i + j))) {
                                automata.add(findComponent(part.substring(i, j + i)).getAutomaton());
                                j--;
                                check = false;
                            }
                        } else {
                            automata.add(findComponent(part.substring(i, j + i)).getAutomaton());
                            break outerLoop;
                        }
                        j++;
                    }
                    i += j;
                }

                if (feature == -1) feature = setFeature(part.charAt(i));
            }
        }
        return getTransitionSystem(feature, transitionSystems);
    }*/


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
                return new Quotient(transitionSystems.toArray(new TransitionSystem[0])[0],transitionSystems.toArray(new TransitionSystem[0])[1]); // TODO: Check if correct
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
            if (ts.getName().equalsIgnoreCase(str)) {
                clocksInCurrentQuery.addAll(ts.getAutomaton().getClocks());
                return ts;
            }

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

    private static void checkRefinementSyntax(String query) throws InvalidQueryException {
        if (query.contains("<=") && !query.contains("refinement:")) throw new InvalidQueryException("Expected: \"refinement:\"");

        if (query.matches(".*<=.*<=.*")) throw new InvalidQueryException("There can only be one refinement");
    }

    private static void isParBalanced(String query) throws InvalidQueryException {
        int counter = 0;

        for (int i = 0; i < query.length(); i++) {
            if (query.charAt(i) == '(') {
                counter++;
            }
            if (query.charAt(i) == ')') {
                counter--;
            }
        }

        if (counter != 0) throw new InvalidQueryException("Parentheses are not balanced");
    }

    private static void beforeAfterParantheses(String query) throws InvalidQueryException {
        String testString = "/=|&:(";

        for (int i = 0; i < query.length(); i++) {
            if (query.charAt(i) == '(') {
                if (i != 0) {
                    if (testString.indexOf(query.charAt(i - 1)) == -1)
                        throw new InvalidQueryException("Before opening Parentheses can be either operator or second Parentheses");
                }
                if (i + 1 < query.length()) {
                    if (!(query.charAt(i + 1) == '(' || Character.isLetter(query.charAt(i + 1)) || Character.isDigit(query.charAt(i + 1))))
                        throw new InvalidQueryException("After opening Parentheses can be either other Parentheses or component");
                }
            }
        }
    }

    private static void checkSyntax(String query) throws InvalidQueryException {
        String testString = "/=|&:";
        for (int i = 0; i < query.length(); i++) {
            if (testString.indexOf(query.charAt(i)) != -1) {
                return;
            }
        }
        throw new InvalidQueryException("Incorrect syntax, does not contain any feature");
    }
}
