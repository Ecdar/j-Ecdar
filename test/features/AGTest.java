package features;

import logic.Composition;
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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AGTest {
    private static Component A, G, Q, Imp;

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
        List<Component> machines = Parser.parse(base, components);

        A = machines.get(0);
        G = machines.get(1);
        Q = machines.get(2);
        Imp = machines.get(3);
    }

    @Test
    public void AGRefinesAImp() {
        Refinement ref = new Refinement(
                new Composition(new ArrayList<>(Arrays.asList(new SimpleTransitionSystem(A), new SimpleTransitionSystem(G)))),
                new Composition(new ArrayList<>(Arrays.asList(new SimpleTransitionSystem(A), new SimpleTransitionSystem(Imp)))));
        assertTrue(ref.check());
    }

    @Test
    public void AImpRefinesAG() {
        Refinement ref = new Refinement(
                new Composition(new ArrayList<>(Arrays.asList(new SimpleTransitionSystem(A), new SimpleTransitionSystem(Imp)))),
                new Composition(new ArrayList<>(Arrays.asList(new SimpleTransitionSystem(A), new SimpleTransitionSystem(G)))));
        assertFalse(ref.check());
    }

    @Test
    public void GRefinesImp() {
        Refinement ref = new Refinement(new SimpleTransitionSystem(G), new SimpleTransitionSystem(Imp));
        assertTrue(ref.check());
    }

    @Test
    public void ImpNotRefinesG() {
        Refinement ref = new Refinement(new SimpleTransitionSystem(Imp), new SimpleTransitionSystem(G));
        assertFalse(ref.check());
    }

    @Test
    public void GRefinesQ() {
        Refinement ref = new Refinement(new SimpleTransitionSystem(G), new SimpleTransitionSystem(Q));
        assertTrue(ref.check());
    }

    @Test
    public void QRefinesG() {
        Refinement ref = new Refinement(new SimpleTransitionSystem(Q), new SimpleTransitionSystem(G));
        assertTrue(ref.check());
    }

    @Test
    public void QRefinesImp() {
        Refinement ref = new Refinement(new SimpleTransitionSystem(Q), new SimpleTransitionSystem(Imp));
        assertTrue(ref.check());
    }

    @Test
    public void ImpNotRefinesQ() {
        Refinement ref = new Refinement(new SimpleTransitionSystem(Imp), new SimpleTransitionSystem(Q));
        assertFalse(ref.check());
    }
}
