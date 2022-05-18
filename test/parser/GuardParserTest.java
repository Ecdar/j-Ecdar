package parser;

import models.Clock;
import models.Guard;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class GuardParserTest {

    @Test
    public void testGuardParser(){
        ArrayList<Clock> clocks = new ArrayList<Clock>() {{add(new Clock("x"));}};
        List<List<Guard>> guardList = GuardParser.parse("x>5", clocks);

        Guard guard = guardList.get(0).get(0);
        assertEquals(true, guard.isStrict());
        assertEquals(5, guard.getLowerBound());
    }

    @Test
    public void testGuardParserAnd(){
        ArrayList<Clock> clocks = new ArrayList<Clock>() {{add(new Clock("x")); add(new Clock("y")); }};
        List<List<Guard>> guardList = GuardParser.parse("x>=5 && y<6", clocks);

        Guard guard = guardList.get(0).get(0);
        assertEquals(false, guard.isStrict());
        assertEquals(5, guard.getLowerBound());

        Guard guard1 = guardList.get(0).get(1);
        assertEquals(true, guard1.isStrict());
        assertEquals(6, guard1.getUpperBound());
    }

    @Test
    public void testGuardParserOr(){
        ArrayList<Clock> clocks = new ArrayList<Clock>() {{add(new Clock("x")); add(new Clock("y")); }};
        List<List<Guard>> guardList = GuardParser.parse("x>2 || y<5", clocks);

        Guard guard1 = guardList.get(0).get(0);
        Guard guard2 = guardList.get(1).get(0);

        assertEquals(true, guard1.isStrict());
        assertEquals(2, guard1.getLowerBound());
        assertEquals(true, guard2.isStrict());
        assertEquals(5, guard2.getUpperBound());
    }
}
