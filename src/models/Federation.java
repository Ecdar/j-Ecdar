package models;

import lib.DBMLib;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Federation {
    private List<Zone> zones;

    private static final int DBM_INF = Integer.MAX_VALUE - 1;

    public Federation(int[][] dbms) {
        this.zones = Arrays.stream(dbms)
                .map(Zone::new).collect(Collectors.toList());
    }

    public Federation(List<Zone> zones) {
        this.zones = zones.stream()
                .map(Zone::new).collect(Collectors.toList());
    }

    public Federation(Zone zone) {
        this(List.of(zone));
    }

    public boolean isEmpty() {
        return zones.isEmpty();
    }

    public int size() {
        return zones.size();
    }

    public List<Zone> getZones() {
        return zones;
    }

    public int[][] getDbms() {
        return getZones().stream().map(Zone::getDbm).toArray(int[][]::new);
    }

    public Federation copy() {
        List<Zone> zones = this.zones.stream()
                .map(Zone::new).collect(Collectors.toList());
        return new Federation(zones);
    }

    public Guard toGuards(List<Clock> clocks) {
        List<Guard> turnedBackIntoGuards = new ArrayList<>();
        for (Zone zone : getZones()) {
            turnedBackIntoGuards.add(zone.buildGuardsFromZone(clocks, clocks));
        }
        return new OrGuard(turnedBackIntoGuards);
    }

    public boolean isUnrestrained(List<Clock> clocks) {
        return Federation.subtract(Federation.createUnrestrainedFederation(clocks), this).isEmpty();
    }

    public Federation down() {
        int[][] zones = getDbms();
        if (zones.length == 0) {
            return this;
        }
        int dimension = (int) Math.sqrt(zones[0].length);

        return new Federation(DBMLib.fed_down(zones, dimension));
    }

    public boolean isValid() {
        return zones.stream().allMatch(Zone::isValid);
    }

    public boolean isSubset(Federation other) {
        int[][] zones1 = this.getZones().stream()
                .map(Zone::getDbm).toArray(int[][]::new);
        int[][] zones2 = other.getZones().stream()
                .map(Zone::getDbm).toArray(int[][]::new);

        if (zones1.length == 0) {
            return true;
        }

        int dimension = Zone.getDbmDimension(zones1[0]);
        return DBMLib.fed_isSubsetEq(zones1, zones2, dimension);  // TODO: Order of zones 1 and 2
    }

    public boolean isUrgent() {
        for (Zone zone : this.getZones()) {
            for (int i = 1; i < zone.getDimension(); i++) {
                int currLower = zone.getDbm()[i];
                int currUpper = zone.getDbm()[zone.getDimension() * i];
                if (DBMLib.dbm_addRawRaw(currLower, currUpper) != 1) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean canDelayIndefinitely() {
        for (Zone zone : this.getZones()) {
            boolean indefinitely = true;
            for (int i = 1; i < zone.getDimension(); i++) {
                int upper = zone.getDbm()[zone.getDimension() * i];
                if (upper < DBM_INF) {
                    indefinitely = false;
                }
            }
            if (indefinitely) {
                return true;
            }
        }
        return false;
    }

    public void delay() {
        int[][] zones = getDbms();
        int dimension = (int) Math.sqrt(zones[0].length);
        Federation tempFed = new Federation(DBMLib.fed_up(zones, dimension));
        this.zones = tempFed.zones;
    }

    public Federation intersect(Federation fed) {
        int[][] zones1 = this.getDbms();
        int[][] zones2 = fed.getDbms();
        if (zones1.length == 0) {
            return this;
        }
        int dimension = Zone.getDbmDimension(zones1[0]);
        return new Federation(DBMLib.fed_intersect_fed(zones1, zones2, dimension));
    }

    public Federation free(int index) {
        int[][] zones = getDbms();
        if (zones.length == 0) {
            return this;
        }
        int dimension = Zone.getDbmDimension(zones[0]);
        return new Federation(DBMLib.fed_freeClock(zones, dimension, index));
    }

    public boolean intersects(Federation fed) {
        int[][] zones1 = this.getDbms();
        int[][] zones2 = fed.getDbms();
        if (zones1.length == 0) return false;
        int dimension = Zone.getDbmDimension(zones1[0]);
        return DBMLib.fed_intersects_dbm(zones1, zones2, dimension);
    }

    public static Federation createUnrestrainedFederation(List<Clock> clocks) {
        Zone emptyZone = new Zone(clocks.size() + 1, true);
        for (int i = 0; i < clocks.size(); i++) {
            emptyZone.freeClock(i);
        }
        return new Federation(emptyZone);
    }

    public static Federation subtract(Federation fed1, Federation fed2) {
        int[][] zones1 = fed1.getDbms();
        int[][] zones2 = fed2.getDbms();

        if (fed1.isEmpty()) return fed1;
        if (fed2.isEmpty()) return fed1;

        int dimension = Zone.getDbmDimension(zones1[0]);

        int[][] result = DBMLib.fed_minus_fed(zones1, zones2, dimension);
        return new Federation(result);
    }

    public static boolean equals(Federation fed1, Federation fed2) {
        if (fed1 == null && fed2 == null) return true;
        if (fed1 == null || fed2 == null) return false;

        int[][] zones1 = fed1.getDbms();
        int[][] zones2 = fed2.getDbms();

        if (zones1.length == 0) {
            return zones2.length == 0;
        }

        int dimension = Zone.getDbmDimension(zones1[0]);
        return DBMLib.fed_eq_fed(zones1, zones2, dimension);
    }

    public static Federation add(Federation fed1, Federation fed2) {
        int[][] zones1 = fed1.getDbms();
        int[][] zones2 = fed2.getDbms();
        if (fed1.isEmpty())
            return fed2;
        if (fed2.isEmpty())
            return fed1;

        int dim = Zone.getDbmDimension(zones1[0]);

        int[][] result = DBMLib.fed_plus_fed(zones1, zones2, dim);
        return new Federation(result);
    }


    public static Federation predt(Federation fed1, Federation fed2) {
        int[][] zones1 = fed1.getDbms();
        int[][] zones2 = fed2.getDbms();

        if (zones1.length == 0) {
            return fed1; // todo: fed2?
        }

        int dimension = Zone.getDbmDimension(zones1[0]);
        int[][] result = DBMLib.fed_const_predt(zones1, zones2, dimension);
        return new Federation(result);
    }

    public static Federation subtract(Zone z1, Zone z2)
            throws IllegalArgumentException {
        if (z1.getDimension() != z2.getDimension()) {
            throw new IllegalArgumentException("Zones must be of the same size");
        }
        return new Federation(DBMLib.dbm_minus_dbm(z1.getDbm(), z2.getDbm(), z1.getDimension()));
    }
}
