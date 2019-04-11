package features;

import logic.Composition;
import logic.Refinement;
import logic.SimpleTransitionSystem;
import logic.TransitionSystem;
import models.Automaton;
import org.junit.BeforeClass;
import org.junit.Test;
import parser.JSONParser;

import static org.junit.Assert.assertTrue;

public class UnspecTest {
    private static TransitionSystem a, aa, b;

    @BeforeClass
    public static void setUpBeforeClass() {
        String base = "./samples/json/Unspec/";
        String[] components = new String[]{"GlobalDeclarations.json",
                "Components/A.json",
                "Components/AA.json",
                "Components/B.json"};
        Automaton[] machines = JSONParser.parse(base, components, true);

        a = new SimpleTransitionSystem(machines[0]);
        aa = new SimpleTransitionSystem(machines[1]);
        b = new SimpleTransitionSystem(machines[2]);
    }

    @Test
    public void testARefinesA() {
        assertTrue(new Refinement(a, a).check());
    }

    @Test
    public void testAaRefinesAa() {
        assertTrue(new Refinement(aa, aa).check());
    }

    @Test
    public void testBRefinesB() {
        assertTrue(new Refinement(b, b).check());
    }

    @Test
    public void compRefinesB() {
        assertTrue(new Refinement(new Composition(new TransitionSystem[]{a, aa}), b).check());
    }
}
