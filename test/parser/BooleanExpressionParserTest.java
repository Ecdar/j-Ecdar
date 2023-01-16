package parser;

import models.*;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class BooleanExpressionParserTest {

    @Test
    public void testGuardParser(){
        List<BoolVar> BVs = new ArrayList<>();
        ArrayList<Clock> clocks = new ArrayList<Clock>() {{add(new Clock("x", "Aut"));}};
        ClockExpression guard = (ClockExpression) ExpressionParser.parse("x>5", clocks, BVs);

        assertThat(guard, instanceOf(ClockExpression.class));
        assertEquals(Relation.GREATER_THAN, guard.getRelation());
        assertEquals(5, guard.getLowerBound());
    }

    @Test
    public void testGuardParserAnd(){
        List<BoolVar> BVs = new ArrayList<>();
        ArrayList<Clock> clocks = new ArrayList<Clock>() {{add(new Clock("x", "Aut")); add(new Clock("y", "Aut")); }};
        AndExpression AndGuard = (AndExpression) ExpressionParser.parse("x>=5 && y<6", clocks, BVs);

        ClockExpression guard = (ClockExpression)AndGuard.getExpressions().get(0);
        assertEquals(Relation.GREATER_EQUAL, guard.getRelation());
        assertEquals(5, guard.getLowerBound());

        ClockExpression guard1 = (ClockExpression)AndGuard.getExpressions().get(1);
        assertEquals(Relation.LESS_THAN, guard1.getRelation());
        assertEquals(6, guard1.getUpperBound());
    }

    @Test
    public void testGuardParserOr(){
        List<BoolVar> BVs = new ArrayList<>();
        ArrayList<Clock> clocks = new ArrayList<Clock>() {{add(new Clock("x", "Aut")); add(new Clock("y", "Aut")); }};
        OrExpression orGuard = (OrExpression) ExpressionParser.parse("x>2 || y<5", clocks, BVs);

        ClockExpression guard = (ClockExpression)orGuard.getExpressions().get(0);
        assertEquals(Relation.GREATER_THAN, guard.getRelation());
        assertEquals(2, guard.getLowerBound());

        ClockExpression guard1 = (ClockExpression)orGuard.getExpressions().get(1);
        assertEquals(Relation.LESS_THAN, guard1.getRelation());
        assertEquals(5, guard1.getUpperBound());
    }

    @Test
    public void testGuardParserOuterAnd(){
        List<BoolVar> BVs = new ArrayList<>();
        ArrayList<Clock> clocks = new ArrayList<Clock>() {{add(new Clock("x", "Aut")); add(new Clock("y", "Aut")); }};
        AndExpression andGuard = (AndExpression) ExpressionParser.parse("(x>2 || y<5) && x>=1", clocks, BVs);

        assertThat(andGuard.getExpressions().get(0), instanceOf(OrExpression.class));
        OrExpression orGuard = (OrExpression)andGuard.getExpressions().get(0);

        ClockExpression guard1 = (ClockExpression)orGuard.getExpressions().get(0);
        assertEquals(Relation.GREATER_THAN, guard1.getRelation());
        assertEquals(2, guard1.getLowerBound());

        ClockExpression guard2 = (ClockExpression)orGuard.getExpressions().get(1);
        assertEquals(Relation.LESS_THAN, guard2.getRelation());
        assertEquals(5, guard2.getUpperBound());

        ClockExpression guard3 = (ClockExpression)andGuard.getExpressions().get(1);
        assertEquals(Relation.GREATER_EQUAL, guard3.getRelation());
        assertEquals(1, guard3.getLowerBound());
    }

    @Test
    public void testGuardParserOuterAnd2(){
        List<BoolVar> BVs = new ArrayList<>();
        ArrayList<Clock> clocks = new ArrayList<Clock>() {{add(new Clock("x", "Aut")); add(new Clock("y", "Aut")); }};
        AndExpression andGuard = (AndExpression) ExpressionParser.parse("(x>2 || y<5) && (x>3 || y<6))", clocks, BVs);

        assertThat(andGuard.getExpressions().get(0), instanceOf(OrExpression.class));
        assertThat(andGuard.getExpressions().get(1), instanceOf(OrExpression.class));
    }

    @Test
    public void testGuardParserDiagionalClock(){
        List<BoolVar> BVs = new ArrayList<>();
        ArrayList<Clock> clocks = new ArrayList<>() {{add(new Clock("x", "Aut")); add(new Clock("y", "Aut")); }};
        ClockExpression clockGuard = (ClockExpression) ExpressionParser.parse("x-y<3", clocks, BVs);

        assertEquals("x" ,clockGuard.getClock().getUniqueName());
        assertEquals("y" ,clockGuard.getDiagonalClock().getUniqueName());
        assertEquals(3, clockGuard.getUpperBound());
    }
}
