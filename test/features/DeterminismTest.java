package features;

import logic.SimpleTransitionSystem;
import logic.TransitionSystem;
import models.*;
import org.junit.BeforeClass;
import org.junit.Test;
import parser.XMLParser;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;

public class DeterminismTest {

    static Automaton[] automata;

    @BeforeClass
    public static void setUpBeforeClass() {
        automata = XMLParser.parse("./samples/xml/ConsTests.xml", true);
    }

    @Test
    public void testG1(){
        TransitionSystem ts = new SimpleTransitionSystem(automata[0]);

        assertTrue(ts.isDeterministic());
    }
    @Test
    public void testG2(){
        TransitionSystem ts = new SimpleTransitionSystem(automata[1]);

        assertTrue(ts.isDeterministic());
    }

    @Test
    public void testG3(){
        TransitionSystem ts = new SimpleTransitionSystem(automata[2]);

        assertTrue(ts.isDeterministic());
    }

    @Test
    public void testG4(){
        TransitionSystem ts = new SimpleTransitionSystem(automata[3]);

        assertTrue(ts.isDeterministic());
    }

    @Test
    public void testG5(){
        TransitionSystem ts = new SimpleTransitionSystem(automata[4]);

        assertTrue(ts.isDeterministic());
    }

    @Test
    public void testG6(){
        TransitionSystem ts = new SimpleTransitionSystem(automata[5]);

        assertTrue(ts.isDeterministic());
    }

    @Test
    public void testG7(){
        TransitionSystem ts = new SimpleTransitionSystem(automata[6]);

        assertTrue(ts.isDeterministic());
    }

    @Test
    public void testG8(){
        TransitionSystem ts = new SimpleTransitionSystem(automata[7]);

        assertTrue(ts.isDeterministic());
    }

    @Test
    public void testG9(){
        TransitionSystem ts = new SimpleTransitionSystem(automata[8]);

        assertFalse(ts.isDeterministic());
    }

    @Test
    public void testG10(){
        TransitionSystem ts = new SimpleTransitionSystem(automata[9]);

        assertTrue(ts.isDeterministic());
    }

    @Test
    public void testG11(){
        TransitionSystem ts = new SimpleTransitionSystem(automata[10]);

        assertTrue(ts.isDeterministic());
    }

    @Test
    public void testG12(){
        TransitionSystem ts = new SimpleTransitionSystem(automata[11]);

        assertTrue(ts.isDeterministic());
    }

    @Test
    public void testG13(){
        TransitionSystem ts = new SimpleTransitionSystem(automata[12]);

        assertTrue(ts.isDeterministic());
    }

    @Test
    public void testG14(){
        TransitionSystem ts = new SimpleTransitionSystem(automata[13]);

        assertFalse(ts.isDeterministic());
    }

    @Test
    public void testG15(){
        TransitionSystem ts = new SimpleTransitionSystem(automata[14]);

        assertTrue(ts.isDeterministic());
    }

    @Test
    public void testG16(){
        TransitionSystem ts = new SimpleTransitionSystem(automata[15]);

        assertFalse(ts.isDeterministic());
    }

    @Test
    public void testG17(){
        TransitionSystem ts = new SimpleTransitionSystem(automata[16]);

        assertTrue(ts.isDeterministic());
    }
}
