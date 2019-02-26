package logic;

import global.LibLoader;
import lib.DBMLib;
import models.Clock;
import models.Guard;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Zone {
    private int[] dbm;
    private int size;
    private int actualSize;
    private static final int DBM_INF = Integer.MAX_VALUE - 1;

    public Zone(int size) {
        this.size = size;
        this.actualSize = size * size;

        LibLoader.load();
        int[] temp = new int[actualSize];
        this.dbm = DBMLib.dbm_init(temp, size);
    }

    public Zone(int[] dbm) {
        this.dbm = dbm.clone();
        this.size = (int) Math.sqrt(dbm.length);
        this.actualSize = dbm.length;

        LibLoader.load();
    }

    // copy constructor
    public Zone(Zone oldZone) {
        this.size = oldZone.size;
        this.actualSize = oldZone.actualSize;
        this.dbm = oldZone.dbm.clone();

        LibLoader.load();
    }

    public int getElementAt(int i) {
        return dbm[i];
    }

    public int[] getZoneValues() {
        int[] newZone = new int[actualSize];

        for (int i = 0; i < actualSize; i++) {
            newZone[i] = DBMLib.raw2bound(dbm[i]);
        }

        return newZone;
    }

    public int getMinUpperBound() {
        int[] newZone = getZoneValues();

        int min = Integer.MAX_VALUE;

        for (int i = 1; i < size; i++) {
            int curr = newZone[size * i];
            if (curr < min)
                min = curr;
        }

        return min;
    }

    public int getMinLowerBound() {
        int[] newZone = getZoneValues();

        int min = Integer.MAX_VALUE;

        for (int i = 1; i < size; i++) {
            int curr = (-1) * newZone[i];
            if (curr < min)
                min = curr;
        }

        return min;
    }

    public void buildConstraintsForGuard(Guard g, int index) {
        boolean isStrict = g.isStrict();

        int lowerBoundI = g.getLowerBound();
        int upperBoundI = g.getUpperBound();

        if (upperBoundI == Integer.MAX_VALUE) {
            constrain1(0, index, (-1) * lowerBoundI, isStrict);
        }

        if (lowerBoundI == 0) {
            dbm = DBMLib.dbm_constrain1(dbm, size, index, 0, upperBoundI, isStrict);
            constrain1(index, 0, upperBoundI, isStrict);
        }
    }

    public void updateValue(int index, int value) {
        dbm = DBMLib.dbm_updateValue(dbm, size, index, value);
    }

    public void delay() {
        dbm = DBMLib.dbm_up(dbm, size);
    }

    public int[] getAbsoluteZone(List<Guard> guards, List<Clock> clocks) {
        int[] result = dbm;
        //TODO figure this out
        boolean isStrict = false;
        List<Integer> clockIndices = IntStream.rangeClosed(1, size - 1).boxed().collect(Collectors.toList());

        for (Guard guard : guards) {
            int index = clocks.indexOf(guard.getClock()) + 1;
            isStrict = guard.isStrict();
            boolean firstVisit = clockIndices.contains(index);

            // Get corresponding UBs (UpperBounds) and LBs (LowerBounds)
            int guardUB = guard.getUpperBound();
            int rawClockUB = dbm[size * index];
            int rawClockLB = dbm[index];
            int constraint, rawConstraint;

            if (firstVisit && rawClockLB != 1) {
                result = DBMLib.dbm_freeDown(result, size, index);
                clockIndices.remove(new Integer(index));
            }
            // If guard is GEQ:
            if (guardUB == Integer.MAX_VALUE) {
                constraint = guard.getLowerBound();
                rawConstraint = DBMLib.boundbool2raw(constraint, isStrict);

                int rawNewLB = rawConstraint - rawClockLB < 0 ? 1 : DBMLib.dbm_addRawRaw(rawConstraint, rawClockLB);
                isStrict = DBMLib.dbm_rawIsStrict(rawNewLB);
                int newLB = DBMLib.raw2bound(rawNewLB);

                result = DBMLib.dbm_constrain1(result, size, 0, index, (-1) * newLB, isStrict);

                if (firstVisit && rawClockUB != DBM_INF) {
                    int rawNewUB = DBMLib.dbm_addRawRaw(rawClockUB, rawClockLB);
                    isStrict = DBMLib.dbm_rawIsStrict(rawNewUB);
                    int newUB = DBMLib.raw2bound(rawNewUB);

                    result = DBMLib.dbm_constrain1(result, size, index, 0, newUB, isStrict);
                }


            } else {
                // If guard is LEQ:
                constraint = guardUB;
                rawConstraint = DBMLib.boundbool2raw(constraint, isStrict);

                int rawNewUB = rawConstraint > rawClockUB && rawClockUB != DBM_INF ?
                        DBMLib.dbm_addRawRaw(rawClockUB, rawClockLB) :
                        DBMLib.dbm_addRawRaw(rawConstraint, rawClockLB);

                isStrict = DBMLib.dbm_rawIsStrict(rawNewUB);

                int newUB = DBMLib.raw2bound(rawNewUB);

                result = DBMLib.dbm_constrain1(result, size, index, 0, newUB, isStrict);
            }
        }

        // After processing all guards we have to update clocks that had no related guards
        for (Integer index : clockIndices){

            int clockUB = dbm[size * index];
            int clockLB = dbm[index];

            if (clockLB != 1) {
                result = DBMLib.dbm_freeDown(result, size, index);

                if (clockUB != DBM_INF) {
                    int rawNewUB = DBMLib.dbm_addRawRaw(clockUB, clockLB);
                    isStrict = DBMLib.dbm_rawIsStrict(rawNewUB);
                    int newUB = DBMLib.raw2bound(rawNewUB);
                    result = DBMLib.dbm_constrain1(result, size, index, 0, newUB, isStrict);
                }
            }
        }
        return result;
    }

    public boolean containsNegatives() {
        if (size > 2) {
            for (int i = size; i < actualSize; i++) {
                if (dbm[i] <= 0) return true;
            }
        }
        return false;
    }

    public boolean absoluteZonesIntersect(Zone absZone1, Zone absZone2) {
        int[] values1 = absZone1.getRowMaxColumnMin();
        int[] values2 = absZone2.getRowMaxColumnMin();

        return values1[0] <= values2[1] && values2[0] <= values1[1];
    }

    private int[] getRowMaxColumnMin() {
        int rowMax = 0; int columnMin = Integer.MAX_VALUE;

        for (int i = 1; i < size; i++) {
            int curr = DBMLib.raw2bound(dbm[i]);
            if (curr > rowMax) rowMax = curr;

            curr = DBMLib.raw2bound(dbm[size * i]) * (-1);
            if (curr < columnMin) columnMin = curr;
        }

        return new int[]{rowMax, columnMin};
    }

    public void updateLowerBounds(Zone prevZone, Zone absZone) {
        for (int i = 1; i < actualSize; i++) {

            int prevValue = DBMLib.raw2bound(prevZone.getElementAt(i) * (-1));
            int absValue = DBMLib.raw2bound(absZone.getElementAt(i) * (-1));
            int currValue = DBMLib.raw2bound(dbm[i] * (-1));
            if (currValue != prevValue + absValue) {
                //TODO update the strictness boolean!!!
                dbm = DBMLib.dbm_constrain1(dbm, size, 0, i, prevValue + absValue, false);
            }
        }
    }

    public boolean isSubset(Zone zone2) {
        return DBMLib.dbm_isSubsetEq(this.dbm, zone2.dbm, size);
    }

    public boolean isValid() {
        return DBMLib.dbm_isValid(dbm, size);
    }

    // FURTHER METHODS ARE ONLY MEANT TO BE USED FOR TESTING. NEVER USE THEM DIRECTLY IN YOUR CODE
    public void constrain1(int i, int j, int constraint, boolean isStrict){
        dbm = DBMLib.dbm_constrain1(dbm, size, i, j, constraint, isStrict);
    }

    public void init(){
        dbm = DBMLib.dbm_init(dbm, size);
    }

    // Method to nicely print DBM for testing purposes.
    // The boolean flag determines if values of the zone will be converted from DBM format to actual bound of constraint
    public void printDBM(boolean toConvert, boolean showStrictness){
        int intLength = 0;
        int toPrint = 0;

        System.out.println("---------------------------------------");
        for(int i = 0,j = 1; i < actualSize; i++, j++){

            toPrint = toConvert? DBMLib.raw2bound(dbm[i]) : dbm[i];

            System.out.print(toPrint);

            if(showStrictness){
                String strictness = DBMLib.dbm_rawIsStrict(dbm[i]) ? " < " : " <=";
                System.out.print(strictness);
            }
            if(j == size){
                System.out.println();
                if(i == actualSize - 1) System.out.println("---------------------------------------");
                j = 0;
            }
            else{
                intLength = String.valueOf(toPrint).length();
                for (int k = 0; k < 14 - intLength; k++)
                {
                    System.out.print(" ");
                }
            }
        }
    }
}
