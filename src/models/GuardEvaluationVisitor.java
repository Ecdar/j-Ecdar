package models;

import java.util.List;
import java.util.Optional;

public class GuardEvaluationVisitor implements GuardVisitor<Optional<Boolean>> {
    @Override
    public Optional<Boolean> visit(AndGuard andGuard) {
        List<Guard> parts = andGuard.getGuards();
        boolean contradiction = parts.stream().anyMatch((part) -> {
            Optional<Boolean> evaluation = part.accept(this);
            return evaluation.isPresent() && !evaluation.get();
        });
        boolean tautology = parts.stream().anyMatch((part) -> {
            Optional<Boolean> evaluation = part.accept(this);
            return evaluation.isPresent() && evaluation.get();
        });

        if (contradiction) {
            return Optional.of(false);
        } else if (tautology) {
            return Optional.of(true);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Boolean> visit(OrGuard orGuard) {
        List<Guard> parts = orGuard.getGuards();
        boolean tautology = parts.stream().anyMatch((part) -> {
            Optional<Boolean> evaluation = part.accept(this);
            return evaluation.isPresent() && evaluation.get();
        });
        boolean contradiction = parts.stream().anyMatch((part) -> {
            Optional<Boolean> evaluation = part.accept(this);
            return evaluation.isPresent() && !evaluation.get();
        });

        if (contradiction) {
            return Optional.of(false);
        } else if (tautology) {
            return Optional.of(true);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Boolean> visit(ClockGuard clockGuard) {
        return Optional.empty();
    }

    @Override
    public Optional<Boolean> visit(BoolGuard boolGuard) {
        return Optional.empty();
    }

    @Override
    public Optional<Boolean> visit(TrueGuard trueGuard) {
        return Optional.of(true);
    }

    @Override
    public Optional<Boolean> visit(FalseGuard falseGuard) {
        return Optional.of(false);
    }
}
