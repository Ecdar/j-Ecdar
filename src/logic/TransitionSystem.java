package logic;

import lib.DBMLib;
import models.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

// parent class for all TS's, so we can use it with regular TS's, composed TS's etc.
public abstract class TransitionSystem {
		private List<Component> machines;
		private List<Clock> clocks;
		private int dbmSize;

		TransitionSystem(List<Component> machines) {
				this.machines = machines;
				this.clocks = new ArrayList<>();
				for (Component machine : machines) {
						clocks.addAll(machine.getClocks());
				}
				dbmSize = clocks.size() + 1;

				String fileName = "src/" + System.mapLibraryName("DBM");
				File lib = new File(fileName);
				System.load(lib.getAbsolutePath());
		}

		int getDbmSize() {
				return dbmSize;
		}

		private List<Clock> getClocks() {
				return clocks;
		}

		public State getInitialState() {
				List<Location> initialLocations = new ArrayList<>();

				for (Component machine : machines) {
						Location init = machine.getInitLoc();
						initialLocations.add(init);
				}

				int[] zone = initializeDBM();
				zone = applyInvariantsOrGuards(zone, getInvariants(initialLocations));
				zone = delay(zone);

				return new State(initialLocations, zone);
		}

		List<State> addNewStates(int[] zone, List<List<Location>> locationsArr, List<List<Transition>> transitionsArr) {
				List<State> states = new ArrayList<>();

				for (int n = 0; n < locationsArr.size(); n++) {
						List<Location> newLocations = locationsArr.get(n);
						List<Guard> guards = new ArrayList<>();
						List<Update> updates = new ArrayList<>();
						for (Transition t : transitionsArr.get(n)) {
								if (t != null) {
										guards.addAll(t.getGuards());
										updates.addAll(t.getUpdates());
								}
						}

						int[] dbm = zone;
						// apply guards
						if (!guards.isEmpty())
								dbm = applyInvariantsOrGuards(dbm, guards);
						// apply resets
						if (!updates.isEmpty()) dbm = applyResets(dbm, updates);
						// apply invariants
						if (!getInvariants(newLocations).isEmpty())
								dbm = applyInvariantsOrGuards(dbm, getInvariants(newLocations));

						// construct new state and add it to list
						if (isDbmValid(dbm)) {
								states.add(new State(newLocations, dbm));
						}
				}

				return states;
		}

		public abstract Set<Channel> getInputs();

		public abstract Set<Channel> getOutputs();

		public abstract Set<Channel> getSyncs();

		public abstract List<State> getNextStates(State currentState, Channel channel);

		int[] initializeDBM() {
				// we need a DBM of size n*n, where n is the number of clocks (x0, x1, x2, ... , xn)
				// clocks x1 to xn are clocks derived from our components, while x0 is a reference clock needed by the library
				// initially dbm is an array of 0's, which is what we need
				int[] dbm = new int[dbmSize*dbmSize];
				dbm = DBMLib.dbm_init(dbm, dbmSize);
				return dbm;
		}

		boolean isDbmValid(int[] dbm) {
				return DBMLib.dbm_isValid(dbm, dbmSize);
		}

		private int[] buildConstraintsForGuard(int[] dbm, int i, Guard g) {
				int max = 1073741823;

				int lowerBoundI = g.getLowerBound();
				int upperBoundI = g.getUpperBound();

				for (int a = 0; a < dbmSize; a++) {
						if (a != i && a == 0) {
								int lowerBoundA = dbm[a] * (-1);
								int upperBoundA = dbm[a * dbmSize];

								dbm = DBMLib.dbm_constrain1(dbm, dbmSize, i, a, (upperBoundI == max) ? max : upperBoundI - lowerBoundA, false);
								dbm = DBMLib.dbm_constrain1(dbm, dbmSize, a, i, (upperBoundA == max) ? max : upperBoundA - lowerBoundI, false);
						}
				}

				return dbm;
		}

		List<Guard> getInvariants(List<Location> locations) {
				List<Guard> invariants = new ArrayList<>();

				for (Location location : locations) {
						Guard invariant = location.getInvariant();
						if (invariant != null) invariants.add(invariant);
				}

				return invariants;
		}

		int[] applyInvariantsOrGuards(int[] dbm, List<Guard> guards) {
				for (Guard guard : guards) {
						// get guard and then its index in the clock array so you know the index in the DBM
						int a = getClocks().indexOf(guard.getClock());
						dbm = buildConstraintsForGuard(dbm, (a + 1), guard);
				}
				return dbm;
		}

		int[] applyResets(int[] dbm, List<Update> resets) {
				for (Update reset : resets) {
						int index = clocks.indexOf(reset.getClock());

						dbm = DBMLib.dbm_updateValue(dbm, dbmSize, (index + 1), reset.getValue());
				}

				return dbm;
		}

		int[] delay(int[] dbm) {
				return DBMLib.dbm_up(dbm, dbmSize);
		}
}