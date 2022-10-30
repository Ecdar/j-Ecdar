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
        ArrayList<Clock> clocks = new ArrayList<Clock>() {{add(new Clock("x", "Aut"));}};
        ClockGuard guard = (ClockGuard) GuardParser.parse("x>5", clocks, BVs);

        assertThat(guard, instanceOf(ClockGuard.class));
        assertEquals(Relation.GREATER_THAN, guard.getRelation());
        assertEquals(5, guard.getLowerBound());
    }

    @Test
    public void testGuardParserAnd(){
        List<BoolVar> BVs = new ArrayList<>();
        ArrayList<Clock> clocks = new ArrayList<Clock>() {{add(new Clock("x", "Aut")); add(new Clock("y", "Aut")); }};
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
        ArrayList<Clock> clocks = new ArrayList<Clock>() {{add(new Clock("x", "Aut")); add(new Clock("y", "Aut")); }};
        OrGuard orGuard = (OrGuard) GuardParser.parse("x>2 || y<5", clocks, BVs);

        ClockGuard guard = (ClockGuard)orGuard.getGuards().get(0);
        assertEquals(Relation.GREATER_THAN, guard.getRelation());
        assertEquals(2, guard.getLowerBound());

        ClockGuard guard1 = (ClockGuard)orGuard.getGuards().get(1);
        assertEquals(Relation.LESS_THAN, guard1.getRelation());
        assertEquals(5, guard1.getUpperBound());
    }

    @Test
    public void testGuardParserOuterAnd(){
        List<BoolVar> BVs = new ArrayList<>();
        ArrayList<Clock> clocks = new ArrayList<Clock>() {{add(new Clock("x", "Aut")); add(new Clock("y", "Aut")); }};
        AndGuard andGuard = (AndGuard) GuardParser.parse("(x>2 || y<5) && x>=1", clocks, BVs);

        assertThat(andGuard.getGuards().get(0), instanceOf(OrGuard.class));
        OrGuard orGuard = (OrGuard)andGuard.getGuards().get(0);

        ClockGuard guard1 = (ClockGuard)orGuard.getGuards().get(0);
        assertEquals(Relation.GREATER_THAN, guard1.getRelation());
        assertEquals(2, guard1.getLowerBound());

        ClockGuard guard2 = (ClockGuard)orGuard.getGuards().get(1);
        assertEquals(Relation.LESS_THAN, guard2.getRelation());
        assertEquals(5, guard2.getUpperBound());

        ClockGuard guard3 = (ClockGuard)andGuard.getGuards().get(1);
        assertEquals(Relation.GREATER_EQUAL, guard3.getRelation());
        assertEquals(1, guard3.getLowerBound());
    }

    @Test
    public void testGuardParserOuterAnd2(){
        List<BoolVar> BVs = new ArrayList<>();
        ArrayList<Clock> clocks = new ArrayList<Clock>() {{add(new Clock("x", "Aut")); add(new Clock("y", "Aut")); }};
        AndGuard andGuard = (AndGuard) GuardParser.parse("(x>2 || y<5) && (x>3 || y<6))", clocks, BVs);

        assertThat(andGuard.getGuards().get(0), instanceOf(OrGuard.class));
        assertThat(andGuard.getGuards().get(1), instanceOf(OrGuard.class));
    }

    @Test
    public void testGuardParserDiagionalClock(){
        List<BoolVar> BVs = new ArrayList<>();
        ArrayList<Clock> clocks = new ArrayList<>() {{add(new Clock("x", "Aut")); add(new Clock("y", "Aut")); }};
        ClockGuard clockGuard = (ClockGuard) GuardParser.parse("x-y<3", clocks, BVs);

        assertEquals("x" ,clockGuard.getClock().getUniqueName());
        assertEquals("y" ,clockGuard.getDiagonalClock().getUniqueName());
        assertEquals(3, clockGuard.getUpperBound());
    }
}
