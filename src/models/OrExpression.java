package models;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.*;
import java.util.stream.Collectors;

public class OrExpression extends Expression {

    private List<Expression> expressions;

    public OrExpression(List<Expression> expressions) {
        this.expressions = expressions;

        /* If any of the expressions are OrExpressions themselves,
         *   then we can decompose their expressions to be contained in this */
        List<OrExpression> worklist = this.expressions
                .stream()
                .filter(expression -> expression instanceof OrExpression)
                .map(expression -> (OrExpression) expression)
                .collect(Collectors.toList());
        while (!worklist.isEmpty()) {
            OrExpression current = worklist.get(0);
            worklist.remove(0);

            for (Expression expression : current.expressions) {
                if (expression instanceof OrExpression) {
                    worklist.add((OrExpression) expression);
                }
                this.expressions.add(expression);
            }

            this.expressions.remove(current);
        }

        // Remove all expressions if there is a true expression
        boolean hasTrueExpression = this.expressions.stream().anyMatch(expression -> expression instanceof TrueExpression);
        if (hasTrueExpression) {
            /* If there are one or more TrueExpressions then we just need a single TrueExpression.
             *   It would be possible to just clear it and let the last predicate,
             *   ensure that the empty OrExpression is a tautology.
             *   However, this is more robust towards changes */
            this.expressions.clear();
            this.expressions.add(new TrueExpression());
        }

        // Remove all false expressions
        this.expressions = this.expressions.stream().filter(expression -> !(expression instanceof FalseExpression)).collect(Collectors.toList());

        // If there are no expressions then it is a tautology
        if (this.expressions.size() == 0) {
            this.expressions.add(new TrueExpression());
        }
    }

    public OrExpression(List<Expression>... expressions) {
        this(Lists.newArrayList(Iterables.concat(expressions)));
    }

    public OrExpression(Expression... expressions) {
        this(Arrays.asList(expressions));
    }

    public OrExpression(OrExpression copy, List<Clock> newClocks, List<Clock> oldClocks, List<BoolVar> newBVs, List<BoolVar> oldBVs) {
        // As this is the copy-constructor we need to create new instances of the expressions
        this(copy.expressions.stream().map(expression ->
            expression.copy(newClocks, oldClocks, newBVs, oldBVs)
        ).collect(Collectors.toList()));
    }

    public List<Expression> getExpressions() {
        return expressions;
    }

    @Override
    int getMaxConstant(Clock clock) {
        int max = 0;
        for (Expression expression : expressions) {
            max = Math.max(max, expression.getMaxConstant(clock));
        }
        return max;
    }

    @Override
    Expression copy(List<Clock> newClocks, List<Clock> oldClocks, List<BoolVar> newBVs, List<BoolVar> oldBVs) {
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
        return Arrays.equals(expressions.toArray(), other.expressions.toArray());
    }

    @Override
    public String toString() {
        if (expressions.size() == 1) {
            return expressions.get(0).toString();
        }

        return "(" +
                expressions.stream()
                    .map(Expression::toString)
                    .collect(Collectors.joining(" or "))
                + ")";
    }

    @Override
    public int hashCode() {
        return Objects.hash(false);
    }

    @Override
    public String prettyPrint() {
        return Expression.compositePrettyPrint(expressions, "||");
    }
}
