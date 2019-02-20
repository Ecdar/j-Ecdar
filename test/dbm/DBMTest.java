package dbm;

import global.LibLoader;
import lib.DBMLib;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

public class DBMTest {
    int[] t1, t2;

    @BeforeClass
    public static void setUpBeforeClass() {
        LibLoader.load();
    }

    @Test
    public void testDbmValid1() {
        t1 = new int[]{1, 1, 2147483646, 1};
        assertTrue(DBMLib.dbm_isValid(t1, 2));
    }

    @Test
    public void testDbmValid2() {
        t1 = new int[]{1, 1, 1, 1};
        assertTrue(DBMLib.dbm_isValid(t1, 2));
    }

    @Test
    public void testDbmValid3() {
        t1 = new int[]{1, -3, 11, 1};
        assertTrue(DBMLib.dbm_isValid(t1, 2));
    }

    @Test
    public void testDbmNotValid1() {
        t1 = new int[]{0, 0, 0, 0};
        assertFalse(DBMLib.dbm_isValid(t1, 2));
    }

    @Test
    public void testDbmNotValid2() {
        t1 = new int[]{-1, 0, 0, 0};
        assertFalse(DBMLib.dbm_isValid(t1, 2));
    }

    @Test
    public void testRaw2Bound1(){
        assertEquals(0, DBMLib.raw2bound(1));
    }

    @Test
    public void testRaw2Bound2(){
        assertEquals(1073741823, DBMLib.raw2bound(2147483646));
    }

    @Test
    public void testBound2Raw1(){
        assertEquals(1, DBMLib.boundbool2raw(0, false));
    }

    @Test
    public void testBound2Raw2(){
        assertEquals(2147483647, DBMLib.boundbool2raw(1073741823, false));
    }

    @Test
    public void testDbmInit1() {
        t1 = new int[]{1, 1, 2147483646, 1};
        t2 = new int[]{0, 0, 0, 0};
        assertArrayEquals(t1, DBMLib.dbm_init(t2, 2));
    }

    @Test
    public void testDbmInit2() {
        t1 = new int[]{1, 1, 1, 2147483646, 1, 2147483646, 2147483646, 2147483646, 1};
        t2 = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0};
        assertArrayEquals(t1, DBMLib.dbm_init(t2, 3));
    }

    @Test
    public void testDbmConstrain1() {
        t1 = new int[]{1, 1, 11, 1};
        t2 = new int[]{1, 1, 2147483646, 1};
        assertArrayEquals(t1, DBMLib.dbm_constrain1(t2, 2, 1, 0, 5));
    }

    @Test
    public void testDbmConstrain2() {
        t1 = new int[]{1, -3, 11, 1};
        t2 = new int[]{1, 1, 11, 1};
        assertArrayEquals(t1, DBMLib.dbm_constrain1(t2, 2, 0, 1, -2));
    }

    @Test
    public void testDbmReset1() {
        t1 = new int[]{1, 1, 1, 1};
        t2 = new int[]{1, -3, 11, 1};
        assertArrayEquals(t1, DBMLib.dbm_updateValue(t2, 2, 1, 0));
    }

    @Test
    public void testDbmReset2() {
        t1 = new int[]{1, 1, 1, 1, 1, 1, 5, 5, 1};
        t2 = new int[]{1, 1, 1, 7, 1, 7, 5, 5, 1};
        assertArrayEquals(t1, DBMLib.dbm_updateValue(t2, 3, 1, 0));
    }

    @Test
    public void testDbmFuture1() {
        t1 = new int[]{1, 1, 2147483646, 1};
        t2 = new int[]{1, 1, 1, 1};
        assertArrayEquals(t1, DBMLib.dbm_up(t2, 2));
    }

    @Test
    public void testDbmFuture2() {
        t1 = new int[]{1, -3, 2147483646, 1};
        t2 = new int[]{1, -3, 11, 1};
        assertArrayEquals(t1, DBMLib.dbm_up(t2, 2));
    }

}
