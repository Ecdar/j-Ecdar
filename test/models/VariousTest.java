package models;

import exceptions.CddAlreadyRunningException;
import exceptions.CddNotRunningException;
import logic.*;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import parser.JSONParser;
import parser.XMLParser;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class VariousTest {

    @After
    public void afterEachTest(){
        CDD.done();
    }

    @BeforeClass
    public static void setUpBeforeClass() {

    }

    @Test
    public void simple() throws CddAlreadyRunningException, CddNotRunningException {
        Automaton[] aut2 = XMLParser.parse("samples/xml/simple.xml", true);

        SimpleTransitionSystem simp2 = new SimpleTransitionSystem(aut2[0]);

        simp2.toXML("testOutput/simple1.xml");

        assert (true);
    }

    @Test
    public void next() {
        Clock y = new Clock("y", "Aut");
        List<Clock> clocks = new ArrayList<>(Arrays.asList(y));
        Zone z1 = new Zone(clocks.size()+1,true);
        z1.init();
        Zone z2 = new Zone(clocks.size()+1,true);
        z2.init();

        ClockGuard g1 = new ClockGuard(y, 5,  Relation.GREATER_EQUAL);
        z1.buildConstraintsForGuard(g1,clocks);

        z1.printDbm(true,true);
        ClockGuard g2 = new ClockGuard(y, 6,  Relation.GREATER_EQUAL);
        System.out.println(g2);
        z2.buildConstraintsForGuard(g2,clocks);
        z2.printDbm(true,true);

        List<Zone> zoneList1 = new ArrayList<>();
        List<Zone> zoneList2 = new ArrayList<>();
        zoneList1.add(z1);
        zoneList2.add(z2);
        Federation f1 = new Federation(zoneList1);
        Federation f2 = new Federation(zoneList2);

        System.out.println(f1.isSubset(f2));
        System.out.println(f2.isSubset(f1));
        System.out.println(f1.isSubset(f1));
        System.out.println(f2.isSubset(f2));
    }

    @Test
    public void testDiagonalConstraints() {
        Clock x = new Clock("x", "Aut");
        Clock y = new Clock("y", "Aut");

        ClockGuard g1 = new ClockGuard(x, 10, Relation.LESS_EQUAL);
        ClockGuard g2 = new ClockGuard(x, 5, Relation.GREATER_EQUAL);
        ClockGuard g3 = new ClockGuard(y, 3, Relation.LESS_EQUAL);
        ClockGuard g4 = new ClockGuard(y, 2, Relation.GREATER_EQUAL);


        List<Guard> inner = new ArrayList<>();
        inner.add(g1);
        inner.add(g2);
        inner.add(g3);
        inner.add(g4);


        List<Clock> clocks = new ArrayList<>();
        clocks.add(x);
        clocks.add(y);
        CDD.init(100,100,100);
        CDD.addClocks(clocks);

        CDD origin1 = new CDD(new AndGuard(inner));


        origin1 = origin1.delay();
        Guard origin1Guards = origin1.getGuard(clocks);
        System.out.println(origin1Guards);
        assert(true);

    }


    @Test
    public void testClockReset() {
        Clock x = new Clock("x", "Aut");
        Clock y = new Clock("y", "Aut");

        ClockGuard g1 = new ClockGuard(x, 10, Relation.GREATER_EQUAL);
        ClockGuard g3 = new ClockGuard(y, 3, Relation.LESS_EQUAL);

        List<List<Guard>> guards1 = new ArrayList<>();
        List<Guard> inner = new ArrayList<>();
        inner.add(g1);
        inner.add(g3);
        guards1.add(inner);

        List<Clock> clocks = new ArrayList<>();
        clocks.add(x);
        clocks.add(y);
        CDD.init(100,100,100);
        CDD.addClocks(clocks);

        CDD origin1 = new CDD(new AndGuard(inner));

        Guard origin1Guards = origin1.getGuard(clocks);
        System.out.println(origin1Guards);



        Update clockUpdate = new ClockUpdate(x,0);
        List<Update>  list1 = new ArrayList<>();
        list1.add(clockUpdate);
        origin1 = origin1.applyReset(list1);

        Guard origin2Guards = origin1.getGuard(clocks);
        System.out.println(origin2Guards);

        assert(origin2Guards.toString().equals("(x==0 && y<=3 && y-x<=3 && x-y<=0)"));

    }

    @Test
    public void conversionTest()
    {
        int rawUpperBound = 43;
        int converted = rawUpperBound>>1;
        boolean included  =  (rawUpperBound & 1)==0 ? false : true;
        System.out.println(converted + " " + included);
    }

    @Test
    public void testFromFramework1() throws FileNotFoundException {
        SimpleTransitionSystem A,A1,G,Q;
        Automaton[] list = JSONParser.parse("samples/json/AG",true);
        A = new SimpleTransitionSystem(list[0]);
        A1 = new SimpleTransitionSystem(list[0]);
        G = new SimpleTransitionSystem(list[2]);
        Q = new SimpleTransitionSystem(list[4]);

        // refinement: A <= ((A || G) \\\\ Q)
        Refinement ref = new Refinement(A, new Quotient(new Composition(A1,G),Q));
        boolean res = ref.check();
        System.out.println(ref.getErrMsg());
        assertTrue(res);
    }



    @Test
    public void testFromFramework2() throws FileNotFoundException {
        SimpleTransitionSystem Inf;
        Automaton[] list = XMLParser.parse("C:\\tools\\ecdar-test\\Ecdar-test\\samples\\xml\\extrapolation_test.xml",false);
        Inf = new SimpleTransitionSystem(list[0]);
        // refinement: A <= ((A || G) \\\\ Q)
        System.out.println(Inf.isDeterministic());
        boolean res = Inf.isLeastConsistent();
        System.out.println(Inf.getLastErr());
        assertTrue(res);
    }
    @Test
    public void testFromFramework3() throws FileNotFoundException {
        SimpleTransitionSystem A2,A1,B;
        Automaton[] list = JSONParser.parse("samples/json/DelayAdd",true);
        A2 = new SimpleTransitionSystem(list[1]);
        B = new SimpleTransitionSystem(list[2]);
        A1 = new SimpleTransitionSystem(list[0]);

        assertFalse(new Refinement(new Composition(A1,A2),B).check());

        // refinement: A2 <= (B \\ A1)
        Refinement ref = new Refinement(A2, new SimpleTransitionSystem(new Quotient(B,A1).getAutomaton()));
        boolean res = ref.check();
        System.out.println(ref.getErrMsg());
        assertFalse(res);
    }

    @Test
    public void testFromFramework4() throws FileNotFoundException {
        SimpleTransitionSystem C1,C2;
        Automaton[] list = JSONParser.parse("samples/json/DelayAdd",true);
        C1 = new SimpleTransitionSystem(list[3]);
        C2 = new SimpleTransitionSystem(list[4]);
        System.out.println(C1.getName());
        System.out.println(C2.getName());
        assertFalse(new Refinement(C1,C2).check());

    }


    @Test
    public void testFromFramework5() throws FileNotFoundException {
        SimpleTransitionSystem GuardParan;
        Automaton[] list = XMLParser.parse("samples/xml/misc_test.xml",true);
        GuardParan = new SimpleTransitionSystem(list[0]);
        assertTrue(GuardParan.isLeastConsistent());
        assertTrue(GuardParan.isFullyConsistent());

    }


    @Test
    public void testCDDAllocateInterval() throws CddAlreadyRunningException, CddNotRunningException
    {
        CDD.init(100,100,100);
        Clock x = new Clock("x","Aut");
        Clock y = new Clock("y", "Aut");
        List<Clock> clocks = new ArrayList<>();
        clocks.add(x);clocks.add(y);
        CDD.addClocks(clocks);
        CDD test = CDD.createInterval(1,0,2,true,3,true);
        System.out.println(test.getGuard(clocks));
        test.printDot();
        assert(true);
    }

    @Test
    public void testCompOfCompRefinesSpec() throws CddAlreadyRunningException, CddNotRunningException {

        Automaton[] aut2 = XMLParser.parse("samples/xml/university-slice.xml", true);

        CDD.init(1000,1000,1000);
        List<Clock> clocks = new ArrayList<>();
        clocks.addAll(aut2[0].getClocks());
        clocks.addAll(aut2[1].getClocks());
        clocks.addAll(aut2[2].getClocks());
        clocks.addAll(aut2[3].getClocks());
        CDD.addClocks(clocks);

        SimpleTransitionSystem adm = new SimpleTransitionSystem((aut2[3]));
        SimpleTransitionSystem machine = new SimpleTransitionSystem((aut2[0]));
        SimpleTransitionSystem researcher = new SimpleTransitionSystem((aut2[1]));
        SimpleTransitionSystem spec = new SimpleTransitionSystem((aut2[2]));

        assertTrue(new Refinement(
                new Composition(new TransitionSystem[]{adm,
                        new Composition(new TransitionSystem[]{machine, researcher})}),
                spec).check()
        );




    }
}
