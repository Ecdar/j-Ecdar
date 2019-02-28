package features;

import logic.Composition;
import logic.Refinement;
import logic.SimpleTransitionSystem;
import logic.TransitionSystem;
import models.Automaton;
import org.junit.BeforeClass;
import org.junit.Test;
import parser.Parser;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AGTest {
    private static TransitionSystem a, g, q, imp;

    @BeforeClass
    public static void setUpBeforeClass() {
        String base = "./samples/AG/";
        String[] components = new String[]{"GlobalDeclarations.json",
                "Components/A.json",
                "Components/G.json",
                "Components/Q.json",
                "Components/Imp.json"};
        Automaton[] machines = Parser.parse(base, components);

        a = new SimpleTransitionSystem(machines[0]);
        g = new SimpleTransitionSystem(machines[1]);
        q = new SimpleTransitionSystem(machines[2]);
        imp = new SimpleTransitionSystem(machines[3]);
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
                new Composition(new TransitionSystem[]{a, g}),
                new Composition(new TransitionSystem[]{a, imp})).check()
        );
    }

    @Test
    public void AImpRefinesAG() {
        assertFalse(new Refinement(
                new Composition(new TransitionSystem[]{a, imp}),
                new Composition(new TransitionSystem[]{a, g})).check()
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

//    @Test
//    public void GRefinesAGQuotientA() {
//        assertTrue(new Refinement(g, new Quotient(new Composition(new TransitionSystem[]{a, g}), a)).check());
//    }
//
//    @Test
//    public void AGQuotientANotRefinesG() {
//        assertFalse(new Refinement(new Quotient(new Composition(new TransitionSystem[]{a, g}), a), g).check());
//    }
//
//    @Test
//    public void AGQuotientANotRefinesImp() {
//        assertFalse(new Refinement(new Quotient(new Composition(new TransitionSystem[]{a, g}), a), imp).check());
//    }
}
