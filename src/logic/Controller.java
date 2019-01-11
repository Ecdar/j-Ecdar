package logic;

import models.Component;
import parser.Parser;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Controller {
    private static String folderLoc;
    private static List<String> Queries = new ArrayList<>();
    private static ArrayList<Component> cmpt = new ArrayList<>();
    private static final int FEATURE_REFINEMENT = 0;
    private static final int FEATURE_COMPOSITION = 1;
    private static final int FEATURE_CONJUNCTION = 2;
    private static final int FEATURE_QUOTIENT = 3;

    public Controller() {

    }

    public List<Boolean> parseFiles(String locQuery) {
        folderLoc = "";
        cmpt.clear();
        Queries.clear();
        separateLocQuery(locQuery); // Separates location and Queries
        parseComponents(folderLoc); // Parses components and adds them to local variable cmpt
        return runQueries();
    }

    public void separateLocQuery(String locQuery) {
        ArrayList<String> temp = new ArrayList<>();
        temp.addAll(Arrays.asList(locQuery.split(",")));
        folderLoc = temp.get(0);
        temp.remove(0);
        Queries.addAll(temp);
    }

    public ArrayList<Component> parseComponents(String folderLocation) {
        cmpt = Parser.parse(folderLocation);
        return cmpt;
    }

    public List<Boolean> runQueries() {
        List<Boolean> returnlist = new ArrayList<Boolean>();
        for (int i = 0; i < Queries.size(); i++) {
            Queries.set(i, Queries.get(i).replaceAll("\\s+", ""));
            if (Queries.get(i).contains("refinement")) {
                List<String> refSplit = Arrays.asList(Queries.get(i).replace("refinement:", "").split("<="));
                Refinement ref = new Refinement(runQuery(refSplit.get(0)), runQuery(refSplit.get(1)));
                returnlist.add(ref.check());
            }
            //add if contains specification or smth else
        }
        return returnlist;
    }

    public TransitionSystem runQuery(String part) {
        ArrayList<TransitionSystem> transitionSystems = new ArrayList<>();
        if (part.charAt(0) == '(' && part.length() > 0) {
            part = part.substring(1);
        }
        int feature = -1;
        outerLoop:
        for (int i = 0; i < part.length(); i++) {
            if (part.charAt(i) == '(') {
                int tempPosition = checkParentheses(part);
                transitionSystems.add(runQuery(part.substring(i, tempPosition)));
                part = part.substring(tempPosition, part.length());
                i = 0;
            }

            if (Character.isLetter(part.charAt(i)) || Character.isDigit(part.charAt(i))) {
                int j = 0;
                boolean check = true;
                while (check) {
                    if (i + j < part.length()) {
                        if (!Character.isLetter(part.charAt(i + j)) && !Character.isDigit(part.charAt(i + j))) {
                            transitionSystems.add(new SimpleTransitionSystem(findComponent(part.substring(i, j + i))));
                            j--;
                            check = false;
                        }
                    } else {
                        transitionSystems.add(new SimpleTransitionSystem(findComponent(part.substring(i, j + i))));
                        break outerLoop;
                    }
                    j++;
                }
                i += j;
            }

            if(feature == -1) feature = setFeature(part.charAt(i));
        }

        return getTransitionSystem(feature, transitionSystems);
    }

    private TransitionSystem getTransitionSystem(int feature, ArrayList<TransitionSystem> transitionSystems){
        switch (feature) {
            case FEATURE_COMPOSITION:
                return new Composition(transitionSystems);
            case FEATURE_CONJUNCTION:
                return new Conjunction(transitionSystems);
            case FEATURE_QUOTIENT:
                break;
            default:
                break;
        }
        return transitionSystems.get(0);
    }

    private int setFeature(char x) {
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

    // Finds and returns Component given the name of that component
    private Component findComponent(String str) {
        for (int i = 0; i < cmpt.size(); i++) {
            if (cmpt.get(i).getName().equalsIgnoreCase(str)) {
                return cmpt.get(i);
            }
        }
        System.out.println("Component does not exist  " + str);
        return null;
    }

    // Returns the index for string at which the first encountered parenthesis closes
    private int checkParentheses(String smth) {
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


}
