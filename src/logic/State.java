package logic;

import global.LibLoader;
import lib.DBMLib;
import models.Clock;
import models.Guard;
import models.Update;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class State {
    private final SymbolicLocation location;
    private int[] zone;
    private final int zoneSize;
    private static final int DBM_INF = 1073741823;

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
        boolean isStrict = g.isStrict();

        int lowerBoundI = g.getLowerBound();
        int upperBoundI = g.getUpperBound();

        if (upperBoundI == Integer.MAX_VALUE) {
            zone = DBMLib.dbm_constrain1(zone, zoneSize, 0, i, (-1) * lowerBoundI, isStrict);
        }

        if (lowerBoundI == 0) {
            zone = DBMLib.dbm_constrain1(zone, zoneSize, i, 0, upperBoundI, isStrict);
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

    public int[] getAbsoluteZone(List<Guard> guards, List<Clock> clocks) {
        int[] result = zone;
        //TODO figure this out
        boolean isStrict = false;
        List<Integer> clockIndices = IntStream.rangeClosed(1, zoneSize - 1).boxed().collect(Collectors.toList());

        for (Guard guard : guards) {
            int index = clocks.indexOf(guard.getClock()) + 1;
            boolean firstVisit = clockIndices.contains(index);

            // Get corresponding UBs (UpperBounds) and LBs (LowerBounds)
            int guardUB = guard.getUpperBound();
            int clockUB = DBMLib.raw2bound(zone[zoneSize * index]);
            int clockLB = DBMLib.raw2bound(zone[index]) * (-1);
            int constraint;

            if (firstVisit && clockLB != 0) {
                result = DBMLib.dbm_freeDown(result, zoneSize, index);
                clockIndices.remove(new Integer(index));
            }
            // If guard is GEQ:
            if (guardUB == Integer.MAX_VALUE) {
                constraint = guard.getLowerBound();

                int newLowerBound = constraint - clockLB < 0 ? 0 : constraint - clockLB;

                if (firstVisit && clockUB != DBM_INF) {
                    int newUpperBound = clockUB - clockLB;

                    result = DBMLib.dbm_constrain1(result, zoneSize, index, 0, newUpperBound, isStrict);
                }

                result = DBMLib.dbm_constrain1(result, zoneSize, 0, index, (-1) * newLowerBound, isStrict);
            } else {
                // If guard is LEQ:
                constraint = guardUB;
                int newUpperBound = constraint > clockUB && clockUB != DBM_INF ? clockUB - clockLB : constraint - clockLB;

                result = DBMLib.dbm_constrain1(result, zoneSize, index, 0, newUpperBound, isStrict);
            }
        }

        // After processing all guards we have to update clocks that had no related guards
        for (Integer index : clockIndices){

            int clockUB = DBMLib.raw2bound(zone[zoneSize * index]);
            int clockLB = DBMLib.raw2bound(zone[index]) * (-1);

            if (clockLB != 0) {
                result = DBMLib.dbm_freeDown(result, zoneSize, index);

                if (clockUB != DBM_INF)
                    result = DBMLib.dbm_constrain1(result, zoneSize, index, 0, clockUB - clockLB, isStrict);
            }
        }
        return result;
    }

    public boolean zoneContainsNegatives(int[] zone){
        if (zoneSize > 2) {
            for (int i = zoneSize; i < zone.length; i++) {
                if (DBMLib.raw2bound(zone[i]) < 0) return true;
            }
        }
        return false;
    }

    public boolean absoluteZonesIntersect(int[] absZone1, int[] absZone2, int zone1Size, int zone2Size){
        int[] values1 = getRowMaxColumnMin(absZone1, zone1Size);
        int[] values2 = getRowMaxColumnMin(absZone2, zone2Size);

        return values1[0] <= values2[1] && values2[0] <= values1[1];
    }

    private int[] getRowMaxColumnMin(int[] zone, int size){
        int rowMax = 0; int columnMin = Integer.MAX_VALUE;

        for (int i = 1; i < size; i++) {
            int curr = DBMLib.raw2bound(zone[i]);
            if(curr > rowMax) rowMax = curr;

            curr = DBMLib.raw2bound(zone[size * i]) * (-1);
            if(curr < columnMin) columnMin = curr;
        }

        return new int[]{rowMax, columnMin};
    }

    public void updateLowerBounds(int[] prevZone, int[] absZone){
        for (int i = 1; i < zone.length; i++) {

            int prevValue = DBMLib.raw2bound(prevZone[i] * (-1));
            int absValue = DBMLib.raw2bound(absZone[i] * (-1));
            int currValue = DBMLib.raw2bound(zone[i] * (-1));
            if (currValue != prevValue + absValue){
                //TODO update the strictness boolean!!!
                zone = DBMLib.dbm_constrain1(zone, zoneSize, 0, i, prevValue + absValue, false);
            }
        }
    }

    public void delay() {
        zone = DBMLib.dbm_up(zone, zoneSize);
    }
}
