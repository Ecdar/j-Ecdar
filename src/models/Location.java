package models;

import logic.State;
import logic.TransitionSystem;
import logic.Pruning;

import java.util.*;

/**
 * {@link Location} is a class used by both {@link Automaton} and {@link TransitionSystem} to decribe a locaton.
 * It is named and has coordinates describing the position it should be drawn.
 * A {@link Location} can be marked as initial, urgent, universal, and inconsistent.
 * In order to reduce the conversions between {@link Guard} and {@link CDD} the invariant is stored as both and only updated when required.
 * For {@link Pruning} it also stores the inconsistent part of its invariant.
 * <p>
 * A {@link Location} can also consist of multiple locations, if this is the case then it is a product.
 * This is the case when combining multiple locations e.g. when performing {@link logic.Conjunction}.
 * For the product it is assumed that the order of location also corresponds to the child {@link TransitionSystem}.
 * <p>
 * A {@link Location} can also be a simple location which is nearly the same as a product with one element.
 * However, if the location is simple then the lazy evaluation of its invariant will always be the invariant of the child.
 * <p>
 * State overview:
 * <ul>
 *     <li>name
 *     <li>x and y coordinates
 *     <li>invariant both as {@link CDD} and {@link Guard}
 *     <li>inconsistent part for {@link Pruning}
 *     <li>whether it is initial, urgent, universal, inconsistent
 * </ul>
 * @see LocationPair
 */
public final class Location {
    private String name;
    private int x, y;

    private Guard invariantGuard;
    private CDD invariantCdd;

    private CDD inconsistentPart;

    private boolean isInitial;
    private boolean isUrgent;
    private boolean isUniversal;
    private boolean isInconsistent;

    private final List<Location> productOf;
    private final Location location;

    private Location(
            String name,
            Guard invariantGuard,
            CDD invariantCdd,
            CDD inconsistentPart,
            boolean isInitial,
            boolean isUrgent,
            boolean isUniversal,
            boolean isInconsistent,
            List<Location> productOf,
            Location location,
            int x,
            int y
    ) {
        this.name = name;
        this.invariantGuard = invariantGuard;
        this.invariantCdd = invariantCdd;
        this.inconsistentPart = inconsistentPart;
        this.isInitial = isInitial;
        this.isUrgent = isUrgent;
        this.isUniversal = isUniversal;
        this.isInconsistent = isInconsistent;
        this.productOf = productOf;
        this.location = location;
        this.x = x;
        this.y = y;
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
                invariant,
                null,
                null,
                isInitial,
                isUrgent,
                isUniversal,
                isInconsistent,
                new ArrayList<>(),
                null,
                x,
                y
        );
    }

    public static Location create(
            String name,
            Guard invariant,
            boolean isInitial,
            boolean isUrgent,
            boolean isUniversal,
            boolean isInconsistent
    ) {
        return create(name, invariant, isInitial, isUrgent, isUniversal, isInconsistent, 0, 0);
    }

    public static Location createFromState(State state, List<Clock> clocks) {
        Location location = state.getLocation();
        return location.copy();
    }

    public static Location createProduct(List<Location> productOf) {
        if (productOf.size() == 0) {
            throw new IllegalArgumentException("Requires at least one location to create a product");
        }

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
                invariant,
                null,
                null,
                isInitial,
                isUrgent,
                isUniversal,
                isInconsistent,
                productOf,
                null,
                x,
                y
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
                new TrueGuard(),
                null,
                null,
                isInitial,
                isUrgent,
                true,
                false,
                new ArrayList<>(),
                null,
                x,
                y
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
                new FalseGuard(),
                null,
                null,
                isInitial,
                isUrgent,
                false,
                true,
                new ArrayList<>(),
                null,
                x,
                y
        );
    }

    public static Location createInconsistentLocation(String name, int x, int y) {
        return Location.createInconsistentLocation(name, false, false, x, y);
    }

    public static Location createSimple(Location location) {
        return new Location(
                location.getName(),
                location.getInvariantGuard(),
                null,
                location.getInconsistentPart(),
                location.isInitial(),
                location.isUrgent(),
                location.isUniversal(),
                location.isInconsistent(),
                new ArrayList<>(),
                location,
                location.getX(),
                location.getY()
        );
    }

    public Location copy() {
        return new Location(
            getName(),
            getInvariantGuard(),
            null,
            getInconsistentPart(),
            isInitial(),
            isUrgent(),
            isUniversal(),
            isInconsistent(),
            new ArrayList<>(),
            null,
                getX(),
                getY()
        );
    }

    public Location copy(
            List<Clock> newClocks,
            List<Clock> oldClocks,
            List<BoolVar> newBVs,
            List<BoolVar> oldBVs
    ) {
        return new Location(
            getName(),
            getInvariantGuard().copy(
                newClocks, oldClocks, newBVs, oldBVs
            ),
            null,
            null,
            isInitial(),
            isUrgent(),
            isUniversal(),
            isInconsistent(),
            getProductOf(),
            null,
                getX(),
                getY()
        );
    }

    public boolean isSimple() {
        return location != null;
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

    public Guard getInvariantGuard() {
        if (invariantGuard == null) {
            invariantGuard = getInvariantCddEager().getGuard();
        }

        return invariantGuard;
    }

    public CDD getInvariantCddEager() {
        return new CDD(getInvariantGuard());
    }

    public CDD getInvariantCddLazy() {
        if (isSimple()) {
            return location.getInvariantCddEager();
        }

        if (invariantCdd == null) {
            if (isInconsistent) {
                invariantCdd = CDD.cddZero();
            } else if (isUniversal) {
                invariantCdd = CDD.cddTrue();
            } else if (isProduct()) {
                this.invariantCdd = CDD.cddTrue();
                for (Location location : productOf) {
                    this.invariantCdd = this.invariantCdd.conjunction(location.getInvariantCddLazy());
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
