package models;

import logic.TransitionSystem;

import java.util.Objects;

public class Clock extends UniquelyNamed {
    /**
     * If true then this clock is used in the scope of multiple {@link TransitionSystem} or {@link Automaton}.
     * As an example, quotients adds a new "quo_new" clock unless there already is one created
     * in one of its operands. Instead of creating a new and different clock the same clock is reused.
     * This is used to tell the {@link logic.UniqueNamedContainer} that this clock is a singleton.
     * Where in that context a singleton is an instance of a {@link UniquelyNamed} instance which should
     * only be present once (Identified by its unique name).
     */
    private final boolean isGlobal;

    public Clock(String name, String ownerName, boolean isGlobal) {
        this.uniqueName = name;
        this.originalName = name;
        this.ownerName = ownerName;
        this.isGlobal = isGlobal;
    }

    public Clock(String name, String ownerName) {
        this(name, ownerName, false);
    }

    public Clock(Clock copy) {
        this(copy.getOriginalName(), copy.getOwnerName(), copy.isGlobal);
    }

    /**
     * Returns true if this clock is meant to be used in the scope of other {@link TransitionSystem} or {@link Automaton}.
     *
     * @return true if it can be used in the scope of other {@link TransitionSystem} or {@link Automaton}.
     */
    public boolean isGlobal() {
        return isGlobal;
    }

    /**
     * Returns true if the {@link logic.UniqueNamedContainer} should apply the singleton
     * renaming rules on the unique name of this clock. As an example, this is used for the "quo_new"
     * where only a single instance of that clock is expected to be in {@link logic.UniqueNamedContainer}
     * as it is reused across other {@link logic.Quotient}.
     *
     * @return true if the singleton renaming rules should be applied on the unique name of this clock.
     */
    @Override
    public boolean isSingleton() {
        return isGlobal;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Clock)) {
            return false;
        }

        Clock other = (Clock) obj;
        return originalName.equals(other.originalName)
                && ownerName.equals(other.ownerName);
    }

    @Override
    public String toString() {
        return "Clock{" +
                "name='" + uniqueName + '\'' +
                '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(originalName, ownerName, isGlobal);
    }

    @Override
    public UniquelyNamed getCopy() {
        return new Clock(this);
    }
}
