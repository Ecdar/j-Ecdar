package models;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ComplexLocation extends SymbolicLocation {
    private final List<SymbolicLocation> locations;
    private CDD invariants;

    public ComplexLocation(List<SymbolicLocation> locations) {
        this.locations = locations;
        CDD invar = CDD.cddTrue();
        for (SymbolicLocation loc1 : locations)
        {
            CDD invarLoc = loc1.getInvariantCDD();
            invar = invar.conjunction(invarLoc);
        }
        invariants = invar;
    }

    public List<SymbolicLocation> getLocations() {
        return locations;
    }

    @Override
    public String getName() {
        String name = "";
        for (SymbolicLocation l: getLocations())
        {
            name+=l.getName();
        }
        return name;
    }

    @Override
    public boolean getIsInitial() {
        boolean isInitial = true;
        for (SymbolicLocation l: getLocations())
        {
            isInitial = isInitial && l.getIsInitial();
        }
        return isInitial;
    }

    @Override
    public int getX() {
        int x = 0;
        for (SymbolicLocation l: getLocations())
        {
            x= l.getX();
        }
        return x/getLocations().size();
    }

    @Override
    public int getY() {
        int y = 0;
        for (SymbolicLocation l: getLocations())
        {
            y= l.getX();
        }
        return y/getLocations().size();
    }

    @Override
    public boolean getIsUrgent() {
        boolean isUrgent = false;
        for (SymbolicLocation l: getLocations())
        {
            isUrgent = isUrgent || l.getIsUrgent();
        }
        return isUrgent;
    }

    @Override
    public boolean getIsUniversal() {
        boolean isUniversal = false;
        for (SymbolicLocation l: getLocations())
        {
            isUniversal = isUniversal|| l.getIsUrgent();
        }
        return isUniversal;
    }

    @Override
    public boolean getIsInconsistent() {
        boolean isInconsistent = false;
        for (SymbolicLocation l: getLocations())
        {
            isInconsistent = isInconsistent|| l.getIsUrgent();
        }
        return isInconsistent;
    }

    public CDD getInvariantCDD() {
        return invariants;
    }

    public void removeInvariants() {
        invariants = CDD.cddTrue();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ComplexLocation that = (ComplexLocation) o;
        return Arrays.equals(locations.toArray(), that.locations.toArray());
    }

    @Override
    public int hashCode() {
        return Objects.hash(locations);
    }

    @Override
    public String toString() {
        return "" + locations;
    }
}