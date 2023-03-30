package models;

import lib.CDDLib;

import java.util.List;

public class CDDFactory {
    private static CDDFactory singleton = null;

    static {
        CDDFactory.singleton = new CDDFactory();
    }

    public static CDD create(Guard guard) throws IllegalArgumentException {
        if (guard instanceof FalseGuard) {
            return singleton.create((FalseGuard) guard);
        } else if (guard instanceof TrueGuard) {
            return singleton.create((TrueGuard) guard);
        } else if (guard instanceof ClockGuard) {
            return singleton.create((ClockGuard) guard);
        } else if (guard instanceof BoolGuard) {
            return singleton.create((BoolGuard) guard);
        } else if (guard instanceof AndGuard) {
            return singleton.create((AndGuard) guard);
        } else if (guard instanceof OrGuard) {
            return singleton.create((OrGuard) guard);
        }
        throw new IllegalArgumentException("Guard instance of class '" + guard.getClass().getName() + "' is not supported ");
    }

    private CDD create(FalseGuard falseGuard) {
        CDD cdd = CDD.cddFalse();
        cdd.setGuard(falseGuard);
        return cdd;
    }

    private CDD create(TrueGuard trueGuard) {
        CDD cdd = CDD.cddTrue();
        cdd.setGuard(trueGuard);
        return cdd;
    }

    private CDD create(ClockGuard clockGuard) {
        Zone zone = new Zone(CDD.numClocks, true);
        zone.init();
        zone.buildConstraintsForGuard(clockGuard, CDD.getClocks());
        CDD cdd = CDD.createFromDbm(zone.getDbm(), CDD.numClocks);
        cdd.setGuard(clockGuard);
        return cdd;
    }

    private CDD create(BoolGuard boolGuard) {
        int level = CDD.bddStartLevel + CDD.indexOf(boolGuard.getVar());
        CDD cdd;
        if (boolGuard.getValue()) {
            cdd = new CDD(CDDLib.cddBddvar(level));
        } else {
            cdd = new CDD(CDDLib.cddNBddvar(level));
        }
        cdd.setGuard(boolGuard);
        return cdd;
    }

    private CDD create(AndGuard andGuard) {
        CDD cdd = CDD.cddTrue();
        for (Guard guard : andGuard.getGuards()) {
            CDD guardCdd = create(guard);
            cdd = cdd.conjunction(guardCdd);
        }
        cdd.setGuard(andGuard);
        return cdd;
    }

    private CDD create(OrGuard orGuard) {
        CDD cdd = CDD.cddFalse();
        for (Guard guard : orGuard.getGuards()) {
            CDD guardCdd = create(guard);
            cdd = cdd.disjunction(guardCdd);
        }
        cdd.setGuard(orGuard);
        return cdd;
    }

    public static CDD create(List<Update> updates) throws IllegalArgumentException {
        CDD res = CDD.cddTrue();
        for (Update update : updates) {
            if (update instanceof ClockUpdate) {
                CDD clockUpdate = singleton.create((ClockUpdate) update);
                res = res.conjunction(clockUpdate);
            } else if (update instanceof BoolUpdate) {
                CDD clockUpdate = singleton.create((BoolUpdate) update);
                res = res.conjunction(clockUpdate);
            } else {
                throw new IllegalArgumentException("Update instance of class '" + update.getClass().getName() + "' is not supported ");
            }
        }
        return res.removeNegative().reduce();
    }

    private CDD create(ClockUpdate clockUpdate) {
        return CDD.createInterval(CDD.indexOf(clockUpdate.getClock()), 0, clockUpdate.getValue(), true, clockUpdate.getValue(), true);
    }

    private CDD create(BoolUpdate boolUpdate) {
        return create(new BoolGuard(boolUpdate.getBV(), Relation.EQUAL, boolUpdate.getValue()));
    }
}