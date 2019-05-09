package features;

import logic.Composition;
import logic.SimpleTransitionSystem;
import logic.TransitionSystem;
import models.Automaton;
import org.junit.BeforeClass;
import org.junit.Test;
import parser.XMLParser;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;

public class ConsistencyTest {

    static Automaton[] automata;
    private static TransitionSystem G1, G5, G8;

    @BeforeClass
    public static void setUpBeforeClass() {
        automata = XMLParser.parse("./samples/xml/ConsTests.xml", true);
        G1 = new SimpleTransitionSystem(automata[0]);
        G5 = new SimpleTransitionSystem(automata[4]);
        G8 = new SimpleTransitionSystem(automata[7]);
    }

    @Test
    public void testG1(){
        assertTrue(G1.isConsistent());
    }
    @Test
    public void testG2(){
        TransitionSystem ts = new SimpleTransitionSystem(automata[1]);

        assertTrue(ts.isConsistent());
    }

    @Test
    public void testG3(){
        TransitionSystem ts = new SimpleTransitionSystem(automata[2]);

        assertFalse(ts.isConsistent());
    }

    @Test
    public void testG4(){
        TransitionSystem ts = new SimpleTransitionSystem(automata[3]);

        assertFalse(ts.isConsistent());
    }

    @Test
    public void testG5(){
        assertFalse(G5.isConsistent());
    }

    @Test
    public void G1G5IsNotConsistent(){
        TransitionSystem ts = new Composition(new TransitionSystem[]{G1, G5});

        assertFalse(ts.isConsistent());
    }

    @Test
    public void G1G8IsConsistent(){
        TransitionSystem ts = new Composition(new TransitionSystem[]{G1, G8});

        assertTrue(ts.isConsistent());
    }

    @Test
    public void testG6(){
        TransitionSystem ts = new SimpleTransitionSystem(automata[5]);

        assertTrue(ts.isConsistent());
    }

    @Test
    public void testG7(){
        TransitionSystem ts = new SimpleTransitionSystem(automata[6]);

        assertFalse(ts.isConsistent());
    }

    @Test
    public void testG8(){
        assertTrue(G8.isConsistent());
    }

    @Test
    public void testG9(){
        TransitionSystem ts = new SimpleTransitionSystem(automata[8]);

        assertFalse(ts.isConsistent());
    }

    @Test
    public void testG10(){
        TransitionSystem ts = new SimpleTransitionSystem(automata[9]);

        assertFalse(ts.isConsistent());
    }

    @Test
    public void testG11(){
        TransitionSystem ts = new SimpleTransitionSystem(automata[10]);

        assertFalse(ts.isConsistent());
    }

    @Test
    public void testG12(){
        TransitionSystem ts = new SimpleTransitionSystem(automata[11]);

        assertFalse(ts.isConsistent());
    }

    @Test
    public void testG13(){
        TransitionSystem ts = new SimpleTransitionSystem(automata[12]);

        assertTrue(ts.isConsistent());
    }

    @Test
    public void testG14(){
        TransitionSystem ts = new SimpleTransitionSystem(automata[13]);

        assertFalse(ts.isConsistent());
    }

    @Test
    public void testG15(){
        TransitionSystem ts = new SimpleTransitionSystem(automata[14]);

        assertTrue(ts.isConsistent());
    }

    @Test
    public void testG16(){
        TransitionSystem ts = new SimpleTransitionSystem(automata[15]);

        assertFalse(ts.isConsistent());
    }

    @Test
    public void testG17(){
        TransitionSystem ts = new SimpleTransitionSystem(automata[16]);

        assertTrue(ts.isConsistent());
    }

    @Test
    public void testG18(){
        TransitionSystem ts = new SimpleTransitionSystem(automata[17]);

        assertTrue(ts.isConsistent());
    }

    @Test
    public void testG19(){
        TransitionSystem ts = new SimpleTransitionSystem(automata[18]);

        assertFalse(ts.isConsistent());
    }

    @Test
    public void testG20(){
        TransitionSystem ts = new SimpleTransitionSystem(automata[19]);

        assertTrue(ts.isConsistent());
    }
}
