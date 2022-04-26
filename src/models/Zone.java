package models;

import lib.DBMLib;

import models.Relation;
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

    private static int getIndexOfClock(Clock clock, List<Clock> clocks) {

        for (int i = 0; i < clocks.size(); i++){
            if(clock.hashCode() == clocks.get(i).hashCode()) return i+1;
        }
        return 0;
    }

    public boolean isEmpty() {return DBMLib.dbm_isEmpty(dbm, size);}

    public int getSize() {
        return size;
    }

    public int getElementAt(int i) {
        return dbm[i];
    }

    public void buildConstraintsForGuard(ClockGuard g, List<Clock> clocks) {
        if (g.isDiagonal())
            buildConstraintsForDiagonalConstraint(g,clocks);
        else
            buildConstraintsForNormalGuard(g,clocks);
    }

    public void buildConstraintsForNormalGuard(ClockGuard g, List<Clock> clocks) {
        int index = getIndexOfClock(g.getClock_i(),clocks);
        Relation rel = g.getRelation();
        int lowerBoundI = g.getLowerBound();
        int upperBoundI = g.getUpperBound();
        switch (rel) {
            case EQUAL: {
                constrain1(0, index, (-1) * lowerBoundI, false);
                constrain1(index, 0, upperBoundI, false);
                break;
            }
            case NOT_EQUAL: {
                // TODO: Zones cannot do a non equal, we would need Federations for that
                break;
            }
            case LESS_THAN: {
                constrain1(index, 0, upperBoundI, true);
                break;
            }
            case LESS_EQUAL: {
                constrain1(index, 0, upperBoundI, false);
                break;
            }
            case GREATER_THAN: {
                constrain1(0, index, (-1) * lowerBoundI, true);
                break;
            }
            case GREATER_EQUAL: {
                constrain1(0, index, (-1) * lowerBoundI, false);
                break;
            }
        }
    }

    public void buildConstraintsForDiagonalConstraint(ClockGuard dc, List<Clock> clocks) {
        Relation rel = dc.getRelation();
        Clock clock_i = dc.getClock_i();
        Clock clock_j = dc.getClock_j();
        int val = dc.getBound();

        int i= getIndexOfClock(clock_i,clocks);
        int j= getIndexOfClock(clock_j,clocks);



        switch (rel) {
            case LESS_THAN: {
                constrain1(i, j, val, true);
                break;
            }
            case LESS_EQUAL: {
                constrain1(i, j, val, false);
                break;
            }
            default: {
                assert(false);
                break;
            }
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

    private static boolean relevantClocksContainsClock(Clock clock, List<Clock> relevantClocks) {

        for (int i = 0; i < relevantClocks.size(); i++){
            if(clock.hashCode() == relevantClocks.get(i).hashCode()) return true;
        }
        return false;
    }

    public Guard buildGuardsFromZone(List<Clock> clocks, List<Clock> relevantClocks) {
        List<Guard> guards = new ArrayList<>();
        guards.addAll(buildNormalGuardsFromZone(clocks,relevantClocks));
        guards.addAll(buildDiagonalConstraintsFromZone(clocks,relevantClocks));
        return new AndGuard(guards);
    }

    public List<ClockGuard> buildNormalGuardsFromZone(List<Clock> clocks, List<Clock> relevantClocks) {
        List<ClockGuard> guards = new ArrayList<>();

        for (int i = 1; i < size; i++) {
            Clock clock = clocks.get(i - 1);
            if (!relevantClocksContainsClock(clock, relevantClocks))
                continue;
            // values from first row, lower bounds
            int lb = dbm[i];

            // values from first column, upper bounds
            int ub = dbm[size * i];

            if (ub==lb && !DBMLib.dbm_rawIsStrict(lb) && !DBMLib.dbm_rawIsStrict(ub))
            {
                ClockGuard g1 = new ClockGuard(clock, (-1) * DBMLib.raw2bound(lb), Relation.EQUAL);
                guards.add(g1);
                continue;
            }

            if (lb != 1) {   // lower bound must be different from 1 (==0)
                if (DBMLib.dbm_rawIsStrict(lb)) {
                    ClockGuard g1 = new ClockGuard(clock, (-1) * DBMLib.raw2bound(lb), Relation.GREATER_THAN);
                    guards.add(g1);
                } else {
                    ClockGuard g1 = new ClockGuard(clock, (-1) * DBMLib.raw2bound(lb), Relation.GREATER_EQUAL);
                    guards.add(g1);
                }
            }

            // upper bound must be different from infinity
            if (ub != DBM_INF) {
                if (DBMLib.dbm_rawIsStrict(ub)) {
                    ClockGuard g2 = new ClockGuard(clock, DBMLib.raw2bound(ub), Relation.LESS_THAN);
                    guards.add(g2);
                } else {
                    ClockGuard g2 = new ClockGuard(clock, DBMLib.raw2bound(ub),Relation.LESS_EQUAL);
                    guards.add(g2);
                }

            }
        }

        return guards;
    }

    public List<ClockGuard> buildDiagonalConstraintsFromZone(List<Clock> clocks, List<Clock> relevantClocks) {
        List<ClockGuard> guards = new ArrayList<>();

        for (int i = 1; i < size; i++) {
            for (int j = 1; j < size; j++) {
                if (i==j)
                    continue;
                //if (i==1 || j==1)
                //    continue;
                Clock clock_i = clocks.get(i - 1);
                Clock clock_j = clocks.get(j - 1);
                if (!relevantClocksContainsClock(clock_i, relevantClocks) || !relevantClocksContainsClock(clock_j, relevantClocks) )
                    continue;
                // values from first row, lower bounds
                int currentValue = dbm[i+j*size];
                if (currentValue==DBM_INF)
                    continue;

                if (DBMLib.dbm_rawIsStrict(currentValue))
                {
                    ClockGuard dc = new ClockGuard(clock_j,clock_i,DBMLib.raw2bound(currentValue), Relation.LESS_THAN);
                    guards.add(dc);
                } else
                {
                    ClockGuard dc = new ClockGuard(clock_j,clock_i,DBMLib.raw2bound(currentValue),Relation.LESS_EQUAL);
                   // System.out.println("i: " + i + " j: " + j + " ci: + " + clock_i + " cj: " + clock_j + " " + dc + " clocks: " + clocks + " relevantClocks: " + relevantClocks);
                    guards.add(dc);
                }

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
