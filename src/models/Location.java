package models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Location {

    private final String name;
    private Guard invariant;
    private int x,y;

    public CDD getInconsistentPart() {
        return inconsistentPart;
    }

    public void setInconsistentPart(CDD inconsistentPart) {
        this.inconsistentPart = inconsistentPart;
    }


    public CDD getInvariantCDD()
    {
        return new CDD(invariant);
    }

    private CDD inconsistentPart;
    private boolean isInitial;

    private  boolean isUrgent;
    private  boolean isUniversal;
    private  boolean isInconsistent;

    public boolean isUrgent() {
        return isUrgent;
    }

    public boolean isInconsistent() {
        return isInconsistent;
    }

    public void setInvariant(Guard invariant) {
        this.invariant = invariant;
    }

    public void setInitial(boolean initial) {
        isInitial = initial;
    }

    public void setX(int x) {
        this.x = x;
    }
    public void setY(int y) {
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setUrgent(boolean urgent) {
        isUrgent = urgent;
    }

    public void setUniversal(boolean universal) {
        isUniversal = universal;
    }

    public void setInconsistent(boolean inconsistent) {
        isInconsistent = inconsistent;
    }



    public Location(String name, Guard invariant, boolean isInitial, boolean isUrgent, boolean isUniversal, boolean isInconsistent) {
        this.name = name;
        this.invariant = invariant;
        this.isInitial = isInitial;
        this.isUrgent = isUrgent;
        this.isUniversal = isUniversal;
        this.isInconsistent = isInconsistent || this.getName().equals("inc");
        this.inconsistentPart = null;
    }
    public Location(String name, Guard invariant, boolean isInitial, boolean isUrgent, boolean isUniversal, boolean isInconsistent, int x, int y) {
        this.name = name;
        this.invariant = invariant;
        this.isInitial = isInitial;
        this.isUrgent = isUrgent;
        this.isUniversal = isUniversal;
        this.isInconsistent = isInconsistent || this.getName().equals("inc");
        this.inconsistentPart = null;
        this.x=x;
        this.y=y;
    }

    public Location(Location copy, List<Clock> newClocks, List<Clock> oldClocks, List<BoolVar> newBVs, List<BoolVar> oldBVs){
        this.name = copy.name;


        if (copy.getInvariant() instanceof  ClockGuard)
            invariant = (new ClockGuard((ClockGuard) copy.getInvariant(), newClocks,oldClocks));
        if (copy.getInvariant() instanceof  BoolGuard)
            invariant = (new BoolGuard((BoolGuard) copy.getInvariant(), newBVs,oldBVs));
        if (copy.getInvariant() instanceof AndGuard)
            invariant = (new AndGuard( (AndGuard) copy.getInvariant(), newClocks, oldClocks, newBVs, oldBVs));
        if (copy.getInvariant() instanceof OrGuard)
            invariant = (new OrGuard( (OrGuard) copy.getInvariant(), newClocks, oldClocks, newBVs, oldBVs));
        if (copy.getInvariant() instanceof FalseGuard)
            invariant = (new FalseGuard());
        if (copy.getInvariant() instanceof TrueGuard)
            invariant = (new TrueGuard());

        this.isInitial = copy.isInitial;
        this.isUrgent = copy.isUrgent;
        this.isUniversal = copy.isUniversal;
        this.isInconsistent = copy.isInconsistent;
    }

    public String getName() {
        return name;
    }

    public Guard getInvariant() {
        return invariant;
    }

    public boolean isInitial() {
        return isInitial;
    }

    public boolean isUniversal() {
        return isUniversal;
    }

    public int getMaxConstant(Clock clock){
        int constant = 0;
        constant = invariant.getMaxConstant();
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
                invariant.equals(location.invariant);
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int hashCode() {
        return Objects.hash(isInitial, isUrgent, isUniversal, isInconsistent, name, invariant);
    }
}
