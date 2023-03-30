package models;

public interface GuardVisitor<T> {
    T visit(AndGuard andGuard);
    T visit(OrGuard orGuard);
    T visit(ClockGuard clockGuard);
    T visit(BoolGuard boolGuard);
    T visit(TrueGuard trueGuard);
    T visit(FalseGuard falseGuard);
}
