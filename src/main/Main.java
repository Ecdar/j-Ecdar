package main;

import models.*;
import lib.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class Main {

    public static void main(String[] args) {

        sampleComponent();

        String fileName = "src/" + System.mapLibraryName("DBM");
        File lib = new File(fileName);
        System.load(lib.getAbsolutePath());

        int res1 = DBMLib.boundbool2raw(5, true);
        int res2 = DBMLib.boundbool2raw(5, false);
        int res3 = DBMLib.raw2bound(10);

        System.out.println("boundbool2raw: " + res1);
        System.out.println("boundbool2raw: " + res2);
        System.out.println("raw2bound: " + res3);
    }

    private static void sampleComponent() {
        Clock y = new Clock();

        Channel coin = new Channel();
        Channel cof = new Channel();
        Channel tea = new Channel();

        Location loc1 = new Location(null, true, false, false, false);
        Guard inv1 = new Guard(y, 6, false, false, false, true);
        Location loc2 = new Location(inv1, false, false, false, false);

        Guard guard1 = new Guard(y,2, false, true, false, false);
        Edge e1 = new Edge(loc1, loc1, tea, false, guard1, null);
        Update upd1 = new Update(y, 0);
        Edge e2 = new Edge(loc1, loc2, coin, true, null, upd1);
        Edge e3 = new Edge(loc2, loc2, coin, true, null, null);
        Edge e4 = new Edge(loc2, loc1, tea, false, null, null);
        Guard guard2 = new Guard(y,4, false, true, false, false);
        Edge e5 = new Edge(loc2, loc1, cof, false, guard2, null);

        ArrayList<Location> locations = new ArrayList<>(Arrays.asList(loc1, loc2));
        ArrayList<Edge> edges = new ArrayList<>(Arrays.asList(e1, e2, e3, e4, e5));
        ArrayList<Clock> clocks = new ArrayList<>(Arrays.asList(y));

        Component Machine = new Component(locations, edges, clocks);
    }
}
