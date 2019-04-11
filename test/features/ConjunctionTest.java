package features;

import logic.Conjunction;
import logic.Refinement;
import logic.SimpleTransitionSystem;
import logic.TransitionSystem;
import models.Automaton;
import org.junit.BeforeClass;
import org.junit.Test;
import parser.JSONParser;

import static org.junit.Assert.assertTrue;

public class ConjunctionTest {
    private static TransitionSystem test1, test2, test3;

    @BeforeClass
    public static void setUpBeforeClass() {
        String base = "./samples/json/Conjunction/";
        String[] components = new String[]{"GlobalDeclarations.json",
                "Components/Test1.json",
                "Components/Test2.json",
                "Components/Test3.json"};
        Automaton[] machines = JSONParser.parse(base, components, true);

        test1 = new SimpleTransitionSystem(machines[0]);
        test2 = new SimpleTransitionSystem(machines[1]);
        test3 = new SimpleTransitionSystem(machines[2]);
    }

    @Test
    public void Test1RefinesTest1() {
        assertTrue(new Refinement(test1, test1).check());
    }

    @Test
    public void Test2RefinesTest2() {
        assertTrue(new Refinement(test2, test2).check());
    }

    @Test
    public void Test3RefinesTest3() {
        assertTrue(new Refinement(test3, test3).check());
    }

    @Test
    public void testTest1ConjTest2RefinesTest3() {
        assertTrue(new Refinement(new Conjunction(new TransitionSystem[]{test1, test2}), test3).check());
    }

    @Test
    public void testTest2ConjTest3RefinesTest1() {
        assertTrue(new Refinement(new Conjunction(new TransitionSystem[]{test2, test3}), test1).check());
    }

    @Test
    public void testTest1ConjTest3RefinesTest2() {
        assertTrue(new Refinement(new Conjunction(new TransitionSystem[]{test1, test3}), test2).check());
    }
}
