package models;

import java.util.Objects;

public class BoolVar extends UniqueNamed { // TODO: Later on make sure that states only store the values

    private boolean initialValue;

    public BoolVar(String name, String ownerName, boolean initialValue)
    {
        this.ownerName = ownerName;
        this.originalName = name;
        this.uniqueName = name;
        this.initialValue=initialValue;
    }

    public BoolVar(BoolVar bv) {
        this.ownerName = bv.ownerName;
        this.originalName = bv.originalName;
        this.uniqueName = bv.originalName;
        this.initialValue = bv.initialValue;
    }

    public boolean getInitialValue() {
        return initialValue;
    }

    public void setInitialValue(boolean value) {
        this.initialValue = initialValue;
    }

    @Override
    public  boolean equals(Object o) {
        if (!(o instanceof BoolVar))
            return false;
        BoolVar other =  (BoolVar) o;
        if (other.getOriginalName().equals(originalName) && initialValue == other.initialValue && Objects.equals(ownerName, other.ownerName))
            return true;
        return false;

    }

    public String toString()
    {
        return getUniqueName() + " = " + getInitialValue();
    }

    @Override
    public UniqueNamed getCopy() {
        return new BoolVar(this);
    }
}
