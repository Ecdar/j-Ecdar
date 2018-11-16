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

		// TODO handle dbms of different sizes
		public boolean check() {
				while (!waiting.isEmpty()) {
						State[] curr = waiting.pop();

						if (!passedContainsState(curr)) {
								passed.add(curr);
								Set<Channel> inputs2 = ts2.getInputs();
								Set<Channel> outputs1 = ts1.getOutputs();

								for (Channel output : outputs1) {
										ArrayList<StateTransition> next1 = ts1.getTransitionsFrom(curr[0], output);
										if (!next1.isEmpty()) {
												ArrayList<StateTransition> next2 = ts2.getTransitionsFrom(curr[1], output);
												if (next2.isEmpty()) {
														return false;
												} else {
														for (StateTransition st1 : next1) {
																for (StateTransition st2 : next2) {
																		// check guards
																		int[] zone1 = st1.getFrom().getZone(); zone1 = ts1.applyInvariantsOrGuards(zone1, st1.getGuards());
																		int[] zone2 = st2.getFrom().getZone(); zone2 = ts2.applyInvariantsOrGuards(zone2, st2.getGuards());

																		if (DBMLib.dbm_isSubsetEq(zone1, zone2, ts1.dbmSize)) {
																				State[] newState = new State[]{st1.getTo(), st2.getTo()};
																				waiting.add(newState);
																		}
																}
														}
												}
										}
								}

								for (Channel input : inputs2) {
										ArrayList<StateTransition> next2 = ts2.getTransitionsFrom(curr[1], input);
										if (!next2.isEmpty()) {
												ArrayList<StateTransition> next1 = ts1.getTransitionsFrom(curr[0], input);
												if (next1.isEmpty()) {
														return false;
												} else {
														for (StateTransition st1 : next1) {
																for (StateTransition st2 : next2) {
																		// check guards
																		int[] zone1 = st1.getFrom().getZone(); zone1 = ts1.applyInvariantsOrGuards(zone1, st1.getGuards());
																		int[] zone2 = st2.getFrom().getZone(); zone2 = ts2.applyInvariantsOrGuards(zone2, st2.getGuards());

																		if (DBMLib.dbm_isSubsetEq(zone1, zone2, ts1.dbmSize)) {
																				State[] newState = new State[]{st1.getTo(), st2.getTo()};
																				waiting.add(newState);
																		}
																}
														}
												}
										}
								}

								// check if both can delay
								int[] zone1 = curr[0].getZone(); zone1 = ts1.delay(zone1);
								int[] zone2 = curr[1].getZone(); zone2 = ts2.delay(zone2);

								if (!DBMLib.dbm_isSubsetEq(zone1, zone2, ts1.dbmSize))
										return false;
						}
				}
				return true;
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
