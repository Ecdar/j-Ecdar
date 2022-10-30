package models;

import java.util.Objects;

public class BoolVar extends UniquelyNamed { // TODO: Later on make sure that states only store the values
    private boolean initialValue;

    public BoolVar(String name, String ownerName, boolean initialValue) {
        this.ownerName = ownerName;
        this.originalName = name;
        this.uniqueName = name;
        this.initialValue = initialValue;
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
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof BoolVar)) {
            return false;
        }

        BoolVar other = (BoolVar) obj;
        return other.getOriginalName().equals(originalName)
                && initialValue == other.initialValue
                && Objects.equals(ownerName, other.ownerName);

    }

    public String toString() {
        return getUniqueName() + " = " + getInitialValue();
    }

    @Override
    public UniquelyNamed getCopy() {
        return new BoolVar(this);
    }
}
