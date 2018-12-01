package logic;

import lib.DBMLib;
import models.Channel;
import models.State;
import models.StateTransition;
import java.io.File;
import java.util.*;

public class Refinement {
		private TransitionSystem ts1, ts2;
		private Deque<State[]> waiting;
		private List<State[]> passed;

		public Refinement(TransitionSystem ts1, TransitionSystem ts2) {
				this.ts1 = ts1;
				this.ts2 = ts2;
				this.waiting = new ArrayDeque<>();
				this.passed = new ArrayList<>();
				waiting.push(new State[]{ts1.getInitialState(), ts2.getInitialState()});

				String fileName = "src/" + System.mapLibraryName("DBM");
				File lib = new File(fileName);
				System.load(lib.getAbsolutePath());
		}

		public boolean check() {
				Set<Channel> inputs2 = ts2.getInputs();
				Set<Channel> outputs1 = ts1.getOutputs();

				while (!waiting.isEmpty()) {
						State[] curr = waiting.pop();

						if (!passedContainsState(curr)) {
								// need to make deep copy
								State newState1 = new State(curr[0].getLocations(), curr[0].getZone());
								State newState2 = new State(curr[1].getLocations(), curr[1].getZone());
								passed.add(new State[] {newState1, newState2});

								for (Channel output : outputs1) {
										List<StateTransition> next1 = ts1.getNextTransitions(curr[0], output);
										if (!next1.isEmpty()) {
												List<StateTransition> next2 = ts2.getNextTransitions(curr[1], output);
												if (next2.isEmpty()) {
														return false;
												} else {
														waiting.addAll(getNewStates(next1, next2));
												}
										}
								}

								for (Channel input : inputs2) {
										List<StateTransition> next2 = ts2.getNextTransitions(curr[1], input);
										if (!next2.isEmpty()) {
												List<StateTransition> next1 = ts1.getNextTransitions(curr[0], input);
												if (next1.isEmpty()) {
														return false;
												} else {
														waiting.addAll(getNewStates(next1, next2));
												}
										}
								}
						}
				}
				return true;
		}

		private List<State[]> getNewStates(List<StateTransition> next1, List<StateTransition> next2) {
				List<State[]> states = new ArrayList<>();

				for (StateTransition t1 : next1) {
						for (StateTransition t2 : next2) {
								State source1 = new State(t1.getSource().getLocations(), t1.getSource().getZone());
								State source2 = new State(t2.getSource().getLocations(), t2.getSource().getZone());

								source1.applyGuards(t1.getGuards(), ts1.getClocks());
								source2.applyGuards(t2.getGuards(), ts2.getClocks());

								int maxSource1 = source1.getMaxValuation(); int maxSource2 = source2.getMaxValuation();
								int minSource1 = source1.getMinValuation(); int minSource2 = source2.getMinValuation();

								if (maxSource1 >= minSource2 && maxSource2 >= minSource1) {
										State target1 = new State(t1.getTarget().getLocations(), t1.getTarget().getZone());
										State target2 = new State(t2.getTarget().getLocations(), t2.getTarget().getZone());

										target1.delay(); target2.delay();
										target1.applyInvariants(ts1.getClocks());
										target2.applyInvariants(ts2.getClocks());

										int maxTarget1 = target1.getMaxValuation(); int maxTarget2 = target2.getMaxValuation();
										if (maxTarget1 <= maxTarget2) {
												State[] newState = new State[]{target1, target2};
												states.add(newState);
										}
								}
						}
				}

				return states;
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
}
