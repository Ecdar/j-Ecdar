package models;

import lib.CDDLib;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GuardFactory {
    private static GuardFactory singleton = null;

    static {
        GuardFactory.singleton = new GuardFactory();
    }

    public static Guard createFrom(CDD cdd) {
        return singleton.create(cdd, CDDRuntime.getAllClocks());
    }

    public static Guard createFrom(CDD cdd, List<Clock> relevantClocks) {
        return singleton.create(cdd, relevantClocks);
    }

    public Guard create(CDD cdd) {
        return create(cdd, CDDRuntime.getAllClocks());
    }

    public Guard create(CDD cdd, List<Clock> relevantClocks) {
        CDD copy = cdd.hardCopy();
        copy = copy.reduce().removeNegative();
        return copy.isBDD() ? createBooleanGuards(copy) : createClockGuards(copy, relevantClocks);
    }

    private Guard createBooleanGuards(CDD cdd) {
        if (cdd.equivFalse()) {
            return new FalseGuard();
        } else if (cdd.equivTrue()) {
            return new TrueGuard();
        }

        long ptr = cdd.getPointer();
        BDDArrays arrays = new BDDArrays(CDDLib.bddToArray(ptr));

        List<Guard> orParts = new ArrayList<>();
        for (int i = 0; i < arrays.traceCount; i++) {

            List<Guard> andParts = new ArrayList<>();
            for (int j = 0; j < arrays.booleanCount; j++) {

                int index = arrays.getVariables().get(i).get(j);
                if (index >= 0) {
                    BoolVar var = CDDRuntime.getAllBooleanVariables().get(index - CDDRuntime.getBddStartLevel());
                    boolean val = arrays.getValues().get(i).get(j) == 1;
                    BoolGuard bg = new BoolGuard(var, Relation.EQUAL, val);

                    andParts.add(bg);
                }
            }

            orParts.add(new AndGuard(andParts));
        }
        return new OrGuard(orParts);
    }

    private Guard createClockGuards(CDD cdd, List<Clock> relevantClocks) {
        List<Guard> orParts = new ArrayList<>();
        while (!cdd.isTerminal()) {
            // Extract and advance the cdd.
            CddExtractionResult extraction = cdd.extract();
            cdd = extraction.getCddPart().reduce().removeNegative();

            // Create clock guards.
            Zone zone = new Zone(extraction.getDbm());
            Guard clockGuard = zone.buildGuardsFromZone(CDDRuntime.getAllClocks(), relevantClocks);

            // Create boolean guards.
            CDD bdd = extraction.getBddPart();
            Guard booleanGuard = createBooleanGuards(bdd);

            // Conjoin and add both clock and boolean guards.
            List<Guard> andParts = new ArrayList<>();
            andParts.add(clockGuard);
            andParts.add(booleanGuard);
            andParts = andParts.stream().filter(guard -> !(guard instanceof TrueGuard)).collect(Collectors.toList());

            orParts.add(new AndGuard(andParts));
        }
        return new OrGuard(orParts);
    }
}
