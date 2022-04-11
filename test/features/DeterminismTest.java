package features;

import Exceptions.CddAlreadyRunningException;
import Exceptions.CddNotRunningException;
import logic.Composition;
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
        CDD.init(100,100,100);
        List<Clock> clocks = new ArrayList<>();
        clocks.addAll(G1.getClocks());
        clocks.addAll(copy.getClocks());
        CDD.addClocks(clocks);
        assertEquals(new SimpleTransitionSystem(CDD.makeInputEnabled(G1.getAutomaton())), new SimpleTransitionSystem(CDD.makeInputEnabled(copy)));
    }

    @Test
    public void testG1NotEqualsNull() throws CddNotRunningException, CddAlreadyRunningException {
        CDD.init(100,100,100);
        List<Clock> clocks = new ArrayList<>();
        clocks.addAll(G1.getClocks());
        CDD.addClocks(clocks);
        assertNotEquals(G1, null);
    }

    @Test
    public void testG1NotEqualsClock(){
        assertNotEquals(G1, new Clock("wierd test"));
    }

    @Test
    public void testG1() throws CddAlreadyRunningException, CddNotRunningException {

        CDD.init(100,100,100);
        List<Clock> clocks = new ArrayList<>();
        clocks.addAll(G1.getClocks());
        CDD.addClocks(clocks);
        assertTrue((new SimpleTransitionSystem(CDD.makeInputEnabled(G1.getAutomaton()))).isDeterministic());
    }

    @Test
    public void testG2() throws CddAlreadyRunningException, CddNotRunningException {
        CDD.init(100,100,100);
        CDD.addClocks(G2.getClocks());
        assertTrue((new SimpleTransitionSystem(CDD.makeInputEnabled(G2.getAutomaton()))).isDeterministic());

}

    @Test
    public void testG3() throws CddAlreadyRunningException, CddNotRunningException {
        CDD.init(100,100,100);
        CDD.addClocks(G3.getClocks());
        assertTrue((new SimpleTransitionSystem(CDD.makeInputEnabled(G3.getAutomaton()))).isDeterministic());
    }

    @Test
    public void G1G5IsDeterministic() throws CddAlreadyRunningException, CddNotRunningException {
        CDD.init(100,100,100);
        CDD.addClocks(G1.getClocks(), G5.getClocks());
        TransitionSystem ts = new Composition(new TransitionSystem[]{(new SimpleTransitionSystem(CDD.makeInputEnabled(G1.getAutomaton()))), (new SimpleTransitionSystem(CDD.makeInputEnabled(G5.getAutomaton())))});
        assertTrue(ts.isDeterministic());
    }

    @Test
    public void G1G9IsNotDeterministic() throws CddAlreadyRunningException, CddNotRunningException {
        CDD.init(100,100,100);
        CDD.addClocks(G1.getClocks(), G9.getClocks());
        TransitionSystem ts = new Composition(new TransitionSystem[]{(new SimpleTransitionSystem(CDD.makeInputEnabled(G1.getAutomaton()))), (new SimpleTransitionSystem(CDD.makeInputEnabled(G9.getAutomaton())))});
        assertFalse(ts.isDeterministic());
    }

    @Test
    public void testG4() throws CddAlreadyRunningException, CddNotRunningException {
        CDD.init(100,100,100);
        CDD.addClocks(G4.getClocks());
        assertTrue(new SimpleTransitionSystem(CDD.makeInputEnabled(G4.getAutomaton())).isDeterministic());
    }

    @Test
    public void testG5() throws CddAlreadyRunningException, CddNotRunningException {
        CDD.init(100,100,100);
        CDD.addClocks(G5.getClocks());
        assertTrue(new SimpleTransitionSystem(CDD.makeInputEnabled(G5.getAutomaton())).isDeterministic());
    }

    @Test
    public void testG6() throws CddAlreadyRunningException, CddNotRunningException {
        CDD.init(100,100,100);
        CDD.addClocks(G6.getClocks());
        assertTrue(new SimpleTransitionSystem(CDD.makeInputEnabled(G6.getAutomaton())).isDeterministic());
    }

    @Test
    public void testG7() throws CddAlreadyRunningException, CddNotRunningException {
        CDD.init(100,100,100);
        CDD.addClocks(G7.getClocks());
        assertTrue(new SimpleTransitionSystem(CDD.makeInputEnabled(G7.getAutomaton())).isDeterministic());
    }

    @Test
    public void testG8() throws CddAlreadyRunningException, CddNotRunningException {
        CDD.init(100,100,100);
        CDD.addClocks(G8.getClocks());
        assertTrue(new SimpleTransitionSystem(CDD.makeInputEnabled(G8.getAutomaton())).isDeterministic());
    }

    @Test
    public void testG9() throws CddAlreadyRunningException, CddNotRunningException {
        CDD.init(100,100,100);
        CDD.addClocks(G9.getClocks());
        assertFalse(new SimpleTransitionSystem(CDD.makeInputEnabled(G9.getAutomaton())).isDeterministic());
    }

    @Test
    public void testG10() throws CddAlreadyRunningException, CddNotRunningException {
        CDD.init(100,100,100);
        CDD.addClocks(G10.getClocks());
        assertTrue(new SimpleTransitionSystem(CDD.makeInputEnabled(G10.getAutomaton())).isDeterministic());
    }

    @Test
    public void testG11() throws CddAlreadyRunningException, CddNotRunningException {
        CDD.init(100,100,100);
        CDD.addClocks(G11.getClocks());
        assertTrue(new SimpleTransitionSystem(CDD.makeInputEnabled(G11.getAutomaton())).isDeterministic());
    }

    @Test
    public void testG12() throws CddAlreadyRunningException, CddNotRunningException {
        CDD.init(100,100,100);
        CDD.addClocks(G12.getClocks());
        assertTrue(new SimpleTransitionSystem(CDD.makeInputEnabled((G12.getAutomaton()))).isDeterministic());
    }

    @Test
    public void testG13() throws CddNotRunningException, CddAlreadyRunningException {
        CDD.init(100,100,100);
        CDD.addClocks(G13.getClocks());
        ((SimpleTransitionSystem) (G13)).toXML("st-ic.xml");
        assertTrue(new SimpleTransitionSystem(CDD.makeInputEnabled(G13.getAutomaton())).isDeterministic());
    }

    @Test
    public void testG14() throws CddAlreadyRunningException, CddNotRunningException {
        CDD.init(100,100,100);
        CDD.addClocks(G14.getClocks());
        assertFalse(G14.isDeterministic());
    }

    @Test
    public void testG15() throws CddAlreadyRunningException, CddNotRunningException {
        CDD.init(100,100,100);
        CDD.addClocks(G15.getClocks());
        assertTrue(G15.isDeterministic());
    }

    @Test
    public void testG16() throws CddAlreadyRunningException, CddNotRunningException {
        CDD.init(100,100,100);
        CDD.addClocks(G16.getClocks());
        assertFalse(G16.isDeterministic());
    }

    @Test
    public void testG17() throws CddAlreadyRunningException, CddNotRunningException {
        CDD.init(100,100,100);
        CDD.addClocks(G17.getClocks());
        SimpleTransitionSystem inputEnabled = new SimpleTransitionSystem(CDD.makeInputEnabled(G17.getAutomaton()));
        inputEnabled.toXML("inputEnabledTestG17.xml");
        assertTrue(inputEnabled.isDeterministic());
    }

    @Test
    public void testG22() throws CddAlreadyRunningException, CddNotRunningException {
        CDD.init(100,100,100);
        CDD.addClocks(G22.getClocks());
        assertTrue(G22.isDeterministic());
    }

    @Test
    public void testG23() throws CddAlreadyRunningException, CddNotRunningException {
        CDD.init(100,100,100);
        CDD.addClocks(G23.getClocks());
        assertFalse(G23.isDeterministic());
    }
}
