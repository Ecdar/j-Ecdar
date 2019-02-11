package logic;

import lib.DBMLib;
import models.Channel;
import models.Guard;

import java.io.File;
import java.util.*;

public class Refinement {
    private TransitionSystem ts1, ts2;
    private Deque<State[]> waiting;
    private List<State[]> passed;

    public Refinement(TransitionSystem system1, TransitionSystem system2) {
        this.ts1 = system1;
        this.ts2 = system2;
        this.waiting = new ArrayDeque<>();
        this.passed = new ArrayList<>();
        // the first states we look at are the initial ones
        waiting.push(new State[]{ts1.getInitialState(), ts2.getInitialState()});

        String fileName = "src/" + System.mapLibraryName("DBM");
        File lib = new File(fileName);
        System.load(lib.getAbsolutePath());
    }

    public boolean check() {
        // get the inputs of machine 2 and the outputs of machine 1
        Set<Channel> inputs2 = ts2.getInputs();
        Set<Channel> outputs1 = ts1.getOutputs();

        // keep looking at states from Waiting as long as it contains elements
        while (!waiting.isEmpty()) {
            State[] curr = waiting.pop();

            // ignore if the zones are included in zones belonging to pairs of states that we already visited
            if (!passedContainsState(curr)) {
                // need to make deep copy
                State newState1 = copyState(curr[0]);
                State newState2 = copyState(curr[1]);
                // mark the pair of states as visited
                passed.add(new State[]{newState1, newState2});

                // check that for every output in machine 1 there is a corresponding output in machine 2
                boolean holds1 = checkOutputs(outputs1, curr[0], curr[1], ts1, ts2);
                if (!holds1)
                    return false;

                // check that for every input in machine 2 there is a corresponding input in machine 1
                boolean holds2 = checkInputs(inputs2, curr[0], curr[1], ts1, ts2);
                if (!holds2)
                    return false;
            }
        }

        // if we got here it means refinement property holds
        return true;
    }

    // takes transitions of machine 1 and 2 and builds the states corresponding to all possible combinations between them
    private List<State[]> getNewStates(List<Transition> next1, List<Transition> next2) {
        List<State[]> states = new ArrayList<>();

        for (Transition t1 : next1) {
            for (Transition t2 : next2) {
                // get source and target states
                State source1 = copyState(t1.getSource());
                State source2 = copyState(t2.getSource());
                State target1 = copyState(t1.getTarget());
                State target2 = copyState(t2.getTarget());

                State[] newState = buildStatePair(source1, source2, t1.getGuards(), t2.getGuards(), target1, target2);
                if (newState != null) {
                    states.add(newState);
                }
            }
        }

        return states;
    }

    private State[] buildStatePair(State source1, State source2, List<Guard> guards1, List<Guard> guards2, State target1, State target2) {
        // apply guards on the source states
        source1.applyGuards(guards1, ts1.getClocks());
        source2.applyGuards(guards2, ts2.getClocks());

        // based on the zone, get the min and max value of the clocks
        int maxSource1 = source1.getMinUpperBound();
        int maxSource2 = source2.getMinUpperBound();
        int minSource1 = source1.getMinLowerBound();
        int minSource2 = source2.getMinLowerBound();

        // check that the zones are compatible
        if (maxSource1 >= minSource2 && maxSource2 >= minSource1) {
            // delay and apply invariants on target states

            target1.delay();
            target2.delay();
            target1.applyInvariants(ts1.getClocks());
            target2.applyInvariants(ts2.getClocks());

            // get the max value of the clocks
            int maxTarget1 = target1.getMinUpperBound();
            int maxTarget2 = target2.getMinUpperBound();
            int minTarget1 = target1.getMinLowerBound();
            int minTarget2 = target2.getMinLowerBound();

            // check again that the zones are compatible
            if (maxTarget1 >= minTarget2 && maxTarget2 >= minTarget1) {
                return new State[]{target1, target2};
            }
        }

        return null;
    }

    private boolean checkInputs(Set<Channel> actions, State state1, State state2, TransitionSystem sys1, TransitionSystem sys2) {
        for (Channel action : actions) {
            List<Transition> next2 = sys2.getNextTransitions(state2, action);
            if (!next2.isEmpty()) {
                List<Transition> next1 = sys1.getNextTransitions(state1, action);
                if (next1.isEmpty()) {
                    // we found an input in machine 2 that doesn't exist in machine 1, so refinement doesn't hold
                    return false;
                } else {
                    List<State[]> newStates = getNewStates(next1, next2);
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

    private boolean checkOutputs(Set<Channel> actions, State state1, State state2, TransitionSystem sys1, TransitionSystem sys2) {
        for (Channel action : actions) {
            List<Transition> next1 = sys1.getNextTransitions(state1, action);
            if (!next1.isEmpty()) {
                List<Transition> next2 = sys2.getNextTransitions(state2, action);
                if (next2.isEmpty()) {
                    // we found an output in machine 1 that doesn't exist in machine 2, so refinement doesn't hold
                    return false;
                } else {
                    List<State[]> newStates = getNewStates(next1, next2);
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

    private State copyState(State state) {
        return new State(state.getLocations(), state.getZone());
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
