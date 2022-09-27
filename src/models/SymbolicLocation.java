package models;

import logic.State;

import java.util.ArrayList;
import java.util.List;

public class SymbolicLocation extends Location {

    private SymbolicLocation(
            String name,
            Guard invariantGuard,
            boolean isInitial,
            boolean isUrgent,
            boolean isUniversal,
            boolean isInconsistent,
            int x,
            int y,
            List<SymbolicLocation> productOf
    ) {
        super(
                name,
                invariantGuard,
                isInitial,
                isUrgent,
                isUniversal,
                isInconsistent,
                x,
                y,
                productOf
        );
        this.name = name;
        this.invariantGuard = invariantGuard;
        this.isInitial = isInitial;
        this.isUrgent = isUrgent;
        this.isUniversal = isUniversal;
        this.isInconsistent = isInconsistent;
        this.x = x;
        this.y = y;
    }

    private SymbolicLocation(Location location) {
        this(
                location.getName(),
                location.getInvariantGuard(),
                location.isInitial(),
                location.isUrgent(),
                location.isUniversal(),
                location.isInconsistent(),
                location.getX(),
                location.getY(),
                new ArrayList<>()
        );
        this.location = location;
    }

    public static SymbolicLocation createProduct(List<SymbolicLocation> productOf) {
        StringBuilder nameBuilder = new StringBuilder();
        boolean isInitial = true;
        boolean isUniversal = true;
        boolean isUrgent = false;
        boolean isInconsistent = false;
        int x = 0;
        int y = 0;

        for (SymbolicLocation location : productOf) {
            nameBuilder.append(location.getName());
            isInitial = isInitial && location.isInitial();
            isUniversal = isUniversal && location.isUniversal();
            isUrgent = isUrgent || location.isUrgent();
            isInconsistent = isInconsistent || location.isInconsistent();
            x += location.getX();
            y += location.getY();
        }

        int amount = productOf.size();
        x /= amount;
        y /= amount;
        String name = nameBuilder.toString();

        Guard invariant = null;

        return new SymbolicLocation(
                name,
                invariant,
                isInitial,
                isUrgent,
                isUniversal,
                isInconsistent,
                x,
                y,
                productOf
        );
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
                y,
                new ArrayList<>()
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
                y,
                new ArrayList<>()
        );
    }

    public static SymbolicLocation createInconsistentLocation(String name, int x, int y) {
        return SymbolicLocation.createInconsistentLocation(name, false, false, x, y);
    }

    public static SymbolicLocation createSimple(Location location) {
        return new SymbolicLocation(location);
    }
}