package models;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Postorder tree traversal of the guards (DFS traversal).
 */
public class DiagonalConstraintRemover implements GuardVisitor<Guard> {
    private final CDDFactory cddFactory;
    private final GuardFactory guardFactory;

    public DiagonalConstraintRemover(GuardFactory guardFactory, CDDFactory cddFactory) {
        this.guardFactory = guardFactory;
        this.cddFactory = cddFactory;
    }

    @Override
    public Guard visit(AndGuard guard) {
        List<Guard> parts = guard.getGuards();
        return removeDiagonalConstraints(parts, AndGuard::new);
    }

    @Override
    public Guard visit(OrGuard guard) {
        List<Guard> parts = guard.getGuards();
        return removeDiagonalConstraints(parts, OrGuard::new);
    }

    @Override
    public Guard visit(ClockGuard guard) {
        return guard;
    }

    @Override
    public Guard visit(BoolGuard guard) {
        return guard;
    }

    @Override
    public Guard visit(TrueGuard guard) {
        return guard;
    }

    @Override
    public Guard visit(FalseGuard guard) {
        return guard;
    }

    private List<Guard> getDiagonalGuards(List<Guard> guards) {
        List<Guard> diagonals = new ArrayList<>();
        for (Guard guard : guards) {
            if (guard instanceof ClockGuard && ((ClockGuard) guard).isDiagonal()) {
                diagonals.add(guard);
            }
        }
        return diagonals;
    }

    private Guard removeDiagonalConstraints(List<Guard> parts, Function<List<Guard>, Guard> constructor) {
        List<Guard> simplifiedParts = new ArrayList<>();

        // Visit children first as we perform DFS traversal through the tree.
        for (Guard part : parts) {
            Guard simplifiedPart = part.accept(this);
            simplifiedParts.add(simplifiedPart);
        }

        // Get all diagonal guards and non-diagonal guards.
        List<Guard> diagonalGuards = getDiagonalGuards(simplifiedParts);
        List<Guard> nonDiagonalGuards = new ArrayList<>(simplifiedParts);
        nonDiagonalGuards.removeAll(diagonalGuards);

        // The final required parts in the simplified AndGuard
        List<Guard> finalParts = new ArrayList<>(nonDiagonalGuards);

        // Pre-compute the non-diagonal CDD.
        CDD nonDiagonalConjunctionCdd = cddFactory.create(constructor.apply(nonDiagonalGuards));

        // Iterate all diagonal guards and check if it is implied or not.
        for (Guard diagonalGuard : diagonalGuards) {
            CDD diagonalGuardCdd = cddFactory.create(diagonalGuard);

            // If the diagonal guard is implied then we can ignore it.
            if (!nonDiagonalConjunctionCdd.implies(diagonalGuardCdd)) {
                finalParts.add(diagonalGuard);
            }
        }

        return constructor.apply(finalParts);
    }
}
