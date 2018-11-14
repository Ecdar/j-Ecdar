package logic;

import lib.DBMLib;
import models.*;
import parser.Parser;

import java.io.File;
import java.util.*;

public class Composition {
		private ArrayList<Component> machines;
		private ArrayList<Clock> clocks;
		private Set<Channel> inputsOutside, outputsOutside, syncs;
		private int dbmSize;

		public Composition(ArrayList<Component> machines) {
				this.machines = machines;
				this.clocks = getClocks();
				this.dbmSize = clocks.size() + 1;
				inputsOutside = new HashSet<>();
				outputsOutside = new HashSet<>();
				syncs = new HashSet<>();

				for (int i = 0; i < machines.size(); i++) {
						Set<Channel> inputsOfI, outputsOfI, sync, outputsOfOthers, inputsOfOthers;
						inputsOfI = new HashSet<>();
						outputsOfI = new HashSet<>();
						sync = new HashSet<>();
						outputsOfOthers = new HashSet<>();
						inputsOfOthers  = new HashSet<>();
						inputsOfI.addAll(machines.get(i).getInputAct());
						outputsOfI.addAll(machines.get(i).getOutputAct());
						sync.addAll(machines.get(i).getOutputAct());

						for (int j = 0; j < machines.size(); j++) {
								if (i != j) {
										// check if output actions overlap
										Set<Channel> diff = new HashSet<>();
										diff.addAll(machines.get(i).getOutputAct());
										diff.retainAll(machines.get(j).getOutputAct());
										if (!diff.isEmpty()) {
												throw new IllegalArgumentException("machines cannot be composed");
										}

										outputsOfOthers.addAll(machines.get(j).getOutputAct());

										inputsOfOthers.addAll(machines.get(j).getInputAct());

										Set<Channel> syncCopy = new HashSet<>();
										syncCopy.addAll(sync);
										syncCopy.retainAll(machines.get(j).getInputAct());
										syncs.addAll(syncCopy);
								}
						}

						// set difference
						inputsOfI.removeAll(outputsOfOthers);
						outputsOfI.removeAll(inputsOfOthers);

						inputsOutside.addAll(inputsOfI);
						outputsOutside.addAll(outputsOfI);
				}

				// load DBM library
				String fileName = "src/" + System.mapLibraryName("DBM");
				File lib = new File(fileName);
				System.load(lib.getAbsolutePath());
		}

		public void compose() {
				// load library
				String fileName = "src/" + System.mapLibraryName("DBM");
				File lib = new File(fileName);
				System.load(lib.getAbsolutePath());

				State initialState = computeInitial();

				ArrayList<State> passed = new ArrayList<>();
				Deque<State> waiting = new ArrayDeque<>();
				ArrayList<StateTransition> stateTransitions = new ArrayList<>();
				waiting.push(initialState);

				Set<Channel> inputsOutside = new HashSet<>();
				Set<Channel> outputsOutside = new HashSet<>();
				Set<Channel> syncs = new HashSet<>();

				int machineCount = machines.size();

				for (int i = 0; i < machineCount; i++) {
						Set<Channel> inputsOfI = new HashSet<>();
						inputsOfI.addAll(machines.get(i).getInputAct());
						Set<Channel> outputsOfI = new HashSet<>();
						outputsOfI.addAll(machines.get(i).getOutputAct());
						Set<Channel> sync = new HashSet<>();
						sync.addAll(machines.get(i).getOutputAct());

						Set<Channel> outputsOfOthers = new HashSet<>();
						Set<Channel> inputsOfOthers = new HashSet<>();

						for (int j = 0; j < machineCount; j++) {
								if (i != j) {
										outputsOfOthers.addAll(machines.get(j).getOutputAct());

										inputsOfOthers.addAll(machines.get(j).getInputAct());

										sync.retainAll(machines.get(j).getInputAct());
										syncs.addAll(sync);
								}
						}

						// set difference
						inputsOfI.removeAll(outputsOfOthers);
						outputsOfI.removeAll(inputsOfOthers);

						inputsOutside.addAll(inputsOfI);
						outputsOutside.addAll(outputsOfI);
				}

				while (waiting.size() > 0) {
						// get state from top of stack
						State state = waiting.pop();
						// delay
						int[] dbm = delay(state.getZone());
						// apply invariants
						dbm = applyInvariantsOrGuards(dbm, getInvariants(state.getLocations()));
						state.setZone(dbm);

						if (!passedContainsState(passed, state)) {
								passed.add(state);
								ArrayList<Location> locations = state.getLocations();
								for (int i = 0; i < locations.size(); i++) {
										Component component = machines.get(i);
										Location loc = locations.get(i);
										ArrayList<Transition> transitions = component.getTransitionsFromLocation(loc);
										for (Transition transition : transitions) {
												Channel channel = transition.getChannel();
												if (inputsOutside.contains(channel) || outputsOutside.contains(channel)) {
														// build state
														ArrayList<Location> newLocations = new ArrayList<>();
														newLocations.addAll(locations);
														newLocations.set(i, transition.getTo());
														ArrayList<Guard> guards = transition.getGuards();
														ArrayList<Update> updates = transition.getUpdates();
														// apply guards
														dbm = applyInvariantsOrGuards(dbm, guards);
														// apply resets
														dbm = applyResets(dbm, updates);
														State newState = new State(newLocations, dbm);
														if (!waiting.contains(newState) || !passedContainsState(passed, newState)) {
																waiting.push(newState);
														}
														boolean isInput = inputsOutside.contains(channel);
														StateTransition stateTransition = new StateTransition(state, newState, channel, isInput, guards, updates);
														if (!stateTransitionsContainsTransition(stateTransitions, stateTransition))
																stateTransitions.add(stateTransition);
												}
												if (syncs.contains(channel)) {
														for (int j = 0; j < locations.size(); j++) {
																if (i != j) {
																		Location locJ = locations.get(j);
																		Component machine = machines.get(j);
																		ArrayList<Transition> transitionsJ = machine.getTransitionsFromLocation(locJ);
																		transitionsJ.removeIf(n -> n.getChannel() != channel);
																		for (Transition transitionJ : transitionsJ) {
																				// build state
																				ArrayList<Location> newLocations = new ArrayList<>();
																				newLocations.addAll(locations);
																				newLocations.set(i, transition.getTo());
																				newLocations.set(j, transitionJ.getTo());

																				ArrayList<Guard> guards = new ArrayList<>();
																				guards.addAll(transition.getGuards());
																				guards.addAll(transitionJ.getGuards());

																				ArrayList<Update> updates = new ArrayList<>();
																				updates.addAll(transition.getUpdates());
																				updates.addAll(transitionJ.getUpdates());

																				// apply guards
																				dbm = applyInvariantsOrGuards(dbm, guards);
																				// apply resets
																				dbm = applyResets(dbm, updates);
																				State newState = new State(newLocations, dbm);
																				if (!waiting.contains(newState) || !passedContainsState(passed, newState)) {
																						waiting.push(newState);
																				}
																				StateTransition stateTransition = new StateTransition(state, newState, channel, true, guards, updates);
																				if (!stateTransitionsContainsTransition(stateTransitions, stateTransition))
																						stateTransitions.add(stateTransition);
																		}
																}
														}
												}
										}
								}
						}
				}
		}

		public ArrayList<State> getNextStates(State state, Channel channel) {
				ArrayList<State> states = new ArrayList<>();
				ArrayList<Location> locations = state.getLocations();

				for (int i = 0; i < machines.size(); i++) {
						ArrayList<Transition> transitions = machines.get(i).getTransitionsFromLocationAndSignal(state.getLocations().get(i), channel);

						for (Transition transition : transitions) {
								if (inputsOutside.contains(channel) || outputsOutside.contains(channel)) {
										ArrayList<Location> newLocations = new ArrayList<>();
										for (int j = 0; j < machines.size(); j++) {
												if (i != j) {
														newLocations.add(locations.get(j));
												} else {
														newLocations.add(transition.getTo());
												}
										}
										int[] dbm = state.getZone();
										// apply guards
										if (!transition.getGuards().isEmpty()) dbm = applyInvariantsOrGuards(dbm, transition.getGuards());
										// apply resets
										if (!transition.getUpdates().isEmpty()) dbm = applyResets(dbm, transition.getUpdates());
										// delay
										dbm = delay(dbm);
										// apply invariants
										if (!getInvariants(newLocations).isEmpty()) dbm = applyInvariantsOrGuards(dbm, getInvariants(newLocations));

										// construct new state and add it to list
										State newState = new State(newLocations, dbm);
										states.add(newState);
								} else if (syncs.contains(channel)) {
										if (machines.get(i).getOutputAct().contains(channel)) {
												for (int j = 0; j < machines.size(); j++) {
														if (machines.get(j).getInputAct().contains(channel)) {
																ArrayList<Transition> transitionsFromJ = machines.get(j).getTransitionsFromLocationAndSignal(locations.get(j), channel);
																for (Transition t : transitionsFromJ) {
																		ArrayList<Location> newLocations = new ArrayList<>();
																		newLocations.addAll(locations);
																		newLocations.set(i, transition.getTo());
																		newLocations.set(j, t.getTo());
																		int[] dbm = state.getZone();

																		ArrayList<Guard> guards = new ArrayList<>();
																		ArrayList<Update> updates = new ArrayList<>();
																		guards.addAll(transition.getGuards());
																		updates.addAll(transition.getUpdates());
																		guards.addAll(t.getGuards());
																		updates.addAll(t.getUpdates());

																		// apply guards
																		if (!guards.isEmpty()) dbm = applyInvariantsOrGuards(dbm, guards);
																		// apply resets
																		if (!updates.isEmpty()) dbm = applyResets(dbm, updates);
																		// delay
																		dbm = delay(dbm);
																		// apply invariants
																		if (!getInvariants(newLocations).isEmpty()) dbm = applyInvariantsOrGuards(dbm, getInvariants(newLocations));

																		State newState = new State(newLocations, dbm);
																		states.add(newState);
																}
														}
												}
										}
								}
						}
				}

				return states;
		}

		public State computeInitial() {
				ArrayList<Location> initialLocations = new ArrayList<>();

				for (Component machine : machines) {
						Location init = machine.getInitLoc();
						initialLocations.add(init);
				}

				int[] zone = initializeDBM();
				zone = delay(zone);
				zone = applyInvariantsOrGuards(zone, getInvariants(initialLocations));

				return new State(initialLocations, zone);
		}

		private boolean passedContainsState(ArrayList<State> passed, State state) {
				// keep only states that have the same locations
				ArrayList<State> passedCopy = new ArrayList<>();
				passedCopy.addAll(passed);
				passedCopy.removeIf(n -> !Arrays.equals(n.getLocations().toArray(), state.getLocations().toArray()));

				for (State passedState : passedCopy) {
						int dim = (int) Math.sqrt(passedState.getZone().length);
						// check for zone inclusion
						if (DBMLib.dbm_isSubsetEq(state.getZone(), passedState.getZone(), dim)) {
								return true;
						}
				}
				return false;
		}

		private boolean stateTransitionsContainsTransition(ArrayList<StateTransition> transitions, StateTransition transition) {
				for (StateTransition stateTransition : transitions) {
						if (stateTransition.equals(transition))
								return true;
				}
				return false;
		}

		public ArrayList<Clock> getClocks() {
				ArrayList<Clock> allClocks = new ArrayList<>();

				for (Component component : machines) {
						allClocks.addAll(component.getClocks());
				}

				return allClocks;
		}

		private ArrayList<Guard> getInvariants(ArrayList<Location> locations) {
				ArrayList<Guard> invariants = new ArrayList<>();

				for (Location location : locations) {
						Guard invariant = location.getInvariant();
						if (invariant != null) invariants.add(invariant);
				}

				return invariants;
		}

		private int[] initializeDBM() {
				// we need a DBM of size n*n, where n is the number of clocks (x0, x1, x2, ... , xn)
				// clocks x1 to xn are clocks derived from our components, while x0 is a reference clock needed by the library
				// initially dbm is an array of 0's, which is what we need
				return new int[dbmSize*dbmSize];
		}

		private int[] delay(int[] dbm) {
				return DBMLib.dbm_up(dbm, dbmSize);
		}

		private int[] applyInvariantsOrGuards(int[] dbm, ArrayList<Guard> guards) {
				// take 2 guards at a time in order to determine constraint (x-y and y-x)
				for (int i = 0; i < guards.size(); i++) {
						// get guard and then its index in the clock array so you know the index in the DBM
						Guard g1 = guards.get(i); int a = clocks.indexOf(g1.getClock());
						dbm = buildConstraintWithX0(dbm, dbmSize, (a+1), g1);

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

		private void printDBM(int[] dbm) {
				for (int x : dbm) System.out.print(x + "  ");
				System.out.println();
		}
}
