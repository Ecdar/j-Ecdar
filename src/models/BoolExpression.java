package models;

import java.util.*;

public class BoolExpression extends Expression {
    private final BoolVar var;
    private final Relation relation;
    private final boolean value;

    public BoolExpression(BoolVar var, Relation relation, boolean value) {
        // These are the only relation types allowed
        if (relation != Relation.EQUAL && relation != Relation.NOT_EQUAL) {
            throw new IllegalArgumentException("The relation of the clock expression is invalid");
        }
        this.var = var;
        this.relation = relation;
        this.value = value;
    }

    public BoolExpression(BoolVar var, String comparator, boolean value)
            throws NoSuchElementException {
        this(var, Relation.fromString(comparator), value);
    }

    public BoolExpression(BoolExpression copy, List<BoolVar> newBVs, List<BoolVar> oldBVs)
            throws IndexOutOfBoundsException, NoSuchElementException {
        this(newBVs.get(oldBVs.indexOf(copy.getVar())), copy.relation, copy.value);
    }

    public BoolVar getVar() {
        return var;
    }

    public boolean getValue() {
        return value;
    }

    public BoolExpression negate() {
        switch (relation) {
            case EQUAL:
                return new BoolExpression(var, Relation.NOT_EQUAL, value);
            case NOT_EQUAL:
                return new BoolExpression(var, Relation.EQUAL, value);
        }
        throw new IllegalStateException("The relation of the boolean expression is invalid");
    }

    @Override
    int getMaxConstant(Clock clock) {
        return 0;
    }

    @Override
    Expression copy(List<Clock> newClocks, List<Clock> oldClocks, List<BoolVar> newBVs, List<BoolVar> oldBVs) {
        return new BoolExpression(
            this, newBVs, oldBVs
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof BoolExpression)) {
            return false;
        }

        BoolExpression other = (BoolExpression) obj;

        return var.equals(other.var) &&
                relation == other.relation &&
                value == other.value;
    }

    @Override
    public String toString() {
        return "(" + var.getOriginalName() + relation.toString() + value + ")";
    }

    @Override
    public int hashCode() {
        return Objects.hash(var, relation, value);
    }
}
