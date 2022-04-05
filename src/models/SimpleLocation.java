package models;

import java.util.List;
import java.util.Objects;

public class SimpleLocation extends SymbolicLocation {
    private final Location location;


    public SimpleLocation(Location location) {
        this.location = location;
    }

    public Location getActualLocation() {
        return location;
    }

    @Override
    public String getName() {
        return location.getName();
    }

    @Override
    public boolean getIsInitial() {
        return location.isInitial();
    }

    @Override
    public boolean getIsUrgent() {
        return location.isUrgent();
    }

    @Override
    public boolean getIsUniversal() {
        return location.isUniversal();
    }

    @Override
    public boolean getIsInconsistent() {
        return location.isInconsistent();
    }

    @Override
    public int getY() {
        return location.getY();
    }

    @Override
    public int getX() {
        return location.getX();
    }

    public CDD getInvariantCDD() {
        return new CDD(location.getInvariant());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleLocation that = (SimpleLocation) o;
        return location.equals(that.location);
    }

    @Override
    public int hashCode() {
        return Objects.hash(location);
    }

    @Override
    public String toString() {
        return "" + location;
    }
}