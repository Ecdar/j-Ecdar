package models;

import java.util.List;
import java.util.Objects;

public class FalseExpression extends BooleanExpression {

    @Override
    int getMaxConstant(Clock clock) {
        return 0;
    }

    @Override
    BooleanExpression copy(List<Clock> newClocks, List<Clock> oldClocks, List<BoolVar> newBVs, List<BoolVar> oldBVs) {
        return new FalseExpression();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof FalseExpression;
    }

    @Override
    public String toString() {
        return "false";
    }

    @Override
    public int hashCode() {
        return Objects.hash(false);
    }
}
