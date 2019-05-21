package models;

import java.util.Objects;

public class LocationPair {
    private SymbolicLocation leftLocation, rightLocation;

    public LocationPair(SymbolicLocation leftLocation, SymbolicLocation rightLocation) {
        this.leftLocation = leftLocation;
        this.rightLocation = rightLocation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LocationPair that = (LocationPair) o;
        return leftLocation.equals(that.leftLocation) &&
                rightLocation.equals(that.rightLocation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(leftLocation, rightLocation);
    }
}
