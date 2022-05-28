package logic;

import logic.query.Query;
import models.Automaton;
import org.json.simple.parser.ParseException;
import parser.JSONParser;
import parser.QueryParser;
import parser.XMLParser;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class Controller {
    private static List<SimpleTransitionSystem> transitionSystems = new ArrayList<>();

    public static List<Query> handleRequest(String location, String queryString, boolean trace) throws Exception {
        ArrayList<String> temp = new ArrayList<>(Arrays.asList(location.split(" ")));
        boolean isJson = temp.get(0).equals("-json");
        String folderLoc = temp.get(1);

        parseComponents(folderLoc, isJson); // Parses components and adds them to local variable cmpt

        return QueryParser.parse(queryString, transitionSystems);
    }

    public static Query handleRequest(String queryString) throws Exception {
        List<Query> queries = QueryParser.parse(queryString, transitionSystems);
        return queries.get(0);
    }

    public static void parseComponents(String folderLocation, boolean isJson) throws FileNotFoundException {
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

    public static void saveToDisk(String location){
        for(TransitionSystem system : transitionSystems){
            JsonAutomatonEncoder.writeToJson(system.getAutomaton(), location);
        }
    }

    public static void saveAutomaton(Automaton aut, String name){
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
