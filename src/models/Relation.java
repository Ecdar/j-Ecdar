package models;

import java.util.Arrays;
import java.util.NoSuchElementException;

public enum Relation {
    LESS_THAN("<"),
    LESS_EQUAL("<="),
    EQUAL("=="),
    GREATER_EQUAL(">="),
    GREATER_THAN(">"),
    NOT_EQUAL("!=");

    private final String operator;

    Relation(String operator) {
        this.operator = operator;
    }

    public static Relation fromString(String text)
            throws IllegalArgumentException, NoSuchElementException {
        if (text == null) {
            throw new IllegalArgumentException("Cannot convert null string to relation");
        }
        return Arrays.stream(Relation.values())
                .filter(relation -> relation.operator.equals(text))
                .findFirst()
                .orElse(null);
    }

    public String toString() {
        return this.operator;
    }
}
