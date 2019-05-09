package features;

import logic.Composition;
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
    private static TransitionSystem G1, G2, G3, G4, G5, G6, G7, G8, G9, G10, G11, G12, G13, G14, G15, G16, G17;

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
    }

    @Test
    public void testG1(){
        assertTrue(G1.isDeterministic());
    }

    @Test
    public void testG2(){
        assertTrue(G2.isDeterministic());
    }

    @Test
    public void testG3(){
        assertTrue(G3.isDeterministic());
    }

    @Test
    public void G1G5IsDeterministic(){
        TransitionSystem ts = new Composition(new TransitionSystem[]{G1, G5});

        assertTrue(ts.isDeterministic());
    }

    @Test
    public void G1G9IsNotDeterministic(){
        TransitionSystem ts = new Composition(new TransitionSystem[]{G1, G9});

        assertFalse(ts.isDeterministic());
    }

    @Test
    public void testG4(){
        assertTrue(G4.isDeterministic());
    }

    @Test
    public void testG5(){
        assertTrue(G5.isDeterministic());
    }

    @Test
    public void testG6(){
        assertTrue(G6.isDeterministic());
    }

    @Test
    public void testG7(){
        assertTrue(G7.isDeterministic());
    }

    @Test
    public void testG8(){
        assertTrue(G8.isDeterministic());
    }

    @Test
    public void testG9(){
        assertFalse(G9.isDeterministic());
    }

    @Test
    public void testG10(){
        assertTrue(G10.isDeterministic());
    }

    @Test
    public void testG11(){
        assertTrue(G11.isDeterministic());
    }

    @Test
    public void testG12(){
        assertTrue(G12.isDeterministic());
    }

    @Test
    public void testG13(){
        assertTrue(G13.isDeterministic());
    }

    @Test
    public void testG14(){
        assertFalse(G14.isDeterministic());
    }

    @Test
    public void testG15(){
        assertTrue(G15.isDeterministic());
    }

    @Test
    public void testG16(){
        assertFalse(G16.isDeterministic());
    }

    @Test
    public void testG17(){
        assertTrue(G17.isDeterministic());
    }
}
