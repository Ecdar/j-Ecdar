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
    private static TransitionSystem t1, t2, t3, t4, t5;

    @BeforeClass
    public static void setUpBeforeClass() {
        String base = "./samples/json/Conjunction/";
        String[] components = new String[]{"GlobalDeclarations.json",
                "Components/Test1.json",
                "Components/Test2.json",
                "Components/Test3.json",
                "Components/Test4.json",
                "Components/Test5.json"};
        Automaton[] machines = JSONParser.parse(base, components, true);

        t1 = new SimpleTransitionSystem(machines[0]);
        t2 = new SimpleTransitionSystem(machines[1]);
        t3 = new SimpleTransitionSystem(machines[2]);
        t4 = new SimpleTransitionSystem(machines[3]);
        t5 = new SimpleTransitionSystem(machines[4]);
    }

    @Test
    public void T1RefinesTt1() {
        assertTrue(new Refinement(t1, t1).check());
    }

    @Test
    public void T2RefinesT2() {
        assertTrue(new Refinement(t2, t2).check());
    }

    @Test
    public void T3RefinesT3() {
        assertTrue(new Refinement(t3, t3).check());
    }

    @Test
    public void T1ConjT2RefinesT3() {
        assertTrue(new Refinement(new Conjunction(new TransitionSystem[]{t1, t2}), t3).check());
    }

    @Test
    public void T2ConjT3RefinesT1() {
        assertTrue(new Refinement(new Conjunction(new TransitionSystem[]{t2, t3}), t1).check());
    }

    @Test
    public void T1ConjT3RefinesT2() {
        assertTrue(new Refinement(new Conjunction(new TransitionSystem[]{t1, t3}), t2).check());
    }

    @Test
    public void T1ConjT2ConjT4RefinesT5() {
        assertTrue(new Refinement(new Conjunction(new TransitionSystem[]{t1, t2, t4}), t5).check());
    }

    @Test
    public void T3ConjT4RefinesT5() {
        assertTrue(new Refinement(new Conjunction(new TransitionSystem[]{t3, t4}), t5).check());
    }

    @Test
    public void test1NestedConjRefinesT5() {
        TransitionSystem ts1 = new Conjunction(new TransitionSystem[]{t1, t2});
        TransitionSystem ts2 = new Conjunction(new TransitionSystem[]{ts1, t4});

        assertTrue(new Refinement(ts2, t5).check());
    }
}
