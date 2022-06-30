package models;

import java.util.*;
import java.util.stream.Collectors;


public class BoolGuard extends Guard {


    public BoolVar getVar() {
        return var;
    } //boolvar is correct

    public String getComperator() {
        return comperator;
    }

    public boolean getValue() {
        return value;
    }

    private final BoolVar var;
    private final String comperator;
    private final boolean value;

    public BoolGuard(BoolVar var, String comperator, boolean value) {
        this.var = var;
        this.comperator = comperator;
        this.value = value;
    }

    // Copy constructor
    public BoolGuard(BoolGuard copy, List<BoolVar> newBVs, List<BoolVar> oldBVs) {
        System.out.println("old bvs" + oldBVs + " " + copy.getVar());
        var = newBVs.get(oldBVs.indexOf(copy.getVar()));
        comperator = copy.comperator;
        value = copy.value;
    }




    public BoolGuard negate() {
        switch (comperator) {
            case "==":
                return new BoolGuard(var, "!=", value);
            case "!=":
                return new BoolGuard(var, "==", value);
            default:
                assert (false);
        }
        return null;
    }

    @Override
    int getMaxConstant() {
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BoolGuard)) return false;
        BoolGuard guard = (BoolGuard) o;
        return var.equals(guard.var) &&
                comperator.equals(guard.comperator) &&
                value == guard.value;
    }

    @Override
    public String toString() {
        return "(" +
                var.getOriginalName() +
                comperator +
                value +
                ')';
    }

    @Override
    public int hashCode() {
        return Objects.hash(var, comperator, value);
    }
}
