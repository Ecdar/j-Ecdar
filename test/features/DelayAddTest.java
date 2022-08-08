package features;

import logic.Composition;
import logic.Refinement;
import logic.SimpleTransitionSystem;
import logic.TransitionSystem;
import models.Automaton;
import models.CDD;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import parser.JSONParser;

import static org.junit.Assert.assertFalse;

public class DelayAddTest {

    private static Automaton[] automata;

    @After
    public void afterEachTest(){
        CDD.done();
    }

    @BeforeClass
    public static void setUpBeforeClass() {
        String base = "./samples/json/DelayAdd/";
        String[] components = new String[]{"GlobalDeclarations.json",
                "Components/A1.json",
                "Components/A2.json",
                "Components/B.json",
                "Components/C1.json",
                "Components/C2.json",
                "Components/D1.json",
                "Components/D2.json"};
        automata = JSONParser.parse(base, components, true);
    }

    @Test
    public void A1A2NotRefinesB() {
        TransitionSystem comp = new Composition(
                        new SimpleTransitionSystem(automata[0]),
                        new SimpleTransitionSystem(automata[1])
        );
        assertFalse(new Refinement(comp, new SimpleTransitionSystem((automata[2]))).check());
    }

    @Test
    public void C1NotRefinesC2() {
        assertFalse(new Refinement(new SimpleTransitionSystem((automata[3])), new SimpleTransitionSystem((automata[4]))).check());
    }

    @Test
    public void D1NotRefinesD2() {
        // should fail because outputs are different

        assertFalse(new Refinement(new SimpleTransitionSystem((automata[5])), new SimpleTransitionSystem((automata[6]))).check());
    }
}
