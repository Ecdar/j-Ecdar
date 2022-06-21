package models;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import logic.TransitionSystem;

public class Clock {
    private String name;
    private String uniqueName;
    private String ownerName;

    public Clock(String name, String ownerName) {
        this.name = name;
        this.uniqueName = name;
        this.ownerName = ownerName;
    }

    public Clock(Clock copy){
        String s = copy.name;
//        while (allClockNames.contains(s))
//            s+="_";
//        allClockNames.add(s);
        this.name = s;
        this.uniqueName = copy.uniqueName;
        this.ownerName = copy.ownerName;
    }

    public String getName() {
        return name;
    }

    public String getUniqueName(){
        return uniqueName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Clock clock = (Clock) o;
        return name.equals(clock.getName());
    }

    @Override
    public String toString() {
        return "Clock{" +
                "name='" + name + '\'' +
                '}';
    }

    public void setUniqueName(int index) {
        if(ownerName != null){
            uniqueName = ownerName + "." + index + "." + name;
        }
    }

    public void setUniqueName() {
        if(ownerName != null){
            uniqueName = ownerName + "." + name;
        }
    }

    public String getOwnerName() {
        return ownerName;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, ownerName);
    }
}
