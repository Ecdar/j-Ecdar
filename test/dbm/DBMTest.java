package dbm;

import global.LibLoader;
import lib.DBMLib;
import logic.SimpleLocation;
import logic.State;
import logic.SymbolicLocation;
import logic.Zone;
import models.Clock;
import models.Guard;
import models.Location;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static features.Helpers.printDBM;
import static org.junit.Assert.*;

public class DBMTest {
    private static final int DBM_INF = 2147483646;
    private static State state1, state2, state3, state4, state5;
    private static Guard g1, g2, g3, g4, g5, g6, g7, g8;
    private static List<Clock> clockList = new ArrayList<>();

    @BeforeClass
    public static void setUpBeforeClass() {
        LibLoader.load();

        Location l1 = new Location("L0", new Guard[]{}, false, false, false, false);
        SymbolicLocation sl1 = new SimpleLocation(l1);

        Clock x = new Clock("x");
        Clock y = new Clock("y");
        Clock z = new Clock("z");

        clockList.addAll(Arrays.asList(x, y, z));

        // STATES----------------------
        // From 0 to inf
        Zone z1 = new Zone(new int[]{1, 1, DBM_INF, 1});
        state1 = new State(sl1, z1);

        // From 2 to inf
        Zone z2 = new Zone(new int[]{1, -3, DBM_INF, 1});
        state2 = new State(sl1, z2);

        // From 0 to 5
        Zone z3 = new Zone(new int[]{1, 1, 11, 1});
        state3 = new State(sl1, z3);

        // From 3 to 12
        Zone z4 = new Zone(new int[]{1, -5, 25, 1});
        state4 = new State(sl1, z4);


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
                DBMLib.dbm_constrain1(new int[]{1, 1, DBM_INF, 1}, 2, 1, 0, 5, false)
        );
    }

    @Test
    public void testDbmConstrain2() {
        assertArrayEquals(new int[]{1, -3, 11, 1},
                DBMLib.dbm_constrain1(new int[]{1, 1, 11, 1}, 2, 0, 1, -2, false)
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
    public void testAZNoGuards1() {
        Zone t1 = state1.getZone().getAbsoluteZone(new ArrayList<>(), new ArrayList<>());

        assertArrayEquals(t1.getDbm(), new int[]{1, 1, DBM_INF, 1});
    }

    @Test
    public void testAZNoGuards2() {
        Zone t1 = state2.getZone().getAbsoluteZone(new ArrayList<>(), new ArrayList<>());

        assertArrayEquals(t1.getDbm(), new int[]{1, 1, DBM_INF, 1});
    }

    @Test
    public void testAZNoGuards3() {
        Zone t1 = state3.getZone().getAbsoluteZone(new ArrayList<>(), new ArrayList<>());

        assertArrayEquals(t1.getDbm(), new int[]{1, 1, 11, 1});
    }

    @Test
    public void testAZNoGuards4() {
        Zone t1 = state4.getZone().getAbsoluteZone(new ArrayList<>(), new ArrayList<>());

        assertArrayEquals(t1.getDbm(), new int[]{1, 1, 19, 1});
    }

    @Test
    public void testAZWithGuards1() {
        List<Guard> guardList1 = new ArrayList<>(Collections.singletonList(g1));
        List<Guard> guardList2 = new ArrayList<>(Collections.singletonList(g2));

        Zone t1 = state4.getZone().getAbsoluteZone(guardList1, clockList);
        Zone t2 = state4.getZone().getAbsoluteZone(guardList2, clockList);

        assertArrayEquals(t1.getDbm(), new int[]{1, -3, 19, 1});
        assertArrayEquals(t2.getDbm(), new int[]{1, 1, 19, 1});
    }

    @Test
    public void testAZWithGuards2() {
        List<Guard> guardList1 = new ArrayList<>(Collections.singletonList(g3));
        List<Guard> guardList2 = new ArrayList<>(Collections.singletonList(g4));

        Zone t1 = state4.getZone().getAbsoluteZone(guardList1, clockList);
        Zone t2 = state4.getZone().getAbsoluteZone(guardList2, clockList);

        assertArrayEquals(t1.getDbm(), new int[]{1, 1, 9, 1});
        assertArrayEquals(t2.getDbm(), new int[]{1, 1, 19, 1});
    }

    @Test
    public void testAZWithGuards3() {
        List<Guard> guardList1 = new ArrayList<>(Collections.singletonList(g1));
        List<Guard> guardList2 = new ArrayList<>(Collections.singletonList(g3));
        List<Guard> guardList3 = new ArrayList<>(Collections.singletonList(g2));

        Zone t1 = state2.getZone().getAbsoluteZone(guardList1, clockList);
        Zone t2 = state2.getZone().getAbsoluteZone(guardList2, clockList);
        Zone t3 = state2.getZone().getAbsoluteZone(guardList3, clockList);

        assertArrayEquals(t1.getDbm(), new int[]{1, -5, DBM_INF, 1});
        assertArrayEquals(t2.getDbm(), new int[]{1, 1, 11, 1});
        assertArrayEquals(t3.getDbm(), new int[]{1, 1, DBM_INF, 1});
    }

    @Test
    public void testAZWithGuards4() {
        List<Guard> guardList1 = new ArrayList<>(Collections.singletonList(g1));
        List<Guard> guardList2 = new ArrayList<>(Collections.singletonList(g2));
        List<Guard> guardList3 = new ArrayList<>(Collections.singletonList(g3));
        List<Guard> guardList4 = new ArrayList<>(Collections.singletonList(g4));

        Zone t1 = state3.getZone().getAbsoluteZone(guardList1, clockList);
        Zone t2 = state3.getZone().getAbsoluteZone(guardList2, clockList);
        Zone t3 = state3.getZone().getAbsoluteZone(guardList3, clockList);
        Zone t4 = state3.getZone().getAbsoluteZone(guardList4, clockList);

        assertArrayEquals(t1.getDbm(), new int[]{1, -9, 11, 1});
        assertArrayEquals(t2.getDbm(), new int[]{1, -1, 11, 1});
        assertArrayEquals(t3.getDbm(), new int[]{1, 1, 11, 1});
        assertArrayEquals(t4.getDbm(), new int[]{1, 1, 11, 1});
    }

    @Test
    public void testAZWithMultipleGuards1() {
        List<Guard> guardList1 = new ArrayList<>(Arrays.asList(g1, g3));
        List<Guard> guardList2 = new ArrayList<>(Arrays.asList(g3, g1));
        List<Guard> guardList3 = new ArrayList<>(Arrays.asList(g2, g4));

        Zone zone = new Zone(new int[]{1, -5, 25, 1});

        Zone t1 = zone.getAbsoluteZone(guardList1, clockList);
        Zone t2 = zone.getAbsoluteZone(guardList2, clockList);
        Zone t3 = zone.getAbsoluteZone(guardList3, clockList);

        assertArrayEquals(t1.getDbm(), new int[]{1, -3, 9, 1});
        assertArrayEquals(t2.getDbm(), new int[]{1, -3, 9, 1});
        assertArrayEquals(t3.getDbm(), new int[]{1, 1, 19, 1});
    }

    @Test
    public void testAZWithMultipleGuards2() {
        List<Guard> guardList1 = new ArrayList<>(Arrays.asList(g1, g3));
        List<Guard> guardList2 = new ArrayList<>(Arrays.asList(g4, g3, g4));

        Zone t1 = state2.getZone().getAbsoluteZone(guardList1, clockList);
        Zone t2 = state2.getZone().getAbsoluteZone(guardList2, clockList);

        assertArrayEquals(t1.getDbm(), new int[]{1, -5, 11, 1});
        assertArrayEquals(t2.getDbm(), new int[]{1, 1, 11, 1});
    }

    @Test
    public void testZoneContainsNegatives1() {
        Zone newZone = new Zone(new int[]{1, -1, 11, 1});
        assertFalse(newZone.containsNegatives());
    }

    @Test
    public void testZoneContainsNegatives2() {
        Zone zone1 = new Zone(new int[]{1, 1, 1, DBM_INF, 1, DBM_INF, DBM_INF, DBM_INF, 1});
        Zone zone2 = new Zone(new int[]{1, 1, -7, 9, 1, 1, DBM_INF, DBM_INF, 1});
        Zone zone3 = new Zone(new int[]{1, 1, -8, 9, 1, 0, DBM_INF, DBM_INF, 1});


        assertFalse(zone1.containsNegatives());
        assertFalse(zone2.containsNegatives());
        assertTrue(zone3.containsNegatives());
    }

    @Test
    public void testAZIntersect1(){
        Zone zone1 = new Zone(new int[]{1, 1, 8, 1});
        Zone zone2 = new Zone(new int[]{1, -8, DBM_INF, 1});
        Zone zone3 = new Zone(new int[]{1, -5, DBM_INF, 1});
        Zone zone4 = new Zone(new int[]{1, -6, DBM_INF, 1});
        Zone zone5 = new Zone(new int[]{1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1});

        zone5.init();
        //zone5.constrain1();

        assertFalse(zone1.absoluteZonesIntersect(zone2));
        assertTrue(zone1.absoluteZonesIntersect(zone3));
        assertTrue(zone1.absoluteZonesIntersect(zone4));
        assertTrue(zone4.absoluteZonesIntersect(zone1));
        assertTrue(zone3.absoluteZonesIntersect(zone1));
        assertTrue(zone3.absoluteZonesIntersect(zone4));
    }

    @Test
    public void testUpdateLowerBounds1(){
        Zone prevZone = new Zone(new int[]{1, -999, 1, DBM_INF, 1, DBM_INF, DBM_INF, DBM_INF, 1});
        Zone targetZone = new Zone(prevZone);

        List<Guard> guardList = new ArrayList<>(Arrays.asList(g5, g6));

        Zone absZone = prevZone.getAbsoluteZone(guardList, clockList);

        Location l2 = new Location("L0", new Guard[]{}, false, false, false, false);
        SymbolicLocation sl2 = new SimpleLocation(l2);
        State state = new State(sl2, targetZone);

        state.applyGuards(guardList, clockList);

        prevZone.printDBM(false, true);
        state.getZone().printDBM(false, true);
        absZone.printDBM(false, true);

        state.getZone().updateLowerBounds(prevZone, absZone.getRawRowMax());
        state.getZone().printDBM(false, true);

        assertArrayEquals(state.getZone().getDbm(), new int[]{1, -1015, -15, DBM_INF, 1, DBM_INF, DBM_INF, DBM_INF, 1});
    }

    @Test
    public void testDBM(){
//        int[] t1 = new int[]{1, 1, 1, 1, 1, 1,
//                9, 1, 9, 9, 9, 9,
//                DBM_INF, DBM_INF, 1, DBM_INF, DBM_INF, DBM_INF,
//                DBM_INF, DBM_INF, DBM_INF, 1, DBM_INF, DBM_INF,
//                DBM_INF, DBM_INF, DBM_INF, DBM_INF, 1, DBM_INF,
//                DBM_INF, DBM_INF, DBM_INF, DBM_INF, DBM_INF, 1};

//        int[] t1 = new int[]{1, 1, 1, 1, 1, 1, 1, 1, 1};
//
//
//        t1 = DBMLib.dbm_init(t1, 3);
//
//        t1 = DBMLib.dbm_constrain1(t1, 3, 1, 0, 4, false);
//
//        printDBM(t1, true, true);
//
//        t1 = DBMLib.dbm_up(t1, 3);
//
//        printDBM(t1, true, true);
//
//        t1 = DBMLib.dbm_constrain1(t1, 3, 1, 0, 5, true);
//        t1 = DBMLib.dbm_constrain1(t1, 3, 0, 2, -8, false);
//
//        printDBM(t1, true, true);

    }
}
