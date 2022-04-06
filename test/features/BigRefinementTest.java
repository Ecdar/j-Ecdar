package features;

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
import static org.junit.Assert.assertTrue;

public class BigRefinementTest {

    private static TransitionSystem comp1, comp1Copy, ref1, ref1Copy;

    @After
    public void afterEachTest(){
        CDD.done();
    }

    @BeforeClass
    public static void setUpBeforeClass() {
        String base = "./samples/json/BigRefinement/";
        String[] components = new String[]{"GlobalDeclarations.json",
                "Components/Comp1.json",
                "Components/Ref1.json"};
        Automaton[] machines = JSONParser.parse(base, components, true);

        comp1 = new SimpleTransitionSystem(machines[0]);
        comp1Copy = new SimpleTransitionSystem(new Automaton(machines[0]));
        ref1 = new SimpleTransitionSystem(machines[1]);
        ref1Copy = new SimpleTransitionSystem(new Automaton(machines[1]));

    }

    @Test
    public void testRef1NotRefinesComp1() {
        // should fail because left side has more inputs
        assertFalse(new Refinement(ref1, comp1).check());
    }

    @Test
    public void testComp1NotRefinesRef1() {
        assertFalse(new Refinement(comp1, ref1).check());
    }

    @Test
    public void testRef1RefinesSelf() {
        assertTrue(new Refinement(ref1, ref1Copy).check());
    }

    @Test
    public void testComp1RefinesSelf() {
        assertTrue(new Refinement(comp1, comp1Copy).check());
    }
}