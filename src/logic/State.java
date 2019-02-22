package logic;

import global.LibLoader;
import lib.DBMLib;
import models.Clock;
import models.Guard;
import models.Update;

import java.util.List;
import java.util.stream.IntStream;

public class State {
    private final SymbolicLocation location;
    private int[] zone;
    private final int zoneSize;

    public State(SymbolicLocation location, int[] zone) {
        this.location = location;
        this.zone = zone;
        this.zoneSize = (int) Math.sqrt(zone.length);

        LibLoader.load();
    }

    public SymbolicLocation getLocation() {
        return location;
    }

    public int[] getZone() {
        return zone;
    }

    private int[] getZoneValues() {
        int[] newZone = new int[zone.length];

        for (int i = 0; i < zone.length; i++) {
            newZone[i] = DBMLib.raw2bound(zone[i]);
        }

        return newZone;
    }

    private void buildConstraintsForGuard(Guard g, List<Clock> clocks) {
        // get the guard's index in the clock array so you know the index in the DBM
        int i = clocks.indexOf(g.getClock()) + 1;

        int lowerBoundI = g.getLowerBound();
        int upperBoundI = g.getUpperBound();

        if (upperBoundI == Integer.MAX_VALUE) {
            zone = DBMLib.dbm_constrain1(zone, zoneSize, 0, i, (-1) * lowerBoundI);
        }

        if (lowerBoundI == 0) {
            zone = DBMLib.dbm_constrain1(zone, zoneSize, i, 0, upperBoundI);
        }
    }

    public int getMinUpperBound() {
        int[] newZone = getZoneValues();

        int min = Integer.MAX_VALUE;

        for (int i = 1; i < zoneSize; i++) {
            int curr = newZone[zoneSize * i];
            if (curr < min)
                min = curr;
        }

        return min;
    }

    public int getMinLowerBound() {
        int[] newZone = getZoneValues();

        int min = Integer.MAX_VALUE;

        for (int i = 1; i < zoneSize; i++) {
            int curr = (-1) * newZone[i];
            if (curr < min)
                min = curr;
        }

        return min;
    }

    private List<Guard> getInvariants() {
        return location.getInvariants();
    }

    public void applyGuards(List<Guard> guards, List<Clock> clocks) {
        for (Guard guard : guards) {
            // get guard and then its index in the clock array so you know the index in the DBM
            buildConstraintsForGuard(guard, clocks);
        }
    }

    public void applyInvariants(List<Clock> clocks) {
        for (Guard invariant : getInvariants()) {
            buildConstraintsForGuard(invariant, clocks);
        }
    }

    public void applyResets(List<Update> resets, List<Clock> clocks) {
        for (Update reset : resets) {
            int index = clocks.indexOf(reset.getClock());

            zone = DBMLib.dbm_updateValue(zone, zoneSize, (index + 1), reset.getValue());
        }
    }

    // Method used to get a new zone, where for each clock:
    // If not infinity, the upper bound is lowered by the value of the lower bound
    // The lower bound is removed (set to 0)
//    public int[] getAbsoluteZone(){
//        int[] result = zone;
//
//        for (int i = 1; i < zoneSize; i++){
//
//            int upperBound = DBMLib.raw2bound(result[zoneSize * i]);
//
//            // If upper bound is infinity then don't change it
//            if (upperBound != 1073741823) {
//                int lowerBound = DBMLib.raw2bound(result[i]) * (-1);
//                int resultBound = upperBound - lowerBound;
//
//                result = DBMLib.dbm_updateValue(result, zoneSize, i, resultBound);
//            }
//        }
//
//        result = DBMLib.dbm_freeAllDown(result, zoneSize);
//
//        return result;
//    }

    public int[] getAbsoluteZone(List<Guard> guards, List<Clock> clocks){
        int[] result = zone;

        //Need to test this
        int[] a = IntStream.range(1, zoneSize).toArray();

        for(Guard guard : guards) {
            int index = clocks.indexOf(guard.getClock());

            // Get correspondings UBs (UpperBounds) and LBs (LowerBounds)
            int guardUB = guard.getUpperBound();
            int clockUB = DBMLib.raw2bound(zone[zoneSize * index]);
            int clockLB = DBMLib.raw2bound(zone[index] * (-1));
            int constraint;

            // If guard is GEQ:
            if (guardUB == Integer.MAX_VALUE) {
                constraint = guard.getLowerBound();

                int newLowerBound = constraint - clockLB < 0 ? 0 : constraint - clockLB;
                int newUpperBound = clockUB - clockLB;

                result = DBMLib.dbm_updateValue(result, zoneSize, index, newUpperBound);
                result = DBMLib.dbm_freeDown(result, zoneSize, index);
                result = DBMLib.dbm_constrain1(result, zoneSize, 0, index, (-1) * newLowerBound);
            }
            else {
                // If guard is LEQ:
                constraint = guardUB;
                int newUpperBound = constraint - clockLB;

                // Protected against semantically invalid guards for the given zone (perhaps just to be removed)
                // Maybe can be simply replaced with dbm_isValid check at the end of the method (or most likely just removed)
                if(newUpperBound < 0) return new int[]{};

                result = DBMLib.dbm_updateValue(result, zoneSize, index, newUpperBound);
                result = DBMLib.dbm_freeDown(result, zoneSize, index);
            }
        }
        return result;
    }

    public void delay() {
        zone = DBMLib.dbm_up(zone, zoneSize);
    }
}
