package features;

import logic.Composition;
import logic.Refinement;
import logic.SimpleTransitionSystem;
import models.Automaton;
import org.junit.BeforeClass;
import org.junit.Test;
import parser.Parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static features.Helpers.selfRefinesSelf;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class UnspecTest {
    private static Automaton a, aa, b;

    @BeforeClass
    public static void setUpBeforeClass() {
        String base = "./samples/Unspec/";
        List<String> components = new ArrayList<>(Arrays.asList("GlobalDeclarations.json",
                "Components/A.json",
                "Components/AA.json",
                "Components/B.json"));
        List<Automaton> machines = Parser.parse(base, components);

        a = machines.get(0);
        aa = machines.get(1);
        b = machines.get(2);
    }

    @Test
    public void testARefinesA() {
        Refinement ref = selfRefinesSelf(a);
        assertTrue(ref.check());
    }

    @Test
    public void testAaRefinesAa() {
        Refinement ref = selfRefinesSelf(aa);
        assertTrue(ref.check());
    }

    @Test
    public void testBRefinesB() {
        Refinement ref = selfRefinesSelf(b);
        assertTrue(ref.check());
    }

    @Test
    public void compNotRefinesB() {
        Refinement ref = new Refinement(
                new Composition(new ArrayList<>(Arrays.asList(new SimpleTransitionSystem(a), new SimpleTransitionSystem(aa)))),
                new SimpleTransitionSystem(b));
        assertFalse(ref.check());
    }
}
