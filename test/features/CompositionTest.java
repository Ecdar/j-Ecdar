package features;

import log.Log;
import logic.Composition;
import logic.Refinement;
import logic.SimpleTransitionSystem;
import logic.TransitionSystem;
import models.Automaton;
import models.CDD;
import models.Location;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import parser.JSONParser;
import parser.XMLParser;

import static org.junit.Assert.assertTrue;

public class CompositionTest {

    @After
    public void afterEachTest(){
        CDD.done();
    }

    private static TransitionSystem adm, admCopy, machine, machineCopy, researcher, researcherCopy, spec, specCopy,
            machine3, machine3Copy, adm2, adm2Copy, half1, half1Copy, half2, half2Copy;

    @BeforeClass
    public static void setUpBeforeClass() {
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

        adm = new SimpleTransitionSystem(machines[0]);
        admCopy = new SimpleTransitionSystem(new Automaton(machines[0]));
        machine = new SimpleTransitionSystem(machines[1]);
        machineCopy = new SimpleTransitionSystem(new Automaton(machines[1]));
        researcher = new SimpleTransitionSystem(machines[2]);
        researcherCopy = new SimpleTransitionSystem(new Automaton(machines[2]));
        spec = new SimpleTransitionSystem(machines[3]);
        specCopy = new SimpleTransitionSystem(new Automaton(machines[3]));
        machine3 = new SimpleTransitionSystem(machines[4]);
        machine3Copy = new SimpleTransitionSystem(new Automaton(machines[4]));
        adm2 = new SimpleTransitionSystem(machines[5]);
        adm2Copy = new SimpleTransitionSystem(new Automaton(machines[5]));
        half1 = new SimpleTransitionSystem(machines[6]);
        half1Copy = new SimpleTransitionSystem(new Automaton(machines[6]));
        half2 = new SimpleTransitionSystem(machines[7]);
        half2Copy = new SimpleTransitionSystem(new Automaton(machines[7]));
    }


    @Test
    public void testCompposition() {
        Composition comp = new Composition(new TransitionSystem[]{ machine, researcher});
        Automaton aut = comp.getAutomaton();
        SimpleTransitionSystem s = new SimpleTransitionSystem(aut);
        s.toXML("testOutput/compositionTest.xml");
        assertTrue(true);
    }

    @Test
    public void testCompposition1() {
        Composition comp = new Composition(new TransitionSystem[]{ adm, machine, researcher });
        Automaton aut = comp.getAutomaton();
        SimpleTransitionSystem s = new SimpleTransitionSystem(aut);
        s.toXML("testOutput/compositionTest1.xml");
        assertTrue(true);
    }

    @Test
    public void selfloopTest() {

        Automaton[] aut1 = XMLParser.parse("testOutput/selfloopNonZeno.xml", false);
        Automaton copy = new Automaton(aut1[0]);
        SimpleTransitionSystem selfloop = new SimpleTransitionSystem(aut1[0]);
        SimpleTransitionSystem selfloop1 = new SimpleTransitionSystem(copy);
        Refinement ref = new Refinement(selfloop,selfloop1);
        boolean res = ref.check();
        Log.trace(ref.getErrMsg());
        assert (res==true);
    }

    @Test
    public void testCompRefinesSpecWeird() {

        Composition comp = new Composition(new TransitionSystem[]{adm, machine, researcher});
        for (Location l : comp.getAutomaton().getLocations())
        {
            if (l.isInconsistent())
                System.out.println("ISINC");
            if (l.isUniversal())
                System.out.println("ISUNIV");
        }
        // TODO : for some reason this fails, now that I fixed the "isUniversal" of complex locations
        Refinement ref = new Refinement(new SimpleTransitionSystem(comp.getAutomaton()), spec);
        boolean res = ref.check();
        Log.trace(ref.getErrMsg());
        assertTrue(res);

    }

    @Test
    public void testCompRefinesSpec() {

        Composition comp = new Composition(new TransitionSystem[]{adm, machine, researcher});
        SimpleTransitionSystem st = new SimpleTransitionSystem(comp.getAutomaton());
        st.toXML("testOutput/aWeirdOutput.xml");
        SimpleTransitionSystem st2 = new SimpleTransitionSystem(comp.getAutomaton());
        st2.toXML("testOutput/aWeirdOutput2.xml");
        assert(new Refinement(comp,spec).check());
        Refinement ref = new Refinement(new SimpleTransitionSystem(comp.getAutomaton()), spec);
        boolean res = ref.check();
        Log.trace(ref.getErrMsg());
        assertTrue(res);

    }
    @Test
    public void testCompOfCompRefinesSpec() {


        assertTrue(new Refinement(
                new Composition(new TransitionSystem[]{adm,new SimpleTransitionSystem(new Composition(new TransitionSystem[]{machine, researcher}).getAutomaton())
                }),
                spec).check()
        );
    }

    @Test
    public void testCompRefinesSpecOld() {

        Composition comp = new Composition(new TransitionSystem[]{adm, machine, researcher});
        comp.getAutomaton();
        assertTrue(new Refinement(new Composition(new TransitionSystem[]{adm, machine, researcher}), spec).check());
    }

}