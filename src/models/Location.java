package models;

import logic.State;

import java.util.*;
import java.util.stream.Collectors;

public class Location {
    protected String name;
    protected int x, y;

    protected Guard invariantGuard;
    protected CDD invariantCdd;

    protected CDD inconsistentPart;

    protected boolean isInitial;
    protected boolean isUrgent;
    protected boolean isUniversal;
    protected boolean isInconsistent;

    protected List<Location> productOf;
    protected Location location;

    private Location(
            String name,
            int x,
            int y,
            Guard invariantGuard,
            CDD invariantCdd,
            CDD inconsistentPart,
            boolean isInitial,
            boolean isUrgent,
            boolean isUniversal,
            boolean isInconsistent,
            List<Location> productOf,
            Location location
    ) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.invariantGuard = invariantGuard;
        this.invariantCdd = invariantCdd;
        this.inconsistentPart = inconsistentPart;
        this.isInitial = isInitial;
        this.isUrgent = isUrgent;
        this.isUniversal = isUniversal;
        this.isInconsistent = isInconsistent;
        this.productOf = productOf;
        this.location = location;
    }

    public static Location create(
            String name,
            Guard invariant,
            boolean isInitial,
            boolean isUrgent,
            boolean isUniversal,
            boolean isInconsistent,
            int x,
            int y
    ) {
        return new Location(
            name,
            x,
            y,
            invariant,
            null,
            null,
            isInitial,
            isUrgent,
            isUniversal,
            isInconsistent,
            new ArrayList<>(),
            null
        );
    }

    public static Location createFromState(State state, List<Clock> clocks) {
        Location location = state.getLocation();
        return new Location(
            location.getName(),
            location.getX(),
            location.getY(),
            state.getInvariants(clocks),
            null,
            location.getInconsistentPart(),
            location.isInitial(),
            location.isUrgent(),
            location.isUniversal(),
            location.isInconsistent(),
            location.getProductOf(),
            location.getSimpleLocation()
        );
    }

    public static Location createProduct(List<Location> productOf) {
        StringBuilder nameBuilder = new StringBuilder();
        boolean isInitial = true;
        boolean isUniversal = true;
        boolean isUrgent = false;
        boolean isInconsistent = false;
        int x = 0;
        int y = 0;

        List<Guard> guards = new ArrayList<>();
        for (Location location : productOf) {
            nameBuilder.append(location.getName());
            isInitial = isInitial && location.isInitial();
            isUniversal = isUniversal && location.isUniversal();
            isUrgent = isUrgent || location.isUrgent();
            isInconsistent = isInconsistent || location.isInconsistent();
            x += location.getX();
            y += location.getY();
            guards.add(location.getInvariantGuard());
        }

        int amount = productOf.size();
        x /= amount;
        y /= amount;
        String name = nameBuilder.toString();

        Guard invariant = new AndGuard(guards);
        return new Location(
                name,
                x,
                y,
                invariant,
                null,
                null,
                isInitial,
                isUrgent,
                isUniversal,
                isInconsistent,
                productOf,
                null
        );
    }

    public static Location createUniversalLocation(
            String name,
            boolean isInitial,
            boolean isUrgent,
            int x,
            int y
    ) {
        return new Location(
                name,
                x,
                y,
                new TrueGuard(),
                null,
                null,
                isInitial,
                isUrgent,
                true,
                false,
                new ArrayList<>(),
                null
        );
    }

    public static Location createUniversalLocation(String name, int x, int y) {
        return Location.createUniversalLocation(name, false, false, x, y);
    }

    public static Location createInconsistentLocation(
            String name,
            boolean isInitial,
            boolean isUrgent,
            int x,
            int y
    ) {
        return new Location(
                name,
                x,
                y,
                new FalseGuard(),
                null,
                null,
                isInitial,
                isUrgent,
                false,
                true,
                new ArrayList<>(),
                null
        );
    }

    public static Location createInconsistentLocation(String name, int x, int y) {
        return Location.createInconsistentLocation(name, false, false, x, y);
    }

    public static Location createSimple(Location location) {
        return new Location(
                location.getName(),
                location.getX(),
                location.getY(),
                location.getInvariantGuard(),
                null,
                location.getInconsistentPart(),
                location.isInitial(),
                location.isUrgent(),
                location.isUniversal(),
                location.isInconsistent(),
                new ArrayList<>(),
                location
        );
    }

    public Location copy() {
        return new Location(
            getName(),
            getX(),
            getY(),
            getInvariantGuard(),
                null,
            getInconsistentPart(),
            isInitial(),
            isUrgent(),
            isUniversal(),
            isInconsistent(),
            new ArrayList<>(),
            null
        );
    }

    public Location copy(
            List<Clock> newClocks,
            List<Clock> oldClocks,
            List<BoolVar> newBVs,
            List<BoolVar> oldBVs
    ) {
        return new Location(
            name,
            x,
            y,
            invariantGuard.copy(
                newClocks, oldClocks, newBVs, oldBVs
            ),
            null,
            null,
            isInitial,
            isUrgent,
            isUniversal,
            isInconsistent,
            productOf,
            null
        );
    }

    public boolean isSimple() {
        return location != null;
    }

    public Location getSimpleLocation() {
        return this;
    }

    public boolean isProduct() {
        return productOf.size() > 0;
    }

    public List<Location> getProductOf() {
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
                for (Location location : productOf) {
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
                getName().equals(that.getName());
    }

    @Override
    public int hashCode() {
        if (isProduct()) {
            return Objects.hash(productOf);
        }

        return Objects.hash(name, getInvariantGuard(), isInitial, isUrgent, isUniversal, isInconsistent);
    }
}
