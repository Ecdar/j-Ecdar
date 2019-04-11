package features;

import logic.Refinement;
import logic.SimpleTransitionSystem;
import logic.TransitionSystem;
import models.Automaton;
import org.junit.BeforeClass;
import org.junit.Test;
import parser.JSONParser;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class BigRefinementTest {

    private static TransitionSystem comp1, ref1;

    @BeforeClass
    public static void setUpBeforeClass() {
        String base = "./samples/json/BigRefinement/";
        String[] components = new String[]{"GlobalDeclarations.json",
                "Components/Comp1.json",
                "Components/Ref1.json"};
        Automaton[] machines = JSONParser.parse(base, components, true);

        comp1 = new SimpleTransitionSystem(machines[0]);
        ref1 = new SimpleTransitionSystem(machines[1]);
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