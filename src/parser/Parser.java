package parser;

import models.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class Parser {

    private static ArrayList<JSONObject> objectList = new ArrayList<>();
    private static ArrayList<Channel> globalChannels = new ArrayList<>();
    private static Set<Clock> componentClocks = new HashSet<>();

    public static ArrayList<Component> parse(String base, List<String> components) {
        ArrayList<String> locations = new ArrayList<>();

        for (String component : components) {
            locations.add(base + component);
        }

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

    private static ArrayList<Component> distrubuteObjects(ArrayList<JSONObject> objList) {
        ArrayList<Component> components = new ArrayList<>();
        try {
            for (JSONObject obj : objList) {
                if (!obj.get("name").toString().equals("Global Declarations")) {
                    addDeclarations((String) obj.get("declarations"));
                    JSONArray locationList = (JSONArray) obj.get("locations");
                    ArrayList<Location> locations = addLocations(locationList);
                    JSONArray edgeList = (JSONArray) obj.get("edges");
                    ArrayList<Transition> transitions = addEdges(edgeList, locations);
                    // make copy of clocks, since calling componentClocks.clear() will empty it and we lose this information
                    Component component = new Component((String) obj.get("name"), locations, transitions, new ArrayList<>(componentClocks));
                    components.add(component);
                    componentClocks.clear();
                } else {
                    addDeclarations((String) obj.get("declarations"));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return components;
    }

    private static void addDeclarations(String declarations) {//add for typedefs
        String[] firstList = (declarations.split(";"));
        for (int i = 0; i < firstList.length; i++) {
            if (firstList[i].contains("broadcast chan")) {
                firstList[i] = firstList[i].replaceFirst("^broadcast chan", "");//get rid of starting text
                firstList[i] = firstList[i].replaceAll("\\s+", ""); //get rid of spaces
                String[] secondList = (firstList[i].split(","));
                for (int j = 0; j < secondList.length; j++) {
                    Channel chan = new Channel(secondList[j]);
                    globalChannels.add(chan);
                }
            }
            if (firstList[i].contains("clock")) {
                firstList[i] = firstList[i].replaceFirst("^clock", "");//get rid of starting text
                firstList[i] = firstList[i].replaceAll("\\s+", ""); //get rid of spaces
                String[] secondList = (firstList[i].split(","));
                for (int j = 0; j < secondList.length; j++) {
                    Clock clock = new Clock(secondList[j]);
                    componentClocks.add(clock);
                }
            }
        }
    }

    private static ArrayList<Location> addLocations(JSONArray locationList) {
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

            List<Guard> invariant = ("".equals(jsonObject.get("invariant").toString()) ? null :
                    addGuards(jsonObject.get("invariant").toString()));
            Location loc = new Location(jsonObject.get("id").toString(), invariant, isInitial, !isNotUrgent,
                    isUniversal, isInconsistent);
            returnLocList.add(loc);
        }
        return returnLocList;
    }

    private static ArrayList<Guard> addGuards(String invariant) {
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
        return guards;
    }

    private static Clock findClock(String clockName) {
        for (Clock clock : componentClocks) {
            if (clock.getName().equals(clockName))
                return clock;
        }

        return null;
    }

    private static ArrayList<Transition> addEdges(JSONArray edgeList, ArrayList<Location> locations) {
        ArrayList<Transition> transitions = new ArrayList<>();
        for (Object obj : edgeList) {
            JSONObject jsonObject = (JSONObject) obj;

            ArrayList<Guard> guards = new ArrayList<>();
            if (!jsonObject.get("guard").toString().equals("")) {
                guards = addGuards((String) jsonObject.get("guard"));
            }
            ArrayList<Update> updates = new ArrayList<>();
            if (!jsonObject.get("update").toString().equals("")) {
                updates = addUpdates((String) jsonObject.get("update"));
            }
            Location sourceLocation = findLoc(locations, (String) jsonObject.get("sourceLocation"));
            Location targetLocation = findLoc(locations, (String) jsonObject.get("targetLocation"));

            boolean isInput = "INPUT".equals(jsonObject.get("status").toString());
            Channel chan = null;
            for (Channel channel : globalChannels) {
                if (channel.getName().equals(jsonObject.get("sync"))) {
                    chan = channel;
                    break;
                }
            }
            if (chan != null) {
                Transition transition = new Transition(sourceLocation, targetLocation, chan, isInput, guards, updates);
                transitions.add(transition);
            }
        }
        return transitions;
    }

    private static ArrayList<Update> addUpdates(String update) {
        ArrayList<Update> updates = new ArrayList<>();
        String[] listOfInv = update.split(",");
        for (String str : listOfInv) {
            String[] s = str.split("=");
            for (int i = 0; i < s.length; i++)
                s[i] = s[i].replaceAll(" ", "");
            Update upd = new Update(findClock(s[0]), Integer.parseInt(s[1]));
            updates.add(upd);
        }
        return updates;
    }

    //Helper method for addEdge in order to find which Location is source and which one is target
    private static Location findLoc(ArrayList<Location> locations, String name) {
        for (Location location : locations) {
            if (location.getName().equals(name))
                return location;
        }
        return null;
    }
}