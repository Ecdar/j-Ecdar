package logic;

import lib.DBMLib;
import models.Channel;
import models.State;
import java.io.File;
import java.util.*;

public class Refinement {
		private TransitionSystem ts1, ts2;
		private Deque<State[]> waiting;
		private ArrayList<State[]> passed;

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
								passed.add(curr);

								for (Channel output : outputs1) {
										ArrayList<State> next1 = ts1.getNextStates(curr[0], output);
										if (!next1.isEmpty()) {
												ArrayList<State> next2 = ts2.getNextStates(curr[1], output);
												if (next2.isEmpty()) {
														return false;
												} else {
														waiting.addAll(getNewStates(next1, next2));
												}
										}
								}

								for (Channel input : inputs2) {
										ArrayList<State> next2 = ts2.getNextStates(curr[1], input);
										if (!next2.isEmpty()) {
												ArrayList<State> next1 = ts1.getNextStates(curr[0], input);
												if (next1.isEmpty()) {
														return false;
												} else {
														waiting.addAll(getNewStates(next1, next2));
												}
										}
								};
						}
				}
				return true;
		}

		private ArrayList<State[]> getNewStates(ArrayList<State> next1, ArrayList<State> next2) {
				ArrayList<State[]> states = new ArrayList<>();

				for (State st1 : next1) {
						for (State st2 : next2) {
								int[] zone1 = st1.getZone();
								int[] zone2 = st2.getZone();

								if (DBMLib.dbm_isSubsetEq(zone1, zone2, ts1.dbmSize)) {
										State[] newState = new State[]{st1, st2};
										states.add(newState);
								}
						}
				}

				return states;
		}

		private boolean passedContainsState(State[] state) {
				// keep only states that have the same locations
				ArrayList<State[]> passedCopy = new ArrayList<>();
				passedCopy.addAll(passed);
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
