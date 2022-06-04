package parser;

import models.*;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class GuardParserTest {

    @Test
    public void testGuardParser(){
        List<BoolVar> BVs = new ArrayList<>();
        ArrayList<Clock> clocks = new ArrayList<Clock>() {{add(new Clock("x"));}};
        ClockGuard guard = (ClockGuard) GuardParser.parse("x>5", clocks, BVs);

        assertThat(guard, instanceOf(ClockGuard.class));
        assertEquals(Relation.GREATER_THAN, guard.getRelation());
        assertEquals(5, guard.getLowerBound());
    }

    @Test
    public void testGuardParserAnd(){
        List<BoolVar> BVs = new ArrayList<>();
        ArrayList<Clock> clocks = new ArrayList<Clock>() {{add(new Clock("x")); add(new Clock("y")); }};
        AndGuard AndGuard = (AndGuard)GuardParser.parse("x>=5 && y<6", clocks, BVs);

        ClockGuard guard = (ClockGuard)AndGuard.getGuards().get(0);
        assertEquals(Relation.GREATER_EQUAL, guard.getRelation());
        assertEquals(5, guard.getLowerBound());

        ClockGuard guard1 = (ClockGuard)AndGuard.getGuards().get(1);
        assertEquals(Relation.LESS_THAN, guard1.getRelation());
        assertEquals(6, guard1.getUpperBound());
    }

    @Test
    public void testGuardParserOr(){
        List<BoolVar> BVs = new ArrayList<>();
        ArrayList<Clock> clocks = new ArrayList<Clock>() {{add(new Clock("x")); add(new Clock("y")); }};
        OrGuard orGuard = (OrGuard) GuardParser.parse("x>2 || y<5", clocks, BVs);

        ClockGuard guard = (ClockGuard)orGuard.getGuards().get(0);
        assertEquals(Relation.GREATER_THAN, guard.getRelation());
        assertEquals(2, guard.getLowerBound());

        ClockGuard guard1 = (ClockGuard)orGuard.getGuards().get(1);
        assertEquals(Relation.LESS_THAN, guard1.getRelation());
        assertEquals(5, guard1.getUpperBound());
    }
}
