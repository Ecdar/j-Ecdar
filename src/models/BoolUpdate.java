package models;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BoolUpdate extends Update{

    private final BoolVar bv; // dont need to know the value here
    private final boolean value;

    public BoolUpdate(BoolVar bv, boolean value) {
        this.bv = bv;
        this.value = value;
    }

    public BoolUpdate(BoolUpdate copy, List<BoolVar> bvs){
        this.bv = bvs.get(bvs.indexOf(copy.bv));
        this.value = copy.value;
    }

    public List<BoolVal> applyUpdate(List<BoolVal> list)
    {
        List<BoolVal> newList = new ArrayList<BoolVal>();
        for (BoolVal bvl : list) {
            if (bvl.getVar().equals(this.bv)) {
                BoolVal copy = new BoolVal(bvl.getVar(), value);
                newList.add(copy);
            } else {
                BoolVal copy = new BoolVal(bvl.getVar(), bvl.getValue());
                newList.add(copy);
            }
        }
        return newList;


    }

    public BoolVar getBV() {
        return bv;
    }

    public boolean getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BoolUpdate)) return false;
        BoolUpdate update = (BoolUpdate) o;
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
