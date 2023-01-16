package models;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.*;
import java.util.stream.Collectors;

public class OrExpression extends BooleanExpression {

    private List<BooleanExpression> booleanExpressions;

    public OrExpression(List<BooleanExpression> booleanExpressions) {
        this.booleanExpressions = booleanExpressions;

        /* If any of the expressions are OrExpressions themselves,
         *   then we can decompose their expressions to be contained in this */
        List<OrExpression> worklist = this.booleanExpressions
                .stream()
                .filter(expression -> expression instanceof OrExpression)
                .map(expression -> (OrExpression) expression)
                .collect(Collectors.toList());
        while (!worklist.isEmpty()) {
            OrExpression current = worklist.get(0);
            worklist.remove(0);

            for (BooleanExpression booleanExpression : current.booleanExpressions) {
                if (booleanExpression instanceof OrExpression) {
                    worklist.add((OrExpression) booleanExpression);
                }
                this.booleanExpressions.add(booleanExpression);
            }

            this.booleanExpressions.remove(current);
        }

        // Remove all expressions if there is a true expression
        boolean hasTrueExpression = this.booleanExpressions.stream().anyMatch(expression -> expression instanceof TrueExpression);
        if (hasTrueExpression) {
            /* If there are one or more TrueExpressions then we just need a single TrueExpression.
             *   It would be possible to just clear it and let the last predicate,
             *   ensure that the empty OrExpression is a tautology.
             *   However, this is more robust towards changes */
            this.booleanExpressions.clear();
            this.booleanExpressions.add(new TrueExpression());
        }

        // Remove all false expressions
        this.booleanExpressions = this.booleanExpressions.stream().filter(expression -> !(expression instanceof FalseExpression)).collect(Collectors.toList());

        // If there are no expressions then it is a tautology
        if (this.booleanExpressions.size() == 0) {
            this.booleanExpressions.add(new TrueExpression());
        }
    }

    public OrExpression(List<BooleanExpression>... expressions) {
        this(Lists.newArrayList(Iterables.concat(expressions)));
    }

    public OrExpression(BooleanExpression... booleanExpressions) {
        this(Arrays.asList(booleanExpressions));
    }

    public OrExpression(OrExpression copy, List<Clock> newClocks, List<Clock> oldClocks, List<BoolVar> newBVs, List<BoolVar> oldBVs) {
        // As this is the copy-constructor we need to create new instances of the expressions
        this(copy.booleanExpressions.stream().map(expression ->
            expression.copy(newClocks, oldClocks, newBVs, oldBVs)
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
        return new OrExpression(
            this, newClocks, oldClocks, newBVs, oldBVs
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof OrExpression)) {
            return false;
        }

        OrExpression other = (OrExpression) obj;
        return Arrays.equals(booleanExpressions.toArray(), other.booleanExpressions.toArray());
    }

    @Override
    public String toString() {
        if (booleanExpressions.size() == 1) {
            return booleanExpressions.get(0).toString();
        }

        return "(" +
                booleanExpressions.stream()
                    .map(BooleanExpression::toString)
                    .collect(Collectors.joining(" or "))
                + ")";
    }

    @Override
    public int hashCode() {
        return Objects.hash(false);
    }

    @Override
    public String prettyPrint() {
        return BooleanExpression.compositePrettyPrint(booleanExpressions, "||");
    }
}
