package parser;

import models.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Parser {

    private static ArrayList<JSONObject> objectList = new ArrayList<>();
    private static final ArrayList<Channel> globalChannels = new ArrayList<>();
    private static final List<Clock> componentClocks = new ArrayList<>();

    public static Automaton[] parse(String folderPath) {
        File dir = new File(folderPath + "/Components");
        File[] files = dir.listFiles((dir1, name) -> name.endsWith(".json"));

        ArrayList<String> locations = new ArrayList<>(Collections.singletonList(folderPath + "/GlobalDeclarations.json"));
        locations.addAll(Arrays.stream(files).map(File::toString).collect(Collectors.toList()));

        objectList = parseFiles(locations);
        return distrubuteObjects(objectList);
    }

    public static Automaton[] parse(String base, String[] components) {
        ArrayList<String> locations = Arrays.stream(components).map(c -> base + c).collect(Collectors.toCollection(ArrayList::new));

        objectList = parseFiles(locations);
        return distrubuteObjects(objectList);
    }

    //---------------------------Testing-----------------

    private static ArrayList<JSONObject> parseFiles(ArrayList<String> locations) {
        JSONParser parser = new JSONParser();
        ArrayList<JSONObject> returnList = new ArrayList<>();

        try {
            for (String location : locations) {
                Object obj = parser.parse(new FileReader(location));
                returnList.add((JSONObject) obj);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return returnList;
    }

    private static Automaton[] distrubuteObjects(ArrayList<JSONObject> objList) {
        ArrayList<Automaton> automata = new ArrayList<>();

        try {
            for (JSONObject obj : objList) {
                if (!obj.get("name").toString().equals("Global Declarations")) {
                    addDeclarations((String) obj.get("declarations"));
                    JSONArray locationList = (JSONArray) obj.get("locations");
                    Location[] locations = addLocations(locationList);
                    JSONArray edgeList = (JSONArray) obj.get("edges");
                    Edge[] edges = addEdges(edgeList, locations);
                    Automaton automaton = new Automaton((String) obj.get("name"), locations, edges, componentClocks.toArray(new Clock[0]));
                    automata.add(automaton);
                    componentClocks.clear();
                } else {
                    addDeclarations((String) obj.get("declarations"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return automata.toArray(new Automaton[0]);
    }

    private static void addDeclarations(String declarations) {//add for typedefs
        String[] firstList = declarations.split(";");

        for (int i = 0; i < firstList.length; i++) {
            boolean isChan = firstList[i].contains("broadcast chan");
            boolean isClock = firstList[i].contains("clock");

            if (isChan || isClock) {
                firstList[i] = firstList[i].replaceFirst("^broadcast chan", "");//get rid of starting text
                firstList[i] = firstList[i].replaceFirst("^clock", "");//get rid of starting text
                firstList[i] = firstList[i].replaceAll("\\s+", ""); //get rid of spaces
                String[] secondList = (firstList[i].split(","));

                for (String s : secondList) {
                    if (isChan) globalChannels.add(new Channel(s));
                    if (isClock) componentClocks.add(new Clock(s));
                }
            }
        }
    }

    private static Location[] addLocations(JSONArray locationList) {
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

            Guard[] invariant = ("".equals(jsonObject.get("invariant").toString()) ? new Guard[]{} :
                    addGuards(jsonObject.get("invariant").toString()));
            Location loc = new Location(jsonObject.get("id").toString(), invariant, isInitial, !isNotUrgent,
                    isUniversal, isInconsistent);

            returnLocList.add(loc);
        }

        return returnLocList.toArray(new Location[0]);
    }

    private static Guard[] addGuards(String invariant) {
        ArrayList<Guard> guards = new ArrayList<>();
        String[] listOfInv = invariant.split("&&");

        for (String str : listOfInv) {
            String symbol = "";
            boolean strict, greater;
            strict = false;
            greater = false;

            if (str.contains("<=")) {
                symbol = "<=";
            }
            if (str.contains(">=")) {
                symbol = ">=";
                greater = true;
            }
            if (str.contains("<") && !str.contains("=")) {
                symbol = "<";
                strict = true;
            }
            if (str.contains(">") && !str.contains("=")) {
                symbol = ">";
                greater = true;
                strict = true;
            }

            String[] s = str.split(symbol);
            for (int x = 0; x < s.length; x++) {
                s[x] = s[x].replaceAll(" ", "");
            }
            guards.add(new Guard(findClock(s[0]), Integer.parseInt(s[1]), greater, strict));
        }

        return guards.toArray(new Guard[0]);
    }

    private static Clock findClock(String clockName) {
        for (Clock clock : componentClocks)
            if (clock.getName().equals(clockName)) return clock;

        return null;
    }

    private static Edge[] addEdges(JSONArray edgeList, Location[] locations) {
        ArrayList<Edge> edges = new ArrayList<>();

        for (Object obj : edgeList) {
            JSONObject jsonObject = (JSONObject) obj;

            Guard[] guards;
            Update[] updates;

            if (!jsonObject.get("guard").toString().equals(""))
                guards = addGuards((String) jsonObject.get("guard"));
            else
                guards = new Guard[]{};

            if (!jsonObject.get("update").toString().equals(""))
                updates = addUpdates((String) jsonObject.get("update"));
            else
                updates = new Update[]{};

            Location sourceLocation = findLoc(locations, (String) jsonObject.get("sourceLocation"));
            Location targetLocation = findLoc(locations, (String) jsonObject.get("targetLocation"));

            boolean isInput = "INPUT".equals(jsonObject.get("status").toString());

            List<Channel> c = globalChannels.stream().filter(channel -> channel.getName().equals(jsonObject.get("sync"))).collect(Collectors.toList());
            if (!c.isEmpty()) {
                Edge edge = new Edge(sourceLocation, targetLocation, c.get(0), isInput, guards, updates);
                edges.add(edge);
            }
        }
        return edges.toArray(new Edge[0]);
    }

    private static Update[] addUpdates(String update) {
        ArrayList<Update> updates = new ArrayList<>();
        String[] listOfInv = update.split(",");

        for (String str : listOfInv) {
            String[] s = str.split("=");
            for (int i = 0; i < s.length; i++)
                s[i] = s[i].replaceAll(" ", "");
            Update upd = new Update(findClock(s[0]), Integer.parseInt(s[1]));
            updates.add(upd);
        }

        return updates.toArray(new Update[0]);
    }

    //Helper method for addEdge in order to find which Location is source and which one is target
    private static Location findLoc(Location[] locations, String name) {
        List<Location> locs = Arrays.stream(locations).filter(location -> location.getName().equals(name)).collect(Collectors.toList());

        if (locs.isEmpty()) return null;

        return locs.get(0);
    }
}