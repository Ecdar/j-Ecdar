package models;

import logic.State;
import logic.TransitionSystem;
import logic.Pruning;

import java.util.*;

/**
 * {@link Location} is a class used by both {@link Automaton} and {@link TransitionSystem} to decribe a location.
 *  It is named and has coordinates describing the position where it should be drawn in the GUI.
 *  A {@link Location} can be marked as initial, urgent, universal, and inconsistent.
 *  In order to reduce the conversions between {@link Expression} and {@link CDD}
 *  the invariant is stored as both and only updated when required.
 *  For {@link Pruning} it also stores the inconsistent part of its invariant.
 * <p>
 * A {@link Location} can also be <b>composed</b> of multiple locations (children).
 *  A composed location is created when performing {@link logic.Conjunction} for multiple systems,
 *  and it represents an n-tuple of locations correlated with the sequence of {@link TransitionSystem TransitionSystems}.
 *  For this reason the composed location are directly addressable with the index of the systems.
 *  The invariant of a composed location is lazily created as the conjunction of its children's invariants.
 *  In this context lazily created means that the locally stored invariant value in this location is only
 *  updated when a change in this composed location warrants an update to it.
 *  This can be warranted when {@link #setInvariant(Expression)} is invoked.
 * </p>
 * <p>
 * A {@link Location} can also be a <b>simple</b> location, which is a location with exactly one child.
 *  A simple location is used when the {@link CDD CDD invariant} of this location
 *  is not directly created from the {@link Expression Invariant}.
 *  Instead the {@link CDD CDD invariant} of this location will always be the {@link CDD CDD invariant} of its child,
 *  whilst the {@link Expression Invariant} of this location can be different from the {@link CDD CDD invariant}.
 *  For this reason a simple location can have a {@link Expression Invariant} and {@link CDD CDD invariant}
 *  which is out of sync.
 *  <b>Deprecation warning:</b> <i>simple</i> locations are planned to be deprecated and one should instead create
 *      composed locations which have a more predictable specification
 * </p>
 * <ul>
 * State overview:
 *     <li>name
 *     <li>x and y coordinates
 *     <li>invariant both as {@link Expression} and {@link CDD}
 *     <li>inconsistent part for {@link Pruning}
 *     <li>whether it is initial, urgent, universal, inconsistent
 * </ul>
 *
 * @see LocationPair
 */
public final class Location {
    private String name;
    private int x, y;

    private Expression invariantExpression;
    private CDD invariantCdd;

    private CDD inconsistentPart;

    private boolean isInitial;
    private boolean isUrgent;
    private boolean isUniversal;
    private boolean isInconsistent;

    private final List<Location> children;

    private Location(
            String name,
            Expression invariantExpression,
            CDD invariantCdd,
            CDD inconsistentPart,
            boolean isInitial,
            boolean isUrgent,
            boolean isUniversal,
            boolean isInconsistent,
            List<Location> children,
            int x,
            int y
    ) {
        if (children == null) {
            children = new ArrayList<>();
        }

        this.name = name;
        this.invariantExpression = invariantExpression;
        this.invariantCdd = invariantCdd;
        this.inconsistentPart = inconsistentPart;
        this.isInitial = isInitial;
        this.isUrgent = isUrgent;
        this.isUniversal = isUniversal;
        this.isInconsistent = isInconsistent;
        this.children = children;
        this.x = x;
        this.y = y;
    }

    public static Location create(
            String name,
            Expression invariant,
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
            x,
            y
        );
    }

    public static Location create(
            String name,
            Expression invariant,
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

    public static Location createInitialLocation(
            String name,
            Expression invariant,
            boolean isUrgent,
            boolean isUniversal,
            boolean isInconsistent
    ) {
        return create(name, invariant, true, isUrgent, isUniversal, isInconsistent);
    }

    public static Location createComposition(List<Location> children) {
        if (children.size() == 0) {
            throw new IllegalArgumentException("Requires at least one location to create a product");
        }

        StringBuilder nameBuilder = new StringBuilder();
        boolean isInitial = true;
        boolean isUniversal = true;
        boolean isUrgent = false;
        boolean isInconsistent = false;
        int x = 0;
        int y = 0;

        List<Expression> expressions = new ArrayList<>();
        for (Location location : children) {
            nameBuilder.append(location.getName());
            isInitial = isInitial && location.isInitial();
            isUniversal = isUniversal && location.isUniversal();
            isUrgent = isUrgent || location.isUrgent();
            isInconsistent = isInconsistent || location.isInconsistent();
            x += location.getX();
            y += location.getY();
            expressions.add(location.getInvariant());
        }

        int amount = children.size();
        x /= amount;
        y /= amount;
        String name = nameBuilder.toString();

        Expression invariant = new AndExpression(expressions);
        return new Location(
            name,
            invariant,
            null,
            null,
            isInitial,
            isUrgent,
            isUniversal,
            isInconsistent,
            children,
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
            new TrueExpression(),
            null,
            null,
            isInitial,
            isUrgent,
            true,
            false,
            new ArrayList<>(),
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
            new FalseExpression(),
            null,
            null,
            isInitial,
            isUrgent,
            false,
            true,
            new ArrayList<>(),
            x,
            y
        );
    }

    public static Location createInconsistentLocation(String name, int x, int y) {
        return Location.createInconsistentLocation(name, false, false, x, y);
    }

    public static Location createSimple(Location child) {
        List<Location> children = new ArrayList<>();
        children.add(child);

        return new Location(
            child.getName(),
            child.getInvariant(),
            null,
            child.getInconsistentPart(),
            child.isInitial(),
            child.isUrgent(),
            child.isUniversal(),
            child.isInconsistent(),
            children,
            child.getX(),
            child.getY()
        );
    }

    public Location copy() {
        return new Location(
            getName(),
            getInvariant(),
            null,
            getInconsistentPart(),
            isInitial(),
            isUrgent(),
            isUniversal(),
            isInconsistent(),
            new ArrayList<>(),
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
            getInvariant().copy(
                    newClocks, oldClocks, newBVs, oldBVs
            ),
            null,
            null,
            isInitial(),
            isUrgent(),
            isUniversal(),
            isInconsistent(),
            getChildren(),
            getX(),
            getY()
        );
    }

    public boolean isSimple() {
        return children.size() == 1;
    }

    public boolean isComposed() {
        return children.size() > 1;
    }

    public List<Location> getChildren() {
        return children;
    }

    public void removeInvariants() {
        invariantExpression = new TrueExpression();
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

    public Expression getInvariant() {
        if (invariantExpression == null) {
            invariantExpression = getInvariantCdd().getExpression();
        }

        return invariantExpression;
    }

    public CDD getInvariantCdd() {
        if (isSimple()) {
            return new CDD(children.get(0).getInvariant());
        }

        if (invariantCdd == null) {
            if (isInconsistent) {
                invariantCdd = CDD.cddZero();
            } else if (isUniversal) {
                invariantCdd = CDD.cddTrue();
            } else if (children.size() > 0) {
                this.invariantCdd = CDD.cddTrue();
                for (Location location : children) {
                    this.invariantCdd = this.invariantCdd.conjunction(location.getInvariantCdd());
                }
            } else {
                invariantCdd = new CDD(getInvariant());
            }
        }

        return invariantCdd;
    }

    public void setInvariant(Expression invariantAsExpression) {
        this.invariantExpression = invariantAsExpression;
        this.invariantCdd = null;
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
        return getInvariant().getMaxConstant(clock);
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

        if (isComposed() && that.isComposed()) {
            if (children.size() != that.children.size()) {
                return false;
            }

            for (int i = 0; i < children.size(); i++) {
                if (!children.get(i).equals(that.children.get(i))) {
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
        if (isComposed()) {
            return Objects.hash(children);
        }

        return Objects.hash(name, getInvariant(), isInitial, isUrgent, isUniversal, isInconsistent);
    }
}
