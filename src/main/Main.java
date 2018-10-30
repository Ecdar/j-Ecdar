package main;

import logic.Composition;
import models.*;
import lib.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Main {

    public static void main(String[] args) {

        compositionTest();

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

    private static void compositionTest() {
        Clock y = new Clock("y");
				Clock z = new Clock("z");

        Channel coin = new Channel("coin");
        Channel cof = new Channel("cof");
        Channel tea = new Channel("tea");
				Channel patent = new Channel("patent");
				Channel grant = new Channel("grant");
				Channel pub = new Channel("pub");

        Location l5 = new Location("l5", null, true, false, false, false);
        Guard inv1 = new Guard(y, 6, false, false, false, true);
        Location l4 = new Location("l4", inv1, false, false, false, false);

				Location l0 = new Location("l0", null, true, false, false, false);
				Guard inv2 = new Guard(z, 2, false, false, false, true);
				Location l1 = new Location("l1", inv2, false, false, false, false);
				Location l2 = new Location("l2", null, true, false, false, false);
				Location l3 = new Location("l3", inv2, true, false, false, false);

        Guard guard1 = new Guard(y,2, false, true, false, false);
        Edge e1 = new Edge(l5, l5, tea, false, guard1, null);
        Update upd1 = new Update(y, 0);
        Edge e2 = new Edge(l5, l4, coin, true, null, upd1);
        Edge e3 = new Edge(l4, l4, coin, true, new ArrayList<Guard>(), new ArrayList<Update>());
        Edge e4 = new Edge(l4, l5, tea, false, new ArrayList<Guard>(), new ArrayList<Update>());
        Guard guard2 = new Guard(y,4, false, true, false, false);
        Edge e5 = new Edge(l4, l5, cof, false, guard2, null);

				Update upd2 = new Update(z, 0);
				Edge e6 = new Edge(l0, l1, grant, true, null, upd2);
				Edge e7 = new Edge(l1, l1, grant, true, new ArrayList<Guard>(), new ArrayList<Update>());
				Edge e8 = new Edge(l1, l1, pub, true, new ArrayList<Guard>(), new ArrayList<Update>());
				Edge e9 = new Edge(l1, l2, coin, false, new ArrayList<Guard>(), new ArrayList<Update>());
				Edge e10 = new Edge(l2, l2, grant, true, new ArrayList<Guard>(), new ArrayList<Update>());
				Edge e11 = new Edge(l2, l3, pub, true, null, upd2);
				Edge e12 = new Edge(l3, l3, grant, true, new ArrayList<Guard>(), new ArrayList<Update>());
				Edge e13 = new Edge(l3, l3, pub, true, new ArrayList<Guard>(), new ArrayList<Update>());
				Edge e14 = new Edge(l3, l0, patent, false, new ArrayList<Guard>(), new ArrayList<Update>());


				ArrayList<Location> locs1 = new ArrayList<>(Arrays.asList(l5, l4));
        ArrayList<Edge> edges1 = new ArrayList<>(Arrays.asList(e1, e2, e3, e4, e5));
        Set<Clock> clks1 = new HashSet<>(Arrays.asList(y));

				ArrayList<Location> locs2 = new ArrayList<>(Arrays.asList(l0, l1, l2, l3));
				ArrayList<Edge> edges2 = new ArrayList<>(Arrays.asList(e6, e7, e8, e9, e10, e11, e12, e13, e14));
				Set<Clock> clks2 = new HashSet<>(Arrays.asList(z));

        Component machine = new Component(locs1, edges1, clks1);
        Component administration = new Component(locs2, edges2, clks2);

				Component composed = Composition.compose(machine, administration);

		}
}
