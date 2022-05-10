package logic;

import models.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

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
            e.printStackTrace();
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


            String guardString ="";
            int i= 0; int j=0;
            for (List<Guard> disjunction: l.getInvariant())
            {
                if (j!=0)
                    guardString=guardString +" or ";
                i=0;
                for (Guard g: disjunction)
                {
                    //System.out.println(g);
                    String interm = "";
                    String lower ="";
                    String upper="";
                    if (g.isStrict()) {
                        lower = g.getClock().getName() + ">" + g.getLowerBound();
                        upper = g.getClock().getName() + "<" + g.getUpperBound();
                    }
                    else
                    {
                        lower = g.getClock().getName() + ">=" + g.getLowerBound();
                        upper = g.getClock().getName() + "<=" + g.getUpperBound();
                    }

                    if (g.getLowerBound()!=0)
                        if (interm.isEmpty())
                            interm += lower;
                        else
                            interm += " && " +lower;
                    if (g.getUpperBound()!= 2147483647)
                        if (interm.isEmpty())
                            interm += upper;
                        else
                            interm += " && " +upper;

                    if (i==0)
                        guardString+= interm;
                    else
                        guardString += " && " + interm;
                    if (!guardString.isEmpty()) i++;
                }
                j++;
            }
            locationJson.put("invariant", guardString);



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

            String guardString ="";
            int i= 0; int j=0;
            for (List<Guard> disjunction: e.getGuards())
            {
                if (j!=0)
                    guardString=guardString +" or ";
                i=0;
                for (Guard g: disjunction)
                {
                    //System.out.println(g);
                    String interm = "";
                    String lower ="";
                    String upper="";
                    if (g.isStrict()) {
                        lower = g.getClock().getName() + ">" + g.getLowerBound();
                        upper = g.getClock().getName() + "<" + g.getUpperBound();
                    }
                    else
                    {
                        lower = g.getClock().getName() + ">=" + g.getLowerBound();
                        upper = g.getClock().getName() + "<=" + g.getUpperBound();
                    }

                    if (g.getLowerBound()!=0)
                        if (interm.isEmpty())
                            interm += lower;
                        else
                            interm += " && " +lower;
                    if (g.getUpperBound()!= 2147483647)
                        if (interm.isEmpty())
                            interm += upper;
                        else
                            interm += " && " +upper;

                    if (i==0)
                        guardString+= interm;
                    else
                        guardString += " && " + interm;
                    if (!guardString.isEmpty()) i++;
                }
                j++;
            }
            edgeJson.put("guard", guardString);

            String updateString = "";
            i= 0;
            for (Update u: e.getUpdates())
            {

                if (i==0) {
                    updateString += u.getClock().getName();
                    updateString += " = " + u.getValue();
                }
                else
                    updateString += ", " + u.getClock().getName() + " = " + u.getValue();
                i++;
            }

            edgeJson.put("update",updateString);
            edgeJson.put("sync", e.getChannel().getName());
            edges.add(edgeJson);
        }

        String localDecString="";
        for (Clock c : aut.getClocks())
        {
            localDecString+= "clock " + c.getName() + "; ";
        }

        JSONObject finalJSON = new JSONObject();
        finalJSON.put("name", aut.getName());
        finalJSON.put("declarations", localDecString);
        finalJSON.put("locations", locations);
        finalJSON.put("edges", edges);

        return finalJSON;
    }

}
