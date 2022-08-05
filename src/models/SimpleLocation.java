package models;

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

    @Override
    public CDD getInvariantCDD() {
        return new CDD(location.getInvariant());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof SimpleLocation)) {
            return false;
        }

        SimpleLocation other = (SimpleLocation) obj;
        return location.equals(other.location);
    }

    @Override
    public int hashCode() {
        return Objects.hash(location);
    }

    @Override
    public String toString() {
        return location.toString();
    }
}