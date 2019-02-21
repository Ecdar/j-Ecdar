package features;

import logic.Composition;
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

public class AGTest {
    private static TransitionSystem a, g, q, imp;

    @BeforeClass
    public static void setUpBeforeClass()  {
        String base = "./samples/AG/";
        List<String> components = new ArrayList<>(Arrays.asList("GlobalDeclarations.json",
                "Components/A.json",
                "Components/G.json",
                "Components/Q.json",
                "Components/Imp.json"));
        List<Automaton> machines = Parser.parse(base, components);

        a = new SimpleTransitionSystem(machines.get(0));
        g = new SimpleTransitionSystem(machines.get(1));
        q = new SimpleTransitionSystem(machines.get(2));
        imp = new SimpleTransitionSystem(machines.get(3));
    }

    @Test
    public void ARefinesA() {
        assertTrue(new Refinement(a, a).check());
    }

    @Test
    public void GRefinesG() {
        assertTrue(new Refinement(g, g).check());
    }

    @Test
    public void QRefinesQ() {
        assertTrue(new Refinement(q, q).check());
    }

    @Test
    public void ImpRefinesImp() {
        assertTrue(new Refinement(imp, imp).check());
    }

    @Test
    public void AGRefinesAImp() {
        assertTrue(new Refinement(
                new Composition(new ArrayList<>(Arrays.asList(a, g))),
                new Composition(new ArrayList<>(Arrays.asList(a, imp)))).check()
        );
    }

    @Test
    public void AImpRefinesAG() {
        assertFalse(new Refinement(
                new Composition(new ArrayList<>(Arrays.asList(a, imp))),
                new Composition(new ArrayList<>(Arrays.asList(a, g)))).check()
        );
    }

    @Test
    public void GRefinesImp() {
        assertTrue(new Refinement(g, imp).check());
    }

    @Test
    public void ImpNotRefinesG() {
        assertFalse(new Refinement(imp, g).check());
    }

    @Test
    public void GRefinesQ() {
        assertTrue(new Refinement(g, q).check());
    }

    @Test
    public void QRefinesG() {
        assertTrue(new Refinement(q, g).check());
    }

    @Test
    public void QRefinesImp() {
        assertTrue(new Refinement(q, imp).check());
    }

    @Test
    public void ImpNotRefinesQ() {
        assertFalse(new Refinement(imp, q).check());
    }
}
