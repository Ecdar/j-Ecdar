package models;

import java.util.Objects;

public class Guard {

    private final Clock clock;
    private int upperBound, lowerBound;
    private boolean isStrict;

    public Guard(Clock clock, int value, boolean greater, boolean isStrict) {
        this.clock = clock;
        this.isStrict = isStrict;

        if (greater) {
            upperBound = Integer.MAX_VALUE;
            lowerBound = value;
        } else {
            upperBound = value;
            lowerBound = 0;
        }
    }

    public Guard(Clock clock, int upper, int lower, boolean isStrict) {
        this.clock = clock;
        this.isStrict = isStrict;
        this.upperBound = upper;
        this.lowerBound = lower;
    }

    public Clock getClock() {
        return clock;
    }

    public int getLowerBound() {
        return lowerBound;
    }

    public int getUpperBound() {
        return upperBound;
    }

    public boolean isStrict() {
        return isStrict;
    }

    public Guard negate() {
        isStrict = !isStrict;
        int newLower = (lowerBound == 0) ? upperBound : 0;
        int newUpper = (upperBound == Integer.MAX_VALUE) ? lowerBound : Integer.MAX_VALUE;

        return new Guard(clock, newUpper, newLower, isStrict);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Guard)) return false;
        Guard guard = (Guard) o;
        return upperBound == guard.upperBound &&
                lowerBound == guard.lowerBound &&
                clock.equals(guard.clock) &&
                isStrict == ((Guard) o).isStrict;
    }

    @Override
    public int hashCode() {
        return Objects.hash(clock, upperBound, lowerBound, isStrict);
    }
}
