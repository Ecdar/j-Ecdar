package models;

import java.util.*;

public class BoolGuard extends Guard {
    private final BoolVar var;
    private final Relation relation;
    private final boolean value;

    public BoolGuard(BoolVar var, Relation relation, boolean value) {
        // These are the only relation types allowed
        if (relation != Relation.EQUAL && relation != Relation.NOT_EQUAL) {
            throw new IllegalArgumentException("The relation of the clock guard is invalid");
        }
        this.var = var;
        this.relation = relation;
        this.value = value;
    }

    public BoolGuard(BoolVar var, String comparator, boolean value)
            throws NoSuchElementException {
        this(var, Relation.fromString(comparator), value);
    }

    public BoolGuard(BoolGuard copy, List<BoolVar> newBVs, List<BoolVar> oldBVs)
            throws IndexOutOfBoundsException, NoSuchElementException {
        this(newBVs.get(oldBVs.indexOf(copy.getVar())), copy.relation, copy.value);
    }

    public BoolVar getVar() {
        return var;
    }

    public boolean getValue() {
        return value;
    }

    public BoolGuard negate() {
        switch (relation) {
            case EQUAL:
                return new BoolGuard(var, Relation.NOT_EQUAL, value);
            case NOT_EQUAL:
                return new BoolGuard(var, Relation.EQUAL, value);
        }
        throw new IllegalStateException("The relation of the boolean guard is invalid");
    }

    @Override
    int getMaxConstant(Clock clock) {
        return 0;
    }

    @Override
    Guard copy(List<Clock> newClocks, List<Clock> oldClocks, List<BoolVar> newBVs, List<BoolVar> oldBVs) {
        return new BoolGuard(
            this, newBVs, oldBVs
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof BoolGuard)) {
            return false;
        }

        BoolGuard other = (BoolGuard) obj;

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
