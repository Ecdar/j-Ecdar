package parser;

import log.Log;
import models.*;
import org.jdom2.Attribute;
import org.jdom2.DataConversionException;
import org.jdom2.Element;
import org.jdom2.input.DOMBuilder;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class XMLParser {

    public static Automaton[] parse(String fileName, boolean makeInpEnabled) {
        List<Automaton> automata = new ArrayList<>();

        try {
            org.jdom2.Document jdomDoc = useDOMParserFile(fileName);

            // automata
            List<Element> elements = jdomDoc.getRootElement().getChildren("template");

            for (Element el : elements) {
                automata.add(buildAutomaton(el, makeInpEnabled));
            }
        } catch (Exception e) {
            CDDRuntime.done();
            throw new RuntimeException(e);
        }

        return automata.toArray(new Automaton[0]);
    }

    public static Automaton[] parseXmlString(String xml, boolean makeInpEnabled) {
        List<Automaton> automata = new ArrayList<>();

        try{
            org.jdom2.Document jdomDoc = useDOMParserString(xml);

            // automata
            List<Element> elements = jdomDoc.getRootElement().getChildren("template");

            for (Element el : elements) {
                automata.add(buildAutomaton(el, makeInpEnabled));
            }

        }catch (Exception e){
            throw new RuntimeException(e);
        }

        return automata.toArray(new Automaton[0]);
    }
    private static String automatonName;
    private static Automaton buildAutomaton(Element element, boolean makeInpEnabled){
        // automaton name
        String name = element.getChildText("name");
        automatonName=name;
        // clocks
        List<Clock> clocks = setClocks(element);

        // clocks
        List<BoolVar> BVs = setBVs(element);

        // initial location
        String initId = element.getChild("init").getAttributeValue("ref");

        // locations
        List<Location> locations = setLocations(element, clocks, BVs, initId);

        // edges
        List<Edge> edges = setEdges(element, clocks, BVs, locations);

        for (Edge edge : edges) {
            Log.debug(edge.getChannel());
        }

        return new Automaton(name, locations, edges, clocks, BVs, makeInpEnabled);
    }

    private static Document convertStringToDocument(String xmlStr) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        try
        {
            builder = factory.newDocumentBuilder();
            Document doc = builder.parse( new InputSource( new StringReader( xmlStr ) ) );
            return doc;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static List<Clock> setClocks(Element el) {
        List<Clock> clockList = new ArrayList<>();
        String text = el.getChildText("declaration");

        if (text != null) {

            for (String line : text.split(";")) {
                if (line.contains("clock"))
                {
                    String clocks = line.replaceAll("//.*\n", "")
                            .replaceFirst("clock", "");

                    clocks = clocks.replaceAll("clock", ",")
                            .replaceAll(";", "")
                            .replaceAll(" ", "")
                            .replaceAll("\n", "");

                    String[] clockArr = clocks.split(",");

                    for (String clk : clockArr) {
                        clockList.add(new Clock(clk, automatonName));
                    }
                }
            }
        }
        //Log.trace(clockList);
        return clockList;
    }



    private static List<BoolVar> setBVs(Element el) {
        List<BoolVar> boolList = new ArrayList<>();
        String text = el.getChildText("declaration");

        if (text != null) {

            for (String line : text.split(";")) {
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
                            boolList.add(new BoolVar(bool.split("=")[0],automatonName, Boolean.parseBoolean(bool.split("=")[1])));
                        else boolList.add(new BoolVar(bool,automatonName, false));
                }
            }
        }
        return boolList;
    }


    private static List<Location> setLocations(Element el, List<Clock> clocks, List<BoolVar> BVs, String initId) {
        List<Location> locationList = new ArrayList<>();

        List<Element> locations = el.getChildren("location");
        for (Element loc : locations) {
            String locName = loc.getAttributeValue("id");
            boolean isInitial = locName.equals(initId);
            List<Element> labels = loc.getChildren("label");
            Guard invariants = new TrueGuard();
            int x=0,y=0;
            boolean xyDefined = false;

            if (loc.getAttribute("x").isSpecified()) {
                //Log.trace(loc.getAttributeValue("x"));
                x = Integer.parseInt(loc.getAttributeValue("x"));
                y = Integer.parseInt(loc.getAttributeValue("y"));
                xyDefined=true;
            }

            for (Element label : labels) {
                if (label.getAttributeValue("kind").equals("invariant")) {
                    if (!label.getText().isEmpty())
                        invariants = GuardParser.parse(label.getText(), clocks, BVs);
                    else
                        invariants = new TrueGuard();
                }
            }

            Location  newLoc;
            if (xyDefined) {
                newLoc = Location.create(locName, invariants, isInitial, false, false, false, x, y);
            }
            else {
                newLoc = Location.create(locName, invariants, isInitial, false, false, false);
            }


            List<Element> names = loc.getChildren("name");
            assert(names.size()<=1);
            for (Element name : names)
            {
                //Log.trace(name.getContent().get(0).getValue().toString());
                if (name.getContent().get(0).getValue().toString().equals("inc")) {
                    //Log.trace("Parsed an inconsistent location");
                    if (xyDefined)
                        newLoc = Location.create(locName, invariants, isInitial, false, false, true, x,y);
                    else
                        newLoc = Location.create(locName, invariants, isInitial, false, false, true);
                }
            }

            locationList.add(newLoc);
        }

        return locationList;
    }

    private static List<Edge> setEdges(Element el, List<Clock> clocks, List<BoolVar> BVs, List<Location> locations) {
        List<Edge> edgeList = new ArrayList<>();

        List<Channel> channelList = new ArrayList<>();
        List<Element> edges = el.getChildren("transition");

        for (Element edge : edges) {
            boolean isInput = true;
            for (Attribute o : edge.getAttributes()) {
                try {
                    if (o.getName().equals("controllable") && !o.getBooleanValue()) isInput = false;
                } catch (DataConversionException e) {
                    Log.error("Controllable flag contains non-boolean value", o);
                    throw new RuntimeException(e);
                }

            }

            Location source = findLocations(locations, edge.getChild("source").getAttributeValue("ref"));
            Location target = findLocations(locations, edge.getChild("target").getAttributeValue("ref"));

            List<Element> labels = edge.getChildren("label");
            Guard guards = new TrueGuard();
            List<Update> updates = new ArrayList<>();
            Channel chan = null;

            for (Element label : labels) {
                String kind = label.getAttributeValue("kind");
                String text = label.getText();

                switch (kind) {
                    case "guard":
                        if (!text.isEmpty()) {
                            guards = GuardParser.parse(text, clocks, BVs);
                        }
                        break;
                    case "synchronisation":
                        if (text.endsWith("?"))
                            isInput = true;
                        if (text.endsWith("!"))
                            isInput = false;
                        String channel = text.replaceAll("\\?", "").replaceAll("!", "");
                        if (!text.isEmpty())
                            chan = addChannel(channelList, channel);
                        break;
                    case "assignment":
                        if (!text.isEmpty()) {
                            updates = UpdateParser.parse(text, clocks, BVs);
                        }
                        break;
                }
            }

            if (chan == null) {
                throw new IllegalStateException(edge + " is missing a channel");
            }

            edgeList.add(new Edge(source, target, chan, isInput, guards, updates));
        }

        return edgeList;
    }

    private static Clock findClock(List<Clock> clocks, String name) {
        for (Clock clock : clocks)
            if (clock.getOriginalName().equals(name))
                return clock;

        return null;
    }

    private static BoolVar findBV(List<BoolVar> BVs, String name) {
        for (BoolVar bv : BVs)
            if (bv.getOriginalName().equals(name))
                return bv;

        return null;
    }


    private static Location findLocations(List<Location> locations, String name) {
        for (Location loc : locations)
            if (loc.getName().equals(name))
                return loc;

        return null;
    }

    private static Channel addChannel(List<Channel> channels, String name) {
        for (Channel channel : channels)
            if (channel.getName().equals(name))
                return channel;

        Channel chan = new Channel(name);
        channels.add(chan);
        return chan;
    }

    //Get JDOM document from DOM JSONParser
    private static org.jdom2.Document useDOMParserFile(String fileName)
            throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilder dBuilder = getDocumentBuilder();

        Document doc = dBuilder.parse(new File(fileName));

        DOMBuilder domBuilder = new DOMBuilder();
        return domBuilder.build(doc);
    }

    private static org.jdom2.Document useDOMParserString(String xml)
            throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilder dBuilder = getDocumentBuilder();

        InputStream stream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
        Document doc = dBuilder.parse(stream);

        DOMBuilder domBuilder = new DOMBuilder();
        return domBuilder.build(doc);
    }

    private static DocumentBuilder getDocumentBuilder() throws ParserConfigurationException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder;
        dbFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        return dbFactory.newDocumentBuilder();
    }
}