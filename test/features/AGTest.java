package features;

import logic.Composition;
import logic.Refinement;
import logic.SimpleTransitionSystem;
import models.Automaton;
import org.junit.BeforeClass;
import org.junit.Test;
import parser.Parser;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static features.Helpers.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AGTest {
    private static Automaton a, g, q, imp;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        String fileName = "src/" + System.mapLibraryName("DBM");
        File lib = new File(fileName);
        System.load(lib.getAbsolutePath());

        String base = "./samples/AG/";
        List<String> components = new ArrayList<>(Arrays.asList("GlobalDeclarations.json",
                "Components/A.json",
                "Components/G.json",
                "Components/Q.json",
                "Components/Imp.json"));
        List<Automaton> machines = Parser.parse(base, components);

        a = machines.get(0);
        g = machines.get(1);
        q = machines.get(2);
        imp = machines.get(3);
    }

    @Test
    public void ARefinesA() {
        Refinement ref = selfRefinesSelf(a);
        assertTrue(ref.check());
    }

    @Test
    public void GRefinesG() {
        Refinement ref = selfRefinesSelf(g);
        assertTrue(ref.check());
    }

    @Test
    public void QRefinesQ() {
        Refinement ref = selfRefinesSelf(q);
        assertTrue(ref.check());
    }

    @Test
    public void ImpRefinesImp() {
        Refinement ref = selfRefinesSelf(imp);
        assertTrue(ref.check());
    }

    @Test
    public void AGRefinesAImp() {
        Refinement ref = new Refinement(
                new Composition(new ArrayList<>(Arrays.asList(new SimpleTransitionSystem(a), new SimpleTransitionSystem(g)))),
                new Composition(new ArrayList<>(Arrays.asList(new SimpleTransitionSystem(a), new SimpleTransitionSystem(imp)))));
        assertTrue(ref.check());
    }

    @Test
    public void AImpRefinesAG() {
        Refinement ref = new Refinement(
                new Composition(new ArrayList<>(Arrays.asList(new SimpleTransitionSystem(a), new SimpleTransitionSystem(imp)))),
                new Composition(new ArrayList<>(Arrays.asList(new SimpleTransitionSystem(a), new SimpleTransitionSystem(g)))));
        assertFalse(ref.check());
    }

    @Test
    public void GRefinesImp() {
        Refinement ref = new Refinement(new SimpleTransitionSystem(g), new SimpleTransitionSystem(imp));
        assertTrue(ref.check());
    }

    @Test
    public void ImpNotRefinesG() {
        Refinement ref = new Refinement(new SimpleTransitionSystem(imp), new SimpleTransitionSystem(g));
        assertFalse(ref.check());
    }

    @Test
    public void GRefinesQ() {
        Refinement ref = new Refinement(new SimpleTransitionSystem(g), new SimpleTransitionSystem(q));
        assertTrue(ref.check());
    }

    @Test
    public void QRefinesG() {
        Refinement ref = new Refinement(new SimpleTransitionSystem(q), new SimpleTransitionSystem(g));
        assertTrue(ref.check());
    }

    @Test
    public void QRefinesImp() {
        Refinement ref = new Refinement(new SimpleTransitionSystem(q), new SimpleTransitionSystem(imp));
        assertTrue(ref.check());
    }

    @Test
    public void ImpNotRefinesQ() {
        Refinement ref = new Refinement(new SimpleTransitionSystem(imp), new SimpleTransitionSystem(q));
        assertFalse(ref.check());
    }
}
