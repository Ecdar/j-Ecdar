package models;

import lib.DBMLib;

import java.util.ArrayList;
import java.util.List;

public class Federation {
    private List<Zone> zones;

    private static final int DBM_INF = Integer.MAX_VALUE - 1;

    public Federation(int[][] dbms) {
        this.zones = new ArrayList<>();

        for (int[] dbm : dbms) {
            Zone zone = new Zone(dbm);
            this.zones.add(zone);
        }
    }

    public Federation getCopy() {
        ArrayList<Zone> zoneArrayList = new ArrayList<>();
        for (Zone z : zones)
            zoneArrayList.add(new Zone(z));
        return new Federation(zoneArrayList);

    }

    public static Federation getUnrestrainedFed(List<Clock> clocks) {
        List<Zone> emptyZoneList = new ArrayList<>();
        Zone emptyZone = new Zone(clocks.size() + 1, true);
        emptyZone.init();
        for (int i=0; i< clocks.size(); i++)
            emptyZone.freeClock(i);
        emptyZoneList.add(emptyZone);
        return new Federation(emptyZoneList);
    }

    public Guard turnFederationToGuards(List<Clock> clocks)
    {
        List<Guard> turnedBackIntoGuards = new ArrayList<>();  // make a function
        for (Zone z : getZones()) {
            turnedBackIntoGuards.add(z.buildGuardsFromZone(clocks,clocks));
        }
        return new OrGuard(turnedBackIntoGuards);
    }

    public boolean isUnrestrained(List<Clock> clocks)
    {
        if (Federation.fedMinusFed(Federation.getUnrestrainedFed(clocks),this).isEmpty())
            return true;
        else
            return false;


    }

    public boolean isEmpty() {
        return zones.isEmpty();
    }

    public int size() {
        return zones.size();
    }

    public Federation(List<Zone> zones) {
        ArrayList<Zone> zoneArrayList = new ArrayList<>();
        for (Zone z : zones)
            zoneArrayList.add(new Zone(z));
        this.zones = zoneArrayList;


        //this.zones = new ArrayList<>(zones);
    }

    public List<Zone> getZones() {
        return zones;
    }

    public Federation down() {
        int[][] zones = getZones().stream().map(Zone::getDbm).toArray(int[][]::new);
        if (zones.length == 0) {
            return this;
        }
        int dim = (int) Math.sqrt(zones[0].length);

        return new Federation(DBMLib.fed_down(zones, dim));
    }

    // todo: is this the correct way?
    public boolean isValid() {
        // System.out.println("reached isValid " + getZones().size());


        boolean isValid = true;
        for (Zone z : getZones()) {

            //z.printDBM(true,true);
            isValid = isValid && z.isValid();
        }
        //System.out.println("and exited it");

        return isValid;
    }

    /*public boolean isSubset(Zone zone2) {
        return DBMLib.dbm_isSubsetEq(this.dbm, zone2.dbm, size);
    }*/

    public boolean isSubset(Federation fed2) {


        //System.out.println("reached: issubset 1");
        int[][] zones1 = this.getZones().stream().map(Zone::getDbm).toArray(int[][]::new);
        int[][] zones2 = fed2.getZones().stream().map(Zone::getDbm).toArray(int[][]::new);
        if (zones1.length == 0)
            return true;

        //System.out.println("reached: issubset 2");
        int dim = (int) Math.sqrt(zones1[0].length);
        // System.out.println("reached: issubset 3");
        return DBMLib.fed_isSubsetEq(zones1, zones2, dim);  // TODO: Order of zones 1 and 2
    }

    public boolean isUrgent() {
        for (Zone z : this.getZones()) {
            for (int i = 1; i < z.getSize(); i++) {
                int currLower = z.getDbm()[i];
                int currUpper = z.getDbm()[z.getSize() * i];
                if (DBMLib.dbm_addRawRaw(currLower, currUpper) != 1)
                    return false;
            }
        }
        return true;
    }

    public boolean canDelayIndefinitely() {

        for (Zone z : this.getZones()) {
            boolean indef = true;
            for (int i = 1; i < z.getSize(); i++) {
                int curr = z.getDbm()[z.getSize() * i];
                if (curr < DBM_INF) indef = false;
            }
            if (indef == true)
                return true;
        }


        return false;
    }


    public void delay() {
        int[][] zones = getZones().stream().map(Zone::getDbm).toArray(int[][]::new);
        int dim = (int) Math.sqrt(zones[0].length);
        Federation tempFed = new Federation(DBMLib.fed_up(zones, dim));
        this.zones = tempFed.zones;
    }

    public Federation intersect(Federation fed) {
        int[][] zones1 = this.getZones().stream().map(Zone::getDbm).toArray(int[][]::new);
        int[][] zones2 = fed.getZones().stream().map(Zone::getDbm).toArray(int[][]::new);
        if (zones1.length == 0) {
            return this;
        }
        int dim = (int) Math.sqrt(zones1[0].length);
        return new Federation(DBMLib.fed_intersect_fed(zones1, zones2, dim));
    }

    public Federation free(int index) {
        int[][] zones = getZones().stream().map(Zone::getDbm).toArray(int[][]::new);
        if (zones.length == 0) {
            return this;
        }
        int dim = (int) Math.sqrt(zones[0].length);
        return new Federation(DBMLib.fed_freeClock(zones, dim, index));
    }

    public boolean intersects(Federation fed) {
        int[][] zones1 = this.getZones().stream().map(Zone::getDbm).toArray(int[][]::new);
        int[][] zones2 = fed.getZones().stream().map(Zone::getDbm).toArray(int[][]::new);
        if (zones1.length == 0) return false;
        int dim = (int) Math.sqrt(zones1[0].length);
        return DBMLib.fed_intersects_dbm(zones1, zones2, dim);

    }

    public static Federation fedMinusFed(Federation fed1, Federation fed2) {
        int[][] zones1 = fed1.getZones().stream().map(Zone::getDbm).toArray(int[][]::new);
        int[][] zones2 = fed2.getZones().stream().map(Zone::getDbm).toArray(int[][]::new);

        if (fed1.isEmpty()) return fed1;
        if (fed2.isEmpty()) return fed1;

        int dim = (int) Math.sqrt(zones1[0].length);

        int[][] result = DBMLib.fed_minus_fed(zones1, zones2, dim);
        return new Federation(result);
    }

    public static boolean fedEqFed(Federation fed1, Federation fed2) {
        if (fed1 == null && fed2 == null) return true;
        if (fed1 == null || fed2 == null) return false;

        int[][] zones1 = fed1.getZones().stream().map(Zone::getDbm).toArray(int[][]::new);
        int[][] zones2 = fed2.getZones().stream().map(Zone::getDbm).toArray(int[][]::new);
        if (zones1.length == 0) {
            if (zones2.length == 0)
                return true;
            else
                return false;

        }

        int dim = (int) Math.sqrt(zones1[0].length);

        boolean result = DBMLib.fed_eq_fed(zones1, zones2, dim);
        return result;
    }

    public static Federation fedPlusFed(Federation fed1, Federation fed2) {
        int[][] zones1 = fed1.getZones().stream().map(Zone::getDbm).toArray(int[][]::new);
        int[][] zones2 = fed2.getZones().stream().map(Zone::getDbm).toArray(int[][]::new);
        if (fed1.isEmpty())
            return fed2;
        if (fed2.isEmpty())
            return fed1;

        int dim = (int) Math.sqrt(zones1[0].length);

        int[][] result = DBMLib.fed_plus_fed(zones1, zones2, dim);
        return new Federation(result);
    }


    public static Federation predt(Federation fed1, Federation fed2) {
        int[][] zones1 = fed1.getZones().stream().map(Zone::getDbm).toArray(int[][]::new);
        int[][] zones2 = fed2.getZones().stream().map(Zone::getDbm).toArray(int[][]::new);

        if (zones1.length == 0) {
            return fed1; // todo: fed2?
        }

        int dim = (int) Math.sqrt(zones1[0].length);

        int[][] result = DBMLib.fed_const_predt(zones1, zones2, dim);
        return new Federation(result);
    }

    public static Federation dbmMinusDbm(Zone z1, Zone z2) {
        if (z1.getSize() != z2.getSize()) throw new IllegalArgumentException("Zones must be of the same size");

        return new Federation(DBMLib.dbm_minus_dbm(z1.getDbm(), z2.getDbm(), z1.getSize()));
    }
}
