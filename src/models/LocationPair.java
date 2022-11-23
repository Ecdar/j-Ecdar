package models;

import java.util.Objects;

public class LocationPair {
    public Location leftLocation, rightLocation;

    public LocationPair(Location leftLocation, Location rightLocation) {
        this.leftLocation = leftLocation;
        this.rightLocation = rightLocation;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof LocationPair)) {
            return false;
        }

        LocationPair other = (LocationPair) obj;
        return leftLocation.equals(other.leftLocation) &&
                rightLocation.equals(other.rightLocation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(leftLocation, rightLocation);
    }
}
