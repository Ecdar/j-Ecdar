package models;

import java.util.Objects;

public class Guard {

    private final Clock clock;
    private int upperBound, lowerBound;

    public Guard(Clock clock, int value, boolean greater, boolean strict) {
        this.clock = clock;
        if (greater) {
            upperBound = Integer.MAX_VALUE;
            lowerBound = strict ? (value + 1) : value;
        } else {
            upperBound = strict ? (value - 1) : value;
            lowerBound = 0;
        }
    }

    public Guard (Clock clock, int upper, int lower) {
        this.clock = clock;
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

    public Guard negate() {
        int newLower = (lowerBound == 0) ? upperBound : 0;
        int newUpper = (upperBound == Integer.MAX_VALUE) ? lowerBound : Integer.MAX_VALUE;

        return new Guard(clock, newUpper, newLower);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Guard)) return false;
        Guard guard = (Guard) o;
        return upperBound == guard.upperBound &&
                lowerBound == guard.lowerBound &&
                clock.equals(guard.clock);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clock, upperBound, lowerBound);
    }
}
