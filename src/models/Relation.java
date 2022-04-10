package models;

public enum Relation {/*
    LESS_THAN ("<"),
    LESS_EQUAL ("≤"),
    EQUAL("=="),
    GREATER_EQUAL("≥"),
    GREATER_THAN (">"),
    NOT_EQUAL ("!=");*/
    LESS_THAN ("<"),
    LESS_EQUAL ("<="),
    EQUAL("=="),
    GREATER_EQUAL(">="),
    GREATER_THAN (">"),
    NOT_EQUAL ("!=");


    private final String name;

    private Relation(String s) {
        name = s;
    }

    public boolean equalsName(String otherName) {
        // (otherName == null) check is not needed because name.equals(null) returns false
        return name.equals(otherName);
    }

    public String toString() {
        return this.name;
    }
}
