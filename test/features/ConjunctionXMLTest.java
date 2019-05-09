package features;

import logic.*;
import models.Automaton;
import org.junit.BeforeClass;
import org.junit.Test;
import parser.XMLParser;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ConjunctionXMLTest {
    private static Automaton[] automata;

    @BeforeClass
    public static void setUpBeforeClass() {
        automata = XMLParser.parse("./samples/xml/conjun.xml", true);
    }

    @Test
    public void P0ConjP1RefP2() {
        TransitionSystem ts = new Conjunction(new TransitionSystem[]{new SimpleTransitionSystem(automata[1]), new SimpleTransitionSystem(automata[0])});
        assertFalse(new Refinement(ts, new SimpleTransitionSystem(automata[2])).check());
    }
    @Test
    public void P3ConjP4CompP5RefP6() {
        TransitionSystem ts = new Conjunction(new TransitionSystem[]{new SimpleTransitionSystem(automata[3]), new SimpleTransitionSystem(automata[4])});
        TransitionSystem ts1 = new Composition(new TransitionSystem[]{ts, new SimpleTransitionSystem(automata[5])});
        assertFalse(new Refinement(ts1, new SimpleTransitionSystem(automata[6])).check());
    }
    @Test
    public void P7ConjP8ConjP9RefP10() {
        TransitionSystem ts = new Conjunction(new TransitionSystem[]{new SimpleTransitionSystem(automata[7]), new SimpleTransitionSystem(automata[8]),new SimpleTransitionSystem(automata[9])});
        assertFalse(new Refinement(ts, new SimpleTransitionSystem(automata[10])).check());
    }

    @Test
    public void P11ConjP12RefP13() {
        TransitionSystem ts = new Conjunction(new TransitionSystem[]{new SimpleTransitionSystem(automata[11]), new SimpleTransitionSystem(automata[12])});
        assertTrue(new Refinement(ts, new SimpleTransitionSystem(automata[13])).check());
    }
}
