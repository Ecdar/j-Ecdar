package models;

import log.Log;
import logic.State;

import java.util.*;
import java.util.stream.Collectors;

public class Location {
    protected String name;
    protected int x, y;

    protected Guard invariantGuard;
    protected CDD invariantCdd = null;

    protected CDD inconsistentPart;

    protected boolean isInitial;
    protected boolean isUrgent;
    protected boolean isUniversal;
    protected boolean isInconsistent;

    protected List<SymbolicLocation> productOf = new ArrayList<>();
    protected Location location;

    public Location() {}

    public Location(
            String name,
            Guard invariant,
            boolean isInitial,
            boolean isUrgent,
            boolean isUniversal,
            boolean isInconsistent,
            int x,
            int y,
            List<SymbolicLocation> productOf
    ) {
        this.name = name;
        this.invariantCdd = null;
        this.invariantGuard = invariant;
        this.isInitial = isInitial;
        this.isUrgent = isUrgent;
        this.isUniversal = isUniversal;
        this.isInconsistent = isInconsistent;
        this.inconsistentPart = null;
        this.x = x;
        this.y = y;
        this.productOf = productOf;
    }

    public Location(
            String name,
            Guard invariant,
            boolean isInitial,
            boolean isUrgent,
            boolean isUniversal,
            boolean isInconsistent,
            int x,
            int y
    ) {
        this(
                name,
                invariant,
                isInitial,
                isUrgent,
                isUniversal,
                isInconsistent,
                x,
                y,
                new ArrayList<>()
        );
    }

    public Location(
            String name,
            Guard invariant,
            boolean isInitial,
            boolean isUrgent,
            boolean isUniversal,
            boolean isInconsistent
    ) {
        this(name, invariant, isInitial, isUrgent, isUniversal, isInconsistent, 0, 0);
    }

    public Location(
            Location copy,
            List<Clock> newClocks,
            List<Clock> oldClocks,
            List<BoolVar> newBVs,
            List<BoolVar> oldBVs
    ) {
        this(
            copy.name,
            copy.invariantGuard.copy(
                newClocks, oldClocks, newBVs, oldBVs
            ),
            copy.isInitial,
            copy.isUrgent,
            copy.isUniversal,
            copy.isInconsistent,
            copy.x,
            copy.y,
            copy.productOf
        );
    }

    public Location(List<Location> locations) {
        if (locations.size() == 0) {
            throw new IllegalArgumentException("At least a single location is required");
        }

        this.name = locations.stream()
                .map(Location::getName)
                .collect(Collectors.joining(""));

        this.isInitial = locations.stream().allMatch(location -> location.isInitial);
        this.isUrgent = locations.stream().anyMatch(location -> location.isUrgent);
        this.isUniversal = locations.stream().allMatch(location -> location.isUniversal);
        this.isInconsistent = locations.stream().anyMatch(location -> location.isInconsistent);

        CDD invariant = CDD.cddTrue();
        for (Location location : locations) {
            invariant = location.getInvariantCdd().conjunction(invariant);
            this.x += location.x;
            this.y = location.y;
        }

        this.invariantCdd = invariant;
        this.invariantGuard = invariant.getGuard();
        // We use the average location coordinates
        this.x /= locations.size();
        this.y /= locations.size();

        this.productOf = new ArrayList<>();
    }

    public Location(State state, List<Clock> clocks) {
        this(
                state.getLocation().getName(),
                state.getInvariants(clocks),
                state.getLocation().isInitial(),
                state.getLocation().isUrgent(),
                state.getLocation().isUniversal(),
                state.getLocation().isInconsistent(),
                state.getLocation().getX(),
                state.getLocation().getX()
        );
    }

    public boolean isSimple() {
        return location != null;
    }

    public Location getSimpleLocation() {
        return location;
    }

    public boolean isProduct() {
        return productOf.size() > 0;
    }

    public List<SymbolicLocation> getProductOf() {
        return productOf;
    }

    public void removeInvariants() {
        invariantGuard = new TrueGuard();
        invariantCdd = CDD.cddTrue();
    }

    public String getName() {
        return name;
    }

    public boolean isInitial() {
        return isInitial;
    }

    public boolean isUrgent() {
        return isUrgent;
    }

    public boolean isInconsistent() {
        return isInconsistent;
    }

    public boolean isUniversal() {
        return isUniversal;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public Guard getInvariantGuard() {
        if (invariantGuard == null) {
            invariantGuard = getInvariantCdd().getGuard();
        }

        return invariantGuard;
    }

    public CDD getInvariantCdd() {
        if (invariantGuard == null) {
            return getInvariantCddNew();
        }

        return new CDD(getInvariantGuard());
    }

    public CDD getInvariantCddNew() {
        if (isSimple()) {
            return location.getInvariantCdd();
        }

        if (invariantCdd == null) {
            if (isInconsistent) {
                invariantCdd = CDD.cddZero();
            } else if (isUniversal) {
                invariantCdd = CDD.cddTrue();
            } else if (isProduct()) {
                this.invariantCdd = CDD.cddTrue();
                for (SymbolicLocation location : productOf) {
                    this.invariantCdd = this.invariantCdd.conjunction(location.getInvariantCddNew());
                }
            } else {
                invariantCdd = new CDD(getInvariantGuard());
            }
        }

        return invariantCdd;
    }

    public void setInvariantGuard(Guard invariantAsGuard) {
        this.invariantGuard = invariantAsGuard;
        this.invariantCdd = null;
    }

    public void setUrgent(boolean urgent) {
        isUrgent = urgent;
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

    public int getMaxConstant(Clock clock) {
        return getInvariantGuard().getMaxConstant(clock);
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Location that = (Location) o;

        if (isSimple() && that.isSimple()) {
            return getSimpleLocation().equals(that.getSimpleLocation());
        }

        if (isProduct() && that.isProduct()) {
            if (productOf.size() != that.productOf.size()) {
                return false;
            }

            for (int i = 0; i < productOf.size(); i++) {
                if (!productOf.get(i).equals(that.productOf.get(i))) {
                    return false;
                }
            }

            return true;
        }

        return isInitial() == that.isInitial() &&
                isUrgent() == that.isUrgent() &&
                isUniversal() == that.isUniversal() &&
                isInconsistent() == that.isInconsistent() &&
                getX() == that.getX() &&
                getY() == that.getY() &&
                getName().equals(that.getName());
    }

    @Override
    public int hashCode() {
        if (isProduct()) {
            return Objects.hash(productOf);
        }

        return Objects.hash(name, getInvariantGuard(), isInitial, isUrgent, isUniversal, isInconsistent, x, y);
    }
}
