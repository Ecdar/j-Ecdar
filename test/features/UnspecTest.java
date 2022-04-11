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
   ;

        a = new SimpleTransitionSystem(CDD.makeInputEnabled(machines[0]));
        aCopy = new SimpleTransitionSystem(new Automaton(CDD.makeInputEnabled(machines[0])));
        aa = new SimpleTransitionSystem(CDD.makeInputEnabled(machines[1]));
        aaCopy = new SimpleTransitionSystem(new Automaton(CDD.makeInputEnabled(machines[1])));
        b = new SimpleTransitionSystem(CDD.makeInputEnabled(machines[2]));
        bCopy = new SimpleTransitionSystem(new Automaton(CDD.makeInputEnabled(machines[2])));
    }

    @Test
    public void testARefinesSelf() {
        CDD.init(100,100,100);
        List<Clock> clocks = new ArrayList<>();
        clocks.addAll(a.getClocks());
        clocks.addAll(aCopy.getClocks());
        CDD.addClocks(clocks);
        assertTrue(new Refinement(new SimpleTransitionSystem(CDD.makeInputEnabled(a.getAutomaton())), aCopy).check());
    }

    @Test
    public void testAaRefinesSelf() {
        CDD.init(100,100,100);
        List<Clock> clocks = new ArrayList<>();
        clocks.addAll(aa.getClocks());
        clocks.addAll(aaCopy.getClocks());
        CDD.addClocks(clocks);
        assertTrue(new Refinement(new SimpleTransitionSystem(CDD.makeInputEnabled(aa.getAutomaton())), aaCopy).check());
    }

    @Test
    public void testBRefinesSelf() {
        CDD.init(100,100,100);
        List<Clock> clocks = new ArrayList<>();
        clocks.addAll(b.getClocks());
        clocks.addAll(bCopy.getClocks());
        CDD.addClocks(clocks);
        assertTrue(new Refinement(new SimpleTransitionSystem(CDD.makeInputEnabled(b.getAutomaton())), bCopy).check());
    }

    @Test
    public void compNotRefinesB() {
        // should fail because right side has more inputs
        CDD.init(100,100,100);
        List<Clock> clocks = new ArrayList<>();
        clocks.addAll(aa.getClocks());
        clocks.addAll(aaCopy.getClocks());
        clocks.addAll(b.getClocks());
        CDD.addClocks(clocks);
        assertFalse(new Refinement(new Composition(new TransitionSystem[]{a, aa}), b).check());
    }
}
