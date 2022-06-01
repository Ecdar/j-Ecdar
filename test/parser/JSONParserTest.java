package parser;

import logic.JsonAutomatonEncoder;
import logic.Pruning;
import logic.SimpleTransitionSystem;
import models.*;
import org.json.simple.parser.ParseException;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class JSONParserTest {
    private static Automaton[] machines, machines2;
    private static Automaton A, G, Q, Imp, Ref1;
    private static String AJsonString = "{\r\n  \"name\": \"A\",\r\n  \"declarations\": \"// comment\\nclock a;\\n// comment\\nclock b, z;\\n// comment\\nclock m;\",\r\n  \"locations\": [\r\n    {\r\n      \"id\": \"L2\",\r\n      \"nickname\": \"\",\r\n      \"invariant\": \"\",\r\n      \"type\": \"INITIAL\",\r\n      \"urgency\": \"NORMAL\",\r\n      \"x\": 240.0,\r\n      \"y\": 350.0,\r\n      \"color\": \"3\",\r\n      \"nicknameX\": 30.0,\r\n      \"nicknameY\": -10.0,\r\n      \"invariantX\": 30.0,\r\n      \"invariantY\": 10.0\r\n    }\r\n  ],\r\n  \"edges\": [\r\n    {\r\n      \"sourceLocation\": \"L2\",\r\n      \"targetLocation\": \"L2\",\r\n      \"status\": \"INPUT\",\r\n      \"select\": \"\",\r\n      \"guard\": \"\",\r\n      \"update\": \"\",\r\n      \"sync\": \"bad\",\r\n      \"isLocked\": false,\r\n      \"nails\": [\r\n        {\r\n          \"x\": 270.0,\r\n          \"y\": 290.0,\r\n          \"propertyType\": \"SYNCHRONIZATION\",\r\n          \"propertyX\": 10.0,\r\n          \"propertyY\": -10.0\r\n        },\r\n        {\r\n          \"x\": 280.0,\r\n          \"y\": 330.0,\r\n          \"propertyType\": \"NONE\",\r\n          \"propertyX\": 0.0,\r\n          \"propertyY\": 0.0\r\n        }\r\n      ]\r\n    },\r\n    {\r\n      \"sourceLocation\": \"L2\",\r\n      \"targetLocation\": \"L2\",\r\n      \"status\": \"INPUT\",\r\n      \"select\": \"\",\r\n      \"guard\": \"\",\r\n      \"update\": \"\",\r\n      \"sync\": \"good\",\r\n      \"isLocked\": false,\r\n      \"nails\": [\r\n        {\r\n          \"x\": 200.0,\r\n          \"y\": 290.0,\r\n          \"propertyType\": \"SYNCHRONIZATION\",\r\n          \"propertyX\": 10.0,\r\n          \"propertyY\": -10.0\r\n        },\r\n        {\r\n          \"x\": 190.0,\r\n          \"y\": 340.0,\r\n          \"propertyType\": \"NONE\",\r\n          \"propertyX\": 0.0,\r\n          \"propertyY\": 0.0\r\n        }\r\n      ]\r\n    },\r\n    {\r\n      \"sourceLocation\": \"L2\",\r\n      \"targetLocation\": \"L2\",\r\n      \"status\": \"OUTPUT\",\r\n      \"select\": \"\",\r\n      \"guard\": \"\",\r\n      \"update\": \"\",\r\n      \"sync\": \"button1\",\r\n      \"isLocked\": false,\r\n      \"nails\": [\r\n        {\r\n          \"x\": 240.0,\r\n          \"y\": 410.0,\r\n          \"propertyType\": \"SYNCHRONIZATION\",\r\n          \"propertyX\": -10.0,\r\n          \"propertyY\": 20.0\r\n        },\r\n        {\r\n          \"x\": 270.0,\r\n          \"y\": 400.0,\r\n          \"propertyType\": \"NONE\",\r\n          \"propertyX\": 0.0,\r\n          \"propertyY\": 0.0\r\n        }\r\n      ]\r\n    }\r\n  ],\r\n  \"description\": \"\",\r\n  \"x\": 5.0,\r\n  \"y\": 5.0,\r\n  \"width\": 450.0,\r\n  \"height\": 600.0,\r\n  \"color\": \"3\",\r\n  \"includeInPeriodicCheck\": false\r\n}";

    private static final List<List<Guard>> emptyGuards = new ArrayList<>();
    private static final Update[] emptyUpdates = new Update[]{};
    private static final List<Clock> emptyClocks = new ArrayList<>();

    @BeforeClass
    public static void setUpBeforeClass() {
        String base = "./samples/json/AG/";
        String[] components = new String[]{"GlobalDeclarations.json",
                "Components/A.json",
                "Components/G.json",
                "Components/Q.json",
                "Components/Imp.json"};
        machines = JSONParser.parse(base, components, false);

        Location l0 = new Location("L0", emptyGuards, true, false, false, false);
        Location l1 = new Location("L1", emptyGuards, false, false, false, false);
        Location l2 = new Location("L2", emptyGuards, true, false, false, false);
        Location l3 = new Location("L3", emptyGuards, true, false, false, false);
        Location l5 = new Location("L5", emptyGuards, true, false, false, false);
        Location u0 = new Location("U0", emptyGuards, false, false, true, false);

        Channel button1 = new Channel("button1");
        Channel button2 = new Channel("button2");
        Channel good = new Channel("good");
        Channel bad = new Channel("bad");

        Edge t1 = new Edge(l2, l2, bad, true, emptyGuards, emptyUpdates);
        Edge t2 = new Edge(l2, l2, good, true, emptyGuards, emptyUpdates);
        Edge t3 = new Edge(l2, l2, button1, false, emptyGuards, emptyUpdates);
        Edge t4 = new Edge(l3, l3, button1, true, emptyGuards, emptyUpdates);
        Edge t5 = new Edge(l3, l3, button2, true, emptyGuards, emptyUpdates);
        Edge t6 = new Edge(l3, l3, good, false, emptyGuards, emptyUpdates);
        Edge t7 = new Edge(l5, u0, button2, true, emptyGuards, emptyUpdates);
        Edge t8 = new Edge(l5, l5, good, false, emptyGuards, emptyUpdates);
        Edge t9 = new Edge(l5, l5, button1, true, emptyGuards, emptyUpdates);
        Edge t10 = new Edge(l0, l0, button1, true, emptyGuards, emptyUpdates);
        Edge t11 = new Edge(l0, l0, good, false, emptyGuards, emptyUpdates);
        Edge t12 = new Edge(l1, l1, button1, true, emptyGuards, emptyUpdates);
        Edge t13 = new Edge(l1, l1, button2, true, emptyGuards, emptyUpdates);
        Edge t14 = new Edge(l1, l1, good, false, emptyGuards, emptyUpdates);
        Edge t15 = new Edge(l1, l1, bad, false, emptyGuards, emptyUpdates);
        Edge t16 = new Edge(l0, l1, button2, true, emptyGuards, emptyUpdates);

        Clock a = new Clock("a");
        Clock b = new Clock("b");
        Clock z = new Clock("z");
        Clock m = new Clock("m");
        List<Clock> clocksOfA = new ArrayList<>(Arrays.asList(a, b, z, m));

        A = new Automaton("A", new ArrayList<>(Collections.singletonList(l2)), new ArrayList<>(Arrays.asList(t1, t2, t3)), clocksOfA, false);
        G = new Automaton("G", new ArrayList<>(Collections.singletonList(l3)), new ArrayList<>(Arrays.asList(t4, t5, t6)), emptyClocks, false);
        Q = new Automaton("Q", new ArrayList<>(Arrays.asList(l5, u0)), new ArrayList<>(Arrays.asList(t7, t8, t9)), emptyClocks, false);
        Imp = new Automaton("Imp", new ArrayList<>(Arrays.asList(l0, l1)), new ArrayList<>(Arrays.asList(t10, t11, t12, t13, t14, t15, t16)), emptyClocks, false);


        // Adding BigRefinement example automata
        base = "./samples/json/BigRefinement/";
        components = new String[]{"GlobalDeclarations.json", "Components/Ref1.json"};
        machines2 = JSONParser.parse(base, components, false);

        Clock x = new Clock("x");
        Clock y = new Clock("y");

        models.Guard g_l12_l17 = new Guard(x, 15, false, false);
        models.Guard g_l12_l14 = new Guard(x, 20, false, true);
        models.Guard g_l12_l13 = new Guard(x, 5, false, true);
        models.Guard g_l12_l15 = new Guard(x, 8, false, false);
        models.Guard g_l12_l16 = new Guard(x, 55, false, true);
        models.Guard g_l15_l18 = new Guard(x, 15, true, true);
        models.Guard inv_l15 = new Guard(x, 20, false, false);

        Update u1 = new Update(x, 0);

        Location l12 = new Location("L12", emptyGuards, true, false, false, false);
        Location l13 = new Location("L13", emptyGuards, false, false, false, false);
        Location l14 = new Location("L14", emptyGuards, false, false, false, false);
        Location l15 = new Location("L15", new ArrayList<>(Collections.singletonList(Collections.singletonList(inv_l15))), false, false, false, false);
        Location l16 = new Location("L16", emptyGuards, false, false, false, false);
        Location l17 = new Location("L17", emptyGuards, false, false, false, false);
        Location l18 = new Location("L18", emptyGuards, false, false, false, false);

        Channel i1 = new Channel("i1");
        Channel i2 = new Channel("i2");
        Channel i3 = new Channel("i3");
        Channel i4 = new Channel("i4");
        Channel i5 = new Channel("i5");
        Channel i6 = new Channel("i6");
        Channel o1 = new Channel("o1");
        Channel o2 = new Channel("o2");
        Channel o3 = new Channel("o3");
        Channel o4 = new Channel("o4");
        Channel o5 = new Channel("o5");
        Channel o6 = new Channel("o6");
        Channel o7 = new Channel("o7");
        Channel o8 = new Channel("o8");
        Channel o9 = new Channel("o9");
        Channel o10 = new Channel("o10");


        t1 = new Edge(l12, l14, i2, true, new ArrayList<>(Collections.singletonList(Collections.singletonList(g_l12_l14))), emptyUpdates);
        t2 = new Edge(l12, l17, i3, true, new ArrayList<>(Collections.singletonList(Collections.singletonList(g_l12_l17))), emptyUpdates);
        t3 = new Edge(l12, l15, i4, true, new ArrayList<>(Collections.singletonList(Collections.singletonList(g_l12_l15))), new Update[]{u1});
        t4 = new Edge(l12, l16, i5, true, new ArrayList<>(Collections.singletonList(Collections.singletonList(g_l12_l16))), emptyUpdates);
        t5 = new Edge(l17, l18, o8, false, emptyGuards, new Update[]{u1});
        t6 = new Edge(l16, l18, o8, false, emptyGuards, emptyUpdates);
        t7 = new Edge(l15, l18, o8, false, new ArrayList<>(Collections.singletonList(Collections.singletonList(g_l15_l18))), emptyUpdates);
        t8 = new Edge(l14, l18, o8, false, emptyGuards, emptyUpdates);
        t9 = new Edge(l13, l18, o8, false, emptyGuards, emptyUpdates);
        t10 = new Edge(l17, l17, o3, false, emptyGuards, emptyUpdates);
        t11 = new Edge(l17, l17, o5, false, emptyGuards, emptyUpdates);
        t12 = new Edge(l17, l14, i6, true, emptyGuards, emptyUpdates);
        t13 = new Edge(l12, l13, i1, true, new ArrayList<>(Collections.singletonList(Collections.singletonList(g_l12_l13))), emptyUpdates);

        Ref1 = new Automaton("Ref1", new ArrayList<>(Arrays.asList(l12, l13, l14, l15, l16, l17, l18)),
                new ArrayList<>(Arrays.asList(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13)), new ArrayList<>(Arrays.asList(x, y)), false);
    }

    @Test
    public void parseJsonStringTest() throws ParseException {
        Automaton parsedA = JSONParser.parseJsonString(AJsonString, false);
        Assert.assertEquals(A,parsedA);
    }

    @Test
    public void testA() {
        Assert.assertEquals(A,machines[0]);
    }

    @Test
    public void testG() {
        assert G.equals(machines[1]);
    }

    @Test
    public void testQ() {
        assert Q.equals(machines[2]);
    }

    @Test
    public void testImp() {
        assert Imp.equals(machines[3]);
    }

    @Test
    public void testRef1() {
        assert Ref1.equals(machines2[0]);
    }

    @Test
    public void parsingNewJson()
    {
        SimpleTransitionSystem selfloopZeno;
        Automaton[] aut3 = XMLParser.parse("samples/xml/quotient/QuotientTestOutputs.xml", false);
        selfloopZeno = new SimpleTransitionSystem(aut3[2]);
        SimpleTransitionSystem pruned = Pruning.pruneIncTimed(selfloopZeno);
        pruned.toXML("testOutput/selfloopNonZeno.xml");
        JsonAutomatonEncoder.writeToJson(pruned.getAutomaton(),"C:/tools/j-Ecdar-master/j-Ecdar-master/testjsonoutput/p1");

        String base = "C:/tools/j-Ecdar-master/j-Ecdar-master/testjsonoutput/p1/";
        String[] components = new String[]{"GlobalDeclarations.json", "SystemDeclarations.json",
                "Components/selfloopNonZeno.json"};
        Automaton[] parsedMachines = JSONParser.parse(base, components, false);
        SimpleTransitionSystem s = new SimpleTransitionSystem(parsedMachines[0]);
        s.toXML("testOutput/jsonToXML.xml");
        assert(true);
//        assert Ref1.equals(machines2[0]);
    }
}
