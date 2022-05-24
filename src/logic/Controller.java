package logic;

import models.Automaton;
import org.json.simple.parser.ParseException;
import parser.JSONParser;
import parser.QueryParser;
import parser.XMLParser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class Controller {
    private static List<SimpleTransitionSystem> transitionSystems = new ArrayList<>();

    public static List<Query> handleRequest(String location, String query, boolean trace) throws Exception {
        ArrayList<String> temp = new ArrayList<>(Arrays.asList(location.split(" ")));
        boolean isJson = temp.get(0).equals("-json");
        String folderLoc = temp.get(1);

        parseComponents(folderLoc, isJson); // Parses components and adds them to local variable cmpt

        List<Query> queries = QueryParser.parse(query, transitionSystems);
        return handleQueries(queries, trace);
    }

    public static Query handleRequest(String queryString) throws Exception {
        List<Query> queries = QueryParser.parse(queryString, transitionSystems);
        return handleQuery(queries.get(0), false);
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
        Refinement ref = new Refinement(query.getSystem1(), query.getSystem2());
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
        TransitionSystem ts = query.getSystem1();
        query.setResult(ts.isLeastConsistent());
        if(!query.getResult()){
            query.addResultString(ts.getLastErr());
        }
        return query;
    }

    private static Query handleImplementation(Query query){
        TransitionSystem ts = query.getSystem1();
        query.setResult(ts.isImplementation());
        if(!query.getResult()){
            query.addResultString(ts.getLastErr());
        }
        return query;
    }

    private static Query handleDeterminism(Query query){
        TransitionSystem ts = query.getSystem1();
        query.setResult(ts.isDeterministic());
        if(!query.getResult()){
            query.addResultString(ts.getLastErr());
        }
        return query;
    }

    private static Query handleGetComponent(Query query){
        TransitionSystem ts = query.getSystem1();
        saveAutomaton(ts.getAutomaton(), query.getComponentName());
        return query;
    }

    private static Query handleBisimMinim(Query query){
        TransitionSystem ts = query.getSystem1();
        Automaton aut = ts.getAutomaton();

        aut = Bisimilarity.checkBisimilarity(aut);

        saveAutomaton(aut, query.getComponentName());
        return query;
    }

    private static Query handlePrune(Query query){
        TransitionSystem ts = query.getSystem1();
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
            JsonAutomatonEncoder.writeToJson(system.getAutomaton(), location);
        }
    }

    private static void saveAutomaton(Automaton aut, String name){
        if(name != null){
            aut.setName(name);
            SimpleTransitionSystem system = new SimpleTransitionSystem(aut);
            transitionSystems.add(system);
        }
    }

    public static String getJsonComponent(String componentName) {
        TransitionSystem transitionSystem = findComponent(componentName);
        return JsonAutomatonEncoder.getAutomatonAsJson(transitionSystem.getAutomaton());
    }

    // Finds and returns Automaton given the name of that component
    private static TransitionSystem findComponent(String str) {
        for (SimpleTransitionSystem ts : transitionSystems)
            if (ts.getName().equalsIgnoreCase(str)) return ts;

        System.out.println("Automaton does not exist  " + str);
        return null;
    }
}
