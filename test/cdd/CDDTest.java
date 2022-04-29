package cdd;

import Exceptions.CddAlreadyRunningException;
import Exceptions.CddNotRunningException;
import models.*;
import org.junit.After;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class CDDTest {

    static final int CDD_INF = 2147483646;
    private static final int DBM_INF = Integer.MAX_VALUE - 1;

    @After
    public void afterEachTest(){

        CDD.done();
    }

    @Test
    public void testCddInit() throws CddAlreadyRunningException {
        assertEquals(0, CDD.init(0,0,0));
    }

    @Test
    public void testConjunctionSameTypeWithOverlap() throws CddNotRunningException, CddAlreadyRunningException {
        CDD.init(100,100,100);
        List<Clock> clocks = new ArrayList<>();
        Clock a = new Clock("a");
        Clock b = new Clock("b");
        clocks.add(a);
        clocks.add(b);
        CDD.addClocks(clocks);
        CDD cdd1 = CDD.allocateInterval(2,1,3, false,5, false);
        CDD cdd2 = CDD.allocateInterval(2,1,4,false,6, false);

        CDD cdd3 = cdd1.conjunction(cdd2);
        System.out.println(CDD.toGuardList(cdd2,clocks));

        Guard g1 = new ClockGuard(b,a,3,Relation.LESS_EQUAL );
        Guard g2 = new ClockGuard(a,b,5,Relation.LESS_EQUAL );

        Guard g3 = new ClockGuard(b,a,4,Relation.LESS_EQUAL );
        Guard g4 = new ClockGuard(a,b,6,Relation.LESS_EQUAL );

        List<Guard> guardList = new ArrayList<>();
        guardList.add(g1);
        guardList.add(g2);
        guardList.add(g3);
        guardList.add(g4);

        System.out.println(CDD.toGuardList(new CDD(new AndGuard(guardList)),clocks));
        // TODO: Make sense of how exactly the interval works, and make a good asser statement

        CDD.free(cdd1);
        CDD.free(cdd2);
        CDD.free(cdd3);
    }

    @Test
    public void testDisjunction() throws CddNotRunningException, CddAlreadyRunningException {
        CDD.init(100,100,100);
        List<Clock> clocks = new ArrayList<>();
        clocks.add(new Clock("a"));
        clocks.add(new Clock("b"));
        CDD.addClocks(clocks);
        CDD cdd1 = CDD.allocateInterval(2,1,3, false,5,false);
        CDD cdd2 = CDD.allocateInterval(2,1,4,false,6,false);

        CDD cdd3 = cdd1.disjunction(cdd2);
        CDDNode node = cdd3.getRoot();

        assertEquals(6, node.getElemAtIndex(0).getBound());
        assertEquals(13, node.getElemAtIndex(1).getBound());

        CDD.free(cdd1);
        CDD.free(cdd2);
        CDD.free(cdd3);
    }

    @Test
    public void getCorrectBounds() throws CddNotRunningException, CddAlreadyRunningException {
        CDD.init(100,100,100);
        List<Clock> clocks = new ArrayList<>();
        clocks.add(new Clock("a"));
        CDD.addClocks(clocks);
        CDD cdd1 = CDD.allocateInterval(1,0,3, false,5,false);
        CDDNode node = cdd1.getRoot();
        List<Elem> bounds = new ArrayList<>();

        node.getElemIterable().forEach(bounds::add);

        assertEquals(6, bounds.get(0).getBound());
        assertEquals(11, bounds.get(1).getBound());
    }

    @Test
    public void cddTrue_RootNodeIsTrueNode() throws CddAlreadyRunningException, CddNotRunningException {
        CDD.init(100,100,100);
        List<Clock> clocks = new ArrayList<>();
        clocks.add(new Clock("a"));
        CDD.addClocks(clocks);

        CDD trueNode = CDD.cddTrue();

        assertTrue(trueNode.getRoot().isTrueTerminal());

        CDD.free(trueNode);
    }

    @Test
    public void cddFalse_RootNodeIsFalseNode() throws CddAlreadyRunningException, CddNotRunningException {
        CDD.init(100,100,100);
        List<Clock> clocks = new ArrayList<>();
        clocks.add(new Clock("a"));
        CDD.addClocks(clocks);

        CDD falseNode = CDD.cddFalse();

        assertTrue(falseNode.getRoot().isFalseTerminal());

        CDD.free(falseNode);
    }

    @Test
    public void isTerminal_trueNodeShouldBeTerminal() throws CddAlreadyRunningException, CddNotRunningException {
        CDD.init(100,100,100);
        List<Clock> clocks = new ArrayList<>();
        clocks.add(new Clock("a"));
        CDD.addClocks(clocks);

        CDD trueNode = CDD.cddTrue();

        assertTrue(trueNode.isTerminal());

        CDD.free(trueNode);
    }

    @Test
    public void isTerminal_shouldNotBeTerminal() throws CddAlreadyRunningException, CddNotRunningException {
        CDD.init(100,100,100);
        List<Clock> clocks = new ArrayList<>();
        clocks.add(new Clock("a"));
        clocks.add(new Clock("b"));
        CDD.addClocks(clocks);

        CDD cdd1 = CDD.allocateInterval(2,1,3, false,5,false);
        CDD cdd2 = CDD.allocateInterval(2,1,4,false,6,false);

        CDD cdd3 = cdd1.conjunction(cdd2);

        assertFalse(cdd3.isTerminal());

        CDD.free(cdd1);
        CDD.free(cdd2);
        CDD.free(cdd3);
    }

    @Test
    public void createCddFromDbm() throws CddNotRunningException, CddAlreadyRunningException {
        CDD.init(100,100,100);
        List<Clock> clocks = new ArrayList<>();
        clocks.add(new Clock("a"));
        CDD.addClocks(clocks);

        CDD cdd1 = CDD.allocateFromDbm(new int[]{1, 1, 11, 1}, 2);
        CDDNode node = cdd1.getRoot();

        assertTrue(node.getElemAtIndex(0).getChild().isFalseTerminal());
        assertTrue(node.getElemAtIndex(1).getChild().isTrueTerminal());
        assertEquals(0, node.getElemAtIndex(0).getBound());
        assertEquals(11, node.getElemAtIndex(1).getBound());

        CDD.free(cdd1);
    }

    @Test(expected = CddNotRunningException.class)
    public void addClocksWithoutInitializing() throws CddNotRunningException {
        List<Clock> clocks = new ArrayList<>();
        clocks.add(new Clock("a"));
        CDD.addClocks(clocks);
    }

    @Test(expected = CddAlreadyRunningException.class)
    public void initializeAlreadyRunningCDD() throws CddAlreadyRunningException {
        CDD.init(1000,1000,1000);

        CDD.init(1000,1000,1000);
    }

    @Test(expected = NullPointerException.class)
    public void cddReducingNullCDD() throws CddNotRunningException, CddAlreadyRunningException {
        CDD.init(100,100,100);
        List<Clock> clocks = new ArrayList<>();
        clocks.add(new Clock("a"));
        CDD.addClocks(clocks);

        CDD cdd = CDD.allocateFromDbm(new int[]{1, 1, 11, 1}, 2);
        CDD.free(cdd);

        cdd = cdd.reduce();
        cdd.isNotFalse();
    }

    @Test(expected = NullPointerException.class)
    public void cddFreeingNullCDD() throws CddNotRunningException, CddAlreadyRunningException {
        CDD.init(100,100,100);
        List<Clock> clocks = new ArrayList<>();
        clocks.add(new Clock("a"));
        CDD.addClocks(clocks);

        CDD cdd = CDD.allocateFromDbm(new int[]{1, 1, 11, 1}, 2);
        CDD.free(cdd);

        CDD.free(cdd);
    }

    @Test
    public void cddLowerBound() throws CddNotRunningException, CddAlreadyRunningException {
        CDD.init(100,100,100);
        List<Clock> clocks = new ArrayList<>();
        clocks.add(new Clock("a"));
        CDD.addClocks(clocks);

        CDD cdd = CDD.allocateLower(1,0,3, true);
        CDDNode node = cdd.getRoot();

        cdd.printDot();

        assertEquals(6, node.getElemAtIndex(0).getBound());
        assertEquals(CDD_INF, node.getElemAtIndex(1).getBound());

        CDD.free(cdd);

    }

    @Test
    public void cddUpperBound() throws CddNotRunningException, CddAlreadyRunningException {
        CDD.init(100,100,100);
        List<Clock> clocks = new ArrayList<>();
        clocks.add(new Clock("a"));
        CDD.addClocks(clocks);

        CDD cdd = CDD.allocateUpper(1,0,6,true);
        CDDNode node = cdd.getRoot();

        cdd.printDot(); // --> the CDD is correct, so I guess the test is wrong
        assertEquals(0, node.getElemAtIndex(0).getBound());
        assertEquals(6, node.getElemAtIndex(1).getBound());

        CDD.free(cdd);
    }

    @Test
    public void guardToCDDTest() throws CddNotRunningException, CddAlreadyRunningException {
        CDD.init(100,100,100);
        List<Clock> clocks = new ArrayList<>();
        Clock x = new Clock("x");
        Clock y = new Clock("y");
        clocks.add(x);
        clocks.add(y);
        CDD.addClocks(clocks);


        Guard e2_g1 = new ClockGuard(x, null, 3,  Relation.GREATER_EQUAL);
        Guard e2_g2 = new ClockGuard(y, null, 5,  Relation.LESS_EQUAL);

        List<Guard> g1 = new ArrayList<>();
        g1.add(e2_g1);
        g1.add(e2_g2);
        CDD res = new CDD(new OrGuard(g1));
        //res.printDot();
        CDD exp = CDD.cddTrue();
        exp = exp.conjunction(CDD.allocateInterval(1, 0, 3, false, DBM_INF/2-1, false));
        exp = exp.disjunction(CDD.allocateInterval(2, 0, 0,true, 5,false));
         System.out.println(CDD.toGuardList(exp.removeNegative().reduce(),clocks));
        System.out.println(CDD.toGuardList(res.removeNegative().reduce(),clocks));
        //exp.printDot();
        exp = exp.removeNegative();
        //exp.printDot();
        assertEquals(res, exp);
    }

    @Test
    public void cddAddBddvar() throws CddNotRunningException, CddAlreadyRunningException {
        CDD.init(100,100,100);
        List<Clock> clocks = new ArrayList<>();
        clocks.add(new Clock("a"));
        CDD.addClocks(clocks);

        List<BoolVar> BVs = new ArrayList<>();
        BVs.add(new BoolVar("a",true));
        BVs.add(new BoolVar("b",true));
        BVs.add(new BoolVar("d",true));
        BVs.add(new BoolVar("c",true));
        BVs.add(new BoolVar("e",true));
        int level = CDD.addBddvar(BVs);
        assertEquals(1, level);
        BVs.clear();
        BVs.add(new BoolVar("f",true));
        BVs.add(new BoolVar("g",true));
        level = CDD.addBddvar(BVs);
        assertEquals(6, level);
    }


}
