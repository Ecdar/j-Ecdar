package models;

import java.util.List;
import java.util.Objects;

public class ClockGuard extends Guard {

    private final Clock clock_i,clock_j;
    private int bound;
    private Relation rel;

    public ClockGuard(Clock clock_i, Clock clock_j, int bound, Relation rel) {
        assert (clock_i != null);
        this.clock_i = clock_i;
        this.clock_j = clock_j;
        this.rel = rel;
        this.bound = bound;
    }

    public ClockGuard(Clock clock_i, int bound, Relation rel) {
        assert (clock_i != null);
        this.clock_i = clock_i;
        this.clock_j = null;
        this.rel = rel;
        this.bound = bound;
    }
    // Copy constructor
    public ClockGuard(ClockGuard orig, List<Clock> clocks) {
        this.clock_i = clocks.get(clocks.indexOf(orig.clock_i));
        if (orig.clock_j==null)
            this.clock_j = null;
        else
            this.clock_j = clocks.get(clocks.indexOf(orig.clock_j));
        this.rel = orig.rel;
        this.bound = orig.bound;
    }

    public boolean isDiagonal()
    {
        if (clock_j==null)
            return false;
        else
            return true;
    }

    public Clock getClock_i() {
        return clock_i;
    }
    public Clock getClock_j() {
        return clock_j;
    }

    public int getLowerBound() {
        switch (rel) {
            case EQUAL:
            case GREATER_EQUAL:
            case GREATER_THAN:{
                return bound;
            }
            case NOT_EQUAL:// TODO: 2021-05-31 wtf?
            case LESS_EQUAL:
            case LESS_THAN:{
                return 0;
            }
            default: {
                assert (false);
            }
        }

        return -1;
    }

    public int getUpperBound() {
        switch (rel) {
            case EQUAL:
            case LESS_EQUAL:
            case LESS_THAN:
            {
                return bound;
            }
            case NOT_EQUAL:
            case GREATER_EQUAL:
            case GREATER_THAN:{
                return Integer.MAX_VALUE;
            }
            default: {
                assert (false);
            }
        }
        return -1;
    }

    // Returns a bound of a guard in the automaton
    public int getBound() {
        return bound;
    }

    public Relation getRelation() {return rel;}



    public ClockGuard negate() {
        // never negat individual clock guards, the negation of == would lead to problems!
        assert(false);
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClockGuard)) return false;
        ClockGuard guard = (ClockGuard) o;

        if (isDiagonal()) {
            return bound == guard.bound &&
                    rel == guard.rel &&
                    clock_i.equals(guard.clock_i) &&
                    clock_j.equals(guard.clock_j);
        }
        else {
            return bound == guard.bound &&
                    rel == guard.rel &&
                    clock_i.equals(guard.clock_i) &&
                    guard.clock_j==null;
        }
    }

    @Override
    public String toString() {
        if (isDiagonal())
        {
            String res = clock_i.getName() + "-" + clock_j.getName();
            res += rel.toString();
            res += bound;
            return res;

        }
        else {
            String res = clock_i.getName();
            res += rel.toString();
            res += bound;
            return res;
        }
    }


    @Override
    public int hashCode() {
        return Objects.hash(clock_i, bound, rel);
    }
}
