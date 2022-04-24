package features;


import logic.Pruning;
import logic.Refinement;
import logic.SimpleTransitionSystem;
import models.Automaton;
import models.CDD;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import parser.JsonFileWriter;
import parser.XMLParser;

import static org.junit.Assert.assertTrue;

public class PruningTest {
    private static SimpleTransitionSystem compTimedReach, compTimedInc, compTimedInc1, compTimedInc2, compTimedInc3, compTimedInc4;
    private static SimpleTransitionSystem selfloopZeno, expectedOutputSelfloopZeno, selfloopNonZeno, expectedOutputSelfloopNonZeno, simple1, expectedOutputSimple1, simple2, expectedOutputSimple2, simple3, expectedOutputSimple3, simple4, expectedOutputSimple4 , simple4inpComp, expectedOutputSimple4inpComp;

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











    }

    @Test
    public void SelfloopZenoPruning() {

        SimpleTransitionSystem pruned = Pruning.pruneIncTimed(selfloopZeno);
        pruned.toXML("selfloopZeno.xml");
        JsonFileWriter.writeToJson(pruned.getAutomaton(),"C:/tools/j-Ecdar-master/j-Ecdar-master/testjsonoutput/p1");
        SimpleTransitionSystem exp = expectedOutputSelfloopZeno;
        System.out.println("Ref1: " + new Refinement(pruned, exp).check());
        System.out.println("Ref2: " +  new Refinement(exp, pruned).check());
        assertTrue(new Refinement(pruned, exp).check()  &&  new Refinement(exp, pruned).check() ) ;

    }
    @Test
    public void SelfloopNonZenoPruning() {

        SimpleTransitionSystem pruned = Pruning.pruneIncTimed(selfloopNonZeno);
        pruned.toXML("selfloopNonZeno.xml");
        SimpleTransitionSystem exp = expectedOutputSelfloopNonZeno;

        System.out.println("finished pruning");
        assertTrue(new Refinement(pruned, exp).check()  &&  new Refinement(exp, pruned).check() ) ;

    }

    @Test
    public void SelfloopSimple1() {

        SimpleTransitionSystem pruned = Pruning.pruneIncTimed(simple1);
        pruned.toXML("simple1.xml");
        SimpleTransitionSystem exp = expectedOutputSimple1;

        assertTrue(new Refinement(pruned, exp).check()  &&  new Refinement(exp, pruned).check() ) ;

    }

    @Test
    public void SelfloopSimple2() {

        SimpleTransitionSystem pruned = Pruning.pruneIncTimed(simple2);
        pruned.toXML("simple2.xml");
        SimpleTransitionSystem exp = expectedOutputSimple2;

        assertTrue(new Refinement(pruned, exp).check()  &&  new Refinement(exp, pruned).check() ) ;

    }

    @Test
    public void SelfloopSimple3() {

        SimpleTransitionSystem pruned = Pruning.pruneIncTimed(simple3);
        pruned.toXML("simple3.xml");
        SimpleTransitionSystem exp = expectedOutputSimple3;

        assertTrue(new Refinement(pruned, exp).check()  &&  new Refinement(exp, pruned).check() ) ;

    }

    @Test
    public void SelfloopSimple4() {

        SimpleTransitionSystem pruned = Pruning.pruneIncTimed(simple4);
        pruned.toXML("simple4.xml");
        SimpleTransitionSystem exp = expectedOutputSimple4;

        assertTrue(new Refinement(pruned, exp).check()  &&  new Refinement(exp, pruned).check() ) ;

    }


    @Test
    public void SelfloopSimple4InpComp() {

        SimpleTransitionSystem pruned = Pruning.pruneIncTimed(simple4inpComp);
        pruned.toXML("simple4inpComp.xml");
        SimpleTransitionSystem exp = expectedOutputSimple4inpComp;

        assertTrue(new Refinement(pruned, exp).check()  &&  new Refinement(exp, pruned).check() ) ;

    }

/*
    @Test
    public void TestReachabilityPruning() {
        System.out.println("calculating reach pruning");

        SimpleTransitionSystem outPrunedReach1 = compTimedReach.pruneReachTimed();
        outPrunedReach1.toXML("compTimedReach.xml");



        assertTrue(outPrunedReach1.getAutomaton().getLocations().size() == 2);
    }

    */
    /*
    @Test
    public void TestInconsistencyPruning() {

        SimpleTransitionSystem outPrunedReach1 = compTimedInc.pruneIncTimed();
        outPrunedReach1.toXML("compTimedInc.xml");

        assertTrue(outPrunedReach1.getAutomaton().getLocations().size() == 3);
    }
    @Test
    public void TestInconsistencyPruning1() {

        SimpleTransitionSystem outPrunedReach1 = compTimedInc1.pruneIncTimed();
        outPrunedReach1.toXML("compTimedInc1.xml");

        assertTrue(outPrunedReach1.getAutomaton().getLocations().size() == 1);
    }
    @Test
    public void TestInconsistencyPruning2() {

        SimpleTransitionSystem outPrunedReach1 = compTimedInc2.pruneIncTimed();
        outPrunedReach1.toXML("compTimedInc2.xml");

        assertTrue(outPrunedReach1.getAutomaton().getLocations().size() == 3);
    }
    @Test
    public void TestInconsistencyPruning3() {

        SimpleTransitionSystem outPrunedReach1 = compTimedInc3.pruneIncTimed();
        outPrunedReach1.toXML("compTimedInc3.xml");

        assertTrue(outPrunedReach1.getAutomaton().getLocations().size() == 3);
    }
    @Test
    public void TestInconsistencyPruning4() {

        SimpleTransitionSystem outPrunedReach1 = compTimedInc4.pruneIncTimed();
        outPrunedReach1.toXML("compTimedInc4.xml");

        assertTrue(outPrunedReach1.getAutomaton().getLocations().size() == 2);
    }
    */

}
