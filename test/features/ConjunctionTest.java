package features;

import logic.Conjunction;
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

import static org.junit.Assert.assertTrue;

public class ConjunctionTest {
    private static TransitionSystem test1, test2, test3;

    @BeforeClass
    public static void setUpBeforeClass() {
        String base = "./samples/Conjunction/";
        List<String> components = new ArrayList<>(Arrays.asList("GlobalDeclarations.json",
                "Components/Test1.json",
                "Components/Test2.json",
                "Components/Test3.json"));
        List<Automaton> machines = Parser.parse(base, components);

        test1 = new SimpleTransitionSystem(machines.get(0));
        test2 = new SimpleTransitionSystem(machines.get(1));
        test3 = new SimpleTransitionSystem(machines.get(2));
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
        assertTrue(new Refinement(new Conjunction(new ArrayList<>(Arrays.asList(test1, test2))), test3).check());
    }

    @Test
    public void testTest2ConjTest3RefinesTest1() {
        assertTrue(new Refinement(new Conjunction(new ArrayList<>(Arrays.asList(test2, test3))), test1).check());
    }

    @Test
    public void testTest1ConjTest3RefinesTest2() {
        assertTrue(new Refinement(new Conjunction(new ArrayList<>(Arrays.asList(test1, test3))), test2).check());
    }
}
