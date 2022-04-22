package models;

import java.util.ArrayList;
import java.util.List;

public class Clock {
    private final String name;

    public Clock(String name) {
        this.name = name;
    }

    public Clock(Clock copy){
        assert(false);
        System.out.println("making a copy");
        String s = copy.name;
//        while (allClockNames.contains(s))
//            s+="_";
//        allClockNames.add(s);
        this.name = s;
    }

    public String getName() {
        return name;
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
}
