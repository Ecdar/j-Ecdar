package logic;

import lib.DBMLib;
import models.*;
import java.io.File;
import java.util.*;

public class Refinement {
		private TransitionSystem ts1, ts2;
		private Deque<State[]> waiting;
		private List<State[]> passed;
		private boolean failed;

		public Refinement(List<Component> machines1, List<Component> machines2) {
				try {
						this.ts1 = machines1.size() == 1 ? new SimpleTransitionSystem(machines1.get(0)) : new ComposedTransitionSystem(machines1);
						this.ts2 = machines2.size() == 1 ? new SimpleTransitionSystem(machines2.get(0)) : new ComposedTransitionSystem(machines2);

						this.waiting = new ArrayDeque<>();
						this.passed = new ArrayList<>();
						// the first states we look at are the initial ones
						waiting.push(new State[]{ts1.getInitialState(), ts2.getInitialState()});

						String fileName = "src/" + System.mapLibraryName("DBM");
						File lib = new File(fileName);
						System.load(lib.getAbsolutePath());

						failed = false;
				} catch (IllegalArgumentException ex) {
						failed = true;
				}
		}

		public boolean check() {
				// if the transitions systems could not be constructed, refinement cannot hold
				if (failed) return false;

				// get the inputs of machine 2 and the outputs of machine 1
				Set<Channel> inputs2 = ts2.getInputs();
				Set<Channel> outputs1 = ts1.getOutputs();
				Set<Channel> actions1 = setDifference(ts1.getActions(), ts2.getActions());
				Set<Channel> actions2 = setDifference(ts2.getActions(), ts1.getActions());

				// keep looking at states from Waiting as long as it contains elements
				while (!waiting.isEmpty()) {
						State[] curr = waiting.pop();

						// ignore if the zones are included in zones belonging to pairs of states that we already visited
						if (!passedContainsState(curr)) {
								// need to make deep copy
								State newState1 = new State(curr[0].getLocations(), curr[0].getZone());
								State newState2 = new State(curr[1].getLocations(), curr[1].getZone());
								// mark the pair of states as visited
								passed.add(new State[] {newState1, newState2});

								// check that for every output in machine 1 there is a corresponding output in machine 2
								boolean holds1 = checkActions(outputs1, curr[0], curr[1], ts1, ts2, false);
								if (!holds1)
										return false;

								// check that for every input in machine 2 there is a corresponding input in machine 1
								boolean holds2 = checkActions(inputs2, curr[1], curr[0], ts2, ts1, true);
								if (!holds2)
										return false;

								// for actions belonging only to machine 1, make a pair for each transition in machine 1 and the state in machine 2
								for (Channel action : actions1) {
										List<StateTransition> transitions = ts1.getNextTransitions(curr[0], action);
										for (StateTransition stateTransition : transitions) {
												State target = new State(stateTransition.getTarget().getLocations(), stateTransition.getTarget().getZone());
												target.delay();
												target.applyInvariants(ts1.getClocks());
												State[] newState = new State[]{target, curr[1]};
												waiting.add(newState);
										}
								}

								// for actions belonging only to machine 2, make a pair for each transition in machine 2 and the state in machine 1
								for (Channel action : actions2) {
										List<StateTransition> transitions = ts2.getNextTransitions(curr[1], action);
										for (StateTransition stateTransition : transitions) {
												State target = new State(stateTransition.getTarget().getLocations(), stateTransition.getTarget().getZone());
												target.delay();
												target.applyInvariants(ts2.getClocks());
												State[] newState = new State[]{curr[0], target};
												waiting.add(newState);
										}
								}
						}
				}

				// if we got here it means refinement property holds
				return true;
		}

		// takes transitions of machine 1 and 2 and builds the states corresponding to all possible combinations between them
		private List<State[]> getNewStates(List<StateTransition> next1, List<StateTransition> next2) {
				List<State[]> states = new ArrayList<>();

				for (StateTransition t1 : next1) {
						for (StateTransition t2 : next2) {
								// get source states and apply guards on them
								State source1 = new State(t1.getSource().getLocations(), t1.getSource().getZone());
								State source2 = new State(t2.getSource().getLocations(), t2.getSource().getZone());
								source1.applyGuards(t1.getGuards(), ts1.getClocks());
								source2.applyGuards(t2.getGuards(), ts2.getClocks());

								// based on the zone, get the min and max value of the clocks
								int maxSource1 = source1.getMinUpperBound(); int maxSource2 = source2.getMinUpperBound();
								int minSource1 = source1.getMinLowerBound(); int minSource2 = source2.getMinLowerBound();

								// check that the zones are compatible
								if (maxSource1 >= minSource2 && maxSource2 >= minSource1) {
										// get target states, delay and apply invariants
										State target1 = new State(t1.getTarget().getLocations(), t1.getTarget().getZone());
										State target2 = new State(t2.getTarget().getLocations(), t2.getTarget().getZone());

										target1.delay(); target2.delay();
										target1.applyInvariants(ts1.getClocks());
										target2.applyInvariants(ts2.getClocks());

										// get the max value of the clocks
										int maxTarget1 = target1.getMinUpperBound(); int maxTarget2 = target2.getMinUpperBound();
										int minTarget1 = target1.getMinLowerBound(); int minTarget2 = target2.getMinLowerBound();

										// check again that the zones are compatible
										if (maxTarget1 >= minTarget2 && maxTarget2 >= minTarget1) {
												State[] newState = new State[]{target1, target2};
												states.add(newState);
										}
								}
						}
				}

				return states;
		}

		private boolean checkActions(Set<Channel> actions, State state1, State state2, TransitionSystem sys1, TransitionSystem sys2, boolean isInput) {
				for (Channel action : actions) {
						List<StateTransition> next1 = sys1.getNextTransitions(state1, action);
						if (!next1.isEmpty()) {
								List<StateTransition> next2 = sys2.getNextTransitions(state2, action);
								if (next2.isEmpty()) {
										// we found an output in machine 1 that doesn't exist in machine 2, so refinement doesn't hold
										return false;
								} else {
										List<State[]> newStates = isInput ? getNewStates(next2, next1) : getNewStates(next1, next2);
										if (newStates.isEmpty()) {
												// if we don't get any new states, it means we found some incompatibility
												return false;
										} else {
												waiting.addAll(newStates);
										}
								}
						}
				}
				return true;
		}

		private boolean passedContainsState(State[] state) {
				// keep only states that have the same locations
				List<State[]> passedCopy = new ArrayList<>(passed);
				passedCopy.removeIf(n -> !(Arrays.equals(n[0].getLocations().toArray(), state[0].getLocations().toArray()) &&
								Arrays.equals(n[1].getLocations().toArray(), state[1].getLocations().toArray())));

				for (State[] passedState : passedCopy) {
						// check for zone inclusion
						if (DBMLib.dbm_isSubsetEq(state[0].getZone(), passedState[0].getZone(), ts1.getDbmSize()) &&
										DBMLib.dbm_isSubsetEq(state[1].getZone(), passedState[1].getZone(), ts2.getDbmSize())) {
								return true;
						}
				}

				return false;
		}

		private Set<Channel> setDifference(Set<Channel> set1, Set<Channel> set2) {
				Set<Channel> newSet = new HashSet<>();
				newSet.addAll(set1);
				newSet.removeAll(set2);

				return newSet;
		}
}
