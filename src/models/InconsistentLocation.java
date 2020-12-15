package models;

import java.util.ArrayList;
import java.util.List;

public class InconsistentLocation extends SymbolicLocation {

    public List<List<Guard>> getInvariants() {
        // TODO the new clock should be <= 0
        return new ArrayList<>();
    }
}
