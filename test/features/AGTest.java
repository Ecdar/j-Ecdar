package features;

import logic.Composition;
import logic.Refinement;
import logic.SimpleTransitionSystem;
import logic.TransitionSystem;
import models.Automaton;
import models.CDD;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import parser.JSONParser;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AGTest {
    private static TransitionSystem a, aCopy, g, gCopy, q, qCopy, imp, impCopy, aa, aaCopy;

    @After
    public void afterEachTest(){
        CDD.done();
    }

    @BeforeClass
    public static void setUpBeforeClass() {
        String base = "./samples/json/AG/";
        String[] components = new String[]{"GlobalDeclarations.json",
                "Components/A.json",
                "Components/G.json",
                "Components/Q.json",
                "Components/Imp.json",
                "Components/AA.json"
        };
        Automaton[] machines = JSONParser.parse(base, components, true);

        a = new SimpleTransitionSystem(machines[0]);
        aCopy = new SimpleTransitionSystem(new Automaton(machines[0]));
        g = new SimpleTransitionSystem(machines[1]);
        gCopy = new SimpleTransitionSystem(new Automaton(machines[1]));
        q = new SimpleTransitionSystem(machines[2]);
        qCopy = new SimpleTransitionSystem(new Automaton(machines[2]));
        imp = new SimpleTransitionSystem(machines[3]);
        impCopy = new SimpleTransitionSystem(new Automaton(machines[3]));
        aa = new SimpleTransitionSystem(machines[4]);
        aaCopy = new SimpleTransitionSystem(new Automaton(machines[4]));
    }

    @Test
    public void ARefinesSelf() {
        assertTrue(new Refinement(a, aCopy).check());
    }

    @Test
    public void GRefinesSelf() {
        assertTrue(new Refinement(g, gCopy).check());
    }

    @Test
    public void QRefinesSelf() {
        assertTrue(new Refinement(q, qCopy).check());
    }

    @Test
    public void ImpRefinesSelf() {
        assertTrue(new Refinement(imp, impCopy).check());
    }

    @Test
    public void AaRefinesSelf() {
        assertTrue(new Refinement(aa, aaCopy).check());
    }

    @Test
    public void AGNotRefinesAImp() {

        // should fail because left side has more inputs
        assertFalse(new Refinement(
                new Composition(new TransitionSystem[]{a, g}),
                new Composition(new TransitionSystem[]{a, imp})).check()
        );
    }

    @Test
    public void AImpNotRefinesAG() {
        // should fail because the right side has more inputs
        assertFalse(new Refinement(
                new Composition(new TransitionSystem[]{a, imp}),
                new Composition(new TransitionSystem[]{a, g})).check()
        );
    }

    @Test
    public void GNotRefinesImp() {
        // should fail because right side has more outputs
        assertFalse(new Refinement(g, imp).check());
    }

    @Test
    public void ImpRefinesG() {
        assertTrue(new Refinement(imp, g).check());
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
    public void QNotRefinesImp() {
        // should fail because right side has more outputs
        assertFalse(new Refinement(q, imp).check());
    }

    @Test
    public void ImpRefinesQ() {
        assertTrue(new Refinement(imp, q).check());
    }

    @Test
    public void ANotRefinesAA() {
        // should fail because right side has more inputs
        assertFalse(new Refinement(a, aa).check());
    }
}
