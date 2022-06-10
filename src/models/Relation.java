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

    public static Relation fromString(String text) {
        if (text != null) {
            for (Relation b : Relation.values()) {
                if (b.name.equals(text)) {
                    return b;
                }
            }
        }
        return null;
    }

    public String toString() {
        return this.name;
    }
}
