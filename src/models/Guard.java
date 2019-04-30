package models;

import java.util.List;
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

    public Guard(Clock clock, int value) {
        this.clock = clock;
        this.isStrict = false;
        this.upperBound = value;
        this.lowerBound = value;
    }

    // Copy constructor
    public Guard(Guard copy, List<Clock> clocks){
        this.clock = clocks.get(clocks.indexOf(copy.clock));
        this.isStrict = copy.isStrict;
        this.upperBound = copy.upperBound;
        this.lowerBound = copy.lowerBound;
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

    // Returns a bound of a guard in the automaton
    public int getActiveBound(){
        if(upperBound == Integer.MAX_VALUE) return lowerBound;
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
    public String toString() {
        return "Guard{" +
                "clock=" + clock +
                ", upperBound=" + upperBound +
                ", lowerBound=" + lowerBound +
                ", isStrict=" + isStrict +
                '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(clock, upperBound, lowerBound, isStrict);
    }
}
