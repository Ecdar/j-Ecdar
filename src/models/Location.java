package models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Location {

    private final String name;
    private final List<Guard> invariant;
    private final boolean isInitial, isUrgent, isUniversal, isInconsistent;

    public Location(String name, List<Guard> invariant, boolean isInitial, boolean isUrgent, boolean isUniversal, boolean isInconsistent) {
        this.name = name;
        this.invariant = invariant;
        this.isInitial = isInitial;
        this.isUrgent = isUrgent;
        this.isUniversal = isUniversal;
        this.isInconsistent = isInconsistent;
    }

    public Location(Location copy, List<Clock> clocks){
        this.name = copy.name;

        this.invariant = new ArrayList<>();
        for (Guard g : copy.invariant) {
            this.invariant.add(new Guard(g, clocks));
        }

        this.isInitial = copy.isInitial;
        this.isUrgent = copy.isUrgent;
        this.isUniversal = copy.isUniversal;
        this.isInconsistent = copy.isInconsistent;
    }

    public String getName() {
        return name;
    }

    public List<Guard> getInvariant() {
        return invariant;
    }

    public boolean isInitial() {
        return isInitial;
    }

    public boolean isUniversal() {
        return isUniversal;
    }

    public int getMaxConstant(){
        int constant = 0;

        for(Guard guard : invariant){
            if(guard.getActiveBound() > constant) constant = guard.getActiveBound();
        }

        return constant;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Location location = (Location) o;
        return isInitial == location.isInitial &&
                isUrgent == location.isUrgent &&
                isUniversal == location.isUniversal &&
                isInconsistent == location.isInconsistent &&
                name.equals(location.name) &&
                Arrays.equals(invariant.toArray(), location.invariant.toArray());
    }

    @Override
    public String toString() {
        return name;
    }
}
