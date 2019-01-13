package parser;

import models.*;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ParserTest {
    private static List<Component> machines;
    private static List<Component> machines2;
    private static Component A, G, Q, Imp, Ref1, Ref2;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        String fileName = "src/" + System.mapLibraryName("DBM");
        File lib = new File(fileName);
        System.load(lib.getAbsolutePath());

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

        Transition t1 = new Transition(l2, l2, bad, true, new ArrayList<>(), new ArrayList<>());
        Transition t2 = new Transition(l2, l2, good, true, new ArrayList<>(), new ArrayList<>());
        Transition t3 = new Transition(l2, l2, button1, false, new ArrayList<>(), new ArrayList<>());
        Transition t4 = new Transition(l3, l3, button1, true, new ArrayList<>(), new ArrayList<>());
        Transition t5 = new Transition(l3, l3, button2, true, new ArrayList<>(), new ArrayList<>());
        Transition t6 = new Transition(l3, l3, good, false, new ArrayList<>(), new ArrayList<>());
        Transition t7 = new Transition(l5, u0, button2, true, new ArrayList<>(), new ArrayList<>());
        Transition t8 = new Transition(l5, l5, good, false, new ArrayList<>(), new ArrayList<>());
        Transition t9 = new Transition(l5, l5, button1, true, new ArrayList<>(), new ArrayList<>());
        Transition t10 = new Transition(l0, l0, button1, true, new ArrayList<>(), new ArrayList<>());
        Transition t11 = new Transition(l0, l0, good, false, new ArrayList<>(), new ArrayList<>());
        Transition t12 = new Transition(l1, l1, button1, true, new ArrayList<>(), new ArrayList<>());
        Transition t13 = new Transition(l1, l1, button2, true, new ArrayList<>(), new ArrayList<>());
        Transition t14 = new Transition(l1, l1, good, false, new ArrayList<>(), new ArrayList<>());
        Transition t15 = new Transition(l1, l1, bad, false, new ArrayList<>(), new ArrayList<>());
        Transition t16 = new Transition(l0, l1, button2, true, new ArrayList<>(), new ArrayList<>());


        A = new Component("A", new ArrayList<>(Arrays.asList(l2)), new ArrayList<>(Arrays.asList(t1, t2, t3)), new ArrayList<>());
        G = new Component("G", new ArrayList<>(Arrays.asList(l3)), new ArrayList<>(Arrays.asList(t4, t5, t6)), new ArrayList<>());
        Q = new Component("Q", new ArrayList<>(Arrays.asList(l5, u0)), new ArrayList<>(Arrays.asList(t7, t8, t9)), new ArrayList<>());
        Imp = new Component("Imp", new ArrayList<>(Arrays.asList(l0, l1)), new ArrayList<>(Arrays.asList(t10, t11, t12, t13, t14, t15, t16)), new ArrayList<>());


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
        Location l15 = new Location("L15", new ArrayList<>(Arrays.asList(inv_l15)), false, false, false, false);
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


        t1 = new Transition(l12, l14, i2, true, new ArrayList<>(Arrays.asList(g_l12_l14)), new ArrayList<>());
        t2 = new Transition(l12, l17, i3, true, new ArrayList<>(Arrays.asList(g_l12_l17)), new ArrayList<>());
        t3 = new Transition(l12, l15, i4, true, new ArrayList<>(Arrays.asList(g_l12_l15)), new ArrayList<>(Arrays.asList(u1)));
        t4 = new Transition(l12, l16, i5, true, new ArrayList<>(Arrays.asList(g_l12_l16)), new ArrayList<>());
        t5 = new Transition(l17, l18, o8, false, new ArrayList<>(), new ArrayList<>(Arrays.asList(u1)));
        t6 = new Transition(l16, l18, o8, false, new ArrayList<>(), new ArrayList<>());
        t7 = new Transition(l15, l18, o8, false, new ArrayList<>(Arrays.asList(g_l15_l18)), new ArrayList<>());
        t8 = new Transition(l14, l18, o8, false, new ArrayList<>(), new ArrayList<>());
        t9 = new Transition(l13, l18, o8, false, new ArrayList<>(), new ArrayList<>());
        t10 = new Transition(l17, l17, o3, false, new ArrayList<>(), new ArrayList<>());
        t11 = new Transition(l17, l17, o5, false, new ArrayList<>(), new ArrayList<>());
        t12 = new Transition(l17, l14, i6, true, new ArrayList<>(), new ArrayList<>());
        t13 = new Transition(l12, l13, i1, true, new ArrayList<>(Arrays.asList(g_l12_l13)), new ArrayList<>());

        Ref1 = new Component("Ref1", new ArrayList<>(Arrays.asList(l12, l13, l14, l15, l16, l17, l18)),
                new ArrayList<>(Arrays.asList(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13)), new ArrayList<>(Arrays.asList(x, y)));

        Ref2 = new Component("Ref1", new ArrayList<>(Arrays.asList(l12, l13, l14, l15, l16, l17, l18)),
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
