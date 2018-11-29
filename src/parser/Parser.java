package parser;

import java.io.FileReader;
import java.util.*;
import models.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;


public class Parser {

    private static ArrayList<JSONObject> objectList = new ArrayList<>();
    private static ArrayList<Channel> globalChannels = new ArrayList<>();
    private static Set<Clock> componentClocks = new HashSet<>();

    public Parser (ArrayList<String> locations){
        objectList = parseFiles(locations);
    }

    //---------------------------Testing-----------------
    public static void main(String[] args) {
        ArrayList<String> locations = new ArrayList<>();

        /*locations.add("/Users/Widok/Documents/GlobalDeclarations.json");
        locations.add("/Users/Widok/Documents/Imp.json");
        locations.add("/Users/Widok/Documents/G.json");
        locations.add("/Users/Widok/Documents/A_Good.json");*/

        locations.add("/Users/cristina/Documents/Ecdar-2.2/samples/EcdarUniversity/GlobalDeclarations.json");
        locations.add("/Users/cristina/Documents/Ecdar-2.2/samples/EcdarUniversity/Components/Machine.json");
				locations.add("/Users/cristina/Documents/Ecdar-2.2/samples/EcdarUniversity/Components/Administration.json");
				locations.add("/Users/cristina/Documents/Ecdar-2.2/samples/EcdarUniversity/Components/Researcher.json");

        objectList = parseFiles(locations);
        ArrayList<Component> components = distrubuteObjects(objectList);
        //printStuff(components);
    }

    public static ArrayList<Component> parse() {
				ArrayList<String> locations = new ArrayList<>();

				locations.add("/Users/cristina/Documents/Ecdar-2.2/samples/EcdarUniversity/GlobalDeclarations.json");
				locations.add("/Users/cristina/Documents/Ecdar-2.2/samples/EcdarUniversity/Components/Administration.json");
				locations.add("/Users/cristina/Documents/Ecdar-2.2/samples/EcdarUniversity/Components/Machine.json");
				locations.add("/Users/cristina/Documents/Ecdar-2.2/samples/EcdarUniversity/Components/Researcher.json");
				locations.add("/Users/cristina/Documents/Ecdar-2.2/samples/EcdarUniversity/Components/Spec.json");
				locations.add("/Users/cristina/Documents/Ecdar-2.2/samples/EcdarUniversity/Components/Machine3.json");

				objectList = parseFiles(locations);
				return distrubuteObjects(objectList);
		}

    private static void printStuff(ArrayList<Component> components) {
    		for (Component component : components)  {
						System.out.println("--------------------------");
						System.out.println("Component name: " + component.getName());
						System.out.println("Initial location: " + component.getInitLoc().getName());
						for (Location loc : component.getLocations()) {
								System.out.println("location: " + loc.getName());

								if (loc.getInvariant() != null)
										System.out.println("Invariant: " + loc.getInvariant().getClock().getName() + " value " +
														loc.getInvariant().getValue());
						}
						for (Transition tran : component.getTransitions()){
								System.out.println("transition from: " + tran.getSource().getName());
								System.out.println("transition to: " + tran.getTarget().getName());
								if (tran.getChannel() != null)
										System.out.println("channel: " + tran.getChannel().getName());
								if (tran.getGuards() != null)
										System.out.println("Guard: " + tran.getGuards().get(0).getClock().getName() + " value " +
														tran.getGuards().get(0).getValue());

						}
				}
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
                    Component component = new Component((String)obj.get("name"), locations, transitions, new HashSet<>(componentClocks));
                    components.add(component);
                    componentClocks.clear();
                }
                else {
                    addDeclarations((String)obj.get("declarations"));
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
                firstList[i]= firstList[i].replaceFirst("^broadcast chan", "");//get rid of starting text
                firstList[i]= firstList[i].replaceAll("\\s+",""); //get rid of spaces
                String[] secondList = (firstList[i].split(","));
                for (int j = 0; j < secondList.length; j++) {
                    Channel chan = new Channel(secondList[j]);
                    globalChannels.add(chan);
                }
            }
            if (firstList[i].contains("clock")) {
                firstList[i]= firstList[i].replaceFirst("^clock", "");//get rid of starting text
                firstList[i]= firstList[i].replaceAll("\\s+",""); //get rid of spaces
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

						Guard invariant = ("".equals(jsonObject.get("invariant").toString()) ? null :
										addGuards(jsonObject.get("invariant").toString()).get(0));
           	Location loc = new Location(jsonObject.get("id").toString(), invariant, isInitial, !isNotUrgent,
										isUniversal, isInconsistent);
            returnLocList.add(loc);
        }
        return returnLocList;
    }

    private static ArrayList<Guard> addGuards(String invariant) {
        ArrayList<Guard> guards = new ArrayList<>();
        String[] listOfInv = invariant.split(";");
        for (String str : listOfInv) {
        		String symbol = "";
        		boolean lt, lte, gt, gte;
        		lt = lte = gt = gte = false;

						if (str.contains("<=")) {
								symbol = "<=";
								lte = true;
						}
						if (str.contains(">=")) {
								symbol = ">=";
								gte = true;
						}
						if (str.contains("<") && !str.contains("=")) {
								symbol = "<";
								lt = true;
						}
						if (str.contains(">") && !str.contains("=")) {
								symbol = ">";
								gt = true;
						}
						String[] s = str.split(symbol);
						for (int x = 0; x < s.length; x++) {
								s[x] = s[x].replaceAll(" ", "");
						}
						guards.add(new Guard(findClock(s[0]), Integer.parseInt(s[1]), gt, gte, lt, lte));
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

    private static  ArrayList<Transition> addEdges(JSONArray edgeList, ArrayList<Location> locations) {
        ArrayList<Transition> transitions = new ArrayList<>();
        for (Object obj : edgeList) {
            JSONObject jsonObject = (JSONObject) obj;

            ArrayList<Guard> guards = new ArrayList<>();
            if (!jsonObject.get("guard").toString().equals("")) {
                guards = addGuards((String) jsonObject.get("guard"));
            }
            ArrayList<Update> updates = new ArrayList<>();
            if (!jsonObject.get("update").toString().equals("")){
                updates = addUpdates((String) jsonObject.get("update"));
            }
            Location sourceLocation = findLoc(locations, (String) jsonObject.get("sourceLocation") );
            Location targetLocation = findLoc(locations, (String) jsonObject.get("targetLocation") );

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

    private static ArrayList<Update> addUpdates(String update){
        ArrayList<Update> updates = new ArrayList<>();
        String[] listOfInv = update.split(";");
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
    private static Location findLoc(ArrayList<Location> locations, String name){
        for (Location location : locations) {
            if (location.getName().equals(name))
            		return location;
        }
        return null;
    }
}