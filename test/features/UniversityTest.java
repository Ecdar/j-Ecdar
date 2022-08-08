package features;

import log.Log;
import logic.*;
import models.*;
import parser.*;
import org.junit.Ignore;
import org.junit.Test;

import java.sql.Ref;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class UniversityTest {
    private final String baseEcdarUniveristy = "./samples/json/EcdarUniversity/Components/";
    private final String baseEcdarSimpleUniversity = "./samples/xml/simpliversity.xml";
    private final String baseXmlSample = "./samples/xml/";

    public TransitionSystem getSimpleResearcher() {
        return new SimpleTransitionSystem(XMLParser.parse(baseEcdarSimpleUniversity, true)[0]);
    }

    public TransitionSystem getSimpleAdm() {
        return new SimpleTransitionSystem(XMLParser.parse(baseEcdarSimpleUniversity, true)[1]);
    }

    public TransitionSystem getSimpleSpec() {
        return new SimpleTransitionSystem(XMLParser.parse(baseEcdarSimpleUniversity, true)[2]);
    }

    public TransitionSystem getAdm() {
        return new SimpleTransitionSystem(JSONParser.parse(baseEcdarUniveristy, "Administration.json", true));
    }

    public TransitionSystem getMachine() {
        return new SimpleTransitionSystem(JSONParser.parse(baseEcdarUniveristy, "Machine.json", true));
    }

    public TransitionSystem getResearcher() {
        return new SimpleTransitionSystem(JSONParser.parse(baseEcdarUniveristy, "Researcher.json", true));
    }

    public TransitionSystem getSpec() {
        return new SimpleTransitionSystem(JSONParser.parse(baseEcdarUniveristy, "Spec.json", true));
    }

    public TransitionSystem getMachine3() {
        return new SimpleTransitionSystem(JSONParser.parse(baseEcdarUniveristy, "Machine3.json", true));
    }

    public TransitionSystem getAdm2() {
        return new SimpleTransitionSystem(JSONParser.parse(baseEcdarUniveristy, "Adm2.json", true));
    }

    public TransitionSystem getHalf1() {
        return new SimpleTransitionSystem(JSONParser.parse(baseEcdarUniveristy, "HalfAdm1.json", true));
    }

    public TransitionSystem getHalf2() {
        return new SimpleTransitionSystem(JSONParser.parse(baseEcdarUniveristy, "HalfAdm2.json", true));
    }

    public TransitionSystem getTest() {
        return new SimpleTransitionSystem(XMLParser.parse(baseXmlSample + "test.xml", false)[0]);
    }

    public TransitionSystem getTest1() {
        return new SimpleTransitionSystem(XMLParser.parse(baseXmlSample + "test1.xml", false)[0]);
    }

    public TransitionSystem getUniversalMachine() {
        return new SimpleTransitionSystem(XMLParser.parse(baseXmlSample + "university-universalSpec.xml", true)[0]);
    }

    public TransitionSystem getUniversalResearcher() {
        return new SimpleTransitionSystem(XMLParser.parse(baseXmlSample + "university-universalSpec.xml", true)[1]);
    }

    public TransitionSystem getUniversalSpec() {
        return new SimpleTransitionSystem(XMLParser.parse(baseXmlSample + "university-universalSpec.xml", true)[2]);
    }

    public TransitionSystem getUniversalUniversity() {
        return new SimpleTransitionSystem(XMLParser.parse(baseXmlSample + "university-universalSpec.xml", true)[3]);
    }

    @Test
    public void specIsUniversal() {
        // 1: refinement: machine || researcher || university <= spec
        // 2: refinement: machine || researcher <= spec \ university
        // 3: refinement: spec <= spec
        Refinement refinement1 = new Refinement(new Composition(getUniversalMachine(), getUniversalResearcher(), getUniversalUniversity()), getUniversalSpec());
        Refinement refinement2 = new Refinement(new Composition(getUniversalMachine(), getUniversalResearcher()), new Quotient(getUniversalSpec(), getUniversalUniversity()));
        Refinement refinement3 = new Refinement(getUniversalSpec(), getUniversalSpec());

        assertTrue(refinement1.check());
        assertTrue(refinement2.check());
        assertTrue(refinement3.check());
    }

    @Test
    public void testAdm2RefinesSelf() {
        // refinement: adm2 <= adm2
        TransitionSystem lhs = getAdm2();
        TransitionSystem rhs = getAdm2();
        Refinement refinement = new Refinement(lhs, rhs);

        boolean refines = refinement.check();

        assertTrue(refines);
    }

    @Test
    public void testHalf1RefinesSelf() {
        // refinement: half1 <= half1
        TransitionSystem lhs = getHalf1();
        TransitionSystem rhs = getHalf1();
        Refinement refinement = new Refinement(lhs, rhs);

        boolean refines = refinement.check();

        assertTrue(refines);
    }

    @Test
    public void newQuotientTest() {
        // refinement: machine || adm2 <= spec \\ researcher
        Composition composition = new Composition(getMachine(), getAdm2());
        Quotient quotient = new Quotient(getSpec(), getResearcher());
        Refinement refinement = new Refinement(composition, quotient);

        boolean refines = refinement.check();

        assertFalse(refines);
    }

    @Test
    public void newQuotientTestAutomaton() {
        // refinement: machine || adm2 <= spec \\ researcher
        Composition composition = new Composition(getMachine(), getAdm2());
        Quotient quotient = new Quotient(getSpec(), getResearcher());
        // Creating a new Automaton is intentional as we are testing the automaton from the quotient more directly
        TransitionSystem quotientTransitionSystem = new SimpleTransitionSystem(new Automaton(quotient.getAutomaton()));
        Refinement refinement = new Refinement(composition, quotientTransitionSystem);

        boolean refines = refinement.check();

        assertFalse(refines);
    }

    @Test
    public void newQuotientTest1() {
        // refinement: machine || adm <= spec \ researcher
        Composition lhs = new Composition(getMachine(), getAdm());
        Quotient rhs = new Quotient(getSpec(), getResearcher());
        Refinement refinement = new Refinement(lhs, rhs);

        boolean refines = refinement.check();

        assertTrue(refines);
    }

    @Test
    public void quotientSelfAdm() {
        // refinement: spec \ adm <= spec \ amd
        Quotient lhs = new Quotient(getSpec(), getAdm());
        Quotient rhs = new Quotient(getSpec(), getAdm());
        Refinement refinement = new Refinement(lhs, rhs);

        boolean refines = refinement.check();

        assertTrue(refines);
    }

    @Test
    @Ignore
    public void quotientSelfAdmAutomaton() {
        // refinement: spec \ adm <= spec \ amd
        TransitionSystem lhs = new SimpleTransitionSystem(new Quotient(getSpec(), getAdm()).getAutomaton());
        TransitionSystem rhs = new SimpleTransitionSystem(new Quotient(getSpec(), getAdm()).getAutomaton());
        Refinement refinement = new Refinement(lhs, rhs);

        boolean refines = refinement.check();
        System.out.println(refinement.getErrMsg());
        assertTrue(refines);
    }

    @Test
    public void quotientEqual() {
        // refinement: spec \ research <= spec \ researcher
        SimpleTransitionSystem lhs = new SimpleTransitionSystem(new Quotient(getSpec(), getResearcher()).getAutomaton());
        Quotient rhs = new Quotient(getSpec(), getResearcher());
        Refinement refinement = new Refinement(lhs, rhs);

        XMLFileWriter.toXML("testOutput/quotient.xml", lhs);
        boolean refines = refinement.check();

        assertTrue(refines);
    }

    @Test
    public void testFromTestFramework() {
        //  refinement: ((HalfAdm1 && HalfAdm2) || Machine) <= (((Adm2 && HalfAdm1) || Machine) && (Adm2 || Machine))

        TransitionSystem right1=new Composition(new Conjunction(getAdm2(),getHalf1()),getMachine());
        TransitionSystem right2=new Composition(getAdm2(),getMachine());
        Log.trace(right2.getOutputs());
        Log.trace(right2.getInputs());
        Log.trace(right2.getAutomaton().getOutputAct());
        Log.trace(right2.getAutomaton().getInputAct());

        TransitionSystem left = new Composition(new Conjunction(getHalf1(),getHalf2()),getMachine());

        TransitionSystem right = new Conjunction(right1, right2);
        Log.trace(right1.getOutputs());
        Log.trace(right2.getOutputs());
        Log.trace(right.getOutputs());

        TransitionSystem rightAut = new Conjunction(new SimpleTransitionSystem(right1.getAutomaton()), new SimpleTransitionSystem(right2.getAutomaton()));
        Log.trace(rightAut.getOutputs());

        XMLFileWriter.toXML("testOutput/right.xml",right.getAutomaton());
        XMLFileWriter.toXML("testOutput/right1.xml",right1.getAutomaton());
        XMLFileWriter.toXML("testOutput/right2.xml",right2.getAutomaton());
        XMLFileWriter.toXML("testOutput/rightAut.xml",rightAut.getAutomaton());

        XMLFileWriter.toXML("testOutput/left.xml",left.getAutomaton());

        Refinement refinement1 = new Refinement(left,rightAut);
        boolean refines1 = refinement1.check(true);
        //Log.trace(refinement1.getTree().toDot());
        assertTrue(refines1);

        Refinement refinement = new Refinement(left,right);
        boolean refines = refinement.check(true);
        //Log.trace(refinement.getTree().toDot());
        assertTrue(refines);

    }


    @Test
    @Ignore
    public void testFromTestFramework1() {
        // refinement: Machine <= ((((Adm2 && HalfAdm1) || Machine || Researcher) \\\\ (Adm2 && HalfAdm2)) \\\\ Researcher)

        TransitionSystem left =getMachine();
        TransitionSystem right1=new Conjunction(getAdm2(),getHalf1());
        TransitionSystem right2=new Composition(right1,getMachine(),getResearcher());
        TransitionSystem right3=new Conjunction(getAdm2(),getHalf2());
//        TransitionSystem q1 = new SimpleTransitionSystem(new Quotient(right2,right3).getAutomaton());
//        TransitionSystem q2 =new SimpleTransitionSystem(new Quotient(q1,getResearcher()).getAutomaton());

        TransitionSystem q1 =new Quotient(right2,right3);
        TransitionSystem q2 =new Quotient(q1,getResearcher());

        Refinement ref1 = new Refinement(new Composition(left,getResearcher(),right3),right2);
        assertTrue(ref1.check());
        Refinement ref2 = new Refinement(new Composition(left,getResearcher()),new Quotient(right2,right3));
        assertTrue(ref2.check());
        Refinement ref3 = new Refinement(new Composition(left,right3),new Quotient(right2,getResearcher()));
        assertTrue(ref3.check());
        Refinement ref = new Refinement(left,q2);
        boolean res = ref.check(true);
        Log.trace(ref.getErrMsg());
        //Log.trace(ref.getTree().toDot());
        assertTrue(res);

    }



    @Test
    public void testFromTestFramework2() {
        // "consistency: ((Spec \\ Machine) \\ Researcher);
        // refinement: Administration <= ((Spec \\ Machine) \\ Researcher)


        TransitionSystem consistency = new Quotient(new Quotient(getSpec(),getMachine()),getResearcher());
        assertTrue(consistency.isFullyConsistent());
        Refinement ref = new Refinement(getAdm(),consistency);
        boolean res = ref.check(true);
        System.out.println(ref.getErrMsg());
        System.out.println(ref.getTree().toDot());
        assertTrue(res);

    }


    @Test
    public void doubleQuotientTest() {
        // refinement: res <= spec \ adm2 \ machine
        TransitionSystem lhs = getResearcher();
        Quotient rhs1 = new Quotient(getSpec(), getAdm2());
        Quotient rhs = new Quotient(rhs1,getMachine());
        Refinement refinement = new Refinement(lhs, rhs);

        boolean refines = refinement.check();

        assertFalse(refines);
    }
    @Test @Ignore
    public void doubleQuotientTest1() {
        // refinement: res <= spec \ adm2 \ machine
        TransitionSystem lhs = getMachine();
        Quotient rhs1 = new Quotient(getSpec(), getAdm2());
        Quotient rhs = new Quotient(rhs1,getResearcher());
        Refinement refinement = new Refinement(lhs, rhs);
        assertFalse(new Refinement(new Composition(getResearcher(),getAdm2(),getMachine()),getSpec()).check());
        boolean refines = refinement.check();

        assertFalse(refines);
    }

    @Test
    public void doubleQuotientTest2() {
        // refinement: res <= spec \ adm2 \ machine
        TransitionSystem lhs = getMachine();
        Quotient rhs1 = new Quotient(getSpec(), getResearcher());
        Quotient rhs = new Quotient(rhs1,getAdm2());
        Refinement refinement = new Refinement(lhs, rhs);

        boolean refines = refinement.check();

        assertFalse(refines);
    }
    @Test
    public void doubleQuotientTest3() {
        // refinement: res <= spec \ adm2 \ machine
        TransitionSystem lhs = getAdm2();
        Quotient rhs1 = new Quotient(getSpec(), getMachine());
        Quotient rhs = new Quotient(rhs1,getResearcher());
        Refinement refinement = new Refinement(lhs, rhs);

        boolean refines = refinement.check();

        assertFalse(refines);
    }

    @Test
    @Ignore
    public void newQuotientTest1Automaton() {
        Composition composition = new Composition(getMachine(), getAdm());
        Quotient quotient = new Quotient(getSpec(), getResearcher());
        Refinement refinement = new Refinement(composition, quotient);

        boolean refines = refinement.check();

        assertTrue(refines);
    }

    @Test
    public void newQuotientTest2() {
        // refinement: machine || researcher <= spec \ adm2
        Composition lhs = new Composition(getMachine(), getResearcher());
        Quotient rhs = new Quotient(getSpec(), getAdm2());
        Refinement refinement = new Refinement(lhs, rhs);

        boolean refines = refinement.check();

        assertFalse(refines);
    }

    @Test
    public void newQuotientTest2Automaton() {
        Composition composition = new Composition(getMachine(), getResearcher());
        Quotient quotient = new Quotient(getSpec(), getAdm2());
        TransitionSystem quotientTransitionSystem = new SimpleTransitionSystem(new Automaton(quotient.getAutomaton()));
        Refinement refinement = new Refinement(composition, quotientTransitionSystem);

        boolean refines = refinement.check();

        assertFalse(refines);
    }

    @Test
    @Ignore
    public void newQuotientTest4A() {
        // refinement: machine || researcher <= spec \ adm
        Composition lhs = new Composition(getMachine(), getResearcher());
        Quotient rhs = new Quotient(getSpec(), getAdm());
        Refinement refinement = new Refinement(lhs, rhs);

        XMLFileWriter.toXML("./testOutput/specDIVadm.xml", lhs.getAutomaton());
        XMLFileWriter.toXML("./testOutput/comp.xml", rhs.getAutomaton());
        boolean refines = refinement.check();

        assertTrue(refines);
    }

    @Test
    @Ignore
    public void newQuotientTest4AAutomaton() {
        /* This test is similar to "newQuotientTest4A".
         *  But here we create a SimpleTransitionSystem for the Quotient,
         *  As of now this creation results in a long-running time
         *  ultimately leading to a timeout (ignore) of the test. */
        // refinement: machine || researcher <= spec \ adm
        Composition lhs = new Composition(getMachine(), getResearcher());
        // This "SimpleTransitionSystem" creation is problematic.
        TransitionSystem rhs = new SimpleTransitionSystem(new Quotient(getSpec(), getAdm()).getAutomaton());
        Refinement refinement = new Refinement(lhs, rhs);

        boolean refines = refinement.check();

        assertTrue(refines);
    }

    @Test
    public void newQuotientTest4B() {
        // refinement: machine || adm <= spec \ researcher
        Composition lhs = new Composition(getMachine(), getAdm());
        Quotient rhs = new Quotient(getSpec(), getResearcher());
        Refinement refinement = new Refinement(lhs, rhs);

        boolean refines = refinement.check();

        assertTrue(refines);
    }

    @Test
    @Ignore
    public void newQuotientTest4BAutomaton() {
        // refinement: machine || adm <= spec \ researcher
        Composition lhs = new Composition(getMachine(), getAdm());
        Quotient rhs = new Quotient(getSpec(), getResearcher());
        Refinement refinement = new Refinement(lhs, rhs);

        boolean refines = refinement.check();

        assertTrue(refines);
    }

    @Test
    public void newQuotientTest4C() {
        // refinement: researcher || adm <= spec \ machine
        Composition lhs = new Composition(getResearcher(), getAdm());
        Quotient rhs = new Quotient(getSpec(), getMachine());
        Refinement refinement = new Refinement(lhs, rhs);

        boolean refines = refinement.check();

        assertTrue(refines);
    }

    @Test
    @Ignore
    public void newQuotientTest4CAutomaton() {
        // refinement: researcher || adm <= spec \ machine
        Composition lhs = new Composition(getResearcher(), getAdm());
        TransitionSystem rhs = new SimpleTransitionSystem(new Quotient(getSpec(), getMachine()).getAutomaton());
        Refinement refinement = new Refinement(lhs, rhs);

        boolean refines = refinement.check();

        assertTrue(refines);
    }

    @Test
    public void newQuotientTest4D() {
        Composition lhs = new Composition(getResearcher(), getAdm());
        Quotient rhs = new Quotient(getSpec(), getMachine());
        Refinement refinement = new Refinement(lhs, rhs);

        boolean refines = refinement.check();

        assertTrue(refines);
    }

    @Test
    @Ignore
    public void newQuotientTest4DAutomaton() {
        // Refinement: researcher || adm <= spec \ machine
        Composition lhs = new Composition(getResearcher(), getAdm());
        TransitionSystem rhs = new SimpleTransitionSystem(new Quotient(getSpec(), getMachine()).getAutomaton());
        Refinement refinement = new Refinement(lhs, rhs);

        boolean refines = refinement.check();

        assertTrue(refines);
    }

    @Test
    public void simpliversityTest1() {
        // refinement: researcher || adm <= spec
        Composition lhs = new Composition(getSimpleResearcher(), getSimpleAdm());
        TransitionSystem rhs = getSimpleSpec();
        Refinement refinement = new Refinement(lhs, rhs);

        boolean refines = refinement.check();

        assertTrue(refines);
    }

    @Test
    @Ignore
    public void simpliversityTest2() {
        // refinement: researcher <= spec \ adm
        TransitionSystem lhs = getSimpleResearcher();

        TransitionSystem rhs = new SimpleTransitionSystem(new Quotient(getSimpleSpec(), getSimpleAdm()).getAutomaton());
//        TransitionSystem rhs = new Quotient(getSimpleSpec(), getSimpleAdm());
        XMLFileWriter.toXML("testOutput/simpleversityQuotient.xml",rhs.getAutomaton());
        Refinement refinement = new Refinement(lhs, rhs);
        assertTrue(new Refinement(new Composition(getSimpleAdm(),getSimpleResearcher()),getSimpleSpec()).check());
        boolean refines = refinement.check();
        Log.trace(refinement.getErrMsg());

        assertTrue(refines);
    }

    @Test
    @Ignore
    public void newQuotientTest3() {
        // refinement: machine || researcher <= spec \ adm
        Composition lhs = new Composition(getMachine(), getResearcher());
        TransitionSystem rhs = new SimpleTransitionSystem(new Quotient(getSpec(), getAdm()).getAutomaton());
        Refinement refinement = new Refinement(lhs, rhs);

        XMLFileWriter.toXML("./testOutput/admnew.xml", getAdm().getAutomaton());
        XMLFileWriter.toXML("./testOutput/adm2new.xml", getAdm2().getAutomaton());
        boolean refines = refinement.check();

        assertTrue(refines);
    }

    @Test
    public void testHalf2RefinesSelf() {
        // refinement: half2 <= half2
        Refinement refinement = new Refinement(getHalf2(), getHalf2());

        boolean refines = refinement.check();

        assertTrue(refines);
    }

    @Test
    public void testAdmRefinesSelf() {
        // refinement: adm <= adm
        Refinement refinement = new Refinement(getAdm(), getAdm());

        boolean refines = refinement.check();

        assertTrue(refines);
    }

    @Test
    public void testMachineRefinesSelf() {
        // refinement: machine <= machine
        Refinement refinement = new Refinement(getMachine(), getMachine());

        boolean refines = refinement.check();

        assertTrue(refines);
    }

    @Test
    public void testMachineRefinesSelfDuplicate() {
        // refinement: machine <= machine
        TransitionSystem machine = getMachine();
        Refinement refinement = new Refinement(machine, machine);

        refinement.check();
        boolean refines = refinement.check();

        assertFalse(refines);
        assert refinement.getErrMsg().contains("Duplicate process instance");
    }

    @Test
    public void testResRefinesSelf() {
        // refinement: researcher <= researcher
        Refinement refinement = new Refinement(getResearcher(), getResearcher());

        boolean refines = refinement.check();

        assertTrue(refines);
    }

    @Test
    public void testSpecRefinesSelf() {
        // refinement: spec <= spec
        Refinement refinement = new Refinement(getSpec(), getSpec());

        boolean refines = refinement.check();

        assertTrue(refines);
    }

    @Test
    public void testMachine3RefinesSelf() {
        // refinement: machine3 <= machine3
        Refinement refinement = new Refinement(getMachine3(), getMachine3());

        boolean refines = refinement.check();

        assertTrue(refines);
    }

    @Test
    public void testAdmNotRefinesMachine() {
        // refinement: adm <= machine
        Refinement refinement = new Refinement(getAdm(), getMachine());

        boolean refines = refinement.check();

        assertFalse(refines);
    }

    @Test
    public void testAdmNotRefinesResearcher() {
        // refinement: adm <= researcher
        Refinement refinement = new Refinement(getAdm(), getResearcher());

        boolean refines = refinement.check();

        assertFalse(refines);
    }

    @Test
    public void testAdmNotRefinesSpec() {
        // refinement: adm <= spec
        Refinement refinement = new Refinement(getAdm(), getSpec());

        boolean refines = refinement.check();

        assertFalse(refines);
    }

    @Test
    public void testAdmNotRefinesMachine3() {
        // refinement: adm <= machine3
        Refinement refinement = new Refinement(getAdm(), getMachine3());

        boolean refines = refinement.check();

        assertFalse(refines);
    }

    @Test
    public void testMachineNotRefinesAdm() {
        // refinement: machine <= adm
        Refinement refinement = new Refinement(getMachine(), getAdm());

        boolean refines = refinement.check();

        assertFalse(refines);
    }

    @Test
    public void testMachineNotRefinesResearcher() {
        // refinement: machine <= researcher
        Refinement refinement = new Refinement(getMachine(), getResearcher());

        boolean refines = refinement.check();

        assertFalse(refines);
    }

    @Test
    public void testMachineNotRefinesSpec() {
        // refinement: machine <= spec
        Refinement refinement = new Refinement(getMachine(), getSpec());

        boolean refines = refinement.check();

        assertFalse(refines);
    }

    @Test
    public void testMachineNotRefinesMachine3() {
        // refinement: machine <= machine3
        Refinement refinement = new Refinement(getMachine(), getMachine3());

        boolean refines = refinement.check();

        assertFalse(refines);
    }

    @Test
    public void testResNotRefinesAdm() {
        // refinement: researcher <= amd
        Refinement refinement = new Refinement(getResearcher(), getAdm());

        boolean refines = refinement.check();

        assertFalse(refines);
    }

    @Test
    public void testResNotRefinesMachine() {
        // refinement: researcher <= machine
        Refinement refinement = new Refinement(getResearcher(), getMachine());

        boolean refines = refinement.check();

        assertFalse(refines);
    }

    @Test
    public void testResNotRefinesSpec() {
        // refinement: researcher <= spec
        Refinement refinement = new Refinement(getResearcher(), getSpec());

        boolean refines = refinement.check();

        assertFalse(refines);
    }

    @Test
    public void testResNotRefinesMachine3() {
        // refinement: researcher <= machine3
        Refinement refinement = new Refinement(getResearcher(), getMachine3());

        boolean refines = refinement.check();

        assertFalse(refines);
    }

    @Test
    public void testSpecNotRefinesAdm() {
        // refinement: spec <= adm
        Refinement refinement = new Refinement(getSpec(), getAdm());

        boolean refines = refinement.check();

        assertFalse(refines);
    }

    @Test
    public void testSpecNotRefinesMachine() {
        // refinement: spec <= machine
        Refinement refinement = new Refinement(getSpec(), getMachine());

        boolean refines = refinement.check();

        assertFalse(refines);
    }

    @Test
    public void testSpecNotRefinesResearcher() {
        // refinement: spec <= researcher
        Refinement refinement = new Refinement(getSpec(), getResearcher());

        boolean refines = refinement.check();

        assertFalse(refines);
    }

    @Test
    public void testSpecNotRefinesMachine3() {
        // refinement: spec <= machine3
        Refinement refinement = new Refinement(getSpec(), getMachine3());

        boolean refines = refinement.check();

        assertFalse(refines);
    }

    @Test
    public void testMachine3RefinesMachine() {
        // refinement: machine3 <= machine
        Refinement refinement = new Refinement(getMachine3(), getMachine());

        boolean refines = refinement.check();

        assertTrue(refines);
    }

    @Test
    public void testMachine3NotRefinesAdm() {
        // refinement: machine3 <= adm
        Refinement refinement = new Refinement(getMachine3(), getAdm());

        boolean refines = refinement.check();

        assertFalse(refines);
    }

    @Test
    public void testMachine3NotRefinesResearcher() {
        // refinement: machine3 <= researcher
        Refinement refinement = new Refinement(getMachine3(), getResearcher());

        boolean refines = refinement.check();

        assertFalse(refines);
    }

    @Test
    public void testMachine3NotRefinesSpec() {
        // refinement: machine3 <= spec
        Refinement refinement = new Refinement(getMachine3(), getSpec());

        boolean refines = refinement.check();

        assertFalse(refines);
    }

    @Test
    public void testCompRefinesSpec() {
        // refinement: adm || machine || researcher <= spec
        Composition composition = new Composition(getAdm(), getMachine(), getResearcher());
        Refinement ref = new Refinement(composition, getSpec());

        boolean refines = ref.check();

        assertTrue(refines);
    }

    @Test
    public void testCompRefinesSpecOld() {
        // refinement: adm || machine || researcher <= spec
        Composition composition = new Composition(getAdm(), getMachine(), getResearcher());
        Refinement ref = new Refinement(composition, getSpec());

        boolean refines = ref.check();

        assertTrue(refines);
    }

    @Test
    public void testCompOfCompRefinesSpec() {
        // refinement: ((machine || researcher) || adm) <= spec
        Composition composition1 = new Composition(getMachine(), getResearcher());
        Composition composition2 = new Composition(getAdm(), composition1);
        Refinement refinement = new Refinement(composition2, getSpec());

        boolean refines = refinement.check();

        assertTrue(refines);
    }

    @Test
    public void testCompRefinesSelf() {
        // refinement1: (machine || researcher || adm) <= (machine || researcher || adm)
        // refinement2: (machine || researcher || adm) <= (machine || researcher || adm)
        TransitionSystem composition1 = new Composition(getAdm(), getMachine(), getResearcher());
        TransitionSystem composition2 = new Composition(getAdm(), getMachine(), getResearcher());

        Refinement refinement1 = new Refinement(composition1, composition2);
        Refinement refinement2 = new Refinement(composition2, composition1);

        new SimpleTransitionSystem(composition1.getAutomaton()).toXML("testOutput/comp1.xml");
        new SimpleTransitionSystem(composition2.getAutomaton()).toXML("testOutput/comp2.xml");

        boolean refines1 = refinement1.check();
        boolean refines2 = refinement2.check();

        assertTrue(refines1);
        assertTrue(refines2);
    }

    @Test
    public void testStored() {
        // refinement1: test <= test1
        // refinement2: test1 <= test
        Refinement refinement1 = new Refinement(getTest(), getTest1());
        Refinement refinement2 = new Refinement(getTest1(), getTest());

        boolean refines1 = refinement1.check();
        boolean refines2 = refinement2.check();

        assertTrue(refines1);
        assertTrue(refines2);
    }

    @Test
    public void testCompRefinesSelfDuplicate() {
        TransitionSystem adm = getAdm();
        TransitionSystem machine = getMachine();
        TransitionSystem researcher = getResearcher();

        Refinement ref = new Refinement(new Composition(adm, machine, researcher), new Composition(machine, researcher, adm));

        assertFalse(ref.check(true));
        assert ref.getErrMsg().contains("Duplicate process instance");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUncomposable() {
        Composition composition = new Composition(getMachine(), getMachine3());

        new Refinement(composition, getMachine());
    }

    @Test
    public void testHalf1AndHalf2RefinesAdm2() {
        Conjunction conjunction = new Conjunction(getHalf1(), getHalf2());
        Refinement refinement = new Refinement(conjunction, getAdm2());

        boolean refines = refinement.check();

        assertTrue(refines);
    }

    @Test
    public void testAdm2RefinesHalf1AndHalf2() {
        Conjunction conjunction = new Conjunction(getHalf1(), getHalf2());
        Refinement refinement = new Refinement(getAdm2(), conjunction);

        boolean refines = refinement.check();

        assertTrue(refines);
    }
}