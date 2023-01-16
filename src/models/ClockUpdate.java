package models;

import java.util.List;
import java.util.Objects;

public class ClockUpdate extends Update {
    private final Clock clock;
    private final int value;

    public ClockUpdate(Clock clock, int value) {
        if (clock == null) {
            throw new IllegalArgumentException("The clock used in a clock update cannot be null.");
        }

        this.clock = clock;
        this.value = value;
    }

    public ClockUpdate(ClockUpdate copy, List<Clock> newClocks, List<Clock> oldClocks) {
        if (copy == null) {
            throw new IllegalArgumentException("The instance to copy cannot be null.");
        }

        if (newClocks == null) {
            throw new IllegalArgumentException("The new clocks for the clock update cannot be null.");
        }

        if (oldClocks == null) {
            throw new IllegalArgumentException("The old clocks for the clock update cannot be null.");
        }

        if (newClocks.size() != oldClocks.size()) {
            throw new IllegalArgumentException("The size of the list with new clocks must be the same as the ones with the old clocks.");
        }

        this.clock = newClocks.get(oldClocks.indexOf(copy.clock));
        this.value = copy.value;
    }

    public Clock getClock() {
        return clock;
    }

    public int getValue() {
        return value;
    }

    @Override
    Update copy(List<Clock> newClocks, List<Clock> oldClocks, List<BoolVar> newBVs, List<BoolVar> oldBVs) {
        return new ClockUpdate(this, newClocks, oldClocks);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClockUpdate)) return false;
        ClockUpdate update = (ClockUpdate) o;
        return value == update.value &&
                clock.equals(update.clock);
    }

    @Override
    public String toString() {
        return "Update{" +
                "clock=" + clock +
                ", value=" + value +
                '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(clock, value);
    }
}
