package features;

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

public class BigRefinementTest {

    private static TransitionSystem comp1, ref1;

    @BeforeClass
    public static void setUpBeforeClass() {
        String base = "./samples/BigRefinement/";
        List<String> components = new ArrayList<>(Arrays.asList("GlobalDeclarations.json",
                "Components/Comp1.json",
                "Components/Ref1.json"));
        List<Automaton> machines = Parser.parse(base, components);

        comp1 = new SimpleTransitionSystem(machines.get(0));
        ref1 = new SimpleTransitionSystem(machines.get(1));
    }

    @Test
    public void testRef1RefinesComp1() {
        assertTrue(new Refinement(ref1, comp1).check());
    }

    @Test
    public void testComp1NotRefinesRef1() {
        assertFalse(new Refinement(comp1, ref1).check());
    }

    @Test
    public void testRef1RefinesRef1() {
        assertTrue(new Refinement(ref1, ref1).check());
    }

    @Test
    public void testComp1RefinesComp1() {
        assertTrue(new Refinement(comp1, comp1).check());
    }
}