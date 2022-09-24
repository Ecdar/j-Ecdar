package models;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class SymbolicLocation {
    private String name;
    private Guard invariant;
    private CDD invariantCdd;
    private boolean isInitial;
    private boolean isUrgent;
    private boolean isUniversal;
    private boolean isInconsistent;
    private int x;
    private int y;
    private List<SymbolicLocation> productOf = new ArrayList<>();
    private Location location;

    public SymbolicLocation() { }

    public SymbolicLocation(
            String name,
            Guard invariant,
            boolean isInitial,
            boolean isUrgent,
            boolean isUniversal,
            boolean isInconsistent,
            int x,
            int y
    ) {
        this.name = name;
        this.invariant = invariant;
        this.isInitial = isInitial;
        this.isUrgent = isUrgent;
        this.isUniversal = isUniversal;
        this.isInconsistent = isInconsistent;
        this.x = x;
        this.y = y;
    }

    public SymbolicLocation(
            String name,
            CDD invariant,
            boolean isInitial,
            boolean isUrgent,
            boolean isUniversal,
            boolean isInconsistent,
            int x,
            int y
    ) {
        this.name = name;
        this.invariantCdd = invariant;
        this.isInitial = isInitial;
        this.isUrgent = isUrgent;
        this.isUniversal = isUniversal;
        this.isInconsistent = isInconsistent;
        this.x = x;
        this.y = y;
    }

    public SymbolicLocation(Location location) {
        this(
                location.getName(),
                location.getInvariant(),
                location.isInitial(),
                location.isUrgent(),
                location.isUniversal(),
                location.isInconsistent(),
                location.getX(),
                location.getY()
        );
        this.location = location;
    }

    public SymbolicLocation(List<SymbolicLocation> productOf) {
        this.productOf = productOf;

        StringBuilder nameBuilder = new StringBuilder();
        this.isInitial = true;
        this.isUniversal = true;
        this.isUrgent = false;
        this.isInconsistent = false;
        this.x = 0;
        this.y = 0;
        for (SymbolicLocation location : productOf) {
            nameBuilder.append(location.getName());
            this.isInitial = isInitial && location.getIsInitial();
            this.isUniversal = isUniversal && location.getIsUniversal();
            this.isUrgent = isUrgent || location.getIsUrgent();
            this.isInconsistent = isInconsistent || location.getIsInconsistent();
            this.x += location.getX();
            this.y += location.getY();
        }

        int amount = productOf.size();
        this.x /= amount;
        this.y /= amount;
        this.name = nameBuilder.toString();
    }


    public static SymbolicLocation createUniversalLocation(
            String name,
            boolean isInitial,
            boolean isUrgent,
            int x,
            int y
    ) {
        return new SymbolicLocation(
                name,
                new TrueGuard(),
                isInitial,
                isUrgent,
                true,
                false,
                x,
                y
        );
    }

    public static SymbolicLocation createUniversalLocation(String name, int x, int y) {
        return SymbolicLocation.createUniversalLocation(name, false, false, x, y);
    }


    public static SymbolicLocation createInconsistentLocation(
            String name,
            boolean isInitial,
            boolean isUrgent,
            int x,
            int y
    ) {
        return new SymbolicLocation(
                name,
                new FalseGuard(),
                isInitial,
                isUrgent,
                false,
                true,
                x,
                y
        );
    }

    public static SymbolicLocation createInconsistentLocation(String name, int x, int y) {
        return SymbolicLocation.createInconsistentLocation(name, false, false, x, y);
    }

    public static SymbolicLocation createProduct(List<SymbolicLocation> locations) {
        return new SymbolicLocation(locations);
    }

    public static SymbolicLocation createSimple(Location location) {
        return new SymbolicLocation(location);
    }

    public String getName() {
        return name;
    }

    public Guard getInvariantAsGuard() {
        if (isSimple()) {
            return location.getInvariant();
        }

        if (invariant == null) {
            if (isInconsistent || isUniversal) {
                invariant = getInvariantAsCdd().getGuard();
            } else {
                invariant = invariantCdd.getGuard();
            }
        }

        return invariant;
    }

    public CDD getInvariantAsCdd() {
        if (isSimple()) {
            return location.getInvariantCDD();
        }

        if (invariantCdd == null) {
            if (isInconsistent) {
                invariantCdd = CDD.cddZero();
            } else if (isUniversal) {
                invariantCdd = CDD.cddTrue();
            } else if (isProduct()) {
                this.invariantCdd = CDD.cddTrue();
                for (SymbolicLocation location : productOf) {
                    this.invariantCdd = this.invariantCdd.conjunction(location.getInvariantAsCdd());
                }
            } else {
                invariantCdd = new CDD(invariant);
            }
        }

        return invariantCdd;
    }

    public void removeInvariants() {
        invariant = new TrueGuard();
        invariantCdd = CDD.cddTrue();
    }

    public List<SymbolicLocation> getProductOf() {
        return productOf;
    }

    public boolean isProduct() {
        return productOf.size() > 0;
    }

    public boolean isSimple() {
        return location != null;
    }

    public Location getSimpleLocation() {
        return location;
    }

    public boolean getIsInitial() {
        return isInitial;
    }

    public boolean getIsUrgent() {
        return isUrgent;
    }

    public boolean getIsUniversal() {
        return isUniversal;
    }

    public boolean getIsInconsistent() {
        return isInconsistent;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SymbolicLocation that = (SymbolicLocation) o;

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

        return getIsInitial() == that.getIsInitial() &&
                getIsUrgent() == that.getIsUrgent() &&
                getIsUniversal() == that.getIsUniversal() &&
                getIsInconsistent() == that.getIsInconsistent() &&
                getX() == that.getX() &&
                getY() == that.getY() &&
                getName().equals(that.getName()) &&
                getInvariantAsCdd().equals(that.getInvariantAsCdd());
    }

    @Override
    public int hashCode() {
        if (isProduct()) {
            return Objects.hash(productOf);
        }

        return Objects.hash(name, invariant, invariantCdd, isInitial, isUrgent, isUniversal, isInconsistent, x, y);
    }
}