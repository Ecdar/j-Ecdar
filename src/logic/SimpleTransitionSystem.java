package logic;

import lib.DBMLib;
import models.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

public class SimpleTransitionSystem {

		private Component component;
		private ArrayList<Clock> clocks;
		private int dbmSize;

		public SimpleTransitionSystem(Component component) {
				this.component = component;
				clocks = new ArrayList<>();
				clocks.addAll(component.getClocks());
				dbmSize = clocks.size() + 1;

				String fileName = "src/" + System.mapLibraryName("DBM");
				File lib = new File(fileName);
				System.load(lib.getAbsolutePath());
		}

		public Set<Channel> getInputs() {
				return component.getInputAct();
		}

		public Set<Channel> getOutputs() {
				return component.getOutputAct();
		}

		public int getDbmSize() {
				return dbmSize;
		}

		public State getInitialState() {
				// size of DBM is clock count + 1 (x0 is reference clock)
				int[] dbm = new int[dbmSize*dbmSize];
				// delay
				dbm = DBMLib.dbm_up(dbm, dbmSize);
				ArrayList<Guard> invariants = new ArrayList<>();
				// apply invariant
				dbm = applyInvariantsOrGuards(dbm, invariants);
				return new State(new ArrayList<>(Arrays.asList(component.getInitLoc())), dbm);
		}

		public ArrayList<State> getNextStates(State currentState, String signal) {
				Channel channel = new Channel(signal);
				return getNextStates(currentState, channel);
		}

		public ArrayList<State> getNextStates(State currentState, Channel channel) {
				ArrayList<State> states = new ArrayList<>();
				ArrayList<Transition> transitions = component.getTransitionsFromLocationAndSignal(currentState.getLocations().get(0), channel);
				for (Transition transition : transitions) {
						Location newLocation = transition.getTo();
						int[] dbm = currentState.getZone();
						// apply guards
						dbm = applyInvariantsOrGuards(dbm, transition.getGuards());
						// apply resets
						dbm = applyResets(dbm, transition.getUpdates());
						// delay
						dbm = DBMLib.dbm_up(dbm, dbmSize);
						// apply invariant
						if (newLocation.getInvariant() != null)
								dbm = applyInvariantsOrGuards(dbm, new ArrayList<>(Arrays.asList(newLocation.getInvariant())));
						State newState = new State(new ArrayList<>(Arrays.asList(newLocation)), dbm);
						states.add(newState);
				}
				return states;
		}

		private int[] applyInvariantsOrGuards(int[] dbm, ArrayList<Guard> guards) {
				for (int i = 0; i < guards.size(); i++) {
						// get guard and then its index in the clock array so you know the index in the DBM
						Guard g1 = guards.get(i); int a = clocks.indexOf(g1.getClock());
						dbm = buildConstraintWithX0(dbm, dbmSize, (a+1), guards.get(i));

						// take 2 guards at a time in order to determine constraint (x-y and y-x)
						for (int j = (i + 1); j < guards.size(); j++) {
								Guard g2 = guards.get(j); int b = clocks.indexOf(g2.getClock());

								// add constraints to dbm
								dbm = buildConstraint(dbm, dbmSize, (a+1), (b+1), g1, g2);
								dbm = buildConstraint(dbm, dbmSize, (b+1), (a+1), g2, g1);
						}
				}
				return dbm;
		}

		private int[] applyResets(int[] dbm, ArrayList<Update> resets) {
				for (Update reset : resets) {
						int index = clocks.indexOf(reset.getClock());

						dbm = DBMLib.dbm_updateValue(dbm, dbmSize, (index+1), reset.getValue());
				}

				return dbm;
		}

		private int[] buildConstraint(int[] dbm, int size, int i, int j, Guard g1, Guard g2) {
				// determine constraint between 2 guards on clocks x and y by taking x's upper bound - y's lower bound
				int bound = (g1.upperBound() == Integer.MAX_VALUE) ? Integer.MAX_VALUE : g1.upperBound() - g2.lowerBound();
				// if either guard is strict, the constraint is also strict
				boolean strict = (g1.isStrict() || g2.isStrict());
				if (!strict && bound != Integer.MAX_VALUE) bound++;
				return DBMLib.dbm_constrain1(dbm, size, i, j, bound, true);
		}

		private int[] buildConstraintWithX0(int[] dbm, int size, int i, Guard g) {
				boolean strict = g.isStrict();
				int lowerBound = g.lowerBound();
				int upperBound = g.upperBound();
				if (!strict && upperBound != Integer.MAX_VALUE) upperBound++;
				dbm = DBMLib.dbm_constrain1(dbm, size, 0, i, lowerBound, true);
				dbm = DBMLib.dbm_constrain1(dbm, size, i, 0, upperBound, true);
				return dbm;
		}
}
