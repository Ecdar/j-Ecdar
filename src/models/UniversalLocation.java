package models;

import java.util.ArrayList;
import java.util.List;

public class UniversalLocation extends SymbolicLocation {

    @Override
    public String getName() {
        return null;
    }

    @Override
    public boolean getIsInitial() {
        return false;
    }

    @Override
    public boolean getIsUrgent() {
        return false;
    }

    @Override
    public boolean getIsUniversal() {
        return true;
    }

    @Override
    public boolean getIsInconsistent() {
        return false;
    }

    @Override
    public int getY() {
        return 0;
    }

    @Override
    public int getX() {
        return 0;
    }

    public List<List<Guard>> getInvariants() {
        // should be true, so no invariants
        return new ArrayList<>();
    }
}
