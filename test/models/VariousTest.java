package models;

import logic.Composition;
import logic.Refinement;
import logic.SimpleTransitionSystem;
import logic.TransitionSystem;
import org.junit.BeforeClass;
import org.junit.Test;
import parser.JSONParser;
import parser.XMLParser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class VariousTest {

    @BeforeClass
    public static void setUpBeforeClass() {

    }

    @Test
    public void simple() {

        Automaton[] aut2 = XMLParser.parse("samples/xml/simple.xml", true);

        SimpleTransitionSystem simp2 = new SimpleTransitionSystem(aut2[0]);

        simp2.toXML("simple1.xml");

        assert (true);
    }

    @Test
    public void next() {
        Clock y = new Clock("y");
        List<Clock> clocks = new ArrayList<>(Arrays.asList(y));
        Zone z1 = new Zone(clocks.size()+1,true);
        z1.init();
        Zone z2 = new Zone(clocks.size()+1,true);
        z2.init();

        Guard g1 = new Guard(y, 5, true, false);
        z1.buildConstraintsForGuard(g1,1);

        z1.printDBM(true,true);
        Guard g2 = new Guard(y, 6, true, false);
        System.out.println(g2);
        z2.buildConstraintsForGuard(g2,1);
        z2.printDBM(true,true);

        List<Zone> zoneList1 = new ArrayList<>();
        List<Zone> zoneList2 = new ArrayList<>();
        zoneList1.add(z1);
        zoneList2.add(z2);
        Federation f1 = new Federation(zoneList1);
        Federation f2 = new Federation(zoneList2);

        System.out.println(f1.isSubset(f2));
        System.out.println(f2.isSubset(f1));
        System.out.println(f1.isSubset(f1));
        System.out.println(f2.isSubset(f2));
    }

    @Test
    public void testCompOfCompRefinesSpec() {

        Automaton[] aut2 = XMLParser.parse("samples/xml/university-slice.xml", true);

        SimpleTransitionSystem adm = new SimpleTransitionSystem(aut2[3]);
        SimpleTransitionSystem machine = new SimpleTransitionSystem(aut2[0]);
        SimpleTransitionSystem researcher = new SimpleTransitionSystem(aut2[1]);
        SimpleTransitionSystem spec = new SimpleTransitionSystem(aut2[2]);

        assertTrue(new Refinement(
                new Composition(new TransitionSystem[]{adm,
                        new Composition(new TransitionSystem[]{machine, researcher})}),
                spec).check()
        );


    }
}
