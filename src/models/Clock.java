package models;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import logic.TransitionSystem;

public class Clock {
    private String uniqueName;
    private String originalName;
    private String ownerName;

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

    public String getOriginalName() {
        return originalName;
    }

    public String getUniqueName(){
        return uniqueName;
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

    public void setUniqueName(int index) {
        if(ownerName != null){
            uniqueName = ownerName + "." + index + "." + originalName;
        }
    }

    public void setUniqueName() {
        if(ownerName != null){
            uniqueName = ownerName + "." + originalName;
        }
    }

    public String getOwnerName() {
        return ownerName;
    }

    @Override
    public int hashCode() {
        return Objects.hash(originalName, ownerName);
    }
}
