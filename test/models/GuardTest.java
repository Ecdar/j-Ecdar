package models;

import org.junit.BeforeClass;
import org.junit.Test;

public class GuardTest {
    private static Clock x;

    @BeforeClass
    public static void setUpBeforeClass() {
        x = new Clock("x", "Aut");
    }

    @Test
    public void test1() {
        ClockGuard g = new ClockGuard(x, 5, Relation.GREATER_THAN);
        assert g.getLowerBound() == 5;
        assert g.getUpperBound() == Integer.MAX_VALUE;
    }

    @Test
    public void test2() {
        ClockGuard g = new ClockGuard(x, 5, Relation.LESS_THAN);
        assert g.getLowerBound() == 0;
        assert g.getUpperBound() == 5;
    }

    @Test
    public void test3() {
        ClockGuard g = new ClockGuard(x, 5, Relation.GREATER_EQUAL);
        assert g.getLowerBound() == 5;
        assert g.getUpperBound() == Integer.MAX_VALUE;
    }

    @Test
    public void test4() {
        ClockGuard g = new ClockGuard(x, 5, Relation.LESS_EQUAL);
        assert g.getLowerBound() == 0;
        assert g.getUpperBound() == 5;
    }
}
