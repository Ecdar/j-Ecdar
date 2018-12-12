package models;

public class Clock {

    private String name;

    public Clock(String name) {
        this.name = name;
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

    // When adding a clock to a set, it shouldn't work if it has the same name as a clock that already exists in the set
    // so the hashCode() method is used to compare them. We override it so 2 different Clock objects with the same
    // name would have the same hash code
    @Override
    public int hashCode() {
        return 10 * name.hashCode();
    }
}
