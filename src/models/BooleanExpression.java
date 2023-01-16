package models;

import java.util.List;
import java.util.stream.Collectors;

public abstract class BooleanExpression {

    abstract int getMaxConstant(Clock clock);

    abstract BooleanExpression copy(List<Clock> newClocks, List<Clock> oldClocks, List<BoolVar> newBVs, List<BoolVar> oldBVs);

    @Override
    public abstract boolean equals(Object o);

    @Override
    public abstract String toString();

    @Override
    public abstract int hashCode();

    public String prettyPrint() {
        return toString();
    }

    static String compositePrettyPrint(List<BooleanExpression> booleanExpressions, String connector) {
        return booleanExpressions.stream()
                .limit(booleanExpressions.size()-1)
                .map(g -> {
                    if (g instanceof OrExpression || g instanceof AndExpression)
                        return String.format("(%s) %s ", g.prettyPrint(), connector);
                    else
                        return String.format("%s %s ", g.prettyPrint(), connector);
                })
                .collect(Collectors.joining())
                + booleanExpressions.get(booleanExpressions.size()-1).prettyPrint();
    }
}
