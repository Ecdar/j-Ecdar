package features;

import exceptions.CddAlreadyRunningException;
import exceptions.CddNotRunningException;
import logic.Composition;
import logic.Refinement;
import logic.SimpleTransitionSystem;
import logic.TransitionSystem;
import models.Automaton;
import models.CDD;
import models.Clock;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import parser.JSONParser;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class UnspecTest {
    private static TransitionSystem a, aCopy, aa, aaCopy, b, bCopy;

    @After
    public void afterEachTest(){
        CDD.done();
    }

    @BeforeClass
    public static void setUpBeforeClass() throws CddAlreadyRunningException, CddNotRunningException {
        String base = "./samples/json/Unspec/";
        String[] components = new String[]{"GlobalDeclarations.json",
                "Components/A.json",
                "Components/AA.json",
                "Components/B.json"};
        Automaton[] machines = JSONParser.parse(base, components, true);

        a = new SimpleTransitionSystem((machines[0]));
        aCopy = new SimpleTransitionSystem(new Automaton((machines[0])));
        aa = new SimpleTransitionSystem((machines[1]));
        aaCopy = new SimpleTransitionSystem(new Automaton((machines[1])));
        b = new SimpleTransitionSystem((machines[2]));
        bCopy = new SimpleTransitionSystem(new Automaton((machines[2])));
    }

    @Test
    public void testARefinesSelf() {
        assertTrue(new Refinement(new SimpleTransitionSystem((a.getAutomaton())), aCopy).check());
    }

    @Test
    public void testAaRefinesSelf() {
        assertTrue(new Refinement(new SimpleTransitionSystem((aa.getAutomaton())), aaCopy).check());
    }

    @Test
    public void testBRefinesSelf() {
        assertTrue(new Refinement(new SimpleTransitionSystem((b.getAutomaton())), bCopy).check());
    }

    @Test
    public void compRefinesB() {
        // in the old test case, the refinement should fail because right side has more inputs, now it should pass
        assertTrue(new Refinement(new Composition(new TransitionSystem[]{a, aa}), b).check());
    }
}
