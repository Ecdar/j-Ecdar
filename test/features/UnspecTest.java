package features;

import logic.Composition;
import logic.Refinement;
import logic.SimpleTransitionSystem;
import logic.TransitionSystem;
import models.Automaton;
import org.junit.BeforeClass;
import org.junit.Test;
import parser.JSONParser;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class UnspecTest {
    private static TransitionSystem a, aCopy, aa, aaCopy, b, bCopy;

    @BeforeClass
    public static void setUpBeforeClass() {
        String base = "./samples/json/Unspec/";
        String[] components = new String[]{"GlobalDeclarations.json",
                "Components/A.json",
                "Components/AA.json",
                "Components/B.json"};
        Automaton[] machines = JSONParser.parse(base, components, true);

        a = new SimpleTransitionSystem(machines[0]);
        aCopy = new SimpleTransitionSystem(new Automaton(machines[0]));
        aa = new SimpleTransitionSystem(machines[1]);
        aaCopy = new SimpleTransitionSystem(new Automaton(machines[1]));
        b = new SimpleTransitionSystem(machines[2]);
        bCopy = new SimpleTransitionSystem(new Automaton(machines[2]));
    }

    @Test
    public void testARefinesSelf() {
        assertTrue(new Refinement(a, aCopy).check());
    }

    @Test
    public void testAaRefinesSelf() {
        assertTrue(new Refinement(aa, aaCopy).check());
    }

    @Test
    public void testBRefinesSelf() {
        assertTrue(new Refinement(b, bCopy).check());
    }

    @Test
    public void compNotRefinesB() {
        // should fail because right side has more inputs
        assertFalse(new Refinement(new Composition(new TransitionSystem[]{a, aa}), b).check());
    }
}
