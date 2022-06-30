package models;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import logic.TransitionSystem;

public class Clock extends UniqueNamed{

    public Clock(String name, String ownerName) {
        this.uniqueName = name;
        this.originalName = name;
        this.ownerName = ownerName;
    }

    public Clock(Clock copy){
        this.uniqueName = copy.originalName;
        this.originalName = copy.originalName;
        this.ownerName = copy.ownerName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Clock clock = (Clock) o;
        return Objects.equals(originalName, clock.originalName) && ownerName.equals(clock.ownerName);
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
    public UniqueNamed getCopy() {
        return new Clock(this);
    }
}
