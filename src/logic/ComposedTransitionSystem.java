package logic;

import models.*;
import java.util.*;
import java.util.stream.Collectors;

public class ComposedTransitionSystem extends TransitionSystem {
		private List<Component> machines;
		private Set<Channel> inputs, outputs, syncs;

		public ComposedTransitionSystem(List<Component> machines) {
				super(machines);
				this.machines = machines;
				inputs = new HashSet<>();
				outputs = new HashSet<>();
				syncs = new HashSet<>();

				for (int i = 0; i < machines.size(); i++) {
						Set<Channel> inputsOfI, outputsOfI, sync, outputsOfOthers, inputsOfOthers;
						inputsOfI = new HashSet<>(machines.get(i).getInputAct());
						outputsOfI = new HashSet<>(machines.get(i).getOutputAct());
						sync = new HashSet<>(machines.get(i).getOutputAct());
						outputsOfOthers = new HashSet<>();
						inputsOfOthers  = new HashSet<>();
						inputs.addAll(inputsOfI);
						outputs.addAll(outputsOfI);

						for (int j = 0; j < machines.size(); j++) {
								if (i != j) {
										// check if output actions overlap
										Set<Channel> diff = new HashSet<>(machines.get(i).getOutputAct());
										diff.retainAll(machines.get(j).getOutputAct());
										if (!diff.isEmpty()) {
												throw new IllegalArgumentException("machines cannot be composed");
										}

										outputsOfOthers.addAll(machines.get(j).getOutputAct());

										inputsOfOthers.addAll(machines.get(j).getInputAct());

										Set<Channel> syncCopy = new HashSet<>(sync);
										syncCopy.retainAll(machines.get(j).getInputAct());
										syncs.addAll(syncCopy);
								}
						}

						// set difference
						inputsOfI.removeAll(outputsOfOthers);
						outputsOfI.removeAll(inputsOfOthers);
				}
				outputs.removeAll(syncs);
				inputs.removeAll(syncs);
		}

		public Set<Channel> getInputs() {
				return inputs;
		}

		public Set<Channel> getOutputs() {
				return outputs;
		}

		public Set<Channel> getSyncs() { return syncs; }

		public List<StateTransition> getNextTransitions(State currentState, Channel channel) {
				List<Location> locations = currentState.getLocations();
				List<List<Location>> locationsArr = new ArrayList<>();
				List<List<Transition>> transitionsArr = new ArrayList<>();

				if (outputs.contains(channel)) {
						for (int i = 0; i < locations.size(); i++) {
								List<Transition> transitions = machines.get(i).getTransitionsFromLocationAndSignal(locations.get(i), channel);

								for (Transition transition : transitions) {
										List<Location> newLocations = new ArrayList<>(locations);
										newLocations.set(i, transition.getTarget());

										locationsArr.add(newLocations);
										transitionsArr.add(new ArrayList<>(Arrays.asList(transition)));
								}
						}
				} else if (inputs.contains(channel) || (syncs.contains(channel))) {
						boolean checkForInputs = checkForInputs(channel, locations);

						if (checkForInputs) {
								List<List<Location>> locationsList = new ArrayList<>();
								List<List<Transition>> transitionsList = new ArrayList<>();
								for (int i = 0; i < locations.size(); i++) {
										List<Transition> transitionsForI = machines.get(i).getTransitionsFromLocationAndSignal(locations.get(i), channel);
										if (transitionsForI.isEmpty()) {
												List<Location> newLocations = new ArrayList<>();
												newLocations.add(locations.get(i));
												locationsList.add(newLocations);
												List<Transition> newTransitions = new ArrayList<>();
												newTransitions.add(null);
												transitionsList.add(newTransitions);
										} else {
												List<Location> newLocations = transitionsForI.stream().map(Transition::getTarget).collect(Collectors.toList());
												locationsList.add(newLocations);
												transitionsList.add(transitionsForI);
										}
								}
								locationsArr = cartesianProduct(locationsList);
								transitionsArr = cartesianProduct(transitionsList);
						}
				}

				return new ArrayList<>(addNewStateTransitions(currentState, locationsArr, transitionsArr));
		}

		private <T> List<List<T>> cartesianProduct(List<List<T>> lists) {
				List<List<T>> resultLists = new ArrayList<>();
				if (lists.size() == 0) {
						resultLists.add(new ArrayList<>());
						return resultLists;
				} else {
						List<T> firstList = lists.get(0);
						List<List<T>> remainingLists = cartesianProduct(lists.subList(1, lists.size()));
						for (T condition : firstList) {
								for (List<T> remainingList : remainingLists) {
										List<T> resultList = new ArrayList<>();
										resultList.add(condition);
										resultList.addAll(remainingList);
										resultLists.add(resultList);
								}
						}
				}
				return resultLists;
		}

		private boolean checkForInputs(Channel channel, List<Location> locations) {
				boolean check = true;

				// for syncs, we must make sure we have an output first
				if (syncs.contains(channel)) {
						for (int i = 0; i < machines.size(); i++) {
								if (machines.get(i).getOutputAct().contains(channel)) {
										List<Transition> transitionsForI = machines.get(i).getTransitionsFromLocationAndSignal(locations.get(i), channel);
										if (transitionsForI.isEmpty())
												check = false;
								}
						}
				}

				return check;
		}
}