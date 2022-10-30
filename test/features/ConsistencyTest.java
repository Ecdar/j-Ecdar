package features;

import log.Log;
import logic.*;
import models.Automaton;
import models.CDD;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import parser.XMLParser;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;

public class ConsistencyTest {

    static Automaton[] automata;
    private static TransitionSystem G1, G3, G4, G5, G7, G8, G9, G10, G12, G21;

    @AfterClass
    public static void afterEachTest(){
        CDD.done();
    }

    @BeforeClass
    public static void setUpBeforeClass() {
        automata = XMLParser.parse("./samples/xml/ConsTests.xml", true);
        G1 = new SimpleTransitionSystem(automata[0]);
        G3 = new SimpleTransitionSystem(automata[2]);
        G4 = new SimpleTransitionSystem(automata[3]);
        G5 = new SimpleTransitionSystem(automata[4]);
        G7 = new SimpleTransitionSystem(automata[6]);
        G8 = new SimpleTransitionSystem(automata[7]);
        G9 = new SimpleTransitionSystem(automata[8]);
        G10 = new SimpleTransitionSystem(automata[9]);
        G12 = new SimpleTransitionSystem(automata[11]);
        G21 = new SimpleTransitionSystem(automata[20]);
    }

    @Test
    public void testG1(){
        assertTrue(G1.isLeastConsistent());
    }

    @Test
    public void testG2(){
        TransitionSystem ts = new SimpleTransitionSystem(automata[1]);

        assertTrue(ts.isLeastConsistent());
    }

    @Test
    public void testG3(){
        TransitionSystem ts = new SimpleTransitionSystem(automata[2]);

        assertFalse(ts.isLeastConsistent());
    }

    @Test
    public void testG4(){
        TransitionSystem ts = new SimpleTransitionSystem(automata[3]);

        assertFalse(ts.isLeastConsistent());
    }

    @Test
    public void testG5(){
        assertFalse(G5.isDeterministic() && G5.isLeastConsistent());
    }

    @Test
    public void G1G5IsNotConsistent(){
        TransitionSystem ts = new Composition(new TransitionSystem[]{G1, G5});

        assertFalse(ts.isLeastConsistent());
    }

    @Test
    public void G1G8isLeastConsistent(){
        TransitionSystem ts = new Composition(new TransitionSystem[]{G1, G8});

        assertTrue(ts.isLeastConsistent());
    }

    @Test
    public void testG6(){

        TransitionSystem ts = new SimpleTransitionSystem(automata[5]);

        ts.getAutomaton().getEdges().forEach(e->Log.trace(e));
        assertTrue(ts.isLeastConsistent());
    }

    @Test
    public void testG7(){
        TransitionSystem ts = new SimpleTransitionSystem(automata[6]);

        assertFalse(ts.isLeastConsistent());
    }

    @Test
    public void testG8(){
        assertTrue(G8.isLeastConsistent());
    }

    @Test
    public void testG9(){
        TransitionSystem ts = new SimpleTransitionSystem(automata[8]);
        assertFalse(ts.isLeastConsistent());
    }

    @Test
    public void testG10(){
        TransitionSystem ts = new SimpleTransitionSystem(automata[9]);

        assertFalse(ts.isLeastConsistent());
    }

    @Test
    public void testG11(){
        TransitionSystem ts = new SimpleTransitionSystem(automata[10]);

        assertFalse(ts.isLeastConsistent());
    }

    @Test
    public void testG12(){
        TransitionSystem ts = new SimpleTransitionSystem(automata[11]);

        assertFalse(ts.isLeastConsistent());
    }

    @Test
    public void testG13(){
        TransitionSystem ts = new SimpleTransitionSystem(automata[12]);

        assertTrue(ts.isLeastConsistent());
    }

    @Test
    public void testG14(){
        TransitionSystem ts = new SimpleTransitionSystem(automata[13]);

        assertFalse(ts.isLeastConsistent());
    }

    @Test
    public void testG15(){
        TransitionSystem ts = new SimpleTransitionSystem(automata[14]);

        assertTrue(ts.isLeastConsistent());
    }

    @Test
    public void testG16(){
        TransitionSystem ts = new SimpleTransitionSystem(automata[15]);

        assertFalse(ts.isLeastConsistent());
    }

    @Test
    public void testG17(){
        TransitionSystem ts = new SimpleTransitionSystem(automata[16]);

        assertTrue(ts.isLeastConsistent());
    }

    @Test
    public void testG18(){
        TransitionSystem ts = new SimpleTransitionSystem(automata[17]);

        assertTrue(ts.isLeastConsistent());
    }

    @Test
    public void testG19(){
        TransitionSystem ts = new SimpleTransitionSystem(automata[18]);

        assertFalse(ts.isLeastConsistent());
    }

    @Test
    public void testG20(){
        TransitionSystem ts = new SimpleTransitionSystem(automata[19]);

        assertTrue(ts.isLeastConsistent());
    }

    @Test
    public void testG21(){
        TransitionSystem ts = new SimpleTransitionSystem(automata[20]);

        assertTrue(ts.isLeastConsistent());
    }

    @Test
    public void multipleInconsistenciesError() {
        TransitionSystem ts1 = new Conjunction(new TransitionSystem[] {G3, G4});
        TransitionSystem ts2 = new Composition(new TransitionSystem[] {G5, G7, G9, G10, G12});
        TransitionSystem comp = new Composition(new TransitionSystem[] {ts1, ts2});

        Refinement ref = new Refinement(comp, G21);

        assertFalse(ref.check());
        Log.trace(ref.getErrMsg());
        assertEquals("Automaton G9 is non-deterministic." + ", Automata G3, G4, G5, G7, G10, G12 are inconsistent.", ref.getErrMsg());
    }
}
