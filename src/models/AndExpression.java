package models;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.*;
import java.util.stream.Collectors;

public class AndExpression extends BooleanExpression {
    private List<BooleanExpression> booleanExpressions;

    public AndExpression(List<BooleanExpression> booleanExpressions) {
        this.booleanExpressions = booleanExpressions;

        /* If any of the expressions are AndExpressions themselves,
         *   then we can decompose their expressions to be contained in this */
        List<AndExpression> worklist = this.booleanExpressions
                .stream()
                .filter(expr -> expr instanceof AndExpression)
                .map(expr -> (AndExpression) expr)
                .collect(Collectors.toList());
        while (!worklist.isEmpty()) {
            AndExpression current = worklist.get(0);
            worklist.remove(0);

            for (BooleanExpression booleanExpression : current.booleanExpressions) {
                if (booleanExpression instanceof AndExpression) {
                    worklist.add((AndExpression) booleanExpression);
                }
                this.booleanExpressions.add(booleanExpression);
            }

            this.booleanExpressions.remove(current);
        }

        /* If the AndExpression contains a FalseExpression,
         *   then remove all expressions and just have a single
         *   FalseExpression as it will always be false. */
        boolean hasFalseExpr = this.booleanExpressions.stream().anyMatch(expr -> expr instanceof FalseExpression);
        if (hasFalseExpr) {
            /* We just know that there is at least one FalseExpression for this
             *   reason we clear all expressions as it handle multiple FalseExpression
             *   then we just add a FalseExpression to account for all of them.
             *   If we left it empty then it would be interpreted as a tautology. */
            this.booleanExpressions.clear();
            this.booleanExpressions.add(new FalseExpression());
        }

        // Remove all true expressions
        this.booleanExpressions = this.booleanExpressions.stream().filter(expr -> !(expr instanceof TrueExpression)).collect(Collectors.toList());

        // If empty then it is a tautology
        if (this.booleanExpressions.size() == 0) {
            this.booleanExpressions.add(new TrueExpression());
        }
    }

    public AndExpression(List<BooleanExpression>... expressions) {
        this(Lists.newArrayList(Iterables.concat(expressions)));
    }

    public AndExpression(BooleanExpression... booleanExpressions) {
        this(Arrays.asList(booleanExpressions));
    }

    public AndExpression(AndExpression copy, List<Clock> newClocks, List<Clock> oldClocks, List<BoolVar> newBVs, List<BoolVar> oldBVs) {
        // As this is the copy-constructor we need to create new instances of the expressions
        this(copy.booleanExpressions.stream().map(expr ->
            expr.copy(newClocks, oldClocks, newBVs, oldBVs)
        ).collect(Collectors.toList()));
    }

    public List<BooleanExpression> getExpressions() {
        return booleanExpressions;
    }

    @Override
    int getMaxConstant(Clock clock) {
        int max = 0;
        for (BooleanExpression booleanExpression : booleanExpressions) {
            max = Math.max(max, booleanExpression.getMaxConstant(clock));
        }
        return max;
    }

    @Override
    BooleanExpression copy(List<Clock> newClocks, List<Clock> oldClocks, List<BoolVar> newBVs, List<BoolVar> oldBVs) {
        return new AndExpression(
            this, newClocks, oldClocks, newBVs, oldBVs
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof AndExpression)) {
            return false;
        }

        AndExpression other = (AndExpression) obj;
        return Arrays.equals(booleanExpressions.toArray(), other.booleanExpressions.toArray());
    }

    @Override
    public String toString() {
        if (booleanExpressions.size() == 1) {
            return booleanExpressions.get(0).toString();
        }

        return "(" +
                booleanExpressions.stream().map(BooleanExpression::toString).collect(Collectors.joining(" && "))
                + ")";
    }

    @Override
    public int hashCode() {
        return Objects.hash(booleanExpressions);
    }

    @Override
    public String prettyPrint() {
        return BooleanExpression.compositePrettyPrint(booleanExpressions, "&&");
    }
}
