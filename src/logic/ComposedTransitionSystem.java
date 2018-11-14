package logic;
import models.*;
import java.util.*;

public class ComposedTransitionSystem extends TransitionSystem {
		private ArrayList<Component> machines;
		private Set<Channel> inputs, outputs, inputsOutside, outputsOutside, syncs;

		public ComposedTransitionSystem(ArrayList<Component> machines) {
				super(machines);
				this.machines = machines;
				inputsOutside = new HashSet<>();
				outputsOutside = new HashSet<>();
				inputs = new HashSet<>();
				outputs = new HashSet<>();
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
						inputs.addAll(inputsOfI);
						outputs.addAll(outputsOfI);

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
				outputs.removeAll(syncs);
		}

		public Set<Channel> getInputs() {
				return inputs;
		}

		public Set<Channel> getOutputs() {
				return outputs;
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

		public State getInitialState() {
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

		private ArrayList<Guard> getInvariants(ArrayList<Location> locations) {
				ArrayList<Guard> invariants = new ArrayList<>();

				for (Location location : locations) {
						Guard invariant = location.getInvariant();
						if (invariant != null) invariants.add(invariant);
				}

				return invariants;
		}
}