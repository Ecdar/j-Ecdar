package models;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.*;
import java.util.stream.Collectors;

public class AndExpression extends Expression {
    private List<Expression> expressions;

    public AndExpression(List<Expression> expressions) {
        this.expressions = expressions;

        /* If any of the expressions are AndExpressions themselves,
         *   then we can decompose their expressions to be contained in this */
        List<AndExpression> worklist = this.expressions
                .stream()
                .filter(expr -> expr instanceof AndExpression)
                .map(expr -> (AndExpression) expr)
                .collect(Collectors.toList());
        while (!worklist.isEmpty()) {
            AndExpression current = worklist.get(0);
            worklist.remove(0);

            for (Expression expression : current.expressions) {
                if (expression instanceof AndExpression) {
                    worklist.add((AndExpression) expression);
                }
                this.expressions.add(expression);
            }

            this.expressions.remove(current);
        }

        /* If the AndExpression contains a FalseExpression,
         *   then remove all expressions and just have a single
         *   FalseExpression as it will always be false. */
        boolean hasFalseExpr = this.expressions.stream().anyMatch(expr -> expr instanceof FalseExpression);
        if (hasFalseExpr) {
            /* We just know that there is at least one FalseExpression for this
             *   reason we clear all expressions as it handle multiple FalseExpression
             *   then we just add a FalseExpression to account for all of them.
             *   If we left it empty then it would be interpreted as a tautology. */
            this.expressions.clear();
            this.expressions.add(new FalseExpression());
        }

        // Remove all true expressions
        this.expressions = this.expressions.stream().filter(expr -> !(expr instanceof TrueExpression)).collect(Collectors.toList());

        // If empty then it is a tautology
        if (this.expressions.size() == 0) {
            this.expressions.add(new TrueExpression());
        }
    }

    public AndExpression(List<Expression>... expressions) {
        this(Lists.newArrayList(Iterables.concat(expressions)));
    }

    public AndExpression(Expression... expressions) {
        this(Arrays.asList(expressions));
    }

    public AndExpression(AndExpression copy, List<Clock> newClocks, List<Clock> oldClocks, List<BoolVar> newBVs, List<BoolVar> oldBVs) {
        // As this is the copy-constructor we need to create new instances of the expressions
        this(copy.expressions.stream().map(expr ->
            expr.copy(newClocks, oldClocks, newBVs, oldBVs)
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
        return Arrays.equals(expressions.toArray(), other.expressions.toArray());
    }

    @Override
    public String toString() {
        if (expressions.size() == 1) {
            return expressions.get(0).toString();
        }

        return "(" +
                expressions.stream().map(Expression::toString).collect(Collectors.joining(" && "))
                + ")";
    }

    @Override
    public int hashCode() {
        return Objects.hash(expressions);
    }

    @Override
    public String prettyPrint() {
        return Expression.compositePrettyPrint(expressions, "&&");
    }
}
