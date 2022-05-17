package parser;

import models.*;
import org.jdom2.Attribute;
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
            e.printStackTrace();
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
            e.printStackTrace();
        }

        return automata.toArray(new Automaton[0]);
    }

    private static Automaton buildAutomaton(Element element, boolean makeInpEnabled){
        // automaton name
        String name = element.getChildText("name");

        // clocks
        List<Clock> clocks = setClocks(element);

        // initial location
        String initId = element.getChild("init").getAttributeValue("ref");

        // locations
        List<Location> locations = setLocations(element, clocks, initId);

        // edges
        List<Edge> edges = setEdges(element, clocks, locations);

        return new Automaton(name, locations, edges, clocks, makeInpEnabled);
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
            e.printStackTrace();
        }
        return null;
    }

    private static List<Clock> setClocks(Element el) {
        List<Clock> clockList = new ArrayList<>();

        String text = el.getChildText("declaration");

        if (text != null) {
            String clocks = text.replaceAll("//.*\n", "")
                    .replaceFirst("clock", "");

            clocks = clocks.replaceAll("clock", ",")
                    .replaceAll(";", "")
                    .replaceAll(" ", "")
                    .replaceAll("\n", "");

            String[] clockArr = clocks.split(",");

            for (String clk : clockArr)
                clockList.add(new Clock(clk));
        }
        //System.out.println(clockList);
        return clockList;
    }

    private static List<Location> setLocations(Element el, List<Clock> clocks, String initId) {
        List<Location> locationList = new ArrayList<>();

        List<Element> locations = el.getChildren("location");
        for (Element loc : locations) {
            String locName = loc.getAttributeValue("id");
            boolean isInitial = locName.equals(initId);
            List<Element> labels = loc.getChildren("label");
            List<List<Guard>> invariants = new ArrayList<>();
            int x=0,y=0;
            boolean xyDefined = false;

            if (loc.getAttribute("x").isSpecified()) {
                //System.out.println(loc.getAttributeValue("x"));
                x = Integer.parseInt(loc.getAttributeValue("x"));
                y = Integer.parseInt(loc.getAttributeValue("y"));
                xyDefined=true;
            }

            for (Element label : labels) {
                if (label.getAttributeValue("kind").equals("invariant")) {
                    if (!label.getText().isEmpty())
                        invariants = GuardParser.parse(label.getText(), clocks);
                }
            }

            Location  newLoc;
            if (xyDefined) newLoc= new Location(locName, invariants, isInitial, false, false, false, x, y);
            else newLoc= new Location(locName, invariants, isInitial, false, false, false);


            List<Element> names = loc.getChildren("name");
            assert(names.size()<=1);
            for (Element name : names)
            {
                //System.out.println(name.getContent().get(0).getValue().toString());
                if (name.getContent().get(0).getValue().toString().equals("inc")) {
                    //System.out.println("Parsed an inconsistent location");
                    if (xyDefined)
                        newLoc = new Location(locName, invariants, isInitial, false, false, true, x,y);
                    else
                        newLoc = new Location(locName, invariants, isInitial, false, false, true);
                }
            }

            locationList.add(newLoc);
        }

        return locationList;
    }

    private static List<Edge> setEdges(Element el, List<Clock> clocks, List<Location> locations) {
        List<Edge> edgeList = new ArrayList<>();

        List<Channel> channelList = new ArrayList<>();
        List<Element> edges = el.getChildren("transition");

        for (Element edge : edges) {
            boolean isInput = true;
            for (Attribute o : edge.getAttributes()) {
                if (o.getName().equals("controllable")) isInput = false;
            }

            Location source = findLocations(locations, edge.getChild("source").getAttributeValue("ref"));
            Location target = findLocations(locations, edge.getChild("target").getAttributeValue("ref"));

            List<Element> labels = edge.getChildren("label");
            List<List<Guard>> guards = new ArrayList<>();
            List<Update> updates = new ArrayList<>();
            Channel chan = null;

            for (Element label : labels) {
                String kind = label.getAttributeValue("kind");
                String text = label.getText();

                switch (kind) {
                    case "guard":
                        if (!text.isEmpty())
                            guards = GuardParser.parse(text, clocks);
                        break;
                    case "synchronisation":
                        String channel = text.replaceAll("\\?", "").replaceAll("!", "");
                        if (!text.isEmpty())
                            chan = addChannel(channelList, channel);
                        break;
                    case "assignment":
                        if (!text.isEmpty())
                            updates = UpdateParser.parse(text, clocks);
                        break;
                }
            }

            edgeList.add(new Edge(source, target, chan, isInput, guards, updates.toArray(new Update[0])));
        }

        return edgeList;
    }

    private static Clock findClock(List<Clock> clocks, String name) {
        for (Clock clock : clocks)
            if (clock.getName().equals(name))
                return clock;

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
