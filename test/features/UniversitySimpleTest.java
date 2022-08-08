package features;

import exceptions.CddAlreadyRunningException;
import exceptions.CddNotRunningException;
import log.Log;
import logic.*;
import models.Automaton;
import models.CDD;
import models.Clock;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import parser.JSONParser;
import parser.XMLFileWriter;
import parser.XMLParser;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class UniversitySimpleTest {

    private static TransitionSystem adm, admCopy, machine, machineCopy, researcher, researcherCopy, spec, specCopy,
            machine3, machine3Copy, adm2, adm2Copy, half1, half1Copy, half2, half2Copy;

    @After
    public void afterEachTest(){
        CDD.done();
    }


    @BeforeClass
    public static void setUpBeforeClass() throws CddAlreadyRunningException, CddNotRunningException {
        String base = "./samples/json/EcdarSimpleUniversity1/";
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

        adm = new SimpleTransitionSystem((machines[0]));
        admCopy = new SimpleTransitionSystem(new Automaton((machines[0])));
        machine = new SimpleTransitionSystem((machines[1]));
        machineCopy = new SimpleTransitionSystem(new Automaton((machines[1])));
        researcher = new SimpleTransitionSystem((machines[2]));
        researcherCopy = new SimpleTransitionSystem(new Automaton((machines[2])));
        spec = new SimpleTransitionSystem((machines[3]));
        specCopy = new SimpleTransitionSystem(new Automaton((machines[3])));
        machine3 = new SimpleTransitionSystem((machines[4]));
        machine3Copy = new SimpleTransitionSystem(new Automaton((machines[4])));
        adm2 = new SimpleTransitionSystem((machines[5]));
        adm2Copy = new SimpleTransitionSystem(new Automaton((machines[5])));
        half1 = new SimpleTransitionSystem((machines[6]));
        half1Copy = new SimpleTransitionSystem(new Automaton((machines[6])));
        half2 = new SimpleTransitionSystem((machines[7]));
        half2Copy = new SimpleTransitionSystem(new Automaton((machines[7])));
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
    public void newQuotientTest() {

        assertFalse(new Refinement(new Composition(new TransitionSystem[]{machine,adm2}), new Quotient(spec,researcher)).check());
    }

    @Test
    public void newQuotientTest1() {

        assertTrue(new Refinement(new Composition(new TransitionSystem[]{machine,adm}), new Quotient(spec,researcher)).check());
    }


    @Test
    @Ignore
    public void newQuotientTest2() {

        assertFalse(new Refinement(new Composition(new TransitionSystem[]{machine,researcher}), new Quotient(spec,adm2)).check());
    }

    @Test
    @Ignore
    public void newQuotientTest4A() {
        Quotient q = new Quotient(spec,adm);
        XMLFileWriter.toXML("./testOutput/specDIVadm.xml", new Automaton[]{q.getAutomaton()});
        XMLFileWriter.toXML("./testOutput/comp.xml",  new Automaton[]{new Composition(new TransitionSystem[]{machine,researcher}).getAutomaton()});
        TransitionSystem comp = new SimpleTransitionSystem(new Composition(new TransitionSystem[]{machine,researcher}).getAutomaton());
        Refinement ref = new Refinement(comp, new SimpleTransitionSystem(q.getAutomaton()) );
        boolean res = ref.check();
        Log.trace(ref.getErrMsg());
        assertTrue(res);
    }
/*

    @Test
    public void newQuotientTest4B() {
        Quotient q = new Quotient(spec,researcher);
        Refinement ref = new Refinement(new Composition(new TransitionSystem[]{machine,adm}), new SimpleTransitionSystem(q.getAutomaton()) );
        boolean res = ref.check();
        Log.trace(ref.getErrMsg());
        assertTrue(res);
    }


    @Test
    public void newQuotientTest4C() {
        Quotient q = new Quotient(spec,machine);
        Refinement ref = new Refinement(new Composition(new TransitionSystem[]{researcher,adm}), new SimpleTransitionSystem(q.getAutomaton()) );
        boolean res = ref.check();
        Log.trace(ref.getErrMsg());
        assertTrue(res);
    }
    @Test
    public void newQuotientTest4D() {
        Quotient q = new Quotient(spec,machine);
        Refinement ref = new Refinement(new Composition(new TransitionSystem[]{researcher,adm}), new SimpleTransitionSystem(q.getAutomaton()) );
        boolean res = ref.check();
        Log.trace(ref.getErrMsg());
        assertTrue(res);
    }

    @Test
    public void simpliversityTest1() {
        Automaton[] auts = XMLParser.parse("samples/xml/simpliversity.xml", true);
        Automaton autResearcher = auts[0];
        Automaton autAdm = auts[1];
        Automaton autSpec = auts[2];
        SimpleTransitionSystem researcher = new SimpleTransitionSystem(autResearcher);
        SimpleTransitionSystem adm = new SimpleTransitionSystem(autAdm);
        SimpleTransitionSystem spec = new SimpleTransitionSystem(autSpec);

        Refinement ref = new Refinement(new Composition(new TransitionSystem[]{researcher,adm}), spec );
        boolean result = ref.check();
        Log.trace(ref.getErrMsg());
        assertTrue(result);
    }

    @Test
    public void simpliversityTest2() {
        Automaton[] auts = XMLParser.parse("samples/xml/simpliversity.xml", true);
        Automaton autResearcher = auts[0];
        Automaton autAdm = auts[1];
        Automaton autSpec = auts[2];
        SimpleTransitionSystem researcher = new SimpleTransitionSystem(autResearcher);
        SimpleTransitionSystem adm = new SimpleTransitionSystem(autAdm);
        SimpleTransitionSystem spec = new SimpleTransitionSystem(autSpec);

        Refinement ref = new Refinement(researcher, new SimpleTransitionSystem(new Quotient(spec,adm).getAutomaton())  );
        boolean result = ref.check();
        Log.trace(ref.getErrMsg());
        assertTrue(result);
    }

    @Test
    public void newQuotientTest5() {
        Automaton quo = XMLParser.parse("samples/xml/staticSpecDIVAdm.xml",true)[0];
        Automaton comp = XMLParser.parse("comp.xml",true)[0];
        Refinement ref = new Refinement(new SimpleTransitionSystem(comp), new SimpleTransitionSystem(quo) );
        boolean res = ref.check();
        Log.trace(ref.getErrMsg());
        assertTrue(res);
    }


    public void newQuotientTest3() {
        XMLFileWriter.toXML("adm2new.xml",new Automaton[]{adm2.getAutomaton()});
        XMLFileWriter.toXML("admnew.xml",new Automaton[]{adm.getAutomaton()});


        SimpleTransitionSystem st =  new SimpleTransitionSystem(new Quotient(spec,adm).getAutomaton());

        Refinement ref = new Refinement(new Composition(new TransitionSystem[]{machine,researcher}), st);
        boolean res = ref.check();
        Log.trace(ref.getErrMsg());
        assertTrue(res);
    }

*/

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
        assertTrue(new Refinement(new SimpleTransitionSystem((researcher.getAutomaton())), new SimpleTransitionSystem((researcherCopy.getAutomaton()))).check());
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
        Refinement ref = new Refinement(comp, spec);
        boolean res = ref.check();
        Log.trace(ref.getErrMsg());


        assertTrue(res);
    }

    @Test
    public void testCompRefinesSpecOld() {

        Composition comp = new Composition(new TransitionSystem[]{adm, machine, researcher});

        Refinement ref = new Refinement(comp, spec);
        boolean res = ref.check();
        Log.trace(ref.getErrMsg());


        assertTrue(res);
    }

    @Test
    public void testCompOfCompRefinesSpec() {

        Refinement ref = new Refinement(
                new Composition(new TransitionSystem[]{adm,
                        new Composition(new TransitionSystem[]{machine, researcher})}),
                spec);
        Log.trace(ref.getErrMsg());
        boolean res = ref.check();

        assertTrue(res
        );
    }

    @Test
    public void testCompRefinesSelf() {
        /*assertTrue(new Refinement(adm,admCopy).check());
        assertTrue(new Refinement(admCopy,adm).check());
        assertTrue(new Refinement(machineCopy,machine).check());
        assertTrue(new Refinement(machine,machineCopy).check());
        assertTrue(new Refinement(researcherCopy,researcher).check());
        assertTrue(new Refinement(researcher,researcherCopy).check());*/

        Automaton comp1 = new Composition(new TransitionSystem[]{adm, machine, researcher}).getAutomaton();
        Automaton comp2 = new Composition(new TransitionSystem[]{admCopy, machineCopy, researcherCopy}).getAutomaton();

        new SimpleTransitionSystem(comp1).toXML("testOutput/comp1.xml");
        new SimpleTransitionSystem(comp2).toXML("testOutput/comp2.xml");

        assertTrue(new Refinement(new SimpleTransitionSystem(comp1),new SimpleTransitionSystem(comp2)).check());
        assertTrue(new Refinement(new SimpleTransitionSystem(comp2),new SimpleTransitionSystem(comp1)).check());
        Refinement ref = new Refinement(new SimpleTransitionSystem(comp1), new SimpleTransitionSystem(comp2));

        assertTrue(ref.check());
    }

    @Test
    public void testStored() {
        Automaton[] aut = XMLParser.parse("samples/xml/test.xml", false);
        Automaton[] aut1 = XMLParser.parse("samples/xml/test1.xml", false);
        SimpleTransitionSystem comp0 = new SimpleTransitionSystem(aut[0]);
        SimpleTransitionSystem comp1 = new SimpleTransitionSystem(aut1[0]);

        assertTrue(new Refinement(comp1,comp0).check());
        assertTrue(new Refinement(comp0,comp1).check());
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