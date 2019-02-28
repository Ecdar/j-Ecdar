package parser;

import models.*;
import org.junit.BeforeClass;
import org.junit.Test;

public class ParserTest {
    private static Automaton[] machines, machines2;
    private static Automaton A, G, Q, Imp, Ref1;

    private static final Guard[] emptyGuards = new Guard[]{};
    private static final Update[] emptyUpdates = new Update[]{};
    private static final Clock[] emptyClocks = new Clock[]{};

    @BeforeClass
    public static void setUpBeforeClass() {
        String base = "./samples/AG/";
        String[] components = new String[]{"GlobalDeclarations.json",
                "Components/A.json",
                "Components/G.json",
                "Components/Q.json",
                "Components/Imp.json"};
        machines = Parser.parse(base, components);

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


        A = new Automaton("A", new Location[]{l2}, new Edge[]{t1, t2, t3}, emptyClocks);
        G = new Automaton("G", new Location[]{l3}, new Edge[]{t4, t5, t6}, emptyClocks);
        Q = new Automaton("Q", new Location[]{l5, u0}, new Edge[]{t7, t8, t9}, emptyClocks);
        Imp = new Automaton("Imp", new Location[]{l0, l1}, new Edge[]{t10, t11, t12, t13, t14, t15, t16}, emptyClocks);


        // Adding BigRefinement example automata
        base = "./samples/BigRefinement/";
        components = new String[]{"GlobalDeclarations.json", "Components/Ref1.json"};
        machines2 = Parser.parse(base, components);

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
        Location l15 = new Location("L15", new Guard[]{inv_l15}, false, false, false, false);
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


        t1 = new Edge(l12, l14, i2, true, new Guard[]{g_l12_l14}, emptyUpdates);
        t2 = new Edge(l12, l17, i3, true, new Guard[]{g_l12_l17}, emptyUpdates);
        t3 = new Edge(l12, l15, i4, true, new Guard[]{g_l12_l15}, new Update[]{u1});
        t4 = new Edge(l12, l16, i5, true, new Guard[]{g_l12_l16}, emptyUpdates);
        t5 = new Edge(l17, l18, o8, false, emptyGuards, new Update[]{u1});
        t6 = new Edge(l16, l18, o8, false, emptyGuards, emptyUpdates);
        t7 = new Edge(l15, l18, o8, false, new Guard[]{g_l15_l18}, emptyUpdates);
        t8 = new Edge(l14, l18, o8, false, emptyGuards, emptyUpdates);
        t9 = new Edge(l13, l18, o8, false, emptyGuards, emptyUpdates);
        t10 = new Edge(l17, l17, o3, false, emptyGuards, emptyUpdates);
        t11 = new Edge(l17, l17, o5, false, emptyGuards, emptyUpdates);
        t12 = new Edge(l17, l14, i6, true, emptyGuards, emptyUpdates);
        t13 = new Edge(l12, l13, i1, true, new Guard[]{g_l12_l13}, emptyUpdates);

        Ref1 = new Automaton("Ref1", new Location[]{l12, l13, l14, l15, l16, l17, l18},
                new Edge[]{t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13}, new Clock[]{x, y});
    }

    @Test
    public void testA() {
        assert A.equals(machines[0]);
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
}
