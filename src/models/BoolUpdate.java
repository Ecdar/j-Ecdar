package models;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class BoolUpdate extends Update {
    // dont need to know the value here
    private final BoolVar bv;
    private final boolean value;

    public BoolUpdate(BoolVar bv, boolean value) {
        this.bv = bv;
        this.value = value;
    }

    public BoolUpdate(BoolUpdate copy, List<BoolVar> bvs, List<BoolVar> oldBVs)
        throws IndexOutOfBoundsException {
        this.bv = bvs.get(oldBVs.indexOf(copy.getBV()));
        this.value = copy.value;
    }

    public BoolVar getBV() {
        return bv;
    }

    public boolean getValue() {
        return value;
    }

    public List<BoolVal> update(List<BoolVal> list) {
        return list
            .stream()
            .map(
                boolVal -> new BoolVal(boolVal.getVar(), boolVal.getVar().equals(this.bv) ? value : boolVal.getValue())
            )
            .collect(Collectors.toList());
    }

    @Override
    Update copy(List<Clock> newClocks, List<Clock> oldClocks, List<BoolVar> newBVs, List<BoolVar> oldBVs) {
        return new BoolUpdate(this, newBVs, oldBVs);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof BoolUpdate)) {
            return false;
        }

        BoolUpdate update = (BoolUpdate) obj;

        return value == update.value &&
                bv.equals(update.bv);
    }

    @Override
    public String toString() {
        return "Update{" +
                "bv=" + bv +
                ", value=" + value +
                '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(bv, value);
    }
}
