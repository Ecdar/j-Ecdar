package features;

import log.Log;
import logic.*;
import models.Automaton;
import models.CDD;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import parser.XMLParser;

import static org.junit.Assert.assertTrue;

public class QuotientTest {

    private static SimpleTransitionSystem comp0,  comp1, comp2, spec01, test1Comp0, test1Comp1, test1Spec,  test2Spec, outputTest,outputTest1;

    @After
    public void afterEachTest(){
        CDD.done();
    }

    @BeforeClass
    public static void setUpBeforeClass() {
        Automaton[] aut = XMLParser.parse("samples/xml/quotient/example_critical_sections_final_versions_pruned-untimed.xml", false);
        comp0 = new SimpleTransitionSystem(aut[0]);
        comp1 = new SimpleTransitionSystem(aut[1]);
        comp2 = new SimpleTransitionSystem(aut[2]);
        spec01 = new SimpleTransitionSystem(aut[4]);
        Automaton[] aut2 = XMLParser.parse("samples/xml/quotient/SimpleTimedQuotientTest03.xml", true);
        test1Comp0 = new SimpleTransitionSystem((aut2[0]));
        test1Comp1 = new SimpleTransitionSystem((aut2[1]));
        test1Spec = new SimpleTransitionSystem((aut2[2]));
        Automaton[] aut3= XMLParser.parse("samples/xml/quotient/SimpleTimedQuotientTest02.xml", false);
        test2Spec = new SimpleTransitionSystem(aut3[0]);
        Automaton[] aut4= XMLParser.parse("samples/xml/quotient/QuotientTestOutputs.xml", false);
        outputTest = new SimpleTransitionSystem(aut4[3]);
        outputTest1 = new SimpleTransitionSystem(aut4[2]);
    }

/*
    @Test
    public void SimpleTimedQuotientTest2() {



        SimpleTransitionSystem outPruned = test2Spec.pruneIncTimed();
        outPruned.toXML("Test2SimpleTimedQuotient-pruned-inc.xml");
        SimpleTransitionSystem outPrunedReach = outPruned.pruneReachTimed();
        outPrunedReach.toXML("Test2SimpleTimedQuotient-pruned.xml");


        assertTrue(new Refinement(new Conjunction(new TransitionSystem[]{test1Comp0, test1Comp1}), test1Spec).check() == new Refinement(test1Comp1, new Quotient(test1Spec, test1Comp0)).check());
    }
*/


    @Test
    public void SimpleTimedQuotientTest() {

        test1Comp0.toXML("testOutput/test1Comp0Completed.xml");
        test1Spec.toXML("testOutput/test1SpecCompleted.xml");

        Quotient quo = new Quotient(test1Spec,test1Comp0);
        SimpleTransitionSystem out = quo.calculateQuotientAutomaton();
        out.toXML("testOutput/SimpleTimedQuotient.xml");

        SimpleTransitionSystem outPruned = Pruning.adversarialPruning(out);
        outPruned.toXML("testOutput/SimpleTimedQuotient-pruned-inc.xml");
        Log.trace("Done pruning inc.");
        SimpleTransitionSystem outPrunedReach = outPruned.pruneReachTimed();
        outPrunedReach.toXML("testOutput/SimpleTimedQuotient-pruned.xml");


        assertTrue(new Refinement(new Conjunction(new TransitionSystem[]{test1Comp0, test1Comp1}), test1Spec).check() == new Refinement(test1Comp1, new Quotient(test1Spec, test1Comp0)).check());
    }



    @Test
    public void QuotientRefinement() {
           assertTrue(new Refinement(new Conjunction(new TransitionSystem[]{comp1, comp2}), spec01).check() == new Refinement(comp2, new Quotient(spec01, comp1)).check());
    }



    @Test
    public void QuotientSpec01Comp1() {
        Quotient quo = new Quotient(spec01,comp0);
        SimpleTransitionSystem out = quo.calculateQuotientAutomaton();
        out.toXML("testOutput/quotient1-disj.xml");

        Log.trace("Built Quotient 1");
        SimpleTransitionSystem outPruned = Pruning.adversarialPruning(out);
        Log.trace("Pruned inconsistency");
        SimpleTransitionSystem outPrunedReach = outPruned.pruneReachTimed();
        Log.trace("Pruned reachability");
        outPrunedReach.toXML("testOutput/quotient1Pruned-disj.xml");


        Quotient quo1 = new Quotient(outPrunedReach,comp1);
        SimpleTransitionSystem out1 = quo1.calculateQuotientAutomaton();
        out1.toXML("testOutput/quotient2-disj.xml");




        Log.trace("Built Quotient 2 ****************************************** ");
        SimpleTransitionSystem outPruned1 = Pruning.adversarialPruning(out1);
        Log.trace("Pruned inconsistency");

        SimpleTransitionSystem outPrunedReach1 = outPruned1.pruneReachTimed();
        outPrunedReach1.toXML("testOutput/quotient2Pruned-disj.xml");
        Log.trace("Pruned reachability");



        Quotient quo2 = new Quotient(outPrunedReach1,comp2);
        SimpleTransitionSystem out2 = quo2.calculateQuotientAutomaton();
        Log.trace("Built Quotient 3");
        out2.toXML("testOutput/quotient3-disj.xml");
        SimpleTransitionSystem outPruned2 = Pruning.adversarialPruning(out2);
        Log.trace("Pruned inconsistency");
        SimpleTransitionSystem outPrunedReach2 = outPruned2.pruneReachTimed();
        outPrunedReach2.toXML("testOutput/quotient3Pruned-disj.xml");
        Log.trace("Pruned reachability");


        Quotient quotient = new Quotient(spec01,comp2);
        SimpleTransitionSystem output = quotient.calculateQuotientAutomaton();
        output.toXML("testOutput/quotient-spec01-comp2-disj.xml");

        SimpleTransitionSystem outputPruned = Pruning.adversarialPruning(output);
        SimpleTransitionSystem outputPrunedReach = outputPruned.pruneReachTimed();
        outputPrunedReach.toXML("testOutput/quotient-spec01-comp2-pruned-disj.xml");



        assertTrue(true);
    }

    @Test
    public void OutputTest() {

        SimpleTransitionSystem outPruned = Pruning.adversarialPruning(outputTest);
        Log.trace("Pruned inconsistency");
        //SimpleTransitionSystem outPrunedReach = outPruned.pruneReachTimed();
       // Log.trace("Pruned reachability");
        outPruned.toXML("testOutput/outputtest-pruned-new1.xml");
        assertTrue(true);
    }

    @Test
    public void OutputTest1() {
        SimpleTransitionSystem outPruned = Pruning.adversarialPruning(outputTest1);
        Log.trace("Pruned inconsistency");
        //SimpleTransitionSystem outPrunedReach = outPruned.pruneReachTimed();
        // Log.trace("Pruned reachability");
        outPruned.toXML("testOutput/outputtest1-pruned.xml");
        assertTrue(true);
    }

}
