package features;

import Exceptions.CddAlreadyRunningException;
import Exceptions.CddNotRunningException;
import logic.*;
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

public class UniversityTest {

    private static TransitionSystem adm, admCopy, machine, machineCopy, researcher, researcherCopy, spec, specCopy,
            machine3, machine3Copy, adm2, adm2Copy, half1, half1Copy, half2, half2Copy;

    @After
    public void afterEachTest(){
        CDD.done();
    }

    @BeforeClass
    public static void setUpBeforeClass() throws CddAlreadyRunningException, CddNotRunningException {
        String base = "./samples/json/EcdarUniversity/";
        String[] components = new String[]{"GlobalDeclarations.json",
                "Components/Administration.json",
                "Components/Machine.json",
                "Components/Researcher.json",
                "Components/Spec.json",
                "Components/Machine3.json",
                "Components/Adm2.json",
                "Components/HalfAdm1.json",
                "Components/HalfAdm2.json"};
        Automaton[] machines = JSONParser.parse(base, components, true);
        CDD.init(100,100,100);
        List<Clock> clocks = new ArrayList<>();
        clocks.addAll(machines[0].getClocks());
        clocks.addAll(machines[1].getClocks());
        clocks.addAll(machines[2].getClocks());
        clocks.addAll(machines[3].getClocks());
        clocks.addAll(machines[4].getClocks());
        clocks.addAll(machines[5].getClocks());
        clocks.addAll(machines[6].getClocks());
        clocks.addAll(machines[7].getClocks());
        CDD.addClocks(clocks);

        adm = new SimpleTransitionSystem(CDD.makeInputEnabled(machines[0]));
        admCopy = new SimpleTransitionSystem(new Automaton(CDD.makeInputEnabled(machines[0])));
        machine = new SimpleTransitionSystem(CDD.makeInputEnabled(machines[1]));
        machineCopy = new SimpleTransitionSystem(new Automaton(CDD.makeInputEnabled(machines[1])));
        researcher = new SimpleTransitionSystem(CDD.makeInputEnabled(machines[2]));
        researcherCopy = new SimpleTransitionSystem(new Automaton(CDD.makeInputEnabled(machines[2])));
        spec = new SimpleTransitionSystem(CDD.makeInputEnabled(machines[3]));
        specCopy = new SimpleTransitionSystem(new Automaton(CDD.makeInputEnabled(machines[3])));
        machine3 = new SimpleTransitionSystem(CDD.makeInputEnabled(machines[4]));
        machine3Copy = new SimpleTransitionSystem(new Automaton(CDD.makeInputEnabled(machines[4])));
        adm2 = new SimpleTransitionSystem(CDD.makeInputEnabled(machines[5]));
        adm2Copy = new SimpleTransitionSystem(new Automaton(CDD.makeInputEnabled(machines[5])));
        half1 = new SimpleTransitionSystem(CDD.makeInputEnabled(machines[6]));
        half1Copy = new SimpleTransitionSystem(new Automaton(CDD.makeInputEnabled(machines[6])));
        half2 = new SimpleTransitionSystem(CDD.makeInputEnabled(machines[7]));
        half2Copy = new SimpleTransitionSystem(new Automaton(CDD.makeInputEnabled(machines[7])));
    }

    @Test
    public void testAdm2RefinesSelf() {
        assertTrue(new Refinement(adm2, adm2Copy).check());
    }

    @Test
    public void testHalf1RefinesSelf() {
        assertTrue(new Refinement(half1, half1Copy).check());
    }

    @Test
    public void testHalf2RefinesSelf() {
        assertTrue(new Refinement(half2, half2Copy).check());
    }

    @Test
    public void testAdmRefinesSelf() {
        assertTrue(new Refinement(adm, admCopy).check());
    }

    @Test
    public void testMachineRefinesSelf() {
        assertTrue(new Refinement(machine, machineCopy).check());
    }

    @Test
    public void testMachineRefinesSelfDuplicate() {
        Refinement ref = new Refinement(machine, machine);
        assertFalse(ref.check());
        assert ref.getErrMsg().contains("Duplicate process instance");
    }

    @Test
    public void testResRefinesSelf() {
        assertTrue(new Refinement(researcher, researcherCopy).check());
    }

    @Test
    public void testSpecRefinesSelf() {
        assertTrue(new Refinement(spec, specCopy).check());
    }

    @Test
    public void testMachine3RefinesSelf() {
        assertTrue(new Refinement(machine3, machine3Copy).check());
    }

    @Test
    public void testAdmNotRefinesMachine() {
        assertFalse(new Refinement(adm, machine).check());
    }

    @Test
    public void testAdmNotRefinesResearcher() {
        assertFalse(new Refinement(adm, researcher).check());
    }

    @Test
    public void testAdmNotRefinesSpec() {
        assertFalse(new Refinement(adm, spec).check());
    }

    @Test
    public void testAdmNotRefinesMachine3() {
        assertFalse(new Refinement(adm, machine3).check());
    }

    @Test
    public void testMachineNotRefinesAdm() {
        assertFalse(new Refinement(machine, adm).check());
    }

    @Test
    public void testMachineNotRefinesResearcher() {
        assertFalse(new Refinement(machine, researcher).check());
    }

    @Test
    public void testMachineNotRefinesSpec() {
        assertFalse(new Refinement(machine, spec).check());
    }

    @Test
    public void testMachineNotRefinesMachine3() {
        assertFalse(new Refinement(machine, machine3).check());
    }

    @Test
    public void testResNotRefinesAdm() {
        assertFalse(new Refinement(researcher, adm).check());
    }

    @Test
    public void testResNotRefinesMachine() {
        assertFalse(new Refinement(researcher, machine).check());
    }

    @Test
    public void testResNotRefinesSpec() {
        assertFalse(new Refinement(researcher, spec).check());
    }

    @Test
    public void testResNotRefinesMachine3() {
        assertFalse(new Refinement(researcher, machine3).check());
    }

    @Test
    public void testSpecNotRefinesAdm() {
        assertFalse(new Refinement(spec, adm).check());
    }

    @Test
    public void testSpecNotRefinesMachine() {
        assertFalse(new Refinement(spec, machine).check());
    }

    @Test
    public void testSpecNotRefinesResearcher() {
        assertFalse(new Refinement(spec, researcher).check());
    }

    @Test
    public void testSpecNotRefinesMachine3() {
        assertFalse(new Refinement(spec, machine3).check());
    }

    @Test
    public void testMachine3RefinesMachine() {
        assertTrue(new Refinement(machine3, machine).check());
    }

    @Test
    public void testMachine3NotRefinesAdm() {
        assertFalse(new Refinement(machine3, adm).check());
    }

    @Test
    public void testMachine3NotRefinesResearcher() {
        assertFalse(new Refinement(machine3, researcher).check());
    }

    @Test
    public void testMachine3NotRefinesSpec() {
        assertFalse(new Refinement(machine3, spec).check());
    }


    @Test
    public void testCompRefinesSpec() {

        Composition comp = new Composition(new TransitionSystem[]{adm, machine, researcher});

        //comp.getAutomaton();
        Refinement ref = new Refinement(new SimpleTransitionSystem(comp.getAutomaton()), spec);
        boolean res = ref.check();
        System.out.println(ref.getErrMsg());


        assertTrue(res);
    }

    @Test
    public void testCompRefinesSpecOld() {

        Composition comp = new Composition(new TransitionSystem[]{adm, machine, researcher});

        Refinement ref = new Refinement(comp, spec);
        boolean res = ref.check();
        System.out.println(ref.getErrMsg());


        assertTrue(res);
    }

    @Test
    public void testCompOfCompRefinesSpec() {

        //Clock x = new Clock("x");
        //List<Clock> l = new ArrayList<>();
        //l.add(x);
        //Zone z = new Zone(l.size()+1,true);
        //Guard g = new Guard(x,5,true,true);
        //Guard g = new Guard(x,1073741823,5,true);
        //z.buildConstraintsForGuard(g,1);
        //z.printDBM(true,true);
        Refinement ref = new Refinement(
                new Composition(new TransitionSystem[]{adm,
                        new Composition(new TransitionSystem[]{machine, researcher})}),
                spec);
        System.out.println(ref.getErrMsg());
        boolean res = ref.check();

        assertTrue(res
        );
    }

    @Test
    public void testCompRefinesSelf() {
        Refinement ref = new Refinement(
                new Composition(new TransitionSystem[]{adm, machine, researcher}),
                new Composition(new TransitionSystem[]{machineCopy, researcherCopy, admCopy}));
        assertTrue(ref.check());
    }

    @Test
    public void testCompRefinesSelfDuplicate() {
        Refinement ref = new Refinement(
                new Composition(new TransitionSystem[]{adm, machine, researcher}),
                new Composition(new TransitionSystem[]{machine, researcher, adm}));

        assertFalse(ref.check(true));
        assert ref.getErrMsg().contains("Duplicate process instance");
    }

    @Test
    public void testUncomposable() {
        boolean fail = false;

        try {
            new Refinement(
                    new Composition(new TransitionSystem[]{machine, machine3}),
                    machine);
        } catch (IllegalArgumentException ex) {
            fail = true;
        }

        assertTrue(fail);
    }

    @Test
    public void testHalf1AndHalf2RefinesAdm2() {
        assertTrue(new Refinement(new Conjunction(new TransitionSystem[]{half1, half2}), adm2).check());
    }

    @Test
    public void testAdm2RefinesHalf1AndHalf2() {
        assertTrue(new Refinement(adm2, new Conjunction(new TransitionSystem[]{half1, half2})).check());
    }
}