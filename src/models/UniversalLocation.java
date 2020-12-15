package models;

import java.util.ArrayList;
import java.util.List;

public class UniversalLocation extends SymbolicLocation {

    public List<List<Guard>> getInvariants() {
        // should be true, so no invariants
        return new ArrayList<>();
    }
}
