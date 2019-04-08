package logic;

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

    public List<Zone> getZones() {
        return zones;
    }
}
