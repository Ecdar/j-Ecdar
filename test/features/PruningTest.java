package features;


import log.Log;
import logic.JsonAutomatonEncoder;
import logic.Pruning;
import logic.Refinement;
import logic.SimpleTransitionSystem;
import models.Automaton;
import models.CDD;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import parser.XMLFileWriter;
import parser.XMLParser;

import static org.junit.Assert.assertTrue;

public class PruningTest {
    private static SimpleTransitionSystem compTimedReach, compTimedInc, compTimedInc1, compTimedInc2, compTimedInc3, compTimedInc4;
    private static SimpleTransitionSystem selfloopZeno, expectedOutputSelfloopZeno, selfloopNonZeno, expectedOutputSelfloopNonZeno, simple1, expectedOutputSimple1, simple2, expectedOutputSimple2, simple3, expectedOutputSimple3, simple3Invar, expectedOutputSimple3Invar, simple4, expectedOutputSimple4 , simple4inpComp, expectedOutputSimple4inpComp;

    @After
    public void afterEachTest(){
        CDD.done();
    }

    @BeforeClass
    public static void setUpBeforeClass() {


        //Automaton[] aut1 = XMLParser.parse("samples/xml/timedReach.xml", false);
        //compTimedReach = new SimpleTransitionSystem(aut1[0]);

        Automaton[] aut2 = XMLParser.parse("samples/xml/timedInconsistency.xml", false);
        compTimedInc = new SimpleTransitionSystem(aut2[0]);
        compTimedInc1 = new SimpleTransitionSystem(aut2[1]);
        compTimedInc2 = new SimpleTransitionSystem(aut2[2]);
        compTimedInc3 = new SimpleTransitionSystem(aut2[3]);
        compTimedInc4 = new SimpleTransitionSystem(aut2[4]);

        Automaton[] aut3 = XMLParser.parse("samples/xml/quotient/QuotientTestOutputs.xml", false);
        selfloopZeno = new SimpleTransitionSystem(aut3[0]);
        expectedOutputSelfloopZeno = new SimpleTransitionSystem(aut3[1]);
        selfloopNonZeno = new SimpleTransitionSystem(aut3[2]);
        expectedOutputSelfloopNonZeno = new SimpleTransitionSystem(aut3[3]);
        simple1 = new SimpleTransitionSystem(aut3[4]);
        expectedOutputSimple1 = new SimpleTransitionSystem(aut3[5]);
        simple2 = new SimpleTransitionSystem(aut3[6]);
        expectedOutputSimple2 = new SimpleTransitionSystem(aut3[7]);
        simple3 = new SimpleTransitionSystem(aut3[8]);
        expectedOutputSimple3 = new SimpleTransitionSystem(aut3[9]);
        simple4 = new SimpleTransitionSystem(aut3[10]);
        expectedOutputSimple4 = new SimpleTransitionSystem(aut3[11]);
        simple4inpComp = new SimpleTransitionSystem(aut3[12]);
        expectedOutputSimple4inpComp = new SimpleTransitionSystem(aut3[13]);
        simple3Invar = new SimpleTransitionSystem(aut3[14]);
        expectedOutputSimple3Invar = new SimpleTransitionSystem(aut3[15]);











    }

    @Test
    public void SelfloopZenoPruning() {

        SimpleTransitionSystem pruned = Pruning.adversarialPruning(selfloopZeno);
        pruned.toXML("testOutput/selfloopZeno.xml");
        JsonAutomatonEncoder.writeToJson(pruned.getAutomaton(),"./testjsonoutput/p1");
        SimpleTransitionSystem exp = expectedOutputSelfloopZeno;
        Log.trace("Ref1: " + new Refinement(pruned, exp).check());
        Log.trace("Ref2: " +  new Refinement(exp, pruned).check());
        assertTrue(new Refinement(pruned, exp).check()  &&  new Refinement(exp, pruned).check() ) ;

    }
    @Test
    public void SelfloopNonZenoPruning() {

        SimpleTransitionSystem pruned = Pruning.adversarialPruning(selfloopNonZeno);
        pruned.toXML("testOutput/selfloopNonZeno.xml");
        SimpleTransitionSystem exp = expectedOutputSelfloopNonZeno;

        Log.trace("finished pruning");
        assertTrue(new Refinement(pruned, exp).check()  &&  new Refinement(exp, pruned).check() ) ;

    }

    @Test
    public void SelfloopSimple1() {

        SimpleTransitionSystem pruned = Pruning.adversarialPruning(simple1);
        pruned.toXML("testOutput/simple1.xml");
        SimpleTransitionSystem exp = expectedOutputSimple1;

        assertTrue(new Refinement(pruned, exp).check()  &&  new Refinement(exp, pruned).check() ) ;

    }

    @Test
    public void SelfloopSimple2() {

        SimpleTransitionSystem pruned = Pruning.adversarialPruning(simple2);
        pruned.toXML("testOutput/simple2.xml");
        SimpleTransitionSystem exp = expectedOutputSimple2;

        assertTrue(new Refinement(pruned, exp).check()  &&  new Refinement(exp, pruned).check() ) ;

    }

    @Test
    public void SelfloopSimple3() {

        SimpleTransitionSystem pruned = Pruning.adversarialPruning(simple3);
        pruned.toXML("testOutput/simple3.xml");
        SimpleTransitionSystem exp = expectedOutputSimple3;

        assertTrue(new Refinement(pruned, exp).check()  &&  new Refinement(exp, pruned).check() ) ;

    }


    @Test
    public void SelfloopSimple3Invar() {

        SimpleTransitionSystem pruned = Pruning.adversarialPruning(simple3Invar);
        pruned.toXML("testOutput/simple3.xml");
        SimpleTransitionSystem exp = expectedOutputSimple3Invar;

        assertTrue(new Refinement(pruned, exp).check()  &&  new Refinement(exp, pruned).check() ) ;

    }

    @Test
    public void SelfloopSimple3RemovedDiagonal() {

        SimpleTransitionSystem pruned = new SimpleTransitionSystem(XMLParser.parse("samples/xml/quotient/simple3NoDiagonal.xml", false)[0]);
        SimpleTransitionSystem exp = expectedOutputSimple3;

        assertTrue(new Refinement(pruned, exp).check()  &&  new Refinement(exp, pruned).check() ) ;

    }

    @Test
    public void pruningWithOrTest() {

        SimpleTransitionSystem orig = new SimpleTransitionSystem(XMLParser.parse("samples/xml/quotient/pruningWithOr.xml", false)[0]);
        SimpleTransitionSystem pruned = Pruning.adversarialPruning(orig);
        SimpleTransitionSystem exp = new SimpleTransitionSystem(XMLParser.parse("samples/xml/quotient/pruningWithOr.xml", false)[1]);
        XMLFileWriter.toXML("testOutput/pruningWithOrAfterPruning.xml",pruned);
        Refinement ref1 = new Refinement(pruned, exp);
        boolean res1= ref1.check();
        Log.trace(ref1.getErrMsg());
        assertTrue(res1);
        Refinement ref2 = new Refinement(exp, pruned);
        boolean res2= ref2.check();
        Log.trace(ref2.getErrMsg());
        assertTrue(res2);


    }

    @Test
    public void pruningWithOrTest1() {

        SimpleTransitionSystem orig1 = new SimpleTransitionSystem(XMLParser.parse("samples/xml/quotient/pruningWithOr.xml", false)[2]);
        SimpleTransitionSystem pruned1 = Pruning.adversarialPruning(orig1);
        SimpleTransitionSystem exp1 = new SimpleTransitionSystem(XMLParser.parse("samples/xml/quotient/pruningWithOr.xml", false)[3]);
        XMLFileWriter.toXML("testOutput/pruningWithOrAfterPruning1.xml",pruned1);
        assertTrue(new Refinement(pruned1, exp1).check()  &&  new Refinement(exp1, pruned1).check() ) ;

    }



    @Test
    public void pruningWithOrTest2() {

        SimpleTransitionSystem orig1 = new SimpleTransitionSystem(XMLParser.parse("samples/xml/quotient/pruningWithOr.xml", false)[4]);
        SimpleTransitionSystem pruned1 = Pruning.adversarialPruning(orig1);
        SimpleTransitionSystem exp1 = new SimpleTransitionSystem(XMLParser.parse("samples/xml/quotient/pruningWithOr.xml", false)[5]);
        XMLFileWriter.toXML("testOutput/pruningWithOrAfterPruning2.xml",pruned1);
        assertTrue(new Refinement(pruned1, exp1).check()  &&  new Refinement(exp1, pruned1).check() ) ;


    }

    @Test
    public void pruningWithOrTest3() {

        SimpleTransitionSystem orig1 = new SimpleTransitionSystem(XMLParser.parse("samples/xml/quotient/pruningWithOr.xml", false)[6]);
        SimpleTransitionSystem pruned1 = Pruning.adversarialPruning(orig1);
        SimpleTransitionSystem exp1 = new SimpleTransitionSystem(XMLParser.parse("samples/xml/quotient/pruningWithOr.xml", false)[7]);
        XMLFileWriter.toXML("testOutput/pruningWithOrAfterPruning3.xml",pruned1);
        assertTrue(new Refinement(pruned1, exp1).check()  &&  new Refinement(exp1, pruned1).check() ) ;


    }


    @Test
    public void pruningWithOrTest4() {

        SimpleTransitionSystem orig1 = new SimpleTransitionSystem(XMLParser.parse("samples/xml/quotient/pruningWithOr.xml", false)[8]);
        SimpleTransitionSystem pruned1 = Pruning.adversarialPruning(orig1);
        SimpleTransitionSystem exp1 = new SimpleTransitionSystem(XMLParser.parse("samples/xml/quotient/pruningWithOr.xml", false)[9]);
        XMLFileWriter.toXML("testOutput/pruningWithOrAfterPruning4.xml",pruned1);
        assertTrue(new Refinement(pruned1, exp1).check()  &&  new Refinement(exp1, pruned1).check() ) ;


    }

    @Test
    public void SelfloopSimple4() {

        SimpleTransitionSystem pruned = Pruning.adversarialPruning(simple4);
        pruned.toXML("testOutput/simple4.xml");
        SimpleTransitionSystem exp = expectedOutputSimple4;

        assertTrue(new Refinement(pruned, exp).check()  &&  new Refinement(exp, pruned).check() ) ;

    }


    @Test
    public void SelfloopSimple4InpComp() {

        SimpleTransitionSystem pruned = Pruning.adversarialPruning(simple4inpComp);
        pruned.toXML("testOutput/simple4inpComp.xml");
        SimpleTransitionSystem exp = expectedOutputSimple4inpComp;

        assertTrue(new Refinement(pruned, exp).check()  &&  new Refinement(exp, pruned).check() ) ;

    }

/*
    @Test
    public void TestReachabilityPruning() {
        Log.trace("calculating reach pruning");

        SimpleTransitionSystem outPrunedReach1 = compTimedReach.pruneReachTimed();
        outPrunedReach1.toXML("testOutput/compTimedReach.xml");



        assertTrue(outPrunedReach1.getAutomaton().getLocations().size() == 2);
    }

    */
    /*
    @Test
    public void TestInconsistencyPruning() {

        SimpleTransitionSystem outPrunedReach1 = compTimedInc.pruneIncTimed();
        outPrunedReach1.toXML("testOutput/compTimedInc.xml");

        assertTrue(outPrunedReach1.getAutomaton().getLocations().size() == 3);
    }
    @Test
    public void TestInconsistencyPruning1() {

        SimpleTransitionSystem outPrunedReach1 = compTimedInc1.pruneIncTimed();
        outPrunedReach1.toXML("testOutput/compTimedInc1.xml");

        assertTrue(outPrunedReach1.getAutomaton().getLocations().size() == 1);
    }
    @Test
    public void TestInconsistencyPruning2() {

        SimpleTransitionSystem outPrunedReach1 = compTimedInc2.pruneIncTimed();
        outPrunedReach1.toXML("testOutput/compTimedInc2.xml");

        assertTrue(outPrunedReach1.getAutomaton().getLocations().size() == 3);
    }
    @Test
    public void TestInconsistencyPruning3() {

        SimpleTransitionSystem outPrunedReach1 = compTimedInc3.pruneIncTimed();
        outPrunedReach1.toXML("testOutput/compTimedInc3.xml");

        assertTrue(outPrunedReach1.getAutomaton().getLocations().size() == 3);
    }
    @Test
    public void TestInconsistencyPruning4() {

        SimpleTransitionSystem outPrunedReach1 = compTimedInc4.pruneIncTimed();
        outPrunedReach1.toXML("testOutput/compTimedInc4.xml");

        assertTrue(outPrunedReach1.getAutomaton().getLocations().size() == 2);
    }
    */

}
