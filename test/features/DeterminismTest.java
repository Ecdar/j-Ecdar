package features;

import exceptions.CddAlreadyRunningException;
import exceptions.CddNotRunningException;
import logic.Composition;
import logic.Refinement;
import logic.SimpleTransitionSystem;
import logic.TransitionSystem;
import models.Automaton;
import models.CDD;
import models.Clock;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import parser.XMLParser;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;

public class DeterminismTest {

    static Automaton[] automata;
    private static TransitionSystem G1, G2, G3, G4, G5, G6, G7, G8, G9, G10, G11, G12, G13, G14, G15, G16, G17, G22, G23;

    @After
    public void afterEachTest(){
        CDD.done();
    }

    @BeforeClass
    public static void setUpBeforeClass() {
        automata = XMLParser.parse("./samples/xml/ConsTests.xml", true);
        G1 = new SimpleTransitionSystem(automata[0]);
        G2 = new SimpleTransitionSystem(automata[1]);
        G3 = new SimpleTransitionSystem(automata[2]);
        G4 = new SimpleTransitionSystem(automata[3]);
        G5 = new SimpleTransitionSystem(automata[4]);
        G6 = new SimpleTransitionSystem(automata[5]);
        G7 = new SimpleTransitionSystem(automata[6]);
        G8 = new SimpleTransitionSystem(automata[7]);
        G9 = new SimpleTransitionSystem(automata[8]);
        G10 = new SimpleTransitionSystem(automata[9]);
        G11 = new SimpleTransitionSystem(automata[10]);
        G12 = new SimpleTransitionSystem(automata[11]);
        G13 = new SimpleTransitionSystem(automata[12]);
        G14 = new SimpleTransitionSystem(automata[13]);
        G15 = new SimpleTransitionSystem(automata[14]);
        G16 = new SimpleTransitionSystem(automata[15]);
        G17 = new SimpleTransitionSystem(automata[16]);
        G22 = new SimpleTransitionSystem(automata[21]);
        G23 = new SimpleTransitionSystem(automata[22]);
    }

    @Test
    public void testG1EqualsG1() throws CddAlreadyRunningException, CddNotRunningException {
        Automaton copy = new Automaton(G1.getAutomaton());
        //assertEquals(new SimpleTransitionSystem((G1.getAutomaton())), new SimpleTransitionSystem((copy)));
        assert(new Refinement(new SimpleTransitionSystem((G1.getAutomaton())), new SimpleTransitionSystem((copy))).check());
        assert(new Refinement(new SimpleTransitionSystem((copy)), new SimpleTransitionSystem((G1.getAutomaton()))).check());
    }

    @Test
    public void testG1NotEqualsNull() throws CddNotRunningException, CddAlreadyRunningException {
        assertNotEquals(G1, null);
    }

    @Test
    public void testG1NotEqualsClock(){
        assertNotEquals(G1, new Clock("wierd test", "Aut"));
    }

    @Test
    public void testG1() throws CddAlreadyRunningException, CddNotRunningException {
        assertTrue((new SimpleTransitionSystem((G1.getAutomaton()))).isDeterministic());
    }

    @Test
    public void testG2() throws CddAlreadyRunningException, CddNotRunningException {
        assertTrue((new SimpleTransitionSystem((G2.getAutomaton()))).isDeterministic());

}

    @Test
    public void testG3() throws CddAlreadyRunningException, CddNotRunningException {
        assertTrue((new SimpleTransitionSystem((G3.getAutomaton()))).isDeterministic());
    }

    @Test
    public void G1G5IsDeterministic() throws CddAlreadyRunningException, CddNotRunningException {
        TransitionSystem ts = new Composition((new SimpleTransitionSystem((G1.getAutomaton()))), (new SimpleTransitionSystem((G5.getAutomaton()))));
        assertTrue(ts.isDeterministic());
    }

    @Test
    public void G1G9IsNotDeterministic() throws CddAlreadyRunningException, CddNotRunningException {
        TransitionSystem ts = new Composition((new SimpleTransitionSystem((G1.getAutomaton()))), (new SimpleTransitionSystem((G9.getAutomaton()))));
        assertFalse(ts.isDeterministic());
    }

    @Test
    public void testG4() throws CddAlreadyRunningException, CddNotRunningException {
        assertTrue(new SimpleTransitionSystem((G4.getAutomaton())).isDeterministic());
    }

    @Test
    public void testG5() throws CddAlreadyRunningException, CddNotRunningException {
        assertTrue(new SimpleTransitionSystem((G5.getAutomaton())).isDeterministic());
    }

    @Test
    public void testG6() throws CddAlreadyRunningException, CddNotRunningException {
        assertTrue(new SimpleTransitionSystem((G6.getAutomaton())).isDeterministic());
    }

    @Test
    public void testG7() throws CddAlreadyRunningException, CddNotRunningException {
        assertTrue(new SimpleTransitionSystem((G7.getAutomaton())).isDeterministic());
    }

    @Test
    public void testG8() throws CddAlreadyRunningException, CddNotRunningException {
        assertTrue(new SimpleTransitionSystem((G8.getAutomaton())).isDeterministic());
    }

    @Test
    public void testG9() throws CddAlreadyRunningException, CddNotRunningException {
        assertFalse(new SimpleTransitionSystem((G9.getAutomaton())).isDeterministic());
    }

    @Test
    public void testG10() throws CddAlreadyRunningException, CddNotRunningException {
        assertTrue(new SimpleTransitionSystem((G10.getAutomaton())).isDeterministic());
    }

    @Test
    public void testG11() throws CddAlreadyRunningException, CddNotRunningException {
        assertTrue(new SimpleTransitionSystem((G11.getAutomaton())).isDeterministic());
    }

    @Test
    public void testG12() throws CddAlreadyRunningException, CddNotRunningException {
        assertTrue(new SimpleTransitionSystem(((G12.getAutomaton()))).isDeterministic());
    }

    @Test
    public void testG13() throws CddNotRunningException, CddAlreadyRunningException {
        ((SimpleTransitionSystem) (G13)).toXML("testOutput/st-ic.xml");
        assertTrue(new SimpleTransitionSystem((G13.getAutomaton())).isDeterministic());
    }

    @Test
    public void testG14() throws CddAlreadyRunningException, CddNotRunningException {
        assertFalse(G14.isDeterministic());
    }

    @Test
    public void testG15() throws CddAlreadyRunningException, CddNotRunningException {
        assertTrue(G15.isDeterministic());
    }

    @Test
    public void testG16() throws CddAlreadyRunningException, CddNotRunningException {
        assertFalse(G16.isDeterministic());
    }

    @Test
    public void testG17() throws CddAlreadyRunningException, CddNotRunningException {
        SimpleTransitionSystem inputEnabled = new SimpleTransitionSystem((G17.getAutomaton()));
        inputEnabled.toXML("testOutput/inputEnabledTestG17.xml");
        assertTrue(inputEnabled.isDeterministic());
    }

    @Test
    public void testG22() throws CddAlreadyRunningException, CddNotRunningException {
        assertTrue(G22.isDeterministic());
    }

    @Test
    public void testG23() throws CddAlreadyRunningException, CddNotRunningException {
        assertFalse(G23.isDeterministic());
    }
}
