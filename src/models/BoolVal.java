package models;

public class BoolVal { // TODO: Later on make sure that states only store the values

    private BoolVar var;
    private boolean value;

    public BoolVal(BoolVar var, boolean value)
    {
        this.var=var;
        this.value=value;
    }

    public String getName() {
        return var.getOriginalName();
    }

    public BoolVar getVar() {
        return var;
    }

    public void setVar(BoolVar var) {
        this.var = var;
    }

    public boolean getValue() {
        return value;
    }

    public void setValue(boolean value) {
        this.value = value;
    }

    @Override
    public  boolean equals(Object o) {
        if (!(o instanceof BoolVal))
            return false;
        BoolVal other =  (BoolVal) o;
        if (other.getName().equals(getName()) && other.value == value)
            return true;
        return false;

    }

    public String toString()
    {
        return getName() + " = " + getValue();
    }
}
