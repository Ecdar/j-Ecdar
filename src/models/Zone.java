package models;

import lib.DBMLib;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Zone {
    private int[] dbm;
    private int size;
    private int actualSize;
    private static final int DBM_INF = Integer.MAX_VALUE - 1;

    public Zone(int size, boolean delay) {
        this.size = size;
        this.actualSize = size * size;

        int[] temp = new int[actualSize];

        // zone for initial state is dbm_zero with delay
        this.dbm = DBMLib.dbm_zero(temp, size);
        if(delay) delay();
    }

    public Zone(int[] dbm) {
        this.dbm = dbm.clone();
        this.size = (int) Math.sqrt(dbm.length);
        this.actualSize = dbm.length;

    }



    // copy constructor
    public Zone(Zone oldZone) {
        this.size = oldZone.size;
        this.actualSize = oldZone.actualSize;
        this.dbm = oldZone.dbm.clone();

    }

    public boolean isEmpty() {return DBMLib.dbm_isEmpty(dbm, size);}

    public int getSize() {
        return size;
    }

    public int getElementAt(int i) {
        return dbm[i];
    }

    public void buildConstraintsForGuard(Guard g, int index) {
        boolean isStrict = g.isStrict();

        int lowerBoundI = g.getLowerBound();
        int upperBoundI = g.getUpperBound();

        // if guard is of type "clock == value"

//        if (upperBoundI == Integer.MAX_VALUE && (lowerBoundI == 0)) {
//            constrain1(index, 0, DBM_INF, isStrict);
//        }
        if (upperBoundI == Integer.MAX_VALUE && lowerBoundI != 0) {
            //System.out.println("been here");
            constrain1(0, index, (-1) * lowerBoundI, isStrict);
        }
        else
        if (lowerBoundI == 0 && upperBoundI != Integer.MAX_VALUE)
            constrain1(index, 0, upperBoundI, isStrict);
        else
        if (lowerBoundI == upperBoundI) {
            constrain1(0, index, (-1) * lowerBoundI, isStrict);
            constrain1(index, 0, upperBoundI, isStrict);
        }
    }

    public void updateValue(int index, int value) {
        dbm = DBMLib.dbm_updateValue(dbm, size, index, value);
    }

    public int[] delayNewDBM() {
        return DBMLib.dbm_up(dbm, size);
    }

    public void delay() {
        dbm = DBMLib.dbm_up(dbm, size);
    }

    public void extrapolateMaxBounds(int[] maxBounds){
        dbm = DBMLib.dbm_extrapolateMaxBounds(dbm, size, maxBounds);
    }

    public boolean isSubset(Zone zone2) {
        return DBMLib.dbm_isSubsetEq(this.dbm, zone2.dbm, size);
    }

    public boolean isValid() {
        return DBMLib.dbm_isValid(dbm, size);
    }

    // This zone and received zone MUST BE OF THE SAME SIZE!!!
    public boolean intersects(Zone zone){
        if(this.size != zone.size) throw new IllegalArgumentException("Zones must be of the same size");
        return DBMLib.dbm_intersection(dbm, zone.dbm, size);
    }

    public boolean canDelayIndefinitely(){
        for (int i = 1; i < size; i++) {
            int curr = dbm[size * i];
            if (curr < DBM_INF) return false;
        }
        return true;
    }

    public Zone close(){
        return new Zone(DBMLib.dbm_close(dbm, size));
    }

    public Zone freeClock(int index)
    {
        return new Zone(DBMLib.dbm_freeClock(dbm, size, index));

    }

    public boolean isUrgent(){
        for (int i = 1; i < size; i++) {
            int currLower = dbm[i];
            int currUpper = dbm[size * i];
            if (DBMLib.dbm_addRawRaw(currLower, currUpper) != 1)
                return false;
        }
        return true;
    }

    public List<Guard> buildGuardsFromZone(List<Clock> clocks) {
        List<Guard> guards = new ArrayList<>();
        for (int i = 1; i < size; i++) {
            Clock clock = clocks.get(i - 1);

            // values from first row, lower bounds
            int lb = dbm[i];
            // lower bound must be different from 1 (==0)
            if (lb != 1) {
                Guard g1 = new Guard(clock, (-1) * DBMLib.raw2bound(lb), true, DBMLib.dbm_rawIsStrict(lb));
                guards.add(g1);
            }
            // values from first column, upper bounds
            int ub = dbm[size*i];
            // upper bound must be different from infinity
            if (ub != DBM_INF) {
                Guard g2 = new Guard(clock, DBMLib.raw2bound(ub), false, DBMLib.dbm_rawIsStrict(ub));
                guards.add(g2);
            }
        }

        return guards;
    }

    // FURTHER METHODS ARE ONLY MEANT TO BE USED FOR TESTING. NEVER USE THEM DIRECTLY IN YOUR CODE
    public void constrain1(int i, int j, int constraint, boolean isStrict) {
        dbm = DBMLib.dbm_constrainBound(dbm, size, i, j, constraint, isStrict);
    }

    public void init() {
        dbm = DBMLib.dbm_init(dbm, size);
    }

    public int[] getDbm() {
        return dbm;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Zone zone = (Zone) o;
        return size == zone.size &&
                actualSize == zone.actualSize &&
                Arrays.equals(dbm, zone.dbm);
    }

    // Method to nicely print DBM for testing purposes.
    // The boolean flag determines if values of the zone will be converted from DBM format to actual bound of constraint
    public void printDBM(boolean toConvert, boolean showStrictness) {
        int intLength = 0;
        int toPrint = 0;

        System.out.println("---------------------------------------");
        for (int i = 0, j = 1; i < actualSize; i++, j++) {

            toPrint = toConvert ? DBMLib.raw2bound(dbm[i]) : dbm[i];

            System.out.print(toPrint);

            if (showStrictness) {
                String strictness = DBMLib.dbm_rawIsStrict(dbm[i]) ? " < " : " <=";
                System.out.print(strictness);
            }
            if (j == size) {
                System.out.println();
                if (i == actualSize - 1) System.out.println("---------------------------------------");
                j = 0;
            } else {
                intLength = String.valueOf(toPrint).length();
                for (int k = 0; k < 14 - intLength; k++) {
                    System.out.print(" ");
                }
            }
        }
    }

    @Override
    public String toString() {
        return Arrays.toString(dbm);
    }
}
