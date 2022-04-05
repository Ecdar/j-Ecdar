package dbm;

import lib.DBMLib;
import logic.Refinement;
import models.*;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class DBMTest {
    private static final int DBM_INF = 2147483646;
    private static Refinement.State state1, state2, state3, state4, state5;
    private static Guard g1, g2, g3, g4, g5, g6, g7, g8;
    private static List<Clock> clockList = new ArrayList<>();

    @BeforeClass
    public static void setUpBeforeClass() {
        Location l1 = new Location("L0", new ArrayList<>(), false, false, false, false);
        SymbolicLocation sl1 = new SimpleLocation(l1);

        Clock x = new Clock("x");
        Clock y = new Clock("y");
        Clock z = new Clock("z");

        clockList.addAll(Arrays.asList(x, y, z));

        // STATES----------------------
        // From 0 to inf
        Zone z1 = new Zone(new int[]{1, 1, DBM_INF, 1});
        List<Zone> list1 = new ArrayList<>();
        list1.add(z1);

        state1 = new Refinement.State(sl1, new Federation(list1));

        // From 2 to inf
        Zone z2 = new Zone(new int[]{1, -3, DBM_INF, 1});
        List<Zone> list2 = new ArrayList<>();
        list2.add(z2);
        state2 = new Refinement.State(sl1, new Federation(list2));

        // From 0 to 5
        Zone z3 = new Zone(new int[]{1, 1, 11, 1});
        List<Zone> list3 = new ArrayList<>();
        list3.add(z3);
        state3 = new Refinement.State(sl1, new Federation(list3));

        // From 3 to 12
        Zone z4 = new Zone(new int[]{1, -5, 25, 1});
        List<Zone> list4 = new ArrayList<>();
        list4.add(z4);
        state4 = new Refinement.State(sl1, new Federation(list4));


        // GUARDS---------------------
        g1 = new Guard(x, 5, true, false);
        g2 = new Guard(x, 1, true, false);
        g3 = new Guard(x, 7, false, false);
        g4 = new Guard(x, 14, false, false);

        g5 = new Guard(x, 505, true, false);
        g6 = new Guard(y, 8, true, false);

    }

    @Test
    public void testDbmValid1() {
        assertTrue(DBMLib.dbm_isValid(new int[]{1, 1, DBM_INF, 1}, 2));
    }

    @Test
    public void testDbmValid2() {
        assertTrue(DBMLib.dbm_isValid(new int[]{1, 1, 1, 1}, 2));
    }

    @Test
    public void testDbmValid3() {
        assertTrue(DBMLib.dbm_isValid(new int[]{1, -3, 11, 1}, 2));
    }

    @Test
    public void testDbmNotValid1() {
        assertFalse(DBMLib.dbm_isValid(new int[]{0, 0, 0, 0}, 2));
    }

    @Test
    public void testDbmNotValid2() {
        assertFalse(DBMLib.dbm_isValid(new int[]{-1, 0, 0, 0}, 2));
    }

    @Test
    public void testRaw2Bound1() {
        assertEquals(0, DBMLib.raw2bound(1));
    }

    @Test
    public void testRaw2Bound2() {
        assertEquals(1073741823, DBMLib.raw2bound(DBM_INF));
    }

    @Test
    public void testBound2Raw1() {
        assertEquals(1, DBMLib.boundbool2raw(0, false));
    }

    @Test
    public void testBound2Raw2() {
        assertEquals(2147483647, DBMLib.boundbool2raw(1073741823, false));
    }

    @Test
    public void testDbmInit1() {
        assertArrayEquals(new int[]{1, 1, DBM_INF, 1}, DBMLib.dbm_init(new int[]{0, 0, 0, 0}, 2));
    }

    @Test
    public void testDbmInit2() {
        assertArrayEquals(new int[]{1, 1, 1, DBM_INF, 1, DBM_INF, DBM_INF, DBM_INF, 1},
                DBMLib.dbm_init(new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0}, 3)
        );
    }

    @Test
    public void testDbmConstrain1() {
        assertArrayEquals(new int[]{1, 1, 11, 1},
                DBMLib.dbm_constrainBound(new int[]{1, 1, DBM_INF, 1}, 2, 1, 0, 5, false)
        );
    }

    @Test
    public void testDbmConstrain2() {
        assertArrayEquals(new int[]{1, -3, 11, 1},
                DBMLib.dbm_constrainBound(new int[]{1, 1, 11, 1}, 2, 0, 1, -2, false)
        );
    }

    @Test
    public void testDbmReset1() {
        assertArrayEquals(new int[]{1, 1, 1, 1},
                DBMLib.dbm_updateValue(new int[]{1, -3, 11, 1}, 2, 1, 0)
        );
    }

    @Test
    public void testDbmReset2() {
        assertArrayEquals(new int[]{1, 1, 1, 1, 1, 1, 5, 5, 1},
                DBMLib.dbm_updateValue(new int[]{1, 1, 1, 7, 1, 7, 5, 5, 1}, 3, 1, 0)
        );
    }

    @Test
    public void testDbmFuture1() {
        assertArrayEquals(new int[]{1, 1, DBM_INF, 1}, DBMLib.dbm_up(new int[]{1, 1, 1, 1}, 2));
    }

    @Test
    public void testDbmFuture2() {
        assertArrayEquals(new int[]{1, -3, DBM_INF, 1}, DBMLib.dbm_up(new int[]{1, -3, 11, 1}, 2));
    }

    @Test
    public void testDbmIntersects1() {
        assertTrue(DBMLib.dbm_intersection(new int[]{1, 1, 11, 1}, new int[]{1, 1, DBM_INF, 1}, 2));
    }

    @Test
    public void testDbmIntersects2() {
        assertTrue(DBMLib.dbm_intersection(
                new int[]{1, -9, 1, 1, DBM_INF, 1, DBM_INF, DBM_INF, DBM_INF, DBM_INF, 1, DBM_INF, DBM_INF, DBM_INF, DBM_INF, 1},
                new int[]{1, 1, 1, 1, 13, 1, 13, 13, DBM_INF, DBM_INF, 1, DBM_INF, DBM_INF, DBM_INF, DBM_INF, 1}, 4)
        );
    }

    @Test
    public void testDbmIntersects3() {
        assertTrue(DBMLib.dbm_intersection(
                new int[]{1, 1, -29, 1, DBM_INF, 1, DBM_INF, DBM_INF, DBM_INF, DBM_INF, 1, DBM_INF, DBM_INF, DBM_INF, DBM_INF, 1},
                new int[]{1, 1, 1, 1, 13, 1, 13, 13, DBM_INF, DBM_INF, 1, DBM_INF, DBM_INF, DBM_INF, DBM_INF, 1}, 4)
        );
    }

    @Test
    public void testDbmNotIntersects1() {
        assertFalse(DBMLib.dbm_intersection(new int[]{1, 1, 11, 1}, new int[]{1, -15, DBM_INF, 1}, 2));
    }

    @Test
    public void testDbmNotIntersects2() {
        assertFalse(DBMLib.dbm_intersection(
                new int[]{1, 1, 1, 1, 11, 1, 11, 11, DBM_INF, DBM_INF, 1, DBM_INF, DBM_INF, DBM_INF, DBM_INF, 1},
                new int[]{1, -15, 1, 1, DBM_INF, 1, DBM_INF, DBM_INF, DBM_INF, DBM_INF, 1, DBM_INF, DBM_INF, DBM_INF, DBM_INF, 1}, 4)
        );
    }

    @Test
    public void testDbmFreeAllDown1() {
        assertArrayEquals(new int[]{1, 1, 11, 1}, DBMLib.dbm_freeAllDown(new int[]{1, -3, 11, 1}, 2));
    }

    @Test
    public void testDbmFreeAllDown2() {
        assertArrayEquals(new int[]{1, 1, 11, 1}, DBMLib.dbm_freeAllDown(new int[]{1, 1, 11, 1}, 2));
    }

    @Test
    public void testDbmFreeAllDown3() {
        assertArrayEquals(new int[]{1, 1, 1, 1, 15, 1, 15, 15, 23, 23, 1, 23, 115, 115, 115, 1},
                DBMLib.dbm_freeAllDown(new int[]{1, -9, -3, -27, 15, 1, 11, -13, 23, 13, 1, -5, 115, 105, 111, 1}, 4)
        );
    }

    @Test
    public void testDbmMinusDbm() {
        int dim = 3;

        int[] dbm1 = new int[]{1, 1, 1, DBM_INF, 1, DBM_INF, DBM_INF, DBM_INF, 1};
        int[] dbm2 = new int[]{1, 1, 1, DBM_INF, 1, DBM_INF, DBM_INF, DBM_INF, 1};

        dbm2 = DBMLib.dbm_constrainBound(dbm2, dim, 0, 1, -2, false);
        dbm2 = DBMLib.dbm_constrainBound(dbm2, dim, 0, 2, -3, false);
        dbm2 = DBMLib.dbm_constrainBound(dbm2, dim, 1, 0, 4, false);
        dbm2 = DBMLib.dbm_constrainBound(dbm2, dim, 2, 0, 5, false);

        int[][] arr1 = DBMLib.dbm_minus_dbm(dbm1, dbm2, dim);
        Federation fed1 = new Federation(arr1);

//        for (Zone zone : fed1.getZones()) {
//            zone.printDBM(true, true);
//        }

        int[] dbm3 = new int[]{1, 1, 1, DBM_INF, 1, DBM_INF, DBM_INF, DBM_INF, 1};

        dbm3 = DBMLib.dbm_constrainBound(dbm3, dim, 0, 1, 0, false);
        dbm3 = DBMLib.dbm_constrainBound(dbm3, dim, 0, 2, 0, false);
        dbm3 = DBMLib.dbm_constrainBound(dbm3, dim, 1, 0, 1, false);
        dbm3 = DBMLib.dbm_constrainBound(dbm3, dim, 2, 0, 1, false);

        int[][] arr2 = DBMLib.fed_minus_dbm(arr1, dbm3, dim);
        Federation fed2 = new Federation(arr2);

//        for (Zone zone : fed2.getZones()) {
//            zone.printDBM(true, true);
//        }

        assertEquals(fed1.size(), 4);
        assertEquals(fed2.size(), 5);
    }

    @Test
    public void testDBMCustom() {
        int[] t1 = new int[]{1, 1, 1, 7, 1, 1, 13, 7, 1};
        int[] max = new int[]{0, 2, 5};

//        t1 = DBMLib.dbm_zero(t1, 3);
//        t1 = DBMLib.dbm_up(t1, 3);
//
//        t1 = DBMLib.dbm_constrainBound(t1, 3, 1, 0, 5, false);
//
//        t1 = DBMLib.dbm_updateValue(t1, 3, 1, 0);
//        t1 = DBMLib.dbm_up(t1, 3);
//        t1 = DBMLib.dbm_constrainBound(t1, 3, 1, 0, 5, false);
//
//        t1 = DBMLib.dbm_updateValue(t1, 3, 1, 0);
//        t1 = DBMLib.dbm_up(t1, 3);
//        t1 = DBMLib.dbm_constrainBound(t1, 3, 1, 0, 5, false);
//        printDBM(t1, true, true);
//
//
//        t1 = DBMLib.dbm_extrapolateMaxBounds(t1, 3, max);
//        printDBM(t1, true, true);

        assertTrue(true);
    }
}
