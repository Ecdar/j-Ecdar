package models;

import java.util.List;
import java.util.Optional;

public class GuardShortCircuitVisitor implements GuardVisitor<Guard> {
    private final List<GuardVisitor<Optional<Boolean>>> evaluators;

    public GuardShortCircuitVisitor(List<GuardVisitor<Optional<Boolean>>> evaluators) {
        this.evaluators = evaluators;
    }

    @Override
    public Guard visit(AndGuard andGuard) {
        return visit((Guard) andGuard);
    }

    @Override
    public Guard visit(OrGuard orGuard) {
        return visit((Guard) orGuard);
    }

    @Override
    public Guard visit(ClockGuard clockGuard) {
        return visit((Guard) clockGuard);
    }

    @Override
    public Guard visit(BoolGuard boolGuard) {
        return visit((Guard) boolGuard);
    }

    @Override
    public Guard visit(TrueGuard trueGuard) {
        return visit((Guard) trueGuard);
    }

    @Override
    public Guard visit(FalseGuard falseGuard) {
        return visit((Guard) falseGuard);
    }

    private Guard visit(Guard guard) {
        return computeResult(guard).orElse(guard);
    }

    private Optional<Guard> computeResult(Guard guard) {
        return convertResult(
                getFirstResult(guard)
        );
    }
    
    private Optional<Guard> convertResult(Optional<Boolean> result) {
        return result.map(val -> val ? new TrueGuard() : new FalseGuard());
    }

    private Optional<Boolean> getFirstResult(Guard guard) {
        for (GuardVisitor<Optional<Boolean>> evaluator : evaluators) {
            Optional<Boolean> evaluation = guard.accept(evaluator);
            if (evaluation.isPresent()) {
                return evaluation;
            }
        }

        return Optional.empty();
    }
}
