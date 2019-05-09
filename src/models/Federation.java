package models;

import lib.DBMLib;

import java.util.ArrayList;
import java.util.List;

public class Federation {
    private List<Zone> zones;

    public Federation(int[][] dbms) {
        this.zones = new ArrayList<>();

        for (int[] dbm : dbms) {
            Zone zone = new Zone(dbm);
            this.zones.add(zone);
        }
    }

    public boolean isEmpty(){
        return zones.isEmpty();
    }

    public int size(){
        return zones.size();
    }

    public Federation(List<Zone> zones) {
        this.zones = new ArrayList<>(zones);
    }

    public List<Zone> getZones() {
        return zones;
    }

    public static Federation fedMinusFed(Federation fed1, Federation fed2) {
        int[][] zones1 = fed1.getZones().stream().map(Zone::getDbm).toArray(int[][]::new);
        int[][] zones2 = fed2.getZones().stream().map(Zone::getDbm).toArray(int[][]::new);

        int dim = (int) Math.sqrt(zones1[0].length);

        int[][] result = DBMLib.fed_minus_fed(zones1, zones2, dim);
        return new Federation(result);
    }

    public static Federation dbmMinusDbm(Zone z1, Zone z2){
        if(z1.getSize() != z2.getSize()) throw new IllegalArgumentException("Zones must be of the same size");

        return new Federation(DBMLib.dbm_minus_dbm(z1.getDbm(), z2.getDbm(), z1.getSize()));
    }
}
