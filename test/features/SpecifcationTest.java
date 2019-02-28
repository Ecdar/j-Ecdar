package features;

import logic.*;
import models.Automaton;
import org.junit.BeforeClass;
import org.junit.Test;
import parser.Parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static features.Helpers.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SpecifcationTest {
    private static Automaton comp, comp1;

    @BeforeClass
    public static void setUpBeforeClass() {
        String base = "./samples/specTest1/";
        String[] components = new String[]{"GlobalDeclarations.json",
                "Components/comp.json",
                "Components/comp1.json"};
        Automaton[] machines = Parser.parse(base, components);

        comp = machines[0];
        comp1 = machines[1];

    }
    @Test
    public void specificationComp() {
        Specification spec = new Specification(new SimpleTransitionSystem(comp));
        assertTrue(spec.isSpecification());
    }

    @Test
    public void specNotDeterministic() {
        Specification spec = new Specification(new SimpleTransitionSystem(comp1));
        assertFalse(spec.isSpecification());
    }

}
