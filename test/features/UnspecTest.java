package features;

import Exceptions.CddAlreadyRunningException;
import Exceptions.CddNotRunningException;
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
        CDD.init(100,100,100);
        List<Clock> clocks = new ArrayList<>();
        clocks.addAll(machines[0].getClocks());
        clocks.addAll(machines[1].getClocks());
        clocks.addAll(machines[2].getClocks());
        CDD.addClocks(clocks);

        a = new SimpleTransitionSystem(CDD.makeInputEnabled(machines[0]));
        aCopy = new SimpleTransitionSystem(new Automaton(CDD.makeInputEnabled(machines[0])));
        aa = new SimpleTransitionSystem(CDD.makeInputEnabled(machines[1]));
        aaCopy = new SimpleTransitionSystem(new Automaton(CDD.makeInputEnabled(machines[1])));
        b = new SimpleTransitionSystem(CDD.makeInputEnabled(machines[2]));
        bCopy = new SimpleTransitionSystem(new Automaton(CDD.makeInputEnabled(machines[2])));
    }

    @Test
    public void testARefinesSelf() {
        assertTrue(new Refinement(a, aCopy).check());
    }

    @Test
    public void testAaRefinesSelf() {
        assertTrue(new Refinement(aa, aaCopy).check());
    }

    @Test
    public void testBRefinesSelf() {
        assertTrue(new Refinement(b, bCopy).check());
    }

    @Test
    public void compNotRefinesB() {
        // should fail because right side has more inputs
        assertFalse(new Refinement(new Composition(new TransitionSystem[]{a, aa}), b).check());
    }
}
