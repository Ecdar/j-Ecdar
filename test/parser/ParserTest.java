package parser;

import models.*;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ParserTest {
    private static List<Automaton> machines;
    private static List<Automaton> machines2;
    private static Automaton A, G, Q, Imp, Ref1, Ref2;

    @BeforeClass
    public static void setUpBeforeClass() {
        String base = "./samples/AG/";
        List<String> components = new ArrayList<>(Arrays.asList("GlobalDeclarations.json",
                "Components/A.json",
                "Components/G.json",
                "Components/Q.json",
                "Components/Imp.json"));
        machines = Parser.parse(base, components);

        Location l0 = new Location("L0", new ArrayList<>(), true, false, false, false);
        Location l1 = new Location("L1", new ArrayList<>(), false, false, false, false);
        Location l2 = new Location("L2", new ArrayList<>(), true, false, false, false);
        Location l3 = new Location("L3", new ArrayList<>(), true, false, false, false);
        Location l5 = new Location("L5", new ArrayList<>(), true, false, false, false);
        Location u0 = new Location("U0", new ArrayList<>(), false, false, true, false);

        Channel button1 = new Channel("button1");
        Channel button2 = new Channel("button2");
        Channel good = new Channel("good");
        Channel bad = new Channel("bad");

        Edge t1 = new Edge(l2, l2, bad, true, new ArrayList<>(), new ArrayList<>());
        Edge t2 = new Edge(l2, l2, good, true, new ArrayList<>(), new ArrayList<>());
        Edge t3 = new Edge(l2, l2, button1, false, new ArrayList<>(), new ArrayList<>());
        Edge t4 = new Edge(l3, l3, button1, true, new ArrayList<>(), new ArrayList<>());
        Edge t5 = new Edge(l3, l3, button2, true, new ArrayList<>(), new ArrayList<>());
        Edge t6 = new Edge(l3, l3, good, false, new ArrayList<>(), new ArrayList<>());
        Edge t7 = new Edge(l5, u0, button2, true, new ArrayList<>(), new ArrayList<>());
        Edge t8 = new Edge(l5, l5, good, false, new ArrayList<>(), new ArrayList<>());
        Edge t9 = new Edge(l5, l5, button1, true, new ArrayList<>(), new ArrayList<>());
        Edge t10 = new Edge(l0, l0, button1, true, new ArrayList<>(), new ArrayList<>());
        Edge t11 = new Edge(l0, l0, good, false, new ArrayList<>(), new ArrayList<>());
        Edge t12 = new Edge(l1, l1, button1, true, new ArrayList<>(), new ArrayList<>());
        Edge t13 = new Edge(l1, l1, button2, true, new ArrayList<>(), new ArrayList<>());
        Edge t14 = new Edge(l1, l1, good, false, new ArrayList<>(), new ArrayList<>());
        Edge t15 = new Edge(l1, l1, bad, false, new ArrayList<>(), new ArrayList<>());
        Edge t16 = new Edge(l0, l1, button2, true, new ArrayList<>(), new ArrayList<>());


        A = new Automaton("A", new ArrayList<>(Collections.singletonList(l2)), new ArrayList<>(Arrays.asList(t1, t2, t3)), new ArrayList<>());
        G = new Automaton("G", new ArrayList<>(Collections.singletonList(l3)), new ArrayList<>(Arrays.asList(t4, t5, t6)), new ArrayList<>());
        Q = new Automaton("Q", new ArrayList<>(Arrays.asList(l5, u0)), new ArrayList<>(Arrays.asList(t7, t8, t9)), new ArrayList<>());
        Imp = new Automaton("Imp", new ArrayList<>(Arrays.asList(l0, l1)), new ArrayList<>(Arrays.asList(t10, t11, t12, t13, t14, t15, t16)), new ArrayList<>());


        // Adding BigRefinement example automata
        base = "./samples/BigRefinement/";
        components = new ArrayList<>(Arrays.asList("GlobalDeclarations.json",
                "Components/Ref1.json"));
        machines2 = Parser.parse(base, components);

        Clock x = new Clock("x");
        Clock y = new Clock("y");

        models.Guard g_l12_l17 = new Guard(x, 15,false, false);
        models.Guard g_l12_l14 = new Guard(x, 20,false, true);
        models.Guard g_l12_l13 = new Guard(x, 5,false, true);
        models.Guard g_l12_l15 = new Guard(x, 8,false, false);
        models.Guard g_l12_l16 = new Guard(x, 55,false, true);
        models.Guard g_l15_l18 = new Guard(x, 15,true, true);
        models.Guard inv_l15 = new Guard(x, 20, false, false);

        Update u1 = new Update(x, 0);

        Location l12 = new Location("L12", new ArrayList<>(), true, false, false, false);
        Location l13 = new Location("L13", new ArrayList<>(), false, false, false, false);
        Location l14 = new Location("L14", new ArrayList<>(), false, false, false, false);
        Location l15 = new Location("L15", new ArrayList<>(Collections.singletonList(inv_l15)), false, false, false, false);
        Location l16 = new Location("L16", new ArrayList<>(), false, false, false, false);
        Location l17 = new Location("L17", new ArrayList<>(), false, false, false, false);
        Location l18 = new Location("L18", new ArrayList<>(), false, false, false, false);

        Channel i1 = new Channel("i1"); Channel i2 = new Channel("i2");
        Channel i3 = new Channel("i3"); Channel i4 = new Channel("i4");
        Channel i5 = new Channel("i5"); Channel i6 = new Channel("i6");
        Channel o1 = new Channel("o1"); Channel o2 = new Channel("o2");
        Channel o3 = new Channel("o3"); Channel o4 = new Channel("o4");
        Channel o5 = new Channel("o5"); Channel o6 = new Channel("o6");
        Channel o7 = new Channel("o7"); Channel o8 = new Channel("o8");
        Channel o9 = new Channel("o9"); Channel o10 = new Channel("o10");


        t1 = new Edge(l12, l14, i2, true, new ArrayList<>(Collections.singletonList(g_l12_l14)), new ArrayList<>());
        t2 = new Edge(l12, l17, i3, true, new ArrayList<>(Collections.singletonList(g_l12_l17)), new ArrayList<>());
        t3 = new Edge(l12, l15, i4, true, new ArrayList<>(Collections.singletonList(g_l12_l15)), new ArrayList<>(Collections.singletonList(u1)));
        t4 = new Edge(l12, l16, i5, true, new ArrayList<>(Collections.singletonList(g_l12_l16)), new ArrayList<>());
        t5 = new Edge(l17, l18, o8, false, new ArrayList<>(), new ArrayList<>(Collections.singletonList(u1)));
        t6 = new Edge(l16, l18, o8, false, new ArrayList<>(), new ArrayList<>());
        t7 = new Edge(l15, l18, o8, false, new ArrayList<>(Collections.singletonList(g_l15_l18)), new ArrayList<>());
        t8 = new Edge(l14, l18, o8, false, new ArrayList<>(), new ArrayList<>());
        t9 = new Edge(l13, l18, o8, false, new ArrayList<>(), new ArrayList<>());
        t10 = new Edge(l17, l17, o3, false, new ArrayList<>(), new ArrayList<>());
        t11 = new Edge(l17, l17, o5, false, new ArrayList<>(), new ArrayList<>());
        t12 = new Edge(l17, l14, i6, true, new ArrayList<>(), new ArrayList<>());
        t13 = new Edge(l12, l13, i1, true, new ArrayList<>(Collections.singletonList(g_l12_l13)), new ArrayList<>());

        Ref1 = new Automaton("Ref1", new ArrayList<>(Arrays.asList(l12, l13, l14, l15, l16, l17, l18)),
                new ArrayList<>(Arrays.asList(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13)), new ArrayList<>(Arrays.asList(x, y)));

        Ref2 = new Automaton("Ref1", new ArrayList<>(Arrays.asList(l12, l13, l14, l15, l16, l17, l18)),
                new ArrayList<>(Arrays.asList(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13)), new ArrayList<>(Arrays.asList(x, y)));
    }

    @Test
    public void testA() {
        assert A.equals(machines.get(0));
    }

    @Test
    public void testG() {
        assert G.equals(machines.get(1));
    }

    @Test
    public void testQ() {
        assert Q.equals(machines.get(2));
    }

    @Test
    public void testImp() {
        assert Imp.equals(machines.get(3));
    }

    @Test
    public void testRef1() {
        assert Ref1.equals(machines2.get(0));
    }
}
