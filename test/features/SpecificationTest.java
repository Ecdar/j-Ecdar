package features;

import logic.SimpleTransitionSystem;
import logic.Specification;
import models.Automaton;
import org.junit.BeforeClass;
import org.junit.Test;
import parser.JSONParser;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SpecificationTest {
    private static Automaton comp, comp1;

    @BeforeClass
    public static void setUpBeforeClass() {
        String base = "./samples/json/specTest1/";
        String[] components = new String[]{"GlobalDeclarations.json",
                "Components/comp.json",
                "Components/comp1.json"};
        Automaton[] machines = JSONParser.parse(base, components);

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
