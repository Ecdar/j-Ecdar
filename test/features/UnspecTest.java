package features;

import logic.Composition;
import logic.Refinement;
import logic.SimpleTransitionSystem;
import logic.TransitionSystem;
import models.Automaton;
import org.junit.BeforeClass;
import org.junit.Test;
import parser.Parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class UnspecTest {
    private static TransitionSystem a, aa, b;

    @BeforeClass
    public static void setUpBeforeClass() {
        String base = "./samples/Unspec/";
        List<String> components = new ArrayList<>(Arrays.asList("GlobalDeclarations.json",
                "Components/A.json",
                "Components/AA.json",
                "Components/B.json"));
        List<Automaton> machines = Parser.parse(base, components);

        a = new SimpleTransitionSystem(machines.get(0));
        aa = new SimpleTransitionSystem(machines.get(1));
        b = new SimpleTransitionSystem(machines.get(2));
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
    public void compNotRefinesB() {
        assertFalse(new Refinement(new Composition(new ArrayList<>(Arrays.asList(a, aa))), b).check());
    }
}
