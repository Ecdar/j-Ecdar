package parser;

import models.Channel;
import models.Component;
import models.Location;
import models.Transition;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ParserTest {
    private static List<Component> machines;
    private static Component A, G, Q, Imp;

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

        Location l0 = new Location("L0", null, true, false, false, false);
        Location l1 = new Location("L1", null, false, false, false, false);
        Location l2 = new Location("L2", null, true, false, false, false);
        Location l3 = new Location("L3", null, true, false, false, false);
        Location l5 = new Location("L5", null, true, false, false, false);
        Location u0 = new Location("U0", null, false, false, true, false);

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
}
