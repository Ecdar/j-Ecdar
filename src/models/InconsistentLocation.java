package models;

import java.util.ArrayList;
import java.util.List;

public class InconsistentLocation extends SymbolicLocation {


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
        return false;
    }

    @Override
    public boolean getIsInconsistent() {
        return true;
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
        // TODO the new clock should be <= 0
        return new ArrayList<>();
    }
}
