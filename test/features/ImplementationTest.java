package features;

import logic.SimpleTransitionSystem;
import logic.TransitionSystem;
import models.Automaton;
import org.junit.BeforeClass;
import org.junit.Test;
import parser.XMLParser;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;

public class ImplementationTest {

    static Automaton[] automata;

    @BeforeClass
    public static void setUpBeforeClass() {
        automata = XMLParser.parse("./samples/xml/ImplTests.xml", true);
    }

    @Test
    public void testG1(){
        TransitionSystem ts = new SimpleTransitionSystem(automata[1]);

        assertFalse(ts.isImplementation());
    }
    @Test
    public void testG2(){
        TransitionSystem ts = new SimpleTransitionSystem(automata[2]);

        assertFalse(ts.isImplementation());
    }

    @Test
    public void testG3(){
        TransitionSystem ts = new SimpleTransitionSystem(automata[3]);

        assertFalse(ts.isImplementation());
    }

    @Test
    public void testG4(){
        TransitionSystem ts = new SimpleTransitionSystem(automata[4]);

        assertFalse(ts.isImplementation());
    }

    @Test
    public void testG5(){
        TransitionSystem ts = new SimpleTransitionSystem(automata[5]);

        assertFalse(ts.isImplementation());
    }

    @Test
    public void testG6(){
        TransitionSystem ts = new SimpleTransitionSystem(automata[6]);

        assertFalse(ts.isImplementation());
    }

    @Test
    public void testG7(){
        TransitionSystem ts = new SimpleTransitionSystem(automata[7]);

        assertFalse(ts.isImplementation());
    }

    @Test
    public void testG8(){
        TransitionSystem ts = new SimpleTransitionSystem(automata[8]);

        assertTrue(ts.isImplementation());
    }

    @Test
    public void testG9(){
        TransitionSystem ts = new SimpleTransitionSystem(automata[9]);

        assertFalse(ts.isImplementation());
    }

    @Test
    public void testG10(){
        TransitionSystem ts = new SimpleTransitionSystem(automata[10]);

        assertFalse(ts.isImplementation());
    }

    @Test
    public void testG11(){
        TransitionSystem ts = new SimpleTransitionSystem(automata[11]);

        assertFalse(ts.isImplementation());
    }

    @Test
    public void testG12(){
        TransitionSystem ts = new SimpleTransitionSystem(automata[12]);

        assertFalse(ts.isImplementation());
    }

    @Test
    public void testG13(){
        TransitionSystem ts = new SimpleTransitionSystem(automata[13]);

        assertTrue(ts.isImplementation());
    }

    @Test
    public void testG14(){
        TransitionSystem ts = new SimpleTransitionSystem(automata[14]);

        assertFalse(ts.isImplementation());
    }

    @Test
    public void testG15(){
        TransitionSystem ts = new SimpleTransitionSystem(automata[15]);

        assertFalse(ts.isImplementation());
    }

    @Test
    public void testG16(){
        TransitionSystem ts = new SimpleTransitionSystem(automata[16]);

        assertFalse(ts.isImplementation());
    }

    @Test
    public void testG17(){
        TransitionSystem ts = new SimpleTransitionSystem(automata[17]);

        assertTrue(ts.isImplementation());
    }
}
