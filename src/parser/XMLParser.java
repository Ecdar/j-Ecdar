package parser;

import models.*;
import org.jdom2.Attribute;
import org.jdom2.Element;
import org.jdom2.input.DOMBuilder;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class XMLParser {

    public static Automaton[] parse(String fileName, boolean makeInpEnabled) {
        List<Automaton> automata = new ArrayList<>();

        try {
            org.jdom2.Document jdomDoc = useDOMParser(fileName);
            Element root = jdomDoc.getRootElement();

            // automata
            List<Element> elements = root.getChildren("template");

            for (Element el : elements) {
                // automaton name
                String name = el.getChildText("name");

                // clocks
                List<Clock> clocks = setClocks(el);

                // initial location
                String initId = el.getChild("init").getAttributeValue("ref");

                // locations
                List<Location> locations = setLocations(el, clocks, initId);

                // edges
                List<Edge> edges = setEdges(el, clocks, locations);

                automata.add(new Automaton(name, locations, edges, clocks, makeInpEnabled));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return automata.toArray(new Automaton[0]);
    }

    private static List<Clock> setClocks(Element el) {
        List<Clock> clockList = new ArrayList<>();

        String text = el.getChildText("declaration");

        if (text != null) {
            String clocks = text.replaceAll("clock", "")
                    .replaceAll(";", "")
                    .replaceAll(" ", "");

            String[] clockArr = clocks.split(",");

            for (String clk : clockArr)
                clockList.add(new Clock(clk));
        }

        return clockList;
    }

    private static List<Location> setLocations(Element el, List<Clock> clocks, String initId) {
        List<Location> locationList = new ArrayList<>();

        List<Element> locations = el.getChildren("location");
        for (Element loc : locations) {
            String locName = loc.getAttributeValue("id");
            boolean isInitial = locName.equals(initId);
            List<Element> labels = loc.getChildren("label");
            List<Guard> invariants = new ArrayList<>();
            for (Element label : labels) {
                if (label.getAttributeValue("kind").equals("invariant")) {
                    invariants = addGuardsOrInvariants(label.getText(), clocks);
                }
            }

            Location newLoc = new Location(locName, invariants, isInitial, false, false, false);
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
            List<Guard> guards = new ArrayList<>();
            List<Update> updates = new ArrayList<>();
            Channel chan = null;

            for (Element label : labels) {
                String kind = label.getAttributeValue("kind");
                String text = label.getText();

                switch (kind) {
                    case "guard":
                        guards = addGuardsOrInvariants(text, clocks);
                        break;
                    case "synchronisation":
                        String channel = text.replaceAll("\\?", "").replaceAll("!", "");
                        chan = addChannel(channelList, channel);
                        break;
                    case "assignment":
                        updates = addUpdates(text, clocks);
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

    private static List<Guard> addGuardsOrInvariants(String text, List<Clock> clockList) {
        List<Guard> list = new ArrayList<>();

        String[] rawInvariants = text.split("&&");

        for (String invariant : rawInvariants) {
            invariant = invariant.replaceAll(" ", "");
            String symbol = "";
            boolean isEq, isGreater, isStrict;
            isEq = false;
            isGreater = false;
            isStrict = false;

            if (invariant.contains("==")) {
                symbol = "==";
                isEq = true;
            } else if (invariant.contains(">=")) {
                symbol = ">=";
                isGreater = true;
            } else if (invariant.contains("<=")) {
                symbol = "<=";
            } else if (invariant.contains(">")) {
                symbol = ">";
                isGreater = true;
                isStrict = true;
            } else if (invariant.contains("<")) {
                symbol = "<";
                isStrict = true;
            }

            String[] inv = invariant.split(symbol);
            Clock clk = findClock(clockList, inv[0]);

            Guard newInv;
            if (isEq)
                newInv = new Guard(clk, Integer.parseInt(inv[1]));
            else
                newInv = new Guard(clk, Integer.parseInt(inv[1]), isGreater, isStrict);

            list.add(newInv);
        }
        return list;
    }

    private static List<Update> addUpdates(String text, List<Clock> clockList) {
        List<Update> list = new ArrayList<>();
        String[] rawUpdates = text.split(",");
        for (String rawUpdate : rawUpdates) {
            rawUpdate = rawUpdate.replaceAll(" ", "");

            String[] update = rawUpdate.split("=");
            Clock clk = findClock(clockList, update[0]);
            Update newUpdate = new Update(clk, Integer.parseInt(update[1]));
            list.add(newUpdate);
        }
        return list;
    }

    //Get JDOM document from DOM JSONParser
    private static org.jdom2.Document useDOMParser(String fileName)
            throws ParserConfigurationException, SAXException, IOException {
        //creating DOM Document
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder;
        dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(new File(fileName));
        DOMBuilder domBuilder = new DOMBuilder();
        return domBuilder.build(doc);
    }
}
