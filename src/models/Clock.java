package models;

import logic.TransitionSystem;

import java.util.Objects;

public class Clock extends UniquelyNamed {
    /**
     * If true then this clock is used in the scope of multiple {@link TransitionSystem} or {@link Automaton}.
     * As an example, quotients creates a new "quo_new" clock unless one already exists.
     * Instead of creating a new and different clock the same clock is reused.
     */
    private final boolean isGlobal;

    /**
     * Constructs a new clock.
     *
     * @param name The original name and initial unique name of the clock.
     * @param ownerName The owner of the clock. E.g. the name of the {@link Automaton}.
     * @param isGlobal if the true the clock will be used across all {@link Automaton Automata}.
     */
    public Clock(String name, String ownerName, boolean isGlobal) {
        this.uniqueName = name;
        this.originalName = name;
        this.ownerName = ownerName;
        this.isGlobal = isGlobal;
    }

    /**
     * Constructs a local clock.
     *
     * @param name The original name and initial unique name of the clock.
     * @param ownerName The owner of the clock. E.g. the name of the {@link Automaton}.
     */
    public Clock(String name, String ownerName) {
        this(name, ownerName, false);
    }

    /**
     * Copy constructor for a clock.
     *
     * @param copy The clock instance to copy.
     */
    public Clock(Clock copy) {
        this(copy.getOriginalName(), copy.getOwnerName(), copy.isGlobal);
    }

    /**
     * Returns true if this clock is meant to be used in the scope of other {@link TransitionSystem TransitionSystems} or {@link Automaton Automata}.
     *
     * Additionally, if the {@link Clock} is global then global naming rules will be applied in the {@link logic.UniqueNamedContainer}.
     * As an example, this is used for the "quo_new", because the generated clock is global amongst all quotients.
     *
     * @return true if it can be used in the scope of other {@link TransitionSystem} or {@link Automaton}.
     */
    @Override
    public boolean isGlobal() {
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
