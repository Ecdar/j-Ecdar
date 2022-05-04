package logic;

import models.Automaton;
import org.json.simple.parser.ParseException;
import parser.JSONParser;
import parser.XMLParser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class Controller {
    private static List<SimpleTransitionSystem> transitionSystems = new ArrayList<>();
    private static final int FEATURE_REFINEMENT = 0;
    private static final int FEATURE_COMPOSITION = 1;
    private static final int FEATURE_CONJUNCTION = 2;
    private static final int FEATURE_QUOTIENT = 3;

    public static List<Query> handleRequest(String location, String query, boolean trace) throws Exception {
        List<Query> queries = new ArrayList<>();
        List<String> queryStrings = Arrays.asList(query.split(";"));
        for (int i = 0; i < queryStrings.size(); i++){
            queries.add(new Query(queryStrings.get(i)));
        }

        ArrayList<String> temp = new ArrayList<>(Arrays.asList(location.split(" ")));
        boolean isJson = temp.get(0).equals("-json");
        String folderLoc = temp.get(1);

        parseComponents(folderLoc, isJson); // Parses components and adds them to local variable cmpt

        return handleQueries(queries, trace);
    }

    public static Query handleRequest(String queryString) throws Exception {
        Query query = new Query(queryString);
        return handleQuery(query, false);
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

    private static Query handleQuery(Query query,boolean trace){
        switch (query.getType()){
            case REFINEMENT:
                return handleRefinement(query, trace);
            case CONSISTENCY:
                return handleConsistency(query);
            case IMPLEMENTATION:
                return handleImplementation(query);
            case DETERMINISM:
                return handleDeterminism(query);
            case GET_COMPONENT:
                return handleGetComponent(query);
            case BISIM_MINIM:
                return handleBisimMinim(query);
            case PRUNE:
                return handlePrune(query);
            default:
                throw new RuntimeException("Invalid query type");
        }
    }

    private static Query handleRefinement(Query query, boolean trace){
        List<String> refSplit = Arrays.asList(query.getQuery().split("<="));
        Refinement ref = new Refinement(runQuery(refSplit.get(0)), runQuery(refSplit.get(1)));
        boolean refCheck;
        if (trace) {
            refCheck = ref.check(true);
            query.setResult(refCheck);
            if(refCheck){
                query.addResultString(JSONParser.writeRefinement(ref.getTree()));
            }
        }
        else {
            query.setResult(ref.check());
        }

        if (!query.getResult()) {
            query.addResultString(ref.getErrMsg());
        }
        return query;
    }

    private static Query handleConsistency(Query query){
        TransitionSystem ts = runQuery(query.getQuery());
        query.setResult(ts.isLeastConsistent());
        if(!query.getResult()){
            query.addResultString(ts.getLastErr());
        }
        return query;
    }

    private static Query handleImplementation(Query query){
        TransitionSystem ts = runQuery(query.getQuery());
        query.setResult(ts.isImplementation());
        if(!query.getResult()){
            query.addResultString(ts.getLastErr());
        }
        return query;
    }

    private static Query handleDeterminism(Query query){
        TransitionSystem ts = runQuery(query.getQuery());
        query.setResult(ts.isDeterministic());
        if(!query.getResult()){
            query.addResultString(ts.getLastErr());
        }
        return query;
    }

    private static Query handleGetComponent(Query query){
        TransitionSystem ts = runQuery(query.getQuery());
        saveAutomaton(ts.getAutomaton(), query.getComponentName());
        return query;
    }

    private static Query handleBisimMinim(Query query){
        TransitionSystem ts = runQuery(query.getQuery());
        Automaton aut = ts.getAutomaton();

        aut = Bisimilarity.checkBisimilarity(aut);

        saveAutomaton(aut, query.getComponentName());
        return query;
    }

    private static Query handlePrune(Query query){
        TransitionSystem ts = runQuery(query.getQuery());
        Automaton aut = ts.getAutomaton();

        SimpleTransitionSystem simp = Pruning.pruneIncTimed(new SimpleTransitionSystem(aut));
        aut = simp.pruneReachTimed().getAutomaton();

        saveAutomaton(aut, query.getComponentName());
        return query;
    }

    private static List<Query> handleQueries(List<Query> queries, boolean trace){
        List<Query> returnlist = new ArrayList<>();

        for (Query query: queries) {
            Query queryResult = handleQuery(query, trace);
            returnlist.add(queryResult);
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
}
