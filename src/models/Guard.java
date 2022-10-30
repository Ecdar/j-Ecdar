package models;

import java.util.List;

public abstract class Guard {

    abstract int getMaxConstant(Clock clock);

    abstract Guard copy(List<Clock> newClocks, List<Clock> oldClocks, List<BoolVar> newBVs, List<BoolVar> oldBVs);

    @Override
    public abstract boolean equals(Object o);

    @Override
    public abstract String toString();

    @Override
    public abstract int hashCode();
}
