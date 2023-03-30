package models;

import java.util.List;
import java.util.stream.Collectors;

public abstract class Guard {

    abstract int getMaxConstant(Clock clock);

    abstract Guard copy(List<Clock> newClocks, List<Clock> oldClocks, List<BoolVar> newBVs, List<BoolVar> oldBVs);

    @Override
    public abstract boolean equals(Object o);

    @Override
    public abstract String toString();

    @Override
    public abstract int hashCode();

    public abstract <T> T accept(GuardVisitor<T> visitor);

    public String prettyPrint() {
        return toString();
    }

    static String compositePrettyPrint(List<Guard> guards, String connector) {
        return guards.stream()
                .limit(guards.size()-1)
                .map(g -> {
                    if (g instanceof OrGuard || g instanceof AndGuard)
                        return String.format("(%s) %s ", g.prettyPrint(), connector);
                    else
                        return String.format("%s %s ", g.prettyPrint(), connector);
                })
                .collect(Collectors.joining())
                + guards.get(guards.size()-1).prettyPrint();
    }
}
