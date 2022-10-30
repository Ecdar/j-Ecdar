package models;

import java.util.List;
import java.util.Objects;

public class FalseGuard extends Guard {

    @Override
    int getMaxConstant(Clock clock) {
        return 0;
    }

    @Override
    Guard copy(List<Clock> newClocks, List<Clock> oldClocks, List<BoolVar> newBVs, List<BoolVar> oldBVs) {
        return new FalseGuard();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof FalseGuard;
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
