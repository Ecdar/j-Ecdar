package logic;

import models.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Composition {

    public static Component compose(Component m1, Component m2) {
    		ArrayList<Location> locs1 = m1.getLocations();
				ArrayList<Location> locs2 = m2.getLocations();
				ArrayList<Edge> edges1 = m1.getEdges();
				ArrayList<Edge> edges2 = m2.getEdges();
				Set<Channel> actions1 = m1.getActions();
				Set<Channel> actions2 = m2.getActions();
				Set<Clock> clks1 = m1.getClocks();
				Set<Clock> clks2 = m2.getClocks();
				ArrayList<Location> locations = calculateLocations(locs1, locs2);
    		return new Component(locations, calculateEdges(edges1, edges2, actions1, actions2, locs1, locs2, locations),
								calculateClocks(clks1, clks2));
		}

		private static ArrayList<Location> calculateLocations(ArrayList<Location> locs1, ArrayList<Location> locs2) {
				ArrayList<Location> locs = new ArrayList<>();

				for (Location loc1 : locs1) {
						for (Location loc2 : locs2) {
								Location newLoc = new Location(loc1.getName(), loc1.getInvariant(), loc1.isInitial(), loc1.isUrgent(),
												loc1.isUniversal(), loc1.isInconsistent(), loc2);
								locs.add(newLoc);
						}
				}

				return locs;
		}

		private static ArrayList<Edge> calculateEdges(ArrayList<Edge> edges1, ArrayList<Edge> edges2, Set<Channel> actions1,
																								 Set<Channel> actions2, ArrayList<Location> locs1,
																								 ArrayList<Location> locs2, ArrayList<Location> locations) {
				ArrayList<Edge> edges = new ArrayList<>();

				Set<Channel> actJust1 = new HashSet<>(actions1);
				actJust1.removeAll(actions2);

				Set<Channel> actJust2 = new HashSet<>(actions2);
				actJust2.removeAll(actions1);

				Set<Channel> intersection = new HashSet<>(actions1);
				intersection.retainAll(actions2);

				for (Edge edge1 : edges1) {
						if (actJust1.contains(edge1.getChannel())) {
								for (Location loc2 : locs2) {
										Location l1 = findLocation(edge1.getFrom(), loc2, locations);
										Location l2 = findLocation(edge1.getTo(), loc2, locations);
										Edge newEdge = new Edge(l1, l2, edge1.getChannel(), edge1.isInput(), edge1.getGuard(), edge1.getUpdate());
										edges.add(newEdge);
								}
						}

						if (intersection.contains(edge1.getChannel())) {
								for (Edge edge2 : edges2) {
										if (edge1.getChannel() == edge2.getChannel()) {
												Location l1 = findLocation(edge1.getFrom(), edge2.getFrom(), locations);
												Location l2 = findLocation(edge1.getTo(), edge2.getTo(), locations);
												ArrayList<Guard> guards = new ArrayList<>(edge1.getGuards());
												guards.addAll(edge2.getGuards());
												ArrayList<Update> updates = new ArrayList<>(edge1.getUpdates());
												updates.addAll(edge2.getUpdates());
												Edge newEdge = new Edge(l1, l2, edge1.getChannel(), false, guards, updates);
												edges.add(newEdge);
										}
								}
						}
				}

				for (Edge edge2 : edges2) {
						if (actJust2.contains(edge2.getChannel())) {
								for (Location loc1 : locs1) {
										Location l1 = findLocation(loc1, edge2.getFrom(), locations);
										Location l2 = findLocation(loc1, edge2.getTo(), locations);
										Edge newEdge = new Edge(l1, l2, edge2.getChannel(), edge2.isInput(), edge2.getGuard(), edge2.getUpdate());
										edges.add(newEdge);
								}
						}
				}

				return edges;
		}

    private static Set<Clock> calculateClocks(Set<Clock> clk1, Set<Clock> clk2) throws IllegalArgumentException {
				Set<Clock> newSet = new HashSet<>(clk1);
        boolean disjoint = newSet.addAll(clk2);
        if (!disjoint) throw new IllegalArgumentException("Clocks are not disjoint");
        return newSet;
    }

		private static Location findLocation(Location loc1, Location loc2, ArrayList<Location> locs) {
				Location toReturn = null;

				for (Location loc : locs) {
						if ((loc.getName() == loc1.getName()) && (loc.getNext().getName() == loc2.getName())) {
								toReturn = loc;
						}
				}

				return toReturn;
		}
}
