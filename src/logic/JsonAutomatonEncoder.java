package logic;
import models.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class JsonAutomatonEncoder {

    public static String getAutomatonAsJson(Automaton automaton){
        return automatonToJson(automaton).toJSONString();
    }

    public static void writeToJson(Automaton aut, String path)
    {
        JSONObject finalJSON = automatonToJson(aut);

        JSONObject globalDec = new JSONObject();
        globalDec.put("name", "Global Declarations");
        String decString = "";
        for (Channel c : aut.getInputAct())
            decString+= "broadcast chan " + c.getName() + "; ";
        for (Channel c : aut.getOutputAct())
            decString+= "broadcast chan " + c.getName() + "; ";
        globalDec.put("declarations", decString);

        JSONObject sysDec = new JSONObject();
        sysDec.put("name", "System Declarations");
        decString = "system " + aut.getName()+"; \n\n IO " + aut.getName() + " { ";
        for (Channel c : aut.getInputAct())
            decString+=  c.getName() + "?, ";
        for (Channel c : aut.getOutputAct())
            decString+=  c.getName() + "!, ";
        decString=decString.substring(0,decString.length()-2);
        decString= decString + "}";
        sysDec.put("declarations", decString);


        File directory = new File(path);
        if (! directory.exists()){
            directory.mkdirs();
            // If you require it to make the entire directory path including parents,
            // use directory.mkdirs(); here instead.
        }
        File componentsDirectory = new File(path + "/Components");
        if (! componentsDirectory.exists()){
            componentsDirectory.mkdirs();
            // If you require it to make the entire directory path including parents,
            // use directory.mkdirs(); here instead.
        }

        try {
            FileWriter writer = new FileWriter(path+"/Components/"+aut.getName()+".json");
            finalJSON.writeJSONString(writer);
            writer.close();
            writer = new FileWriter(path+"/SystemDeclarations.json");
            sysDec.writeJSONString(writer);
            writer.close();

            writer = new FileWriter(path+"/GlobalDeclarations.json");
            globalDec.writeJSONString(writer);
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

    private static JSONObject automatonToJson(Automaton aut){
        JSONArray locations= new JSONArray();
        JSONArray edges = new JSONArray();
        for (Location l :aut.getLocations())
        {
            JSONObject locationJson = new JSONObject();
            locationJson.put("nickname", l.getName());
            locationJson.put("id", l.getName());
            locationJson.put("x", l.getX());
            locationJson.put("y", l.getY());


            String guardString =l.getInvariant().toString();
            /*int i= 0; int j=0;
            for (List<Guard> disjunction: l.getInvariant())
            {
                if (j!=0)
                    guardString=guardString +" or ";
                i=0;
                for (Guard g1: disjunction) {
                    if (g1 instanceof ClockGuard) {
                        ClockGuard g = (ClockGuard) g1;
                        //Log.trace(g);
                        String interm = g.toString();

                        if (i == 0)
                            guardString += interm;
                        else
                            guardString += " && " + interm;
                        if (!guardString.isEmpty()) i++;
                    }
                    else
                    {
                        if (g1 instanceof BoolGuard) {
                            BoolGuard g = (BoolGuard) g1;
                            //Log.trace(g);
                            String interm = g.toString();
                            if (i == 0)
                                guardString += interm;
                            else
                                guardString += " && " + interm;
                            if (!guardString.isEmpty()) i++;
                        }
                    }
                }
                j++;
            }
            */
            locationJson.put("invariant", guardString.replaceAll("≤","<=").replaceAll("≥",">="));



            if (l.isUrgent())
                locationJson.put("urgency","URGENT");
            else
                locationJson.put("urgency","NORMAL");
            if (l.isInitial())
                locationJson.put("type", "INITIAL");
            else
                locationJson.put("type", "NORMAL");
            if (l.isInconsistent())
                locationJson.put("inconsistency", "INCONSISTENT");
            else
                locationJson.put("inconsistency", "NORMAL");

            locations.add(locationJson);
        }

        for (Edge e :aut.getEdges()) {

            JSONObject edgeJson = new JSONObject();
            edgeJson.put("sourceLocation", e.getSource().getName());
            edgeJson.put("targetLocation", e.getTarget().getName());
            if (e.isInput())
                edgeJson.put("status", "INPUT");
            else
                edgeJson.put("status", "OUTPUT");
            edgeJson.put("select", "");

            String guardString = e.getGuard().toString();
            /*int i= 0; int j=0;
            for (List<Guard> disjunction: e.getGuards())
            {
                if (j!=0)
                    guardString=guardString +" or ";
                i=0;
                for (Guard g1: disjunction) {
                    if (g1 instanceof ClockGuard) {

                        ClockGuard g = (ClockGuard) g1;
                        //Log.trace(g);
                        String interm = g.toString();

                        if (i == 0)
                            guardString += interm;
                        else
                            guardString += " && " + interm;
                        if (!guardString.isEmpty()) i++;
                    }
                    else
                    {
                        BoolGuard g = (BoolGuard) g1;
                        String interm = g.getVar().getName()+g.getComperator()+g.getValue();
                        if (i == 0)
                            guardString += interm;
                        else
                            guardString += " && " + interm;
                        if (!guardString.isEmpty()) i++;
                    }
                }

                j++;
            }
*/

            edgeJson.put("guard", guardString.replaceAll("≤","<=").replaceAll("≥",">="));

            String updateString = "";
            int i= 0;
            for (Update u1: e.getUpdates())
            {
                if (u1 instanceof ClockUpdate) {
                    ClockUpdate u = (ClockUpdate) u1;
                    if (i == 0) {
                        updateString += u.getClock().getOriginalName();
                        updateString += " = " + u.getValue();
                    } else
                        updateString += ", " + u.getClock().getOriginalName() + " = " + u.getValue();
                    i++;
                }
                else
                {
                    if (u1 instanceof BoolUpdate)
                    {
                        BoolUpdate u = (BoolUpdate) u1;
                        if (i == 0) {
                            updateString += u.getBV().getOriginalName();
                            updateString += " = " + u.getValue();
                        } else
                            updateString += ", " + u.getBV().getOriginalName() + " = " + u.getValue();
                        i++;
                    }
                }

            }


            edgeJson.put("update",updateString);
            edgeJson.put("sync", e.getChannel().getName());
            edges.add(edgeJson);
        }

        String localDecString="";
        for (Clock c : aut.getClocks())
        {
            localDecString+= "clock " + c.getOriginalName() + "; ";
        }

        for (BoolVar bv : aut.getBVs())
        {
            localDecString+= "bool " + bv.getOriginalName() + "; ";
        }

        JSONObject finalJSON = new JSONObject();
        finalJSON.put("name", aut.getName());
        finalJSON.put("declarations", localDecString);
        finalJSON.put("locations", locations);
        finalJSON.put("edges", edges);

        return finalJSON;
    }

}
