package models;

import org.junit.BeforeClass;
import org.junit.Test;

public class GuardTest {
    private static Clock x;

    @BeforeClass
    public static void setUpBeforeClass() {
        x = new Clock("x");
    }

    @Test
    public void test1() {
        Guard g = new Guard(x, 5, true, true);
        assert g.getLowerBound() == 6;
        assert g.getUpperBound() == Integer.MAX_VALUE;
    }

    @Test
    public void test2() {
        Guard g = new Guard(x, 5, false, true);
        assert g.getLowerBound() == 0;
        assert g.getUpperBound() == 4;
    }

    @Test
    public void test3() {
        Guard g = new Guard(x, 5, true, false);
        assert g.getLowerBound() == 5;
        assert g.getUpperBound() == Integer.MAX_VALUE;
    }

    @Test
    public void test4() {
        Guard g = new Guard(x, 5, false, false);
        assert g.getLowerBound() == 0;
        assert g.getUpperBound() == 5;
    }
}
