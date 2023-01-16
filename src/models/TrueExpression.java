package models;

import java.util.List;
import java.util.Objects;

public class TrueExpression extends BooleanExpression {

    @Override
    int getMaxConstant(Clock clock) {
        return 0;
    }

    @Override
    BooleanExpression copy(List<Clock> newClocks, List<Clock> oldClocks, List<BoolVar> newBVs, List<BoolVar> oldBVs) {
        return new TrueExpression();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof TrueExpression;
    }

    @Override
    public String toString() {
        return "true";
    }

    @Override
    public int hashCode() {
        return Objects.hash(true);
    }
}
