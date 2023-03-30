package models;

import lib.CDDLib;

import java.util.List;

public class CDDFactory {
    private static CDDFactory singleton = null;

    static {
        CDDFactory.singleton = new CDDFactory();
    }

    public static CDD createFrom(Guard guard) throws IllegalArgumentException {
        return CDDFactory.singleton.create(guard);
    }

    public CDD create(Guard guard) throws IllegalArgumentException {
        if (guard instanceof FalseGuard) {
            return singleton.createFrom((FalseGuard) guard);
        } else if (guard instanceof TrueGuard) {
            return singleton.createFrom((TrueGuard) guard);
        } else if (guard instanceof ClockGuard) {
            return singleton.createFrom((ClockGuard) guard);
        } else if (guard instanceof BoolGuard) {
            return singleton.createFrom((BoolGuard) guard);
        } else if (guard instanceof AndGuard) {
            return singleton.createFrom((AndGuard) guard);
        } else if (guard instanceof OrGuard) {
            return singleton.createFrom((OrGuard) guard);
        }
        throw new IllegalArgumentException("Guard instance of class '" + guard.getClass().getName() + "' is not supported ");
    }

    private CDD createFrom(FalseGuard falseGuard) {
        CDD cdd = CDD.cddFalse();
        cdd.setGuard(falseGuard);
        return cdd;
    }

    private CDD createFrom(TrueGuard trueGuard) {
        CDD cdd = CDD.cddTrue();
        cdd.setGuard(trueGuard);
        return cdd;
    }

    private CDD createFrom(ClockGuard clockGuard) {
        Zone zone = new Zone(CDDRuntime.getNumberOfClocks(), true);
        zone.init();
        zone.buildConstraintsForGuard(clockGuard, CDDRuntime.getAllClocks());
        CDD cdd = CDD.createFromDbm(zone.getDbm(), CDDRuntime.getNumberOfClocks());
        cdd.setGuard(clockGuard);
        return cdd;
    }

    private CDD createFrom(BoolGuard boolGuard) {
        int level = CDDRuntime.getBddStartLevel() + CDDRuntime.indexOf(boolGuard.getVar());
        CDD cdd;
        if (boolGuard.getValue()) {
            cdd = new CDD(CDDLib.cddBddvar(level));
        } else {
            cdd = new CDD(CDDLib.cddNBddvar(level));
        }
        cdd.setGuard(boolGuard);
        return cdd;
    }

    private CDD createFrom(AndGuard andGuard) {
        CDD cdd = CDD.cddTrue();
        for (Guard guard : andGuard.getGuards()) {
            CDD guardCdd = createFrom(guard);
            cdd = cdd.conjunction(guardCdd);
        }
        cdd.setGuard(andGuard);
        return cdd;
    }

    private CDD createFrom(OrGuard orGuard) {
        CDD cdd = CDD.cddFalse();
        for (Guard guard : orGuard.getGuards()) {
            CDD guardCdd = createFrom(guard);
            cdd = cdd.disjunction(guardCdd);
        }
        cdd.setGuard(orGuard);
        return cdd;
    }

    public static CDD createFrom(List<Update> updates) throws IllegalArgumentException {
        CDD res = CDD.cddTrue();
        for (Update update : updates) {
            if (update instanceof ClockUpdate) {
                CDD clockUpdate = singleton.createFrom((ClockUpdate) update);
                res = res.conjunction(clockUpdate);
            } else if (update instanceof BoolUpdate) {
                CDD clockUpdate = singleton.createFrom((BoolUpdate) update);
                res = res.conjunction(clockUpdate);
            } else {
                throw new IllegalArgumentException("Update instance of class '" + update.getClass().getName() + "' is not supported ");
            }
        }
        return res.removeNegative().reduce();
    }

    private CDD createFrom(ClockUpdate clockUpdate) {
        return CDD.createInterval(CDDRuntime.indexOf(clockUpdate.getClock()), 0, clockUpdate.getValue(), true, clockUpdate.getValue(), true);
    }

    private CDD createFrom(BoolUpdate boolUpdate) {
        return createFrom(new BoolGuard(boolUpdate.getBV(), Relation.EQUAL, boolUpdate.getValue()));
    }
}
