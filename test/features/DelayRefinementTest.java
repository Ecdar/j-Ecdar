package features;

import logic.Composition;
import logic.Refinement;
import logic.SimpleTransitionSystem;
import logic.TransitionSystem;
import models.Automaton;
import models.CDD;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import parser.XMLFileWriter;
import parser.XMLParser;

import java.util.ArrayList;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

public class DelayRefinementTest {

    private static Automaton[] automata;


    @After
    public void afterEachTest(){
        CDD.done();
    }
//Class

    @Before
    public  void setUpBeforeClass() {
        automata = XMLParser.parse("./samples/xml/delayRefinement.xml", true);
    }

    // Self Refinements
    @Test
    public void T1RefinesSelf() {

        Automaton automaton2 = new Automaton(automata[0]);
        CDD.init(100,100,100);
        CDD.addClocks(automata[0].getClocks(),automaton2.getClocks());
        assertTrue(new Refinement(new SimpleTransitionSystem(CDD.makeInputEnabled(automata[0])), new SimpleTransitionSystem(CDD.makeInputEnabled(automaton2))).check());
    }

    @Test
    public void T2RefinesSelf() {
        Automaton automaton2 = new Automaton(automata[1]);
        CDD.init(10000,10000,10000);
        CDD.addClocks(automata[1].getClocks(),automaton2.getClocks() );
        assertTrue(new Refinement(new SimpleTransitionSystem(automata[1]), new SimpleTransitionSystem(automaton2)).check());
    }

    @Test
    public void T3RefinesSelf() {

        Automaton automaton2 = new Automaton(automata[2]);
        CDD.init(10000,10000,10000);
        CDD.addClocks(automata[2].getClocks(),automaton2.getClocks() );
        System.out.println(CDD.numClocks);
        assertTrue(new Refinement(new SimpleTransitionSystem(automata[2]), new SimpleTransitionSystem(automaton2)).check());
    }

    @Test
    public void C1RefinesSelf() {

        Automaton automata2 = new Automaton(automata[3]);
        CDD.init(100,100,100);
        CDD.addClocks(automata[3].getClocks(),automata2.getClocks());
        assertTrue(new Refinement(new SimpleTransitionSystem(CDD.makeInputEnabled(automata[3])), new SimpleTransitionSystem((CDD.makeInputEnabled(automata2)))).check());
    }

    @Test
    public void C2RefinesSelf() {

        Automaton automata2 = new Automaton(automata[4]);
        CDD.init(100,100,100);
        CDD.addClocks(automata[4].getClocks(),automata2.getClocks());
        assertTrue(new Refinement(new SimpleTransitionSystem(automata[4]), new SimpleTransitionSystem(automata2)).check());
    }

    @Test
    public void F1RefinesSelf() {

        Automaton automata2 = new Automaton(automata[7]);
        //assert(automata2.equals(automata[7]));
        CDD.init(100,100,100);
        CDD.addClocks(automata[7].getClocks(),automata2.getClocks());
        assertTrue(new Refinement(new SimpleTransitionSystem(automata[7]), new SimpleTransitionSystem(automata2)).check());
    }

    @Test
    public void F2RefinesSelf() {
        Automaton automata2 = new Automaton(automata[8]);

        CDD.init(100,100,100);
        CDD.addClocks(automata[8].getClocks(),automata2.getClocks());
        assertTrue(new Refinement(new SimpleTransitionSystem(CDD.makeInputEnabled(automata[8])), new SimpleTransitionSystem(CDD.makeInputEnabled(automata2))).check());
    }

    @Test
    public void F3RefinesSelf() {
        Automaton automata2 = new Automaton(automata[9]);

        CDD.init(100,100,100);
        CDD.addClocks(automata[9].getClocks(),automata2.getClocks());
        assertTrue(new Refinement(new SimpleTransitionSystem(CDD.makeInputEnabled(automata[9])), new SimpleTransitionSystem(CDD.makeInputEnabled(automata2))).check());
    }

    @Test
    public void T4RefinesSelf() {

        Automaton automata2 = new Automaton(automata[10]);
        CDD.init(100,100,100);
        CDD.addClocks(automata[10].getClocks(),automata2.getClocks());
        assertTrue(new Refinement(new SimpleTransitionSystem(CDD.makeInputEnabled(automata[10])), new SimpleTransitionSystem(CDD.makeInputEnabled(automata2))).check());
    }

    @Test
    public void T0RefinesSelf() {

        CDD.init(100,100,100);
        CDD.addClocks(automata[11].getClocks(),automata[11].getClocks());
        assertTrue(new Refinement(new SimpleTransitionSystem(automata[11]), new SimpleTransitionSystem(new Automaton(automata[11]))).check());
    }

    @Test
    public void T5RefinesSelf() {

        Automaton automata2 = new Automaton(automata[12]);
        CDD.init(100,100,100);
        CDD.addClocks(automata[12].getClocks(),automata2.getClocks());
        assertTrue(new Refinement(new SimpleTransitionSystem(CDD.makeInputEnabled(automata[12])), new SimpleTransitionSystem(CDD.makeInputEnabled(automata2))).check());
    }

    @Test
    public void T6RefinesSelf() {

        Automaton automata2 = new Automaton(automata[13]);
        CDD.init(100,100,100);
        CDD.addClocks(automata[13].getClocks(),automata2.getClocks());
        assertTrue(new Refinement(new SimpleTransitionSystem(CDD.makeInputEnabled(automata[13])), new SimpleTransitionSystem(CDD.makeInputEnabled(automata2))).check());
    }

    @Test
    public void T7RefinesSelf() {
        CDD.init(100,100,100);
        CDD.addClocks(automata[14].getClocks(),automata[14].getClocks());
        assertTrue(new Refinement(new SimpleTransitionSystem(automata[14]), new SimpleTransitionSystem(new Automaton(automata[14]))).check());
    }

    @Test
    public void T8RefinesSelf() {
        Automaton automata2 = new Automaton(automata[15]);

        CDD.init(100,100,100);
        CDD.addClocks(automata[15].getClocks(),automata2.getClocks());
        assertTrue(new Refinement(new SimpleTransitionSystem(CDD.makeInputEnabled(automata[15])), new SimpleTransitionSystem(CDD.makeInputEnabled(automata2))).check());
    }

    @Test
    public void T9RefinesSelf() {
        CDD.init(100,100,100);
        CDD.addClocks(automata[16].getClocks(),automata[16].getClocks());
        assertTrue(new Refinement(new SimpleTransitionSystem(automata[16]), new SimpleTransitionSystem(new Automaton(automata[16]))).check());
    }

    @Test
    public void T10RefinesSelf() {
        Automaton automata2 = new Automaton(automata[17]);
        CDD.init(100,100,100);
        CDD.addClocks(automata[17].getClocks(),automata2.getClocks());
        assertTrue(new Refinement(new SimpleTransitionSystem(CDD.makeInputEnabled(automata[17])), new SimpleTransitionSystem(CDD.makeInputEnabled(automata2))).check());
    }

    @Test
    public void T11RefinesSelf() {
        Automaton automata2 = new Automaton(automata[18]);
        CDD.init(100,100,100);
        CDD.addClocks(automata[18].getClocks(),automata2.getClocks());
        assertTrue(new Refinement(new SimpleTransitionSystem(CDD.makeInputEnabled(automata[18])), new SimpleTransitionSystem(CDD.makeInputEnabled(automata2))).check());
    }

    @Test
    public void N1RefinesSelf() {
        CDD.init(100,100,100);
        CDD.addClocks(automata[19].getClocks(),automata[19].getClocks());
        assertTrue(new Refinement(new SimpleTransitionSystem(automata[19]), new SimpleTransitionSystem(new Automaton(automata[19]))).check());
    }

    @Test
    public void N2RefinesSelf() {
        CDD.init(100,100,100);
        CDD.addClocks(automata[20].getClocks(),automata[20].getClocks());
        assertTrue(new Refinement(new SimpleTransitionSystem(automata[20]), new SimpleTransitionSystem(new Automaton(automata[20]))).check());
    }

    @Test
    public void N3RefinesSelf() {
        CDD.init(100,100,100);
        CDD.addClocks(automata[21].getClocks(),automata[21].getClocks());
        assertTrue(new Refinement(new SimpleTransitionSystem(automata[21]), new SimpleTransitionSystem(new Automaton(automata[21]))).check());
    }

    @Test
    public void N4RefinesSelf() {
        CDD.init(100,100,100);
        CDD.addClocks(automata[22].getClocks(),automata[22].getClocks());
        assertTrue(new Refinement(new SimpleTransitionSystem(automata[22]), new SimpleTransitionSystem(new Automaton(automata[22]))).check());
    }

    @Test
    public void D1RefinesSelf() {
        CDD.init(100,100,100);
        CDD.addClocks(automata[23].getClocks(),automata[23].getClocks());
        assertTrue(new Refinement(new SimpleTransitionSystem(automata[23]), new SimpleTransitionSystem(new Automaton(automata[23]))).check());
    }

    @Test
    public void D2RefinesSelf() {
        CDD.init(100,100,100);
        CDD.addClocks(automata[24].getClocks(),automata[24].getClocks());
        assertTrue(new Refinement(new SimpleTransitionSystem(automata[24]), new SimpleTransitionSystem(new Automaton(automata[24]))).check());
    }

    @Test
    public void K1RefinesSelf() {
        CDD.init(100,100,100);
        CDD.addClocks(automata[25].getClocks(),automata[25].getClocks());
        assertTrue(new Refinement(new SimpleTransitionSystem(automata[25]), new SimpleTransitionSystem(new Automaton(automata[25]))).check());
    }

    @Test
    public void K2RefinesSelf() {

        Automaton automata2 = new Automaton(automata[26]);
        CDD.init(100,100,100);
        CDD.addClocks(automata[26].getClocks(),automata2.getClocks());
        assertTrue(new Refinement(new SimpleTransitionSystem(automata[26]), new SimpleTransitionSystem(CDD.makeInputEnabled(automata2))).check());
    }

    @Test
    public void K3RefinesSelf() {

        Automaton automata2 = new Automaton(automata[27]);
        CDD.init(100,100,100);
        CDD.addClocks(automata[27].getClocks(),automata2.getClocks());
        assertTrue(new Refinement(new SimpleTransitionSystem(CDD.makeInputEnabled(automata[27])), new SimpleTransitionSystem(CDD.makeInputEnabled(automata2))).check());
    }

    @Test
    public void K4RefinesSelf() {
        CDD.init(100,100,100);
        CDD.addClocks(automata[28].getClocks(),automata[28].getClocks());
        assertTrue(new Refinement(new SimpleTransitionSystem(automata[28]), new SimpleTransitionSystem(new Automaton(automata[28]))).check());
    }

    @Test
    public void K5RefinesSelf() {

        Automaton automata2 = new Automaton(automata[29]);
        CDD.init(100,100,100);
        CDD.addClocks(automata[29].getClocks(),automata2.getClocks());
        assertTrue(new Refinement(new SimpleTransitionSystem(CDD.makeInputEnabled(automata[29])), new SimpleTransitionSystem(CDD.makeInputEnabled(automata2))).check());
    }

    @Test
    public void K6RefinesSelf() {
        CDD.init(100,100,100);
        CDD.addClocks(automata[30].getClocks(),automata[30].getClocks());
        assertTrue(new Refinement(new SimpleTransitionSystem(automata[30]), new SimpleTransitionSystem(new Automaton(automata[30]))).check());
    }

    @Test
    public void P0RefinesSelf() {
        CDD.init(100,100,100);
        CDD.addClocks(automata[31].getClocks(),automata[31].getClocks());
        assertTrue(new Refinement(new SimpleTransitionSystem(automata[31]), new SimpleTransitionSystem(new Automaton(automata[31]))).check());
    }

    @Test
    public void P1RefinesSelf() {
        CDD.init(100,100,100);
        CDD.addClocks(automata[32].getClocks(),automata[32].getClocks());
        assertTrue(new Refinement(new SimpleTransitionSystem(automata[32]), new SimpleTransitionSystem(new Automaton(automata[32]))).check());
    }

    @Test
    public void P2RefinesSelf() {
        CDD.init(100,100,100);
        CDD.addClocks(automata[33].getClocks(),automata[33].getClocks());
        assertTrue(new Refinement(new SimpleTransitionSystem(automata[33]), new SimpleTransitionSystem(new Automaton(automata[33]))).check());
    }

    @Test
    public void P3RefinesSelf() {
        CDD.init(100,100,100);
        CDD.addClocks(automata[34].getClocks(),automata[34].getClocks());
        assertTrue(new Refinement(new SimpleTransitionSystem(automata[34]), new SimpleTransitionSystem(new Automaton(automata[34]))).check());
    }

    @Test
    public void P4RefinesSelf() {
        CDD.init(100,100,100);
        CDD.addClocks(automata[35].getClocks(),automata[35].getClocks());
        assertTrue(new Refinement(new SimpleTransitionSystem(automata[35]), new SimpleTransitionSystem(new Automaton(automata[35]))).check());
    }

    @Test
    public void P5RefinesSelf() {
        Automaton automata2 = new Automaton(automata[36]);
        CDD.init(100,100,100);
        CDD.addClocks(automata[36].getClocks(),automata2.getClocks());
        System.out.println(CDD.numClocks);
        //assertTrue(new SimpleTransitionSystem(automata[36]).isDeterministic());
        assertTrue(new Refinement(new SimpleTransitionSystem(automata[36]), new SimpleTransitionSystem(automata2)).check());
    }

    @Test
    public void P6RefinesSelf() {
        Automaton automata2 = new Automaton(automata[37]);
        CDD.init(1000,1000,1000);
        CDD.addClocks(automata[37].getClocks(),automata2.getClocks());
        assertTrue(new Refinement(new SimpleTransitionSystem(CDD.makeInputEnabled(automata[37])), new SimpleTransitionSystem((CDD.makeInputEnabled(automata2)))).check());
    }

    @Test
    public void P7RefinesSelf() {
        Automaton automata2 = new Automaton(automata[38]);
        CDD.init(100,100,100);
        CDD.addClocks(automata[38].getClocks(),automata2.getClocks());
        assertTrue(new Refinement(new SimpleTransitionSystem(automata[38]), new SimpleTransitionSystem(automata2)).check());
    }

    @Test
    public void L1RefinesSelf() {
        CDD.init(100,100,100);
        CDD.addClocks(automata[39].getClocks(),automata[39].getClocks());
        assertTrue(new Refinement(new SimpleTransitionSystem(automata[39]), new SimpleTransitionSystem(new Automaton(automata[39]))).check());
    }

    @Test
    public void L2RefinesSelf() {
        CDD.init(100,100,100);
        CDD.addClocks(automata[40].getClocks(),automata[40].getClocks());
        assertTrue(new Refinement(new SimpleTransitionSystem(automata[40]), new SimpleTransitionSystem(new Automaton(automata[40]))).check());
    }

    @Test
    public void L3RefinesSelf() {
        CDD.init(100,100,100);
        CDD.addClocks(automata[41].getClocks(),automata[41].getClocks());
        assertTrue(new Refinement(new SimpleTransitionSystem(automata[41]), new SimpleTransitionSystem(new Automaton(automata[41]))).check());
    }

    @Test
    public void L4RefinesSelf() {
        Automaton automata2 = new Automaton(automata[42]);
        CDD.init(100,100,100);
        CDD.addClocks(automata[42].getClocks(),automata2.getClocks());
        assertTrue(new Refinement(new SimpleTransitionSystem(CDD.makeInputEnabled(automata[42])), new SimpleTransitionSystem(CDD.makeInputEnabled(automata2))).check());
    }

    @Test
    public void L5RefinesSelf() {

        Automaton automata2 = new Automaton(automata[43]);
        CDD.init(100,100,100);
        CDD.addClocks(automata[43].getClocks(),automata2.getClocks());
        assertTrue(new Refinement(new SimpleTransitionSystem(CDD.makeInputEnabled(automata[43])), new SimpleTransitionSystem(CDD.makeInputEnabled(automata2))).check());
    }

    @Test
    public void L6RefinesSelf() {
        CDD.init(100,100,100);
        CDD.addClocks(automata[44].getClocks(),automata[44].getClocks());
        assertTrue(new Refinement(new SimpleTransitionSystem(automata[44]), new SimpleTransitionSystem(new Automaton(automata[44]))).check());
    }

    @Test
    public void L7RefinesSelf() {
        CDD.init(100,100,100);
        CDD.addClocks(automata[45].getClocks(),automata[45].getClocks());
        assertTrue(new Refinement(new SimpleTransitionSystem(automata[45]), new SimpleTransitionSystem(new Automaton(automata[45]))).check());
    }

    @Test
    public void Z1RefinesSelf() {
        Automaton automata2 = new Automaton(automata[46]);
        CDD.init(1000,1000,1000);
        CDD.addClocks(automata[46].getClocks(),automata2.getClocks());
        SimpleTransitionSystem inpuEn = new SimpleTransitionSystem(CDD.makeInputEnabled(automata[46]));
        XMLFileWriter.toXML("inpue.xml",inpuEn);
        assertTrue(new Refinement(new SimpleTransitionSystem(CDD.makeInputEnabled(automata[46])), new SimpleTransitionSystem(CDD.makeInputEnabled(automata2))).check());
    }

    @Test
    public void Z2RefinesSelf() {
        CDD.init(100,100,100);
        CDD.addClocks(automata[47].getClocks(),automata[47].getClocks());
        assertTrue(new Refinement(new SimpleTransitionSystem(automata[47]), new SimpleTransitionSystem(new Automaton(automata[47]))).check());
    }

    @Test
    public void Z3RefinesSelf() {

        Automaton automata2 = new Automaton(automata[48]);
        CDD.init(100,100,100);
        CDD.addClocks(automata[48].getClocks(),automata2.getClocks());
        assertTrue(new Refinement(new SimpleTransitionSystem(CDD.makeInputEnabled(automata[48])), new SimpleTransitionSystem(CDD.makeInputEnabled(automata2))).check());
    }

    @Test
    public void Z4RefinesSelf() {
        CDD.init(100,100,100);
        CDD.addClocks(automata[49].getClocks(),automata[49].getClocks());
        assertTrue(new Refinement(new SimpleTransitionSystem(automata[49]), new SimpleTransitionSystem(new Automaton(automata[49]))).check());
    }

    @Test
    public void Z5RefinesSelf() {
        CDD.init(100,100,100);
        CDD.addClocks(automata[50].getClocks(),automata[50].getClocks());
        System.out.println(CDD.numClocks);
        assertTrue(new Refinement(new SimpleTransitionSystem(automata[50]), new SimpleTransitionSystem(new Automaton(automata[50]))).check());
    }

    @Test
    public void Z6RefinesSelf() {
        CDD.init(100,100,100);
        CDD.addClocks(automata[51].getClocks(),automata[51].getClocks());
        assertTrue(new Refinement(new SimpleTransitionSystem(automata[51]), new SimpleTransitionSystem(new Automaton(automata[51]))).check());
    }

    @Test
    public void Z7RefinesSelf() {
        CDD.init(100,100,100);
        CDD.addClocks(automata[52].getClocks(),automata[52].getClocks());
        assertTrue(new Refinement(new SimpleTransitionSystem(automata[52]), new SimpleTransitionSystem(new Automaton(automata[52]))).check());
    }

    // Rest of the tests


    @Test
    public void T2isImplementation() {
        CDD.init(100,100,100);
        CDD.addClocks(automata[1].getClocks());
        assertTrue(new SimpleTransitionSystem(CDD.makeInputEnabled(automata[1])).isDeterministic());
        assertTrue(new SimpleTransitionSystem(CDD.makeInputEnabled(automata[1])).isFullyConsistent());
    }



    @Test
    public void T1T2RefinesT3() { // TODO: T2 is not consistent...... see test before
        CDD.init(1000,1000,1000);
        CDD.addClocks(automata[0].getClocks(),automata[1].getClocks(),automata[2].getClocks());
        TransitionSystem comp = new Composition(
                new TransitionSystem[]{
                        new SimpleTransitionSystem(CDD.makeInputEnabled(automata[0])),
                        new SimpleTransitionSystem(CDD.makeInputEnabled(automata[1]))});
       Automaton array[] = new Automaton[]{comp.getAutomaton()};
        XMLFileWriter.toXML("compT1T2.xml", array);
        assertTrue(new Refinement(comp, new SimpleTransitionSystem(CDD.makeInputEnabled(automata[2]))).check());
    }
/*
    @Test
    public void T12RefinesT3() { // never finished the test
        CDD.init(1000,1000,1000);
        CDD.addClocks(automata[55].getClocks(),automata[2].getClocks());
        assertTrue(new Refinement(new SimpleTransitionSystem(CDD.makeInputEnabled(automata[55])), new SimpleTransitionSystem(CDD.makeInputEnabled(automata[2]))).check());
    }*/


    @Test
    public void C1RefinesC2() {
        CDD.init(100,100,100);
        CDD.addClocks(automata[3].getClocks(),automata[4].getClocks());
        assertTrue(new Refinement(new SimpleTransitionSystem(automata[3]), new SimpleTransitionSystem(automata[4])).check());
    }

    @Test
    public void C2RefinesC1() {
        CDD.init(100,100,100);
        CDD.addClocks(automata[3].getClocks(),automata[4].getClocks());
        assertTrue(new Refinement(new SimpleTransitionSystem(automata[4]), new SimpleTransitionSystem(automata[3])).check());
    }

    @Test
    public void T0T1T2RefinesT3() {
        CDD.init(100,100,100);
        CDD.addClocks(automata[11].getClocks(),automata[0].getClocks(),automata[1].getClocks(),automata[2].getClocks());
        TransitionSystem comp = new Composition(
                new TransitionSystem[]{
                        new SimpleTransitionSystem(automata[11]),
                        new SimpleTransitionSystem(automata[0]),
                        new SimpleTransitionSystem(automata[1])});
        assertTrue(new Refinement(comp, new SimpleTransitionSystem(automata[2])).check());
    }

    @Test
    public void F1F2RefinesF3() {

        CDD.init(100,100,100);
        CDD.addClocks(automata[7].getClocks(),automata[8].getClocks(),automata[9].getClocks());

        TransitionSystem comp = new Composition(
                new TransitionSystem[]{
                        new SimpleTransitionSystem(automata[7]),
                        new SimpleTransitionSystem(automata[8])});
        Refinement ref = new Refinement(comp, new SimpleTransitionSystem(automata[9]));
        boolean result =ref.check();
        //System.out.println(comp.isImplementation());
        System.out.println(ref.getErrMsg());


        assertTrue(result);
    }

    @Test
    public void T4RefinesT3() { // TODO: T4 has no independent progress
        CDD.init(100,100,100);
        CDD.addClocks(automata[10].getClocks(),automata[2].getClocks());
        //assertTrue(new SimpleTransitionSystem(CDD.makeInputEnabled(automata[10])).isFullyConsistent());
        //assertTrue(new SimpleTransitionSystem(CDD.makeInputEnabled(automata[2])).isFullyConsistent());
       // assertTrue(new SimpleTransitionSystem(CDD.makeInputEnabled(automata[10])).isImplementation());

        assertTrue(new Refinement(new SimpleTransitionSystem(CDD.makeInputEnabled(automata[10])), new SimpleTransitionSystem(CDD.makeInputEnabled(automata[2]))).check());
    }

    @Test
    public void T6RefinesT5() {
        CDD.init(100,100,100);
        CDD.addClocks(automata[13].getClocks(),automata[12].getClocks());
        assertTrue(new Refinement(new SimpleTransitionSystem(CDD.makeInputEnabled(automata[13])), new SimpleTransitionSystem(CDD.makeInputEnabled(automata[12]))).check(true));
    }

    @Test
    public void T7NotRefinesT8() {
        CDD.init(100,100,100);
        CDD.addClocks(automata[14].getClocks(),automata[15].getClocks());
        // should fail because left side has more inputs
        assertFalse(new Refinement(new SimpleTransitionSystem(automata[14]), new SimpleTransitionSystem(automata[15])).check());
    }

    @Test
    public void T9NotRefinesT8() {
        CDD.init(100,100,100);
        CDD.addClocks(automata[15].getClocks(),automata[16].getClocks());
        assertFalse(new Refinement(new SimpleTransitionSystem(CDD.makeInputEnabled(automata[16])), new SimpleTransitionSystem(CDD.makeInputEnabled(automata[15]))).check());
    }

    @Test
    public void T10NotRefinesT11() {
        CDD.init(100,100,100);
        CDD.addClocks(automata[17].getClocks(),automata[18].getClocks());
        // should fail because left side has more inputs
        //assertFalse(new Refinement(new SimpleTransitionSystem(CDD.makeInputEnabled(automata[17])), new SimpleTransitionSystem(CDD.makeInputEnabled(automata[18]))).check());
    }

    @Test
    public void N1RefinesN2() {
        CDD.init(100,100,100);
        CDD.addClocks(automata[19].getClocks(),automata[20].getClocks());
        assertTrue(new Refinement(new SimpleTransitionSystem(automata[19]), new SimpleTransitionSystem(automata[20])).check());
    }

    @Test
    public void N3NotRefinesN4() {
        // should fail because right side has more inputs
        CDD.init(100,100,100);
        CDD.addClocks(automata[21].getClocks(),automata[22].getClocks());
        assertFalse(new Refinement(new SimpleTransitionSystem(automata[21]), new SimpleTransitionSystem(automata[22])).check());
    }

    @Test
    public void D2RefinesD1() {
        CDD.init(100,100,100);
        CDD.addClocks(automata[24].getClocks(),automata[23].getClocks());
        assertTrue(new Refinement(new SimpleTransitionSystem(automata[24]), new SimpleTransitionSystem(automata[23])).check());
    }

    @Test
    public void D1NotRefinesD2() {
        // should fail because right side has more outputs
        CDD.init(100,100,100);
        CDD.addClocks(automata[23].getClocks(),automata[24].getClocks());
        assertFalse(new Refinement(new SimpleTransitionSystem(automata[23]), new SimpleTransitionSystem(automata[24])).check());
    }

    @Test
    public void K1NotRefinesK2() {
        CDD.init(100,100,100);
        CDD.addClocks(automata[25].getClocks(),automata[26].getClocks());
        assertFalse(new Refinement(new SimpleTransitionSystem(automata[25]), new SimpleTransitionSystem(automata[26])).check());
    }

    @Test
    public void K3NotRefinesK4() {
        CDD.init(100,100,100);
        CDD.addClocks(automata[27].getClocks(),automata[28].getClocks());
        assertFalse(new Refinement(new SimpleTransitionSystem(CDD.makeInputEnabled(automata[27])), new SimpleTransitionSystem(CDD.makeInputEnabled(automata[28]))).check());
    }

    @Test
    public void K5NotRefinesK6() {
        CDD.init(100,100,100);
        CDD.addClocks(automata[29].getClocks(),automata[30].getClocks());
        assertFalse(new Refinement(new SimpleTransitionSystem(automata[29]), new SimpleTransitionSystem(automata[30])).check());
    }

    @Test
    public void P0RefinesP1() {
        CDD.init(100,100,100);
        CDD.addClocks(automata[31].getClocks(),automata[32].getClocks());
        assertTrue(new Refinement(new SimpleTransitionSystem(automata[31]), new SimpleTransitionSystem(automata[32])).check());
    }

    @Test
    public void P2NotRefinesP3() {
        CDD.init(100,100,100);
        CDD.addClocks(automata[33].getClocks(),automata[34].getClocks());
        assertFalse(new Refinement(new SimpleTransitionSystem(automata[33]), new SimpleTransitionSystem(automata[34])).check());
    }

    @Test
    public void P4RefinesP5() {
        CDD.init(100,100,100);
        CDD.addClocks(automata[35].getClocks(),automata[36].getClocks());
        assertTrue(new Refinement(new SimpleTransitionSystem(automata[35]), new SimpleTransitionSystem(automata[36])).check());
    }

    @Test
    public void P6RefinesP7() {
        CDD.init(100,100,100);
        CDD.addClocks(automata[37].getClocks(),automata[38].getClocks());
        assertTrue(new Refinement(new SimpleTransitionSystem(automata[37]), new SimpleTransitionSystem(automata[38])).check());
    }

    @Test
    public void L1L2NotRefinesL3(){
        CDD.init(100,100,100);
        CDD.addClocks(automata[39].getClocks(),automata[40].getClocks(),automata[41].getClocks());
        TransitionSystem comp = new Composition(
                new TransitionSystem[]{
                        new SimpleTransitionSystem(CDD.makeInputEnabled(automata[39])),
                        new SimpleTransitionSystem(CDD.makeInputEnabled(automata[40]))});
        boolean result = new Refinement(comp, new SimpleTransitionSystem(CDD.makeInputEnabled(automata[41]))).check();
        System.out.println(result);
        assertFalse(result);
    }

    @Test
    public void L4RefinesL5() {
        CDD.init(100,100,100);
        CDD.addClocks(automata[43].getClocks(),automata[42].getClocks());
        assertTrue(new Refinement(new SimpleTransitionSystem(automata[42]), new SimpleTransitionSystem(automata[43])).check());
    }

    @Test
    public void L6NotRefinesL7() {
        CDD.init(100,100,100);
        CDD.addClocks(automata[44].getClocks(),automata[45].getClocks());
        assertFalse(new Refinement(new SimpleTransitionSystem(automata[44]), new SimpleTransitionSystem(automata[45])).check());
    }

    @Test
    public void Z1RefinesZ2() {
        CDD.init(100,100,100);
        CDD.addClocks(automata[46].getClocks(),automata[47].getClocks());
        assertTrue(new Refinement(new SimpleTransitionSystem(automata[46]), new SimpleTransitionSystem(automata[47])).check());
    }

    @Test
    public void Z3RefinesZ4() {
        CDD.init(100,100,100);
        CDD.addClocks(automata[48].getClocks(),automata[49].getClocks());
        assertTrue(new Refinement(new SimpleTransitionSystem(automata[48]), new SimpleTransitionSystem(automata[49])).check());
    }

    @Test
    public void Z5Z6NotRefinesZ7() {
        CDD.init(100,100,100);
        CDD.addClocks(automata[50].getClocks(),automata[51].getClocks(),automata[52].getClocks());
        TransitionSystem comp = new Composition(
                new TransitionSystem[]{
                        new SimpleTransitionSystem(automata[50]),
                        new SimpleTransitionSystem(automata[51])});
        assertFalse(new Refinement(comp, new SimpleTransitionSystem(automata[52])).check());
    }

    @Test
    public void Q1RefinesQ2() {
        CDD.init(100,100,100);
        CDD.addClocks(automata[53].getClocks(),automata[54].getClocks());
        assertFalse(new Refinement(new SimpleTransitionSystem(automata[53]), new SimpleTransitionSystem(automata[54])).check());
    }

    @Test
    public void Q2RefinesQ1() {
        CDD.init(100,100,100);
        CDD.addClocks(automata[54].getClocks(),automata[53].getClocks());
        assertFalse(new Refinement(new SimpleTransitionSystem(automata[54]), new SimpleTransitionSystem(automata[53])).check());
    }

}
