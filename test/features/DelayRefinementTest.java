package features;

import logic.Composition;
import logic.Refinement;
import logic.SimpleTransitionSystem;
import logic.TransitionSystem;
import models.Automaton;
import org.junit.BeforeClass;
import org.junit.Test;
import parser.XMLParser;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

public class DelayRefinementTest {

    private static Automaton[] automata;

    @BeforeClass
    public static void setUpBeforeClass() {
        automata = XMLParser.parse("./samples/xml/delayRefinement.xml");
    }

    @Test
    public void C1RefinesC1() {
        assertTrue(new Refinement(new SimpleTransitionSystem(automata[3]), new SimpleTransitionSystem(automata[3])).check());
    }

    @Test
    public void C1RefinesC2() {
        assertFalse(new Refinement(new SimpleTransitionSystem(automata[3]), new SimpleTransitionSystem(automata[4])).check());
    }

    @Test
    public void C2RefinesC1() {
        assertFalse(new Refinement(new SimpleTransitionSystem(automata[4]), new SimpleTransitionSystem(automata[3])).check());
    }

    @Test
    public void T1T2RefinesT3() {
        TransitionSystem comp = new Composition(
                new TransitionSystem[]{
                        new SimpleTransitionSystem(automata[0]),
                        new SimpleTransitionSystem(automata[1])});
        assertTrue(new Refinement(comp, new SimpleTransitionSystem(automata[2])).check());
    }

    @Test
    public void T0T1T2RefinesT3() {
        TransitionSystem comp = new Composition(
                new TransitionSystem[]{
                        new SimpleTransitionSystem(automata[11]),
                        new SimpleTransitionSystem(automata[0]),
                        new SimpleTransitionSystem(automata[1])});
        assertTrue(new Refinement(comp, new SimpleTransitionSystem(automata[2])).check());
    }

    @Test
    public void F1F2RefinesF3() {
        TransitionSystem comp = new Composition(
                new TransitionSystem[]{
                        new SimpleTransitionSystem(automata[7]),
                        new SimpleTransitionSystem(automata[8])});
        assertTrue(new Refinement(comp, new SimpleTransitionSystem(automata[9])).check());
    }

    @Test
    public void T4RefinesT3() {
        assertTrue(new Refinement(new SimpleTransitionSystem(automata[10]), new SimpleTransitionSystem(automata[2])).check());
    }

    @Test
    public void T6RefinesT5() {
        assertTrue(new Refinement(new SimpleTransitionSystem(automata[13]), new SimpleTransitionSystem(automata[12])).check());
    }

    @Test
    public void T7RefinesT8() {
        assertTrue(new Refinement(new SimpleTransitionSystem(automata[14]), new SimpleTransitionSystem(automata[15])).check());
    }

    @Test
    public void T9RefinesT8() {
        assertTrue(new Refinement(new SimpleTransitionSystem(automata[16]), new SimpleTransitionSystem(automata[15])).check());
    }

    @Test
    public void T10RefinesT11() {
        assertTrue(new Refinement(new SimpleTransitionSystem(automata[17]), new SimpleTransitionSystem(automata[18])).check());
    }

    @Test
    public void N1RefinesN2() {
        assertTrue(new Refinement(new SimpleTransitionSystem(automata[19]), new SimpleTransitionSystem(automata[20])).check());
    }

    @Test
    public void N3RefinesN4() {
        assertTrue(new Refinement(new SimpleTransitionSystem(automata[21]), new SimpleTransitionSystem(automata[22])).check());
    }

    @Test
    public void D2RefinesD1() {
        assertTrue(new Refinement(new SimpleTransitionSystem(automata[24]), new SimpleTransitionSystem(automata[23])).check());
    }

    @Test
    public void D1RefinesD2() {
        assertFalse(new Refinement(new SimpleTransitionSystem(automata[23]), new SimpleTransitionSystem(automata[24])).check());
    }

    @Test
    public void K1RefinesK2() {
        assertFalse(new Refinement(new SimpleTransitionSystem(automata[25]), new SimpleTransitionSystem(automata[26])).check());
    }

    @Test
    public void K3RefinesK4() {
        assertFalse(new Refinement(new SimpleTransitionSystem(automata[27]), new SimpleTransitionSystem(automata[28])).check());
    }
}
