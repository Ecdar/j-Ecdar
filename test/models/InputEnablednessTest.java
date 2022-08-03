package models;

import exceptions.CddAlreadyRunningException;
import exceptions.CddNotRunningException;
import log.Log;
import logic.Refinement;
import logic.SimpleTransitionSystem;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import parser.JSONParser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InputEnablednessTest {

    private static Automaton expected, actual;
    private static List<Update> noUpdate = new ArrayList<>();

    @After
    public void afterEachTest(){
        CDD.done();
    }

    @BeforeClass
    public static void setUpBeforeClass() throws CddAlreadyRunningException, CddNotRunningException {
        Clock x = new Clock("x", "Aut");
        Clock y = new Clock("y", "Aut");

        Log.trace("started setup");

        ClockGuard invL1 = new ClockGuard(x, 10, Relation.LESS_EQUAL);

        Location l0 = new Location("L0", new TrueGuard(), true, false, false, false);
        Location l1 = new Location("L1", invL1, false, false, false, false);
        Location l2 = new Location("L2", new TrueGuard(), false, false, false, false);
        Location l3 = new Location("L3", new TrueGuard(), false, false, false, false);
        Location l4 = new Location("L4", new TrueGuard(), false, false, false, false);

        Channel i1 = new Channel("i1");
        Channel i2 = new Channel("i2");
        Channel o = new Channel("o");


        ClockGuard e2_g1 = new ClockGuard(x, 3,  Relation.GREATER_EQUAL);
        ClockGuard e2_g2 = new ClockGuard(x, 5,  Relation.LESS_EQUAL);
        ClockGuard e2_g3 = new ClockGuard(y, 4, Relation.GREATER_EQUAL);
        ClockGuard e3_g1 = new ClockGuard(x, 2,  Relation.LESS_EQUAL); // x<=2 // TODO: was this supposed to be x<=2 or x<2? The comment originally was "x<2" and did not fit the code
        ClockGuard e3_g2 = new ClockGuard(y, 3,  Relation.LESS_EQUAL);

        ClockGuard e8_g1 = new ClockGuard(x, 10,  Relation.LESS_EQUAL); // greater: false => x<10
        ClockGuard e9_g1 = new ClockGuard(x, 10,  Relation.LESS_EQUAL);


        Guard e5_g1_1 = new ClockGuard(x, null,2, Relation.LESS_EQUAL); // x>10
        Guard e5_g1_2 = new ClockGuard(y, null, 3, Relation.GREATER_THAN);
        Guard e5_g2_1 = new ClockGuard(x, null,2, Relation.GREATER_THAN);
        Guard e5_g2_2 = new ClockGuard(x, null,3, Relation.LESS_THAN);
        Guard e5_g3_1 = new ClockGuard(x, null,3, Relation.GREATER_EQUAL);  // x>2
        Guard e5_g3_2 = new ClockGuard(x, null,5, Relation.LESS_EQUAL);
        Guard e5_g3_3 = new ClockGuard(y, null,4, Relation.LESS_THAN);
        Guard e5_g4 = new ClockGuard(x, null,5, Relation.GREATER_THAN);

        Guard e6_g1 = new ClockGuard(x, null,10, Relation.GREATER_THAN);



        List<Guard> e5_1 = new ArrayList<>();
        e5_1.add(e5_g1_1);
        e5_1.add(e5_g1_2);
        Guard guards_e5_1= new AndGuard(e5_1);

        List<Guard> e5_2 = new ArrayList<>();
        e5_2.add(e5_g2_1);
        e5_2.add(e5_g2_2);
        Guard guards_e5_2= new AndGuard(e5_2);

        List<Guard> e5_3 = new ArrayList<>();
        e5_3.add(e5_g3_1);
        e5_3.add(e5_g3_2);
        e5_3.add(e5_g3_3);
        Guard guards_e5_3= new AndGuard(e5_3);

        List<Guard> e5_4 = new ArrayList<>();
        e5_4.add(e5_g4);
        Guard guards_e5_4= new AndGuard(e5_4);

        List<Guard> guards_e5_or = new ArrayList<>();
        guards_e5_or.add(guards_e5_1);
        guards_e5_or.add(guards_e5_2);
        guards_e5_or.add(guards_e5_3);
        guards_e5_or.add(guards_e5_4);
        Guard guards_e5 = new OrGuard(guards_e5_or);

        //Guard e9_g2 = new Guard(y, 10, false, false);

        Edge e1 = new Edge(l0, l1, i1, true, invL1, noUpdate);
        Edge e2 = new Edge(l0, l2, i2, true, new AndGuard(e2_g1, e2_g2, e2_g3), noUpdate);
        Edge e3 = new Edge(l0, l3, i2, true, new AndGuard(e3_g1, e3_g2), noUpdate);
        Edge e4 = new Edge(l1, l4, o, false, new TrueGuard(), noUpdate);
        Edge e6 = new Edge(l0, l0, i1, true, e6_g1, noUpdate);
        Edge e5 = new Edge(l0, l0, i2, true, guards_e5, noUpdate);
        //Edge e6 = new Edge(l0, l0, i2, true, new ArrayList<>(Collections.singletonList(Arrays.asList(e6_g1, e6_g2, e6_g3, e6_g4))), noUpdate);
        //Edge e7 = new Edge(l0, l0, i2, true, new ArrayList<>(Collections.singletonList(Arrays.asList(e7_g1, e7_g2))), noUpdate);
        Edge e8 = new Edge(l1, l1, i1, true,e8_g1/*, e8_g2*/, noUpdate);
        Edge e9 = new Edge(l1, l1, i2, true, e9_g1/*, e9_g2*/, noUpdate);
        Edge e10 = new Edge(l2, l2, i1, true, new TrueGuard(), noUpdate);
        Edge e11 = new Edge(l2, l2, i2, true, new TrueGuard(), noUpdate);
        Edge e12 = new Edge(l3, l3, i1, true, new TrueGuard(), noUpdate);
        Edge e13 = new Edge(l3, l3, i2, true, new TrueGuard(), noUpdate);
        Edge e14 = new Edge(l4, l4, i1, true, new TrueGuard(), noUpdate);
        Edge e15 = new Edge(l4, l4, i2, true, new TrueGuard(), noUpdate);

        List<Location> locations = new ArrayList<>(Arrays.asList(l0, l1, l2, l3, l4));
        List<Edge> edges = new ArrayList<>(Arrays.asList(e1, e2, e3, e4, e6, e5, e8, e9, e10, e11, e12, e13, e14, e15));
        List<Clock> clocks = new ArrayList<>(Arrays.asList(x, y));
        List<BoolVar> BVs = new ArrayList<>();

        expected = new Automaton("Automaton", locations, edges, clocks, BVs, false);

        String base = "./samples/json/InputEnabled/";
        String[] components = new String[]{"GlobalDeclarations.json", "Components/Automaton.json"};


        actual = JSONParser.parse(base, components, true)[0];
        //SimpleTransitionSystem st = new SimpleTransitionSystem(actual);
       //  st.toXML("BASE.xml");
        actual = (actual);
        Log.trace("fnished setup");
    }

    @Test
    public void testAutomaton() {
        SimpleTransitionSystem st = new SimpleTransitionSystem(actual);
        st.toXML("testOutput/wtf.xml");
        SimpleTransitionSystem st1 = new SimpleTransitionSystem(expected);
        st1.toXML("testOutput/wtf-exp.xml");
        assert (new Refinement(new SimpleTransitionSystem(expected),new SimpleTransitionSystem(actual)).check());
        assert (new Refinement(new SimpleTransitionSystem(actual),new SimpleTransitionSystem(expected)).check());
    }

}
