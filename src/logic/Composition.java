package logic;

import models.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Composition {

    public static Component compose(Component m1, Component m2) {
    		ArrayList<Location> locs1 = m1.getLocations();
				ArrayList<Location> locs2 = m2.getLocations();
				ArrayList<Transition> transitions1 = m1.getTransitions();
				ArrayList<Transition> transitions2 = m2.getTransitions();
				Set<Channel> actions1 = m1.getActions();
				Set<Channel> actions2 = m2.getActions();
				Set<Clock> clks1 = m1.getClocks();
				Set<Clock> clks2 = m2.getClocks();
				ArrayList<Location> locations = calculateLocations(locs1, locs2);
    		return new Component(locations, calculateTransitions(transitions1, transitions2, actions1, actions2, locs1, locs2, locations),
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

		private static ArrayList<Transition> calculateTransitions(ArrayList<Transition> transitions1, ArrayList<Transition> transitions2, Set<Channel> actions1,
																												Set<Channel> actions2, ArrayList<Location> locs1,
																												ArrayList<Location> locs2, ArrayList<Location> locations) {
				ArrayList<Transition> transitions = new ArrayList<>();

				Set<Channel> actJust1 = new HashSet<>(actions1);
				actJust1.removeAll(actions2);

				Set<Channel> actJust2 = new HashSet<>(actions2);
				actJust2.removeAll(actions1);

				Set<Channel> intersection = new HashSet<>(actions1);
				intersection.retainAll(actions2);

				for (Transition transition1 : transitions1) {
						if (actJust1.contains(transition1.getChannel())) {
								for (Location loc2 : locs2) {
										Location l1 = findLocation(transition1.getFrom(), loc2, locations);
										Location l2 = findLocation(transition1.getTo(), loc2, locations);
										Transition newTransition = new Transition(l1, l2, transition1.getChannel(), transition1.isInput(), transition1.getGuard(), transition1.getUpdate());
										transitions.add(newTransition);
								}
						}

						if (intersection.contains(transition1.getChannel())) {
								for (Transition transition2 : transitions2) {
										if (transition1.getChannel() == transition2.getChannel()) {
												Location l1 = findLocation(transition1.getFrom(), transition2.getFrom(), locations);
												Location l2 = findLocation(transition1.getTo(), transition2.getTo(), locations);
												ArrayList<Guard> guards = new ArrayList<>(transition1.getGuards());
												guards.addAll(transition2.getGuards());
												ArrayList<Update> updates = new ArrayList<>(transition1.getUpdates());
												updates.addAll(transition2.getUpdates());
												Transition newTransition = new Transition(l1, l2, transition1.getChannel(), false, guards, updates);
												transitions.add(newTransition);
										}
								}
						}
				}

				for (Transition transition2 : transitions2) {
						if (actJust2.contains(transition2.getChannel())) {
								for (Location loc1 : locs1) {
										Location l1 = findLocation(loc1, transition2.getFrom(), locations);
										Location l2 = findLocation(loc1, transition2.getTo(), locations);
										Transition newTransition = new Transition(l1, l2, transition2.getChannel(), transition2.isInput(), transition2.getGuard(), transition2.getUpdate());
										transitions.add(newTransition);
								}
						}
				}

				return transitions;
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
