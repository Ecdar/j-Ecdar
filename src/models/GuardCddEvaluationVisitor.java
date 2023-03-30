package models;

import java.util.Optional;

public class GuardCddEvaluationVisitor implements GuardVisitor<Optional<Boolean>> {
    private final CDDFactory factory;

    public GuardCddEvaluationVisitor(CDDFactory factory) {
        this.factory = factory;
    }

    @Override
    public Optional<Boolean> visit(AndGuard andGuard) {
        return visit((Guard) andGuard);
    }

    @Override
    public Optional<Boolean> visit(OrGuard orGuard) {
        return visit((Guard) orGuard);
    }

    @Override
    public Optional<Boolean> visit(ClockGuard clockGuard) {
        return visit((Guard) clockGuard);
    }

    @Override
    public Optional<Boolean> visit(BoolGuard boolGuard) {
        return visit((Guard) boolGuard);
    }

    @Override
    public Optional<Boolean> visit(TrueGuard trueGuard) {
        return visit((Guard) trueGuard);
    }

    @Override
    public Optional<Boolean> visit(FalseGuard falseGuard) {
        return visit((Guard) falseGuard);
    }

    private Optional<Boolean> visit(Guard guard) {
        CDD cdd = factory.create(guard);
        if (cdd.equivTrue()) {
            return Optional.of(true);
        } else if (cdd.equivFalse()) {
            return Optional.of(false);
        }
        return Optional.empty();
    }
}
