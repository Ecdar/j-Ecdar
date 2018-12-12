package features;

import logic.Refinement;
import models.Component;
import org.junit.BeforeClass;
import org.junit.Test;
import parser.Parser;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class BigRefinementTest {

    private static Component comp1, ref1;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        String fileName = "src/" + System.mapLibraryName("DBM");
        File lib = new File(fileName);
        System.load(lib.getAbsolutePath());

        String base = "./samples/BigRefinement/";
        List<String> components = new ArrayList<>(Arrays.asList("GlobalDeclarations.json",
                "Components/Comp1.json",
                "Components/Ref1.json"));
        List<Component> machines = Parser.parse(base, components);

        comp1 = machines.get(0);
        ref1 = machines.get(1);
    }

    @Test
    public void testRef1RefinesComp() {
        Refinement ref = simpleRefinesSimple(ref1, comp1);
        assertTrue(ref.check());
    }

    @Test
    public void testComp1NotRefinesRef1() {
        Refinement ref = simpleRefinesSimple(comp1, ref1);
        assertFalse(ref.check());
    }

    @Test
    public void testRef1RefinesRef1() {
        Refinement ref = selfRefinesSelf(ref1);
        assertTrue(ref.check());
    }

    @Test
    public void testComp1RefinesComp1() {
        Refinement ref = selfRefinesSelf(comp1);
        assertTrue(ref.check());
    }

    // helper functions
    private Refinement selfRefinesSelf(Component component) {
        return simpleRefinesSimple(component, component);
    }

    private Refinement simpleRefinesSimple(Component component1, Component component2) {
        return new Refinement(new ArrayList<>(Arrays.asList(component1)),
                new ArrayList<>(Arrays.asList(component2)));
    }
    //
}