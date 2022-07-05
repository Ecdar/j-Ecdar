package parser;

import logic.GraphEdge;
import logic.GraphNode;
import models.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
public class JSONParser {

    private static ArrayList<JSONObject> objectList = new ArrayList<>();
    private static final ArrayList<Channel> globalChannels = new ArrayList<>();
    private static final List<Clock> componentClocks = new ArrayList<>();
    private static final List<BoolVar> BVs = new ArrayList<>();

    public static Automaton[] parse(String folderPath, boolean makeInpEnabled) throws FileNotFoundException {
        File dir = new File(folderPath + "/Components");
        File[] files = dir.listFiles((dir1, name) -> name.endsWith(".json"));
        System.out.println(folderPath);
        ArrayList<String> locations = new ArrayList<>(Collections.singletonList(folderPath + "/GlobalDeclarations.json"));
        if (files != null) {
            locations.addAll(Arrays.stream(files).map(File::toString).collect(Collectors.toList()));
        }else {
            throw new FileNotFoundException("Could not find any .json files at location: " + folderPath + "/Components");
        }

        objectList = parseFiles(locations);

        return distrubuteObjects(objectList, makeInpEnabled);
    }

    public static Automaton parseJsonString(String json, boolean makeInpEnabled) throws ParseException {
        JSONObject obj = (JSONObject) JSONValue.parseWithException(json);

        return distrubuteObject(obj, makeInpEnabled);
    }

    public static Automaton parse(String base, String component, boolean makeInputEnabled) {
        return parse(base, new String[]{ component }, makeInputEnabled)[0];
    }

    public static Automaton[] parse(String base, String[] components, boolean makeInpEnabled) {
        ArrayList<String> locations = Arrays.stream(components).map(c -> base + c).collect(Collectors.toCollection(ArrayList::new));

        objectList = parseFiles(locations);
        return distrubuteObjects(objectList, makeInpEnabled);
    }

    public static String writeRefinement(GraphNode refTree) {
        JSONObject obj = new JSONObject();
        List<GraphEdge> children = refTree.getSuccessors();
        obj.put("initial sp id", "" + refTree.getNodeId());
        obj.put("left", "" + refTree.getStatePair().getLeft().getLocation());
        obj.put("right", "" + refTree.getStatePair().getRight().getLocation());
        obj.put("federation", "" + refTree.getStatePair().getLeft().getCDD());
        obj.put("transitions", helper(children));

        System.out.println(obj.toJSONString());
        return obj.toJSONString();
    }

    private static JSONArray helper(List<GraphEdge> children) {
        JSONArray transitions = new JSONArray();
        for (GraphEdge child : children) {
            JSONObject transition = new JSONObject();

            if (child.getSubsetZone() == null) {
                JSONObject statePair = new JSONObject();
                statePair.put("state pair id", "" + child.getTarget().getNodeId());
                statePair.put("left", "" + child.getTarget().getStatePair().getLeft().getLocation());
                statePair.put("right", "" + child.getTarget().getStatePair().getRight().getLocation());
                statePair.put("federation", "" + child.getTarget().getStatePair().getLeft().getCDD());
                transition.put("source sp id", "" + child.getSource().getNodeId());
                transition.put("target sp id", "" + child.getTarget().getNodeId());
                transition.put("target sp", statePair);
                transitions.add(transition);
                if (child.getTarget().getSuccessors().size() != 0)
                    statePair.put("transitions", helper(child.getTarget().getSuccessors()));
            } else {
                transition.put("source sp id", "" + child.getSource().getNodeId());
                transition.put("target sp id", "" + child.getTarget().getNodeId());
                transitions.add(transition);
            }
        }


        return transitions;
    }
    //---------------------------Testing-----------------

    private static ArrayList<JSONObject> parseFiles(ArrayList<String> locations) {
        org.json.simple.parser.JSONParser parser = new org.json.simple.parser.JSONParser();
        ArrayList<JSONObject> returnList = new ArrayList<>();

        try {
            for (String location : locations) {
                Object obj = parser.parse(new FileReader(location));
                returnList.add((JSONObject) obj);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return returnList;
    }
    private static String automatonName;
    private static Automaton distrubuteObject(JSONObject obj, boolean makeInpEnabled){
        automatonName= (String) obj.get("name");
        addDeclarations((String) obj.get("declarations"));
        JSONArray locationList = (JSONArray) obj.get("locations");
        List<Location> locations = addLocations(locationList);
        JSONArray edgeList = (JSONArray) obj.get("edges");
        List<Edge> edges = addEdges(edgeList, locations);
        Automaton automaton = new Automaton((String) obj.get("name"), locations, edges, new ArrayList<>(componentClocks), BVs, makeInpEnabled);
        componentClocks.clear();
        BVs.clear();
        return automaton;
    }

    private static Automaton[] distrubuteObjects(ArrayList<JSONObject> objList, boolean makeInpEnabled) {
        ArrayList<Automaton> automata = new ArrayList<>();

        for (JSONObject obj : objList) {
            if (!obj.get("name").toString().equals("Global Declarations")&&!obj.get("name").toString().equals("System Declarations")) {
                automata.add(distrubuteObject(obj, makeInpEnabled));
            }
        }


        return automata.toArray(new Automaton[0]);
    }

    private static void addDeclarations(String declarations) {//add for typedefs
        String[] firstList = declarations.split(";");

        for (String value : firstList) {
            boolean isClock = value.contains("clock");

            if (isClock) {
                String clocks = value;

                clocks = clocks.replaceAll("//.*\n", "")
                        .replaceFirst("clock", "");

                clocks = clocks.replaceAll("clock", ",")
                        .replaceAll(";", "")
                        .replaceAll(" ", "")
                        .replaceAll("\n", "");

                String[] clockArr = clocks.split(",");

                for (String s : clockArr) {
                    componentClocks.add(new Clock(s, automatonName));
                }
            }
        }
    }

    private static void addBVs(String declarations) {//add for typedefs
        String[] firstList = declarations.split(";");

        for (String line : firstList) {
            boolean isBV = line.contains("bool");

            if (isBV) {
                if (line.contains("bool")) {
                    String bools = line.replaceAll("//.*\n", "")
                            .replaceFirst("bool", "");

                    bools = bools.replaceAll("bool", ",")
                            .replaceAll(";", "")
                            .replaceAll(" ", "")
                            .replaceAll("\n", "");

                    String[] boolArr = bools.split(",");

                    for (String bool : boolArr)
                        if (bool.contains("="))
                            BVs.add(new BoolVar(bool.split("=")[0], automatonName, Boolean.parseBoolean(bool.split("=")[1])));
                        else BVs.add(new BoolVar(bool,automatonName, false));
                }
            }
        }
    }

    private static List<Location> addLocations(JSONArray locationList) {
        ArrayList<Location> returnLocList = new ArrayList<>();

        for (Object obj : locationList) {
            JSONObject jsonObject = (JSONObject) obj;

            boolean isInitial, isUniversal, isInconsistent;
            isInitial = isUniversal = isInconsistent = false;

            switch (jsonObject.get("type").toString()) {
                case "INITIAL":
                    isInitial = true;
                    break;
                case "UNIVERSAL":
                    isUniversal = true;
                    break;
                case "INCONSISTENT":
                    isInconsistent = true;
                    break;
            }

            boolean isNotUrgent = "NORMAL".equals(jsonObject.get("urgency").toString());

            Guard invariant = ("".equals(jsonObject.get("invariant").toString()) ? new TrueGuard() :
                    GuardParser.parse(jsonObject.get("invariant").toString(), componentClocks, BVs));
            Location loc = new Location(jsonObject.get("id").toString(), invariant, isInitial, !isNotUrgent,
                    isUniversal, isInconsistent);

            returnLocList.add(loc);
        }

        return returnLocList;
    }

    private static Clock findClock(String clockName) {
        for (Clock clock : componentClocks)
            if (clock.getOriginalName().equals(clockName)) return clock;

        return null;
    }
    private static BoolVar findBV(String name) {
        for (BoolVar bv : BVs)
            if (bv.getOriginalName().equals(name))
                return bv;

        return null;
    }

    private static List<Edge> addEdges(JSONArray edgeList, List<Location> locations) {
        ArrayList<Edge> edges = new ArrayList<>();

        for (Object obj : edgeList) {
            JSONObject jsonObject = (JSONObject) obj;

            Guard guards;
            List<ClockUpdate> clockUpdates = new ArrayList<>();
            List<BoolUpdate> boolUpdates = new ArrayList<>();

            List<Update> updates;

            if (!jsonObject.get("guard").toString().equals("")) {
                guards = GuardParser.parse((String) jsonObject.get("guard"), componentClocks, BVs);
            } else
                guards = new TrueGuard();

            if (!jsonObject.get("update").toString().equals(""))
                updates = UpdateParser.parse((String) jsonObject.get("update"), componentClocks, BVs);
            else
                updates = new ArrayList<>();

            for (Update u: updates)
                if (u instanceof  ClockUpdate)
                    clockUpdates.add((ClockUpdate) u);
                else
                    boolUpdates.add((BoolUpdate) u);

            List<Update> updatesList = new ArrayList<>();
            updatesList.addAll(clockUpdates);
            updatesList.addAll(boolUpdates);

            Location sourceLocation = findLoc(locations, (String) jsonObject.get("sourceLocation"));
            Location targetLocation = findLoc(locations, (String) jsonObject.get("targetLocation"));

            boolean isInput = "INPUT".equals(jsonObject.get("status").toString());

            Channel c = addChannel(jsonObject.get("sync").toString());
            if (c != null) {
                Edge edge = new Edge(sourceLocation, targetLocation, c, isInput, guards, updatesList);
                edges.add(edge);
            }
        }
        return edges;
    }

    private static Channel addChannel(String name) {
        if (name.equals("*")) return null;

        for (Channel channel : globalChannels)
            if (channel.getName().equals(name))
                return channel;

        Channel chan = new Channel(name);
        globalChannels.add(chan);
        return chan;
    }

    //Helper method for addEdge in order to find which Location is source and which one is target
    private static Location findLoc(List<Location> locations, String name) {
        List<Location> locs = locations.stream().filter(location -> location.getName().equals(name)).collect(Collectors.toList());

        if (locs.isEmpty()) return null;

        return locs.get(0);
    }
}