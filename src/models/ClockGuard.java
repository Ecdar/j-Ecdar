package models;

import java.util.List;
import java.util.Objects;

public class ClockGuard extends Guard {
    private final Clock clock, diagonalClock;
    private final int bound;
    private final Relation relation;

    public ClockGuard(Clock main, Clock secondary, int bound, Relation relation) throws IllegalArgumentException {
        if (main == null) {
            throw new IllegalArgumentException("The main clock of the clock guard cannot be null");
        }
        // These are the only relation types allowed
        if (relation != Relation.LESS_THAN &&
            relation != Relation.LESS_EQUAL &&
            relation != Relation.EQUAL &&
            relation != Relation.GREATER_EQUAL &&
            relation != Relation.GREATER_THAN) {
            throw new IllegalArgumentException("The relation of the clock guard is invalid");
        }
        this.clock = main;
        this.diagonalClock = secondary;
        this.relation = relation;
        this.bound = bound;
    }

    public ClockGuard(Clock clock_i, int bound, Relation relation) throws IllegalArgumentException {
        this(clock_i, null, bound, relation);
    }

    public ClockGuard(ClockGuard orig, List<Clock> newClocks, List<Clock> oldClocks) throws IndexOutOfBoundsException, IllegalArgumentException {
        this(newClocks.get(oldClocks.indexOf(orig.clock)), orig.diagonalClock == null ? null : newClocks.get(oldClocks.indexOf(orig.diagonalClock)), orig.getBound(), orig.getRelation());
    }

    public boolean isDiagonal() {
        return diagonalClock != null;
    }

    public Clock getClock() {
        return clock;
    }

    public Clock getDiagonalClock() {
        return diagonalClock;
    }

    public int getLowerBound() throws IllegalStateException {
        /* Default case is not handled as the constructor
         *   ensures that the relation cannot be none of these. */
        switch (relation) {
            case EQUAL:
            case GREATER_EQUAL:
            case GREATER_THAN: {
                return bound;
            }
            case LESS_EQUAL:
            case LESS_THAN: {
                return 0;
            }
        }
        throw new IllegalStateException("The relation of the clock guard is invalid");
    }

    public int getUpperBound() throws IllegalStateException {
        switch (relation) {
            case EQUAL:
            case LESS_EQUAL:
            case LESS_THAN: {
                return bound;
            }
            case GREATER_EQUAL:
            case GREATER_THAN: {
                return Integer.MAX_VALUE;
            }
        }
        throw new IllegalStateException("The relation of the clock guard is invalid");
    }

    // Returns a bound of a guard in the automaton
    public int getBound() {
        return bound;
    }

    public Relation getRelation() {
        return relation;
    }

    @Override
    int getMaxConstant(Clock clock)
        throws IllegalArgumentException {
        if (clock == null) {
            throw new IllegalArgumentException("Max constant for null clock is not supported");
        }
        if (clock.equals(this.clock) || clock.equals(this.diagonalClock)) {
            return bound;
        }
        return 0;
    }

    @Override
    Guard copy(List<Clock> newClocks, List<Clock> oldClocks, List<BoolVar> newBVs, List<BoolVar> oldBVs) {
        return new ClockGuard(this, newClocks, oldClocks);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ClockGuard)) {
            return false;
        }

        ClockGuard other = (ClockGuard) obj;

        if (bound != other.bound ||
            relation != other.relation ||
            !clock.equals(other.clock)) {
            return false;
        }

        return diagonalClock == null ||
                diagonalClock.equals(other.diagonalClock);
    }

    @Override
    public String toString() {
        String resultant = clock.getUniqueName();
        if (isDiagonal()) {
            resultant += "-" + diagonalClock.getUniqueName();
        }
        resultant += relation.toString();
        resultant += bound;
        return resultant;
    }

    @Override
    public int hashCode() {
        return Objects.hash(clock, bound, relation);
    }
}
