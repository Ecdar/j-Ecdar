package models;

public class BoolVar { // TODO: Later on make sure that states only store the values

    private String name;
    private boolean initialValue;

    public BoolVar(String name, boolean initialValue)
    {
        this.name=name;
        this.initialValue=initialValue;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
        if (other.getName().equals(name) && initialValue == other.initialValue)
            return true;
        return false;

    }

    public String toString()
    {
        return getName() + " = " + getInitialValue();
    }
}
