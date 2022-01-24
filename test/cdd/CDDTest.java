package cdd;

import lib.CDDLib;
import models.CDD;
import models.CDDNode;
import models.Elem;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class CDDTest {

    static final int CDD_INF = 2147483646;

    @After
    public void afterEachTest(){
        CDDLib.cddDone();
    }

    @Test
    public void testCddInit(){
        assertEquals(0, CDD.init(0,0,0));
    }

    @Test
    public void testConjunctionSameTypeWithOverlap(){
        CDD.init(100,100,100);
        CDD.addClocks(3);
        CDD cdd1 = CDD.allocateInterval(2,1,3,5);
        CDD cdd2 = CDD.allocateInterval(2,1,4,6);

        CDD cdd3 = cdd1.conjunction(cdd2);
        CDDNode node = cdd3.getRoot();

        assertEquals(4, node.getElemAtIndex(0).getBound());
        assertEquals(5, node.getElemAtIndex(1).getBound());

        CDD.free(cdd1);
        CDD.free(cdd2);
        CDD.free(cdd3);
    }

    @Test
    public void testDisjunction(){
        CDD.init(100,100,100);
        CDD.addClocks(3);
        CDD cdd1 = CDD.allocateInterval(2,1,3,5);
        CDD cdd2 = CDD.allocateInterval(2,1,4,6);

        CDD cdd3 = cdd1.disjunction(cdd2);
        CDDNode node = cdd3.getRoot();

        assertEquals(3, node.getElemAtIndex(0).getBound());
        assertEquals(6, node.getElemAtIndex(1).getBound());

        CDD.free(cdd1);
        CDD.free(cdd2);
        CDD.free(cdd3);
    }

    @Test
    public void getCorrectBounds(){
        CDD.init(100,100,100);
        CDD.addClocks(2);
        CDD cdd1 = CDD.allocateInterval(1,0,3,5);
        CDDNode node = cdd1.getRoot();
        List<Elem> bounds = new ArrayList<>();

        node.getElemIterable().forEach(bounds::add);

        assertEquals(3, bounds.get(0).getBound());
        assertEquals(5, bounds.get(1).getBound());
    }

    @Test
    public void createCddFromDbm(){
        CDD.init(100,100,100);
        CDD.addClocks(2);

        CDD cdd1 = CDD.allocateFromDbm(new int[]{1, 1, 11, 1}, 2);
        CDDNode node = cdd1.getRoot();

        assertEquals(0, node.getElemAtIndex(0).getBound());
        assertEquals(11, node.getElemAtIndex(1).getBound());

        CDD.free(cdd1);
    }

    @Test
    public void cddLowerBound(){
        CDD.init(100,100,100);
        CDD.addClocks(2);

        CDD cdd = CDD.allocateLower(1,0,3);
        CDDNode node = cdd.getRoot();

        assertEquals(3, node.getElemAtIndex(0).getBound());
        assertEquals(CDD_INF, node.getElemAtIndex(1).getBound());

        CDD.free(cdd);

    }

    @Test
    public void cddUpperBound(){
        CDD.init(100,100,100);
        CDD.addClocks(2);

        CDD cdd = CDD.allocateUpper(1,0,6);
        CDDNode node = cdd.getRoot();

        assertEquals(0, node.getElemAtIndex(0).getBound());
        assertEquals(6, node.getElemAtIndex(1).getBound());

        CDD.free(cdd);
    }

    @Test
    public void cddAddBddvar(){
        CDD.init(100,100,100);
        CDD.addClocks(2);

        int level = CDD.addBddvar(5);
        assertEquals(1, level);
        level = CDD.addBddvar(2);
        assertEquals(6, level);
    }
}
