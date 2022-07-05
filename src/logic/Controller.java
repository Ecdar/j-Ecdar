package logic;

import logic.query.Query;
import models.Automaton;
import org.json.simple.parser.ParseException;
import parser.JSONParser;
import parser.QueryParser;
import parser.XMLParser;

import java.io.FileNotFoundException;
import models.Clock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Controller {
    private static List<Automaton> automata = new ArrayList<>();
    private static List<Clock> clocksInCurrentQuery = new ArrayList<>();

    public static List<Query> handleRequest(String location, String queryString, boolean trace) throws Exception {
        ArrayList<String> temp = new ArrayList<>(Arrays.asList(location.split(" ")));
        boolean isJson = temp.get(0).equals("-json");
        String folderLoc = temp.get(1);

        parseComponents(folderLoc, isJson); // Parses components and adds them to local variable cmpt

        return QueryParser.parse(queryString, automata);
    }

    public static Query handleRequest(String queryString) throws Exception {
        List<Query> queries = QueryParser.parse(queryString, automata);
        return queries.get(0);
    }

    public static void parseComponents(String folderLocation, boolean isJson) throws FileNotFoundException {
        Automaton[] cmpt = isJson ? JSONParser.parse(folderLocation, true) : XMLParser.parse(folderLocation, true);
        for (Automaton automaton : cmpt) {
            automata.add(automaton);
        }
    }

    public static void parseComponentJson(String json) throws ParseException {
        Automaton automaton = JSONParser.parseJsonString(json, true);
        automata.add(automaton);
    }

    public static void parseComponentXml(String xml) {
        Automaton[] automatons = XMLParser.parseXmlString(xml, true);
        for (Automaton automaton : automatons) {
            automata.add(automaton);
        }
    }

    public static void saveToDisk(String location){
        for(Automaton system : automata){
            JsonAutomatonEncoder.writeToJson(system, location);
        }
    }

    public static void saveAutomaton(Automaton aut, String name){
        if(name != null){
            aut.setName(name);
            automata.add(aut);
        }
    }

    public static String getJsonComponent(String componentName) {
        TransitionSystem transitionSystem = findComponent(componentName);
        return JsonAutomatonEncoder.getAutomatonAsJson(transitionSystem.getAutomaton());
    }

    // Finds and returns Automaton given the name of that component
    private static TransitionSystem findComponent(String str) {
        for (Automaton aut : automata)
            if (aut.getName().equalsIgnoreCase(str)) {
                clocksInCurrentQuery.addAll(aut.getClocks());
                return new SimpleTransitionSystem(aut);
            }

        return null;
    }
}
