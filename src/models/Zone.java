package models;

import lib.DBMLib;
import log.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Zone {
    private static final int DBM_INF = Integer.MAX_VALUE - 1;

    private int[] dbm;
    private final int dimension;
    private final int length;

    public Zone(int length, boolean delay) {
        this.dimension = length;
        this.length = length * length;

        // zone for initial state is dbm_zero with delay
        this.dbm = DBMLib.dbm_zero(new int[this.length], length);
        if (delay) {
            delay();
        }
    }

    public Zone(int[] dbm) {
        this.dbm = dbm.clone();
        this.dimension = (int) Math.sqrt(dbm.length);
        this.length = dbm.length;
    }

    public Zone(Zone zone) {
        this.dimension = zone.dimension;
        this.length = zone.length;
        this.dbm = zone.dbm.clone();
    }

    private static int getIndexOfClock(Clock clock, List<Clock> clocks) {
        int index = clocks.indexOf(clock);
        return index == -1 ? 0 : index + 1;
    }

    public boolean isEmpty() {
        return DBMLib.dbm_isEmpty(dbm, dimension);
    }

    public int getDimension() {
        return dimension;
    }

    public void buildConstraintsForGuard(ClockGuard guard, List<Clock> clocks) {
        if (guard.isDiagonal()) {
            buildConstraintsForDiagonalConstraint(guard, clocks);
        } else {
            buildConstraintsForNormalGuard(guard, clocks);
        }
    }

    public void buildConstraintsForNormalGuard(ClockGuard guard, List<Clock> clocks) {
        int index = getIndexOfClock(guard.getClock(), clocks);
        Relation relation = guard.getRelation();

        int lowerBoundI = guard.getLowerBound();
        int upperBoundI = guard.getUpperBound();
        switch (relation) {
            case EQUAL: {
                constrain(0, index, (-1) * lowerBoundI, false);
                constrain(index, 0, upperBoundI, false);
                break;
            }
            case LESS_THAN: {
                constrain(index, 0, upperBoundI, true);
                break;
            }
            case LESS_EQUAL: {
                constrain(index, 0, upperBoundI, false);
                break;
            }
            case GREATER_THAN: {
                constrain(0, index, (-1) * lowerBoundI, true);
                break;
            }
            case GREATER_EQUAL: {
                constrain(0, index, (-1) * lowerBoundI, false);
                break;
            }
        }
    }

    public void buildConstraintsForDiagonalConstraint(ClockGuard guard, List<Clock> clocks)
            throws IllegalArgumentException {
        Relation relation = guard.getRelation();
        Clock clock_i = guard.getClock();
        Clock clock_j = guard.getDiagonalClock();
        int bound = guard.getBound();

        int i = getIndexOfClock(clock_i, clocks);
        int j = getIndexOfClock(clock_j, clocks);

        switch (relation) {
            case LESS_THAN: {
                constrain(i, j, bound, true);
                break;
            }
            case LESS_EQUAL: {
                constrain(i, j, bound, false);
                break;
            }
            default: {
                throw new IllegalArgumentException("Guard relation can only be < or <=");
            }
        }
    }

    public void updateValue(int index, int value) {
        dbm = DBMLib.dbm_updateValue(dbm, dimension, index, value);
    }

    public int[] delayNewDBM() {
        return DBMLib.dbm_up(dbm, dimension);
    }

    public void delay() {
        dbm = DBMLib.dbm_up(dbm, dimension);
    }

    public void extrapolateMaxBounds(int[] maxBounds) {
        dbm = DBMLib.dbm_extrapolateMaxBounds(dbm, dimension, maxBounds);
    }

    public void extrapolateMaxBoundsDiagonal(int[] maxBounds) {
        dbm = DBMLib.dbm_extrapolateMaxBoundsDiag(dbm, dimension, maxBounds);
    }

    public boolean isSubset(Zone zone2) {
        return DBMLib.dbm_isSubsetEq(this.dbm, zone2.dbm, dimension);
    }

    public boolean isValid() {
        return DBMLib.dbm_isValid(dbm, dimension);
    }

    public boolean intersects(Zone zone) {
        if (this.dimension != zone.dimension) {
            throw new IllegalArgumentException("Zones must be of the same size");
        }
        return DBMLib.dbm_intersection(dbm, zone.dbm, dimension);
    }

    public boolean canDelayIndefinitely() {
        for (int i = 1; i < dimension; i++) {
            int curr = dbm[dimension * i];
            if (curr < DBM_INF) {
                return false;
            }
        }
        return true;
    }

    public Zone close() {
        return new Zone(DBMLib.dbm_close(dbm, dimension));
    }

    public void freeClock(int index) {
        DBMLib.dbm_freeClock(dbm, dimension, index);
    }

    public boolean isUrgent() {
        for (int i = 1; i < dimension; i++) {
            int currLower = dbm[i];
            int currUpper = dbm[dimension * i];
            if (DBMLib.dbm_addRawRaw(currLower, currUpper) != 1) {
                return false;
            }
        }
        return true;
    }

    private static boolean containsClock(Clock clock, List<Clock> clocks) {
        return clocks.contains(clock);
    }

    public Guard buildGuardsFromZone(List<Clock> clocks, List<Clock> relevantClocks) {
        List<Guard> guards = new ArrayList<>();
        guards.addAll(buildNormalGuardsFromZone(clocks, relevantClocks));
        guards.addAll(buildDiagonalConstraintsFromZone(clocks, relevantClocks));
        return new AndGuard(guards);
    }

    public List<ClockGuard> buildNormalGuardsFromZone(List<Clock> clocks, List<Clock> relevantClocks) {
        List<ClockGuard> guards = new ArrayList<>();

        for (int i = 1; i < dimension; i++) {
            Clock clock = clocks.get(i - 1);
            if (!containsClock(clock, relevantClocks)) {
                continue;
            }

            // values from first row, lower bounds
            int lower = dbm[i];
            // values from first column, upper bounds
            int upper = dbm[dimension * i];

            if (upper == lower && !DBMLib.dbm_rawIsStrict(lower) && !DBMLib.dbm_rawIsStrict(upper)) {
                guards.add(
                        new ClockGuard(clock, (-1) * DBMLib.raw2bound(lower), Relation.EQUAL)
                );
                continue;
            }

            // lower bound must be different from 1 (==0)
            if (lower != 1) {
                if (DBMLib.dbm_rawIsStrict(lower)) {
                    guards.add(
                            new ClockGuard(clock, (-1) * DBMLib.raw2bound(lower), Relation.GREATER_THAN)
                    );
                } else {
                    guards.add(
                            new ClockGuard(clock, (-1) * DBMLib.raw2bound(lower), Relation.GREATER_EQUAL)
                    );
                }
            }

            // upper bound must be different from infinity
            if (upper != DBM_INF) {
                if (DBMLib.dbm_rawIsStrict(upper)) {
                    guards.add(
                            new ClockGuard(clock, DBMLib.raw2bound(upper), Relation.LESS_THAN)
                    );
                } else {
                    guards.add(
                            new ClockGuard(clock, DBMLib.raw2bound(upper), Relation.LESS_EQUAL)
                    );
                }

            }
        }

        return guards;
    }

    public List<ClockGuard> buildDiagonalConstraintsFromZone(List<Clock> clocks, List<Clock> relevantClocks) {
        List<ClockGuard> guards = new ArrayList<>();

        for (int i = 1; i < dimension; i++) {
            for (int j = 1; j < dimension; j++) {
                if (i == j) {
                    continue;
                }

                Clock clock_i = clocks.get(i - 1);
                Clock clock_j = clocks.get(j - 1);
                if (!containsClock(clock_i, relevantClocks) || !containsClock(clock_j, relevantClocks)) {
                    continue;
                }

                // values from first row, lower bounds
                int currentValue = dbm[i + j * dimension];
                if (currentValue == DBM_INF) {
                    continue;
                }

                if (DBMLib.dbm_rawIsStrict(currentValue)) {
                    guards.add(
                            new ClockGuard(clock_j, clock_i, DBMLib.raw2bound(currentValue), Relation.LESS_THAN)
                    );
                } else {
                    guards.add(
                            new ClockGuard(clock_j, clock_i, DBMLib.raw2bound(currentValue), Relation.LESS_EQUAL)
                    );
                }

            }
        }

        return guards;
    }

    // FURTHER METHODS ARE ONLY MEANT TO BE USED FOR TESTING. NEVER USE THEM DIRECTLY IN YOUR CODE
    private void constrain(int i, int j, int constraint, boolean isStrict) {
        dbm = DBMLib.dbm_constrainBound(dbm, dimension, i, j, constraint, isStrict);
    }

    public void init() {
        dbm = DBMLib.dbm_init(dbm, dimension);
    }

    public int[] getDbm() {
        return dbm;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Zone)) {
            return false;
        }

        Zone other = (Zone) obj;
        return dimension == other.dimension &&
                length == other.length &&
                Arrays.equals(dbm, other.dbm);
    }

    // Method to nicely print DBM for testing purposes.
    // The boolean flag determines if values of the zone will be converted from DBM format to actual bound of constraint
    public void printDbm(boolean toConvert, boolean showStrictness) {
        int intLength = 0;
        int toPrint = 0;

        Log.trace("---------------------------------------");
        for (int i = 0, j = 1; i < length; i++, j++) {

            toPrint = toConvert ? DBMLib.raw2bound(dbm[i]) : dbm[i];

            System.out.print(toPrint);

            if (showStrictness) {
                String strictness = DBMLib.dbm_rawIsStrict(dbm[i]) ? " < " : " <=";
                System.out.print(strictness);
            }
            if (j == dimension) {
                Log.trace();
                if (i == length - 1) Log.trace("---------------------------------------");
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

    public static int getDbmDimension(int[] dbm) {
        return (int) Math.sqrt(dbm.length);
    }
}
