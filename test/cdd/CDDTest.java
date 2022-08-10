package cdd;

import exceptions.CddAlreadyRunningException;
import exceptions.CddNotRunningException;
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
        Clock a = new Clock("a", "A");
        Clock b = new Clock("b", "B");
        clocks.add(a);
        clocks.add(b);
        CDD.addClocks(clocks);
        CDD cdd1 = CDD.createInterval(2,1,3, true,5, true);
        CDD cdd2 = CDD.createInterval(2,1,4,true,6, true);

        CDD cdd3 = cdd1.conjunction(cdd2);
        System.out.println(cdd2.getGuard(clocks));

        Guard g1 = new ClockGuard(b,a,3,Relation.LESS_EQUAL );
        Guard g2 = new ClockGuard(a,b,5,Relation.LESS_EQUAL );

        Guard g3 = new ClockGuard(b,a,4,Relation.LESS_EQUAL );
        Guard g4 = new ClockGuard(a,b,6,Relation.LESS_EQUAL );

        List<Guard> guardList = new ArrayList<>();
        guardList.add(g1);
        guardList.add(g2);
        guardList.add(g3);
        guardList.add(g4);

        System.out.println(new CDD(new AndGuard(guardList)).getGuard(clocks));
        // TODO: Make sense of how exactly the interval works, and make a good asser statement

        cdd1.free();
        cdd2.free();
        cdd3.free();
    }

    @Test
    public void testDisjunction() throws CddNotRunningException, CddAlreadyRunningException {
        CDD.init(100,100,100);
        List<Clock> clocks = new ArrayList<>();
        clocks.add(new Clock("a", "A"));
        clocks.add(new Clock("b", "B"));
        CDD.addClocks(clocks);
        CDD cdd1 = CDD.createInterval(2,1,3, true,5,true);
        CDD cdd2 = CDD.createInterval(2,1,4,true,6,true);

        CDD cdd3 = cdd1.disjunction(cdd2);
        CDDNode node = cdd3.getRoot();

        assertEquals(0, node.getSegmentAtIndex(0).getUpperBound());
        assertEquals(CDD_INF>>1, node.getSegmentAtIndex(1).getUpperBound());

        cdd1.free();
        cdd2.free();
        cdd3.free();
    }

    @Test
    public void getCorrectBounds() throws CddNotRunningException, CddAlreadyRunningException {
        CDD.init(100,100,100);
        List<Clock> clocks = new ArrayList<>();
        clocks.add(new Clock("a", "A"));
        CDD.addClocks(clocks);
        CDD cdd1 = CDD.createInterval(1,0,30, true,50,true);
        CDDNode node = cdd1.getRoot();
        List<Segment> bounds = new ArrayList<>();

        node.getElemIterable().forEach(bounds::add);

        assertEquals(30, bounds.get(0).getUpperBound());
        assertEquals(false,bounds.get(0).isUpperBoundIncluded());
        assertEquals(50, bounds.get(1).getUpperBound());
        assertEquals(true,bounds.get(1).isUpperBoundIncluded());
    }

    @Test
    public void cddTrue_RootNodeIsTrueNode() throws CddAlreadyRunningException, CddNotRunningException {
        CDD.init(100,100,100);
        List<Clock> clocks = new ArrayList<>();
        clocks.add(new Clock("a", "A"));
        CDD.addClocks(clocks);

        CDD trueNode = CDD.cddTrue();

        assertTrue(trueNode.getRoot().isTrueTerminal());

        trueNode.free();
    }

    @Test
    public void cddFalse_RootNodeIsFalseNode() throws CddAlreadyRunningException, CddNotRunningException {
        CDD.init(100,100,100);
        List<Clock> clocks = new ArrayList<>();
        clocks.add(new Clock("a", "A"));
        CDD.addClocks(clocks);

        CDD falseNode = CDD.cddFalse();

        assertTrue(falseNode.getRoot().isFalseTerminal());

        falseNode.free();
    }

    @Test
    public void isTerminal_trueNodeShouldBeTerminal() throws CddAlreadyRunningException, CddNotRunningException {
        CDD.init(100,100,100);
        List<Clock> clocks = new ArrayList<>();
        clocks.add(new Clock("a", "A"));
        CDD.addClocks(clocks);

        CDD trueNode = CDD.cddTrue();

        assertTrue(trueNode.isTerminal());

        trueNode.free();
    }

    @Test
    public void isTerminal_shouldNotBeTerminal() throws CddAlreadyRunningException, CddNotRunningException {
        CDD.init(100,100,100);
        List<Clock> clocks = new ArrayList<>();
        clocks.add(new Clock("a", "A"));
        clocks.add(new Clock("b", "A"));
        CDD.addClocks(clocks);

        CDD cdd1 = CDD.createInterval(2,1,3, true,5,true);
        CDD cdd2 = CDD.createInterval(2,1,4,true,6,true);

        CDD cdd3 = cdd1.conjunction(cdd2);

        assertFalse(cdd3.isTerminal());

        cdd1.free();
        cdd2.free();
        cdd3.free();
    }

    @Test
    public void createCddFromDbm() throws CddNotRunningException, CddAlreadyRunningException {
        CDD.init(100,100,100);
        List<Clock> clocks = new ArrayList<>();
        clocks.add(new Clock("a", "A"));
        CDD.addClocks(clocks);

        CDD cdd1 = CDD.createFromDbm(new int[]{1, 0, 80, 1}, 2);
        CDDNode node = cdd1.getRoot();

        cdd1.printDot();

        assertTrue(node.getSegmentAtIndex(0).getChild().isFalseTerminal());
        assertTrue(node.getSegmentAtIndex(1).getChild().isTrueTerminal());
        assertEquals(0, node.getSegmentAtIndex(0).getUpperBound());
        assertEquals(40, node.getSegmentAtIndex(1).getUpperBound());

        cdd1.free();
    }

    @Test(expected = CddNotRunningException.class)
    public void addClocksWithoutInitializing() throws CddNotRunningException {
        List<Clock> clocks = new ArrayList<>();
        clocks.add(new Clock("a", "A"));
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
        clocks.add(new Clock("a", "A"));
        CDD.addClocks(clocks);

        CDD cdd = CDD.createFromDbm(new int[]{1, 1, 11, 1}, 2);
        cdd.free();

        cdd = cdd.reduce();
        cdd.isNotFalse();
    }

    @Test(expected = NullPointerException.class)
    public void cddFreeingNullCDD() throws CddNotRunningException, CddAlreadyRunningException {
        CDD.init(100,100,100);
        List<Clock> clocks = new ArrayList<>();
        clocks.add(new Clock("a", "A"));
        CDD.addClocks(clocks);

        CDD cdd = CDD.createFromDbm(new int[]{1, 1, 11, 1}, 2);
        cdd.free();

        cdd.free();
    }

    @Test
    public void cddLowerBound() throws CddNotRunningException, CddAlreadyRunningException {
        CDD.init(100,100,100);
        List<Clock> clocks = new ArrayList<>();
        clocks.add(new Clock("a", "A"));
        CDD.addClocks(clocks);

        CDD cdd = CDD.createLower(1,0,3, true);
        CDDNode node = cdd.getRoot();

        cdd.printDot();

        assertEquals(3, node.getSegmentAtIndex(0).getUpperBound());
        assertEquals(CDD_INF>>1, node.getSegmentAtIndex(1).getUpperBound());

        cdd.free();

    }

    @Test
    public void cddUpperBound() throws CddNotRunningException, CddAlreadyRunningException {
        CDD.init(100,100,100);
        List<Clock> clocks = new ArrayList<>();
        clocks.add(new Clock("a", "A"));
        clocks.add(new Clock("b", "B"));
        CDD.addClocks(clocks);

        //CDD interval = CDD.allocateInterval(1,0,3, true,7, true);
        CDD cdd = CDD.createUpper(1,0,6,true);
        CDD cdd1 = CDD.createUpper(2,0,4,true);
        //CDD result = interval.conjunction(cdd);
        CDD result = cdd.conjunction(cdd1);

        CDDNode node = result.getRoot();
        System.out.println("here " + node);
        System.out.println(node.getSegmentAtIndex(0).getUpperBound());

        result.printDot(); // --> the CDD is correct, so I guess the test is wrong
        assertEquals(0, node.getSegmentAtIndex(0).getUpperBound());
        assertEquals(6, node.getSegmentAtIndex(1).getUpperBound());
        assertEquals(CDD_INF>>1, node.getSegmentAtIndex(2).getUpperBound());

        cdd.free();
    }

    @Test
    public void guardToCDDTest() throws CddNotRunningException, CddAlreadyRunningException {
        CDD.init(100,100,100);
        List<Clock> clocks = new ArrayList<>();
        Clock x = new Clock("x", "X");
        Clock y = new Clock("y", "Y");
        clocks.add(x);
        clocks.add(y);
        CDD.addClocks(clocks);


        Guard e2_g1 = new ClockGuard(x, null, 3,  Relation.GREATER_EQUAL);
        //Guard e2_g3 = new ClockGuard(x, null, 999,  Relation.LESS_THAN);
        Guard e2_g2 = new ClockGuard(y, null, 5,  Relation.LESS_EQUAL);

        List<Guard> g1 = new ArrayList<>();
        //  g1.add(new AndGuard(e2_g1, e2_g3));
        g1.add(e2_g1);
        g1.add(e2_g2);
        CDD res = new CDD(new OrGuard(g1));
        //res.printDot();
        CDD exp = CDD.cddTrue();
        exp = exp.conjunction(CDD.createInterval(1, 0, 3, true, CDD_INF/2, false));
        exp = exp.disjunction(CDD.createInterval(2, 0, 0,true, 5,true));
        System.out.println(exp.removeNegative().reduce().getGuard(clocks));
        System.out.println(res.removeNegative().reduce().getGuard(clocks));
        //exp.printDot();
        exp = exp.removeNegative().reduce();
        res = res.removeNegative().reduce();
        exp.printDot();
        res.printDot();
        //exp.printDot();
        assert(res.equiv(exp));
    }

    @Test
    public void cddAddBddvar() throws CddNotRunningException, CddAlreadyRunningException {
        CDD.init(100,100,100);
        List<Clock> clocks = new ArrayList<>();
        clocks.add(new Clock("a", "A"));
        CDD.addClocks(clocks);

        List<BoolVar> BVs = new ArrayList<>();
        BVs.add(new BoolVar("a", "aut", true));
        BVs.add(new BoolVar("b", "aut", true));
        BVs.add(new BoolVar("d", "aut", true));
        BVs.add(new BoolVar("c", "aut", true));
        BVs.add(new BoolVar("e", "aut", true));
        int level = CDD.addBooleans(BVs);
        assertEquals(1, level);
        BVs.clear();
        BVs.add(new BoolVar("f", "aut", true));
        BVs.add(new BoolVar("g", "aut", true));
        level = CDD.addBooleans(BVs);
        assertEquals(6, level);
    }


}
