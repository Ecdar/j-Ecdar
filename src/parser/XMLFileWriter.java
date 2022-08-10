package parser;

import logic.SimpleTransitionSystem;
import models.*;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class XMLFileWriter {

    public static void toXML(String filename, Automaton automaton) {
        toXML(filename, new Automaton[]{automaton});
    }

    public static void toXML(String filename, Automaton[] auts) {


        Element nta = new Element("nta");
        Document doc = new Document();
        Element declaration = new Element("declaration");
        nta.addContent(declaration);

        Set<Channel> allChans = new HashSet<Channel>();
        for (int i = 0; i < auts.length; i++)
            allChans.addAll(auts[i].getActions());


        String decString = "";
        for (Channel c : allChans.toArray(new Channel[0]))
            decString += "chan " + c.getName() + "; ";


        declaration.addContent(decString);

        String names="";
        for (int i = 0; i < auts.length; i++)
        {
            Element aut = processAutomaton(filename, auts[i]);
            nta.addContent(aut);
            if (names.equals(""))
                names = auts[i].getName();
            else
                names = names + ", " + auts[i].getName();
        }
        Element sys = new Element("system");
        sys.addContent("system "+ names +";");
        nta.addContent(sys);
        doc.setRootElement(nta);
        XMLOutputter outter = new XMLOutputter();
        outter.setFormat(Format.getPrettyFormat());
        try {
            outter.output(doc, new FileWriter(new File(filename)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }




    public static void toXML(String filename, SimpleTransitionSystem ts )
    {
        Element nta=new Element("nta");
        Document doc=new Document();
        Element declaration = new Element("declaration");
        nta.addContent(declaration);

        String decString = "";
        for (Channel c : ts.getAutomaton().getInputAct())
            decString+= "chan " + c.getName() + "; ";
        for (Channel c : ts.getAutomaton().getOutputAct())
            decString+= "chan " + c.getName() + "; ";


        declaration.addContent(decString);

        Element aut = processAutomaton(filename, ts.getAutomaton());

        nta.addContent(aut);
        Element sys = new Element("system");
        sys.addContent("system "+ ts.getAutomaton().getName() +";");
        nta.addContent(sys);
        doc.setRootElement(nta);
        XMLOutputter outter = new XMLOutputter();
        outter.setFormat(Format.getPrettyFormat());
        try {
            File file = new File(filename);
            file.getParentFile().mkdirs();
            outter.output(doc, new FileWriter(file));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }


    public static  Element processAutomaton(String filename, Automaton automaton )
    {
        //String file =" <?xml version="1.0" encoding="utf-8"?><!DOCTYPE nta PUBLIC '-//Uppaal Team//DTD Flat System 1.1//EN' 'http://www.it.uu.se/research/group/darts/uppaal/flat-1_2.dtd'><nta><declaration>// Place global declarations here."


        Element aut = new Element("template");
        Element name = new Element("name");
        name.addContent(automaton.getName());
        aut.addContent(name);


        Element localDeclaration = new Element("declaration");
        aut.addContent(localDeclaration);
        String localDecString="";
        for (Clock c : automaton.getClocks())
        {
            localDecString+= "clock " + c.getOriginalName() + "; ";
        }

        for (BoolVar bv : automaton.getBVs())
        {
            localDecString+= "bool " + bv.getOriginalName() + "=" + bv.getInitialValue()+ "; ";
        }
        localDeclaration.addContent(localDecString);

        for (Location l : automaton.getLocations())
        {
            Element loc = new Element("location");
            Element locname = new Element("name");
            locname.addContent(l.getName());
            loc.addContent(locname);
            loc.setAttribute("id",l.getName());
            loc.setAttribute("x",String.valueOf(l.getX()));
            loc.setAttribute("y",String.valueOf(l.getY()));

            Element invarLabel = new Element("label");
            invarLabel.setAttribute("kind", "invariant");
            String guardString = l.getInvariant().toString();
/*            int j=0;
            for (List<Guard> list: l.getInvariant()) {
                int i = 0;
                String inner = "";
                for (Guard g1 : list) {
                    if (g1 instanceof ClockGuard) {

                        ClockGuard g = (ClockGuard) g1;

                    //Log.trace(g);
                    String interm = g.toString();

                    if (i == 0)
                        inner += interm;
                    else
                        inner += " && " + interm;
                    if (!inner.isEmpty()) i++;
                }
                    else if (g1 instanceof BoolGuard)
                    {
                        BoolGuard g = (BoolGuard) g1;
                        String interm = g.getVar().getName()+g.getComperator()+g.getValue();
                        if (i == 0)
                            inner += interm;
                        else
                            inner += " && " + interm;
                        if (!guardString.isEmpty()) i++;
                    }
                    else if (g1 instanceof FalseGuard)
                    {

                        String interm = "false";
                        if (i == 0)
                            inner += interm;
                        else
                            inner += " && " + interm;
                        if (!guardString.isEmpty()) i++;
                    }

            }
                if (j == 0)
                    guardString += inner;
                else
                    guardString += " || " + inner;
                if (!guardString.isEmpty()) j++;
            }

 */
            invarLabel.addContent(guardString.replaceAll("≤","<=").replaceAll("≥",">="));
            if (l.isInconsistent())
                loc.setAttribute("color","#A66C0F");
            loc.addContent(invarLabel);




            aut.addContent(loc);
        }
        Element init = new Element("init");
        init.setAttribute("ref",automaton.getInitial().getName());
        aut.addContent(init);
        for (Edge e : automaton.getEdges())
        {
            Element edge = new Element("transition");
            if (automaton.getInputAct().contains(e.getChannel()))
            {
                //edge.setAttribute("controllable","true");
            }
            else
                edge.setAttribute("controllable","false");

            Element source = new Element("source");
            source.setAttribute("ref", e.getSource().getName());
            edge.addContent(source);
            Element target = new Element("target");
            target.setAttribute("ref", e.getTarget().getName());
            edge.addContent(target);

            Element synchlabel = new Element("label");
            synchlabel.setAttribute("kind", "synchronisation");
            if (e.getChannel().getX()!=-999)
                synchlabel.setAttribute("x",""+e.getChannel().getX());
            if (e.getChannel().getY()!=-999)
                synchlabel.setAttribute("y",""+e.getChannel().getY());


            if (automaton.getInputAct().contains(e.getChannel()))
                synchlabel.addContent(e.getChannel().getName()+"?");
            else
                synchlabel.addContent(e.getChannel().getName()+"!");
            edge.addContent(synchlabel);

            Element guardlabel = new Element("label");
            guardlabel.setAttribute("kind", "guard");
            String guardString = e.getGuard().toString();

/*
            int i= 0; int j=0;
            for (List<Guard> disjunction: e.getGuards()) {
                if (j != 0)
                    guardString = guardString + " or ";
                i = 0;
                for (Guard g1 : disjunction) {


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
                    else  if (g1 instanceof BoolGuard)
                    {
                        BoolGuard g = (BoolGuard) g1;
                        String interm = g.getVar().getName()+g.getComperator()+g.getValue();
                        if (i == 0)
                            guardString += interm;
                        else
                            guardString += " && " + interm;
                        if (!guardString.isEmpty()) i++;
                    }
                    else if (g1 instanceof FalseGuard)
                    {
                            guardString = "false"; // TODO: this could be better
                    }
            }




                j++;

            }

 */
            guardlabel.addContent(guardString.replaceAll("≤","<=").replaceAll("≥",">="));
            edge.addContent(guardlabel);

            Element updatelabel = new Element("label");
            updatelabel.setAttribute("kind", "assignment");
            String updateString = "";
            int i= 0;
            for (Update u: e.getUpdates()) {

                if (u instanceof BoolUpdate)
                {
                    if (i == 0) {
                        updateString += ((BoolUpdate) u).getBV().getOriginalName();
                        updateString += " = " + ((BoolUpdate) u).getValue();
                    } else
                        updateString += ", " + ((BoolUpdate) u).getBV().getOriginalName() + " = " + ((BoolUpdate) u).getValue();
                    i++;
                }
                if (u instanceof ClockUpdate)
                {

                    if (i == 0) {
                        updateString += ((ClockUpdate) u).getClock().getOriginalName();
                        updateString += " = " + ((ClockUpdate) u).getValue();
                    } else
                        updateString += ", " + ((ClockUpdate) u).getClock().getOriginalName() + " = " + ((ClockUpdate) u).getValue();
                    i++;
                }
            }


            updatelabel.addContent(updateString);
            edge.addContent(updatelabel);
            aut.addContent(edge);
        }
       return aut;
//        automaton.

    }

}
