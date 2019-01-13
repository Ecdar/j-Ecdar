package features;

import logic.Conjunction;
import logic.Refinement;
import logic.SimpleTransitionSystem;
import models.Component;
import org.junit.BeforeClass;
import org.junit.Test;
import parser.Parser;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static features.Helpers.selfRefinesSelf;
import static org.junit.Assert.assertTrue;

public class ConjunctionTest {
    private static Component test1, test2, test3;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        String fileName = "src/" + System.mapLibraryName("DBM");
        File lib = new File(fileName);
        System.load(lib.getAbsolutePath());

        String base = "./samples/Conjunction/";
        List<String> components = new ArrayList<>(Arrays.asList("GlobalDeclarations.json",
                "Components/Test1.json",
                "Components/Test2.json",
                "Components/Test3.json"));
        List<Component> machines = Parser.parse(base, components);

        test1 = machines.get(0);
        test2 = machines.get(1);
        test3 = machines.get(2);
    }

    @Test
    public void Test1RefinesTest1() {
        Refinement ref = selfRefinesSelf(test1);
        assertTrue(ref.check());
    }

    @Test
    public void Test2RefinesTest2() {
        Refinement ref = selfRefinesSelf(test2);
        assertTrue(ref.check());
    }

    @Test
    public void Test3RefinesTest3() {
        Refinement ref = selfRefinesSelf(test3);
        assertTrue(ref.check());
    }

    @Test
    public void testTest1ConjTest2RefinesTest3() {
        Refinement ref = new Refinement(
                new Conjunction(new ArrayList<>(Arrays.asList(new SimpleTransitionSystem(test1), new SimpleTransitionSystem(test2)))),
                new SimpleTransitionSystem(test3));

        assertTrue(ref.check());
    }

    @Test
    public void testTest2ConjTest3RefinesTest1() {
        Refinement ref = new Refinement(
                new Conjunction(new ArrayList<>(Arrays.asList(new SimpleTransitionSystem(test2), new SimpleTransitionSystem(test3)))),
                new SimpleTransitionSystem(test1));

        assertTrue(ref.check());
    }

    @Test
    public void testTest1ConjTest3RefinesTest2() {
        Refinement ref = new Refinement(
                new Conjunction(new ArrayList<>(Arrays.asList(new SimpleTransitionSystem(test1), new SimpleTransitionSystem(test3)))),
                new SimpleTransitionSystem(test2));

        assertTrue(ref.check());
    }
}
