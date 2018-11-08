package parser;
import java.io.FileReader;
import java.lang.reflect.Array;
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
        locations.add("/Users/Widok/Documents/GlobalDeclarations.json");
        locations.add("/Users/Widok/Documents/Imp.json");
        locations.add("/Users/Widok/Documents/G.json");
        locations.add("/Users/Widok/Documents/A_Good.json");
        objectList = parseFiles(locations);
        ArrayList<Component> components = distrubuteObjects(objectList);
        printStuff(components);

    }
    private static void printStuff(ArrayList<Component> components){
        for (int i = 0; i < components.size(); i++){
            System.out.println("--------------------------");
            System.out.println("Component name: " + components.get(i).getName());
            System.out.println("Initial location: " + components.get(i).getInitLoc().getName());
            for (Location loc : components.get(i).getLocations()){
                System.out.println("location: " + loc.getName());

                if (loc.getInvariant() != null)
                System.out.println("Invariant: " + loc.getInvariant().getClock().getName() +" value "+
                        loc.getInvariant().getValue());
            }
            for (Transition tran : components.get(i).getTransitions()){
                System.out.println("transition from: " + tran.getFrom().getName());
                System.out.println("transition to: " + tran.getTo().getName());
                if (tran.getChannel() != null)
                System.out.println("channel: " + tran.getChannel().getName());
                if (tran.getGuards() != null)
                    System.out.println("Guard: " + tran.getGuards().get(0).getClock().getName() +" value "+
                            tran.getGuards().get(0).getValue());

            }
        }
    }
    //---------------------------Testing-----------------

    private static ArrayList<JSONObject> parseFiles(ArrayList<String> locations) {
        JSONParser parser = new JSONParser();
        ArrayList<JSONObject> returnList = new ArrayList<>();
        try {
            for (int i = 0; i < locations.size(); i++) {
                Object obj = parser.parse(new FileReader(
                        locations.get(i)));
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
            for (int i = 0; i < objList.size(); i++) {
                if (!objList.get(i).get("name").toString().equals("Global Declarations")) {
                    addDeclarations((String)objList.get(i).get("declarations"));
                    JSONArray locationList = (JSONArray) objList.get(i).get("locations");
                    ArrayList<Location> locations = addLocations(locationList);
                    JSONArray edgeList = (JSONArray) objList.get(i).get("edges");
                    ArrayList<Transition> transitions =addEdges(edgeList, locations);
                    Component component = new Component((String)objList.get(i).get("name"),locations, transitions,componentClocks);
                    components.add(component);
                    componentClocks.clear();
                }
                else{
                    addDeclarations((String)objList.get(i).get("declarations"));
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
        Clock clock = new Clock("name", 10);
        Guard guard = new Guard(clock,10,true,true, true,true);
        ArrayList<Location> returnLocList = new ArrayList<>();
        for (int i = 0; i < locationList.size(); i++){
            JSONObject jsonObject = (JSONObject) locationList.get(i);
            models.Location loc;
            boolean isInitial;
            if ("INITIAL".equals(jsonObject.get("type").toString())){
                isInitial = true;
            }else{isInitial = false;}
            boolean isUrgernt;
            if ("NORMAL".equals(jsonObject.get("urgency").toString())){
                isUrgernt = false;
            }else{isUrgernt = true;}
            if("".equals(jsonObject.get("invariant").toString())) {
                 loc = new Location(jsonObject.get("id").toString(), null,
                        isInitial, isUrgernt, false, false);
            }else{;
                loc = new Location(jsonObject.get("id").toString(),
                        addGuards(jsonObject.get("invariant").toString()).get(0), isInitial,
                        isUrgernt, false, false);}

            returnLocList.add(loc);
        }
        return returnLocList;
    }
    private static ArrayList<Guard> addGuards(String invariant){
        ArrayList<Guard> guards = new ArrayList<>();
        String[] listOfInv = invariant.split(";");
        for (int i = 0; i < listOfInv.length; i++) {
            if (listOfInv[i].contains("<=")) {
                String[] s = listOfInv[i].split("<=");
                guards.add(new Guard(findClock(s[0]), Integer.parseInt(s[1]), false, false, false, true));
            }
            if (listOfInv[i].contains(">=")) {
                String[] s = listOfInv[i].split(">=");
                guards.add(new Guard(findClock(s[0]), Integer.parseInt(s[1]), false, true, false, false));
            }
            if (listOfInv[i].contains("<") && !listOfInv[i].contains("=")) {
                String[] s = listOfInv[i].split("<");
                guards.add(new Guard(findClock(s[0]), Integer.parseInt(s[1]), false, false, true, false));
            }
            if (listOfInv[i].contains(">")&& !listOfInv[i].contains("=")) {
                String[] s = listOfInv[i].split(">");
                guards.add(new Guard(findClock(s[0]), Integer.parseInt(s[1]), true, false, false, false));
            }
        }
        return guards;
    }
    private static Clock findClock(String clockName){
        for (Iterator<Clock> it = componentClocks.iterator(); it.hasNext(); ) {
            Clock f = it.next();
            if (f.getName().equals(clockName))
                return f;
        }
        return null;
    }
    private static  ArrayList<Transition> addEdges(JSONArray edgeList, ArrayList<Location> locations) {
        ArrayList<Transition> transitions = new ArrayList<>();
        for (int i = 0; i < edgeList.size(); i++) {
            JSONObject jsonObject = (JSONObject) edgeList.get(i);

            ArrayList<Guard> guards = null;
            if(!jsonObject.get("guard").toString().equals("")){
                guards = addGuards((String) jsonObject.get("guard"));
            }
            ArrayList<Update> updates = null;
            if(!jsonObject.get("update").toString().equals("")){
                updates = addUpdates((String) jsonObject.get("update"));
            }
            Location sourceLocation =findLoc(locations,(String) jsonObject.get("sourceLocation") );
            Location targetLocation =findLoc(locations,(String) jsonObject.get("targetLocation") );
            boolean isInput;
            if ("INPUT".equals(jsonObject.get("status").toString()) ){
                isInput = true;
            } else {isInput = false;}
            Channel chan = null;
            for (Channel channel : globalChannels) {
                if (channel.getName().equals( jsonObject.get("sync"))){
                    chan = channel;
                    break;
                }
            }
            Transition transition = new Transition(sourceLocation,targetLocation,chan,
                    isInput,guards,updates);
            transitions.add(transition);
        }
        return transitions;
    }
    private static ArrayList<Update> addUpdates(String update){
        ArrayList<Update> updates = new ArrayList<>();
        String[] listOfInv = update.split(";");
        for (int i = 0; i < listOfInv.length; i++) {
            String[] s = listOfInv[i].split("=");
            Update upd = new Update(findClock(s[0]), Integer.parseInt(s[1]));
            updates.add(upd);
        }
        return updates;
    }
    //Helper method for addEdge in order to find which Location is source and which one is target
    private static Location findLoc(ArrayList<Location> locations, String name){
        for (int i = 0; i < locations.size(); i ++){
            if(locations.get(i).getName().equals(name))
            {return locations.get(i);}
        }
        return null;
    }



}







