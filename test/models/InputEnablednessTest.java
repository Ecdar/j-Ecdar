package models;

import org.junit.BeforeClass;
import org.junit.Test;
import parser.JSONParser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class InputEnablednessTest {

    private static Automaton expected, actual;
    private static Update[] noUpdate = new Update[]{};
    private static List<Guard> noguard = new ArrayList<>();

    @BeforeClass
    public static void setUpBeforeClass() {
        Clock x = new Clock("x");
        Clock y = new Clock("y");

        Guard invL1 = new Guard(x, 10, false, false);

        Location l0 = new Location("L0", noguard, true, false, false, false);
        Location l1 = new Location("L1", new ArrayList<>(Collections.singletonList(invL1)), false, false, false, false);
        Location l2 = new Location("L2", noguard, false, false, false, false);
        Location l3 = new Location("L3", noguard, false, false, false, false);
        Location l4 = new Location("L4", noguard, false, false, false, false);

        Channel i1 = new Channel("i1");
        Channel i2 = new Channel("i2");
        Channel o = new Channel("o");

        Guard e2_g1 = new Guard(x, 3, true, false);
        Guard e2_g2 = new Guard(x, 5, false, false);
        Guard e2_g3 = new Guard(y, 4, true, false);
        Guard e3_g1 = new Guard(x, 2, false, false);
        Guard e3_g2 = new Guard(y, 3, false, false);
        Guard e5_g1 = new Guard(x, 10, true, true);
        Guard e5_g2 = new Guard(y, 10, true, true);
        Guard e6_g1 = new Guard(x, 2, true, true);
        Guard e6_g2 = new Guard(x, 4, false, true);
        Guard e6_g3 = new Guard(y, 2, true, true);
        Guard e6_g4 = new Guard(y, 4, false, true);
        Guard e7_g1 = new Guard(x, 5, true, true);
        Guard e7_g2 = new Guard(y, 5, true, true);
        Guard e8_g1 = new Guard(x, 10, false, false);
        Guard e8_g2 = new Guard(y, 10, false, false);
        Guard e9_g1 = new Guard(x, 10, false, false);
        Guard e9_g2 = new Guard(y, 10, false, false);

        Edge e1 = new Edge(l0, l1, i1, true, new ArrayList<>(Collections.singletonList(invL1)), noUpdate);
        Edge e2 = new Edge(l0, l2, i2, true, new ArrayList<>(Arrays.asList(e2_g1, e2_g2, e2_g3)), noUpdate);
        Edge e3 = new Edge(l0, l3, i2, true, new ArrayList<>(Arrays.asList(e3_g1, e3_g2)), noUpdate);
        Edge e4 = new Edge(l1, l4, o, false, noguard, noUpdate);
        Edge e5 = new Edge(l0, l0, i1, true, new ArrayList<>(Arrays.asList(e5_g1, e5_g2)), noUpdate);
        Edge e6 = new Edge(l0, l0, i2, true, new ArrayList<>(Arrays.asList(e6_g1, e6_g2, e6_g3, e6_g4)), noUpdate);
        Edge e7 = new Edge(l0, l0, i2, true, new ArrayList<>(Arrays.asList(e7_g1, e7_g2)), noUpdate);
        Edge e8 = new Edge(l1, l1, i1, true, new ArrayList<>(Arrays.asList(e8_g1, e8_g2)), noUpdate);
        Edge e9 = new Edge(l1, l1, i2, true, new ArrayList<>(Arrays.asList(e9_g1, e9_g2)), noUpdate);
        Edge e10 = new Edge(l2, l2, i1, true, noguard, noUpdate);
        Edge e11 = new Edge(l2, l2, i2, true, noguard, noUpdate);
        Edge e12 = new Edge(l3, l3, i1, true, noguard, noUpdate);
        Edge e13 = new Edge(l3, l3, i2, true, noguard, noUpdate);
        Edge e14 = new Edge(l4, l4, i1, true, noguard, noUpdate);
        Edge e15 = new Edge(l4, l4, i2, true, noguard, noUpdate);

        List<Location> locations = new ArrayList<>(Arrays.asList(l0, l1, l2, l3, l4));
        List<Edge> edges = new ArrayList<>(Arrays.asList(e1, e2, e3, e4, e5, e6, e7, e8, e9, e10, e11, e12, e13, e14, e15));
        List<Clock> clocks = new ArrayList<>(Arrays.asList(x, y));

        expected = new Automaton("Automaton", locations, edges, clocks, false);

        String base = "./samples/json/InputEnabled/";
        String[] components = new String[]{"GlobalDeclarations.json", "Components/Automaton.json"};

        actual = JSONParser.parse(base, components, true)[0];
    }

    @Test
    public void testAutomaton() {
        assert actual.equals(expected);
    }
}
