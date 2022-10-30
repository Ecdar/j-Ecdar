package models;

import java.util.Objects;

public class Clock extends UniquelyNamed {
    public Clock(String name, String ownerName) {
        this.uniqueName = name;
        this.originalName = name;
        this.ownerName = ownerName;
    }

    public Clock(Clock copy) {
        this.uniqueName = copy.originalName;
        this.originalName = copy.originalName;
        this.ownerName = copy.ownerName;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Clock)) {
            return false;
        }

        Clock other = (Clock) obj;
        return originalName.equals(other.originalName)
                && ownerName.equals(other.ownerName);
    }

    @Override
    public String toString() {
        return "Clock{" +
                "name='" + uniqueName + '\'' +
                '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(originalName, ownerName);
    }

    @Override
    public UniquelyNamed getCopy() {
        return new Clock(this);
    }
}
