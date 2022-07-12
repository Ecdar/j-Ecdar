package models;

import java.util.*;

public class Location {
    private final String name;

    private int x, y;
    private Guard invariant;
    private CDD inconsistentPart;

    // Must be final as Automaton expects it to be constant through the lifetime
    private final boolean isInitial;
    private boolean isUrgent;
    private boolean isUniversal;
    private boolean isInconsistent;

    public Location(String name, Guard invariant, boolean isInitial, boolean isUrgent, boolean isUniversal, boolean isInconsistent, int x, int y) {
        this.name = name;
        this.invariant = invariant;
        this.isInitial = isInitial;
        this.isUrgent = isUrgent;
        this.isUniversal = isUniversal;
        this.isInconsistent = isInconsistent || this.getName().equals("inc");
        this.inconsistentPart = null;
        this.x = x;
        this.y = y;
    }

    public Location(String name, Guard invariant, boolean isInitial, boolean isUrgent, boolean isUniversal, boolean isInconsistent) {
        this(name, invariant, isInitial, isUrgent, isUniversal, isInconsistent, 0, 0);
    }

    public Location(Location copy, List<Clock> newClocks, List<Clock> oldClocks, List<BoolVar> newBVs, List<BoolVar> oldBVs) {
        this(
            copy.name,
            copy.invariant.copy(
                newClocks, oldClocks, newBVs, oldBVs
            ),
            copy.isInitial,
            copy.isUrgent,
            copy.isUniversal,
            copy.isInconsistent,
            copy.x,
            copy.y
        );
    }

    public String getName() {
        return name;
    }

    public boolean isUrgent() {
        return isUrgent;
    }

    public boolean isInconsistent() {
        return isInconsistent;
    }

    public CDD getInvariantCDD() {
        return new CDD(invariant);
    }

    public void setInvariant(Guard invariant) {
        this.invariant = invariant;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setUrgent(boolean urgent) {
        isUrgent = urgent;
    }

    public boolean isUniversal() {
        return isUniversal;
    }

    public void setUniversal(boolean universal) {
        isUniversal = universal;
    }

    public CDD getInconsistentPart() {
        return inconsistentPart;
    }

    public void setInconsistent(boolean inconsistent) {
        isInconsistent = inconsistent;
    }

    public void setInconsistentPart(CDD inconsistentPart) {
        this.inconsistentPart = inconsistentPart;
    }

    public Guard getInvariant() {
        return invariant;
    }

    public boolean isInitial() {
        return isInitial;
    }

    public int getMaxConstant(Clock clock) {
        return invariant.getMaxConstant(clock);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Location)) {
            return false;
        }

        Location location = (Location) obj;

        return isInitial == location.isInitial &&
                isUrgent == location.isUrgent &&
                isUniversal == location.isUniversal &&
                isInconsistent == location.isInconsistent &&
                name.equals(location.name) &&
                invariant.equals(location.invariant);
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            name, isInitial, isUrgent, isUniversal, isInconsistent, invariant, inconsistentPart
        );
    }
}
