package logic;

import lib.DBMLib;
import models.Channel;
import models.Guard;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class Refinement {
    private TransitionSystem ts1, ts2;
    private Deque<StatePair> waiting;
    private List<StatePair> passed;
    private Set<Channel> inputs2, outputs1, syncs1, syncs2;

    public Refinement(TransitionSystem system1, TransitionSystem system2) {
        this.ts1 = system1;
        this.ts2 = system2;
        this.waiting = new ArrayDeque<>();
        this.passed = new ArrayList<>();
        // the first states we look at are the initial ones
        waiting.push(new StatePair(ts1.getInitialState(), ts2.getInitialState()));

        inputs2 = ts2.getInputs();
        outputs1 = ts1.getOutputs();
        syncs1 = ts1.getSyncs();
        syncs2 = ts2.getSyncs();

        String fileName = "src/" + System.mapLibraryName("DBM");
        File lib = new File(fileName);
        System.load(lib.getAbsolutePath());
    }

    public boolean check() {


        // keep looking at states from Waiting as long as it contains elements
        while (!waiting.isEmpty()) {
            StatePair curr = waiting.pop();

            // ignore if the zones are included in zones belonging to pairs of states that we already visited
            if (!passedContainsStatePair(curr)) {
                // need to make deep copy
                State newState1 = copyState(curr.getLeft());
                State newState2 = copyState(curr.getRight());
                // mark the pair of states as visited
                passed.add(new StatePair(newState1, newState2));

                // check that for every output in TS 1 there is a corresponding output in TS 2
                boolean holds1 = checkOutputs(curr.getLeft(), curr.getRight(), ts1, ts2);
                if (!holds1)
                    return false;

                // check that for every input in TS 2 there is a corresponding input in TS 1
                boolean holds2 = checkInputs(curr.getLeft(), curr.getRight(), ts1, ts2);
                if (!holds2)
                    return false;
            }
        }

        // if we got here it means refinement property holds
        return true;
    }


    // takes transitions of automata 1 and 2 and builds the states corresponding to all possible combinations between them
    private List<StatePair> getNewStates(List<Transition> next1, List<Transition> next2) {
        List<StatePair> states = new ArrayList<>();

        for (Transition t1 : next1) {
            for (Transition t2 : next2) {
                // get source and target states
                State source1 = copyState(t1.getSource());
                State source2 = copyState(t2.getSource());
                State target1 = copyState(t1.getTarget());
                State target2 = copyState(t2.getTarget());

                StatePair newState = buildStatePair(source1, source2, t1.getGuards(), t2.getGuards(), target1, target2);
                if (newState != null) {
                    states.add(newState);
                }
            }
        }

        return states;
    }

    private StatePair buildStatePair(State source1, State source2, List<Guard> guards1, List<Guard> guards2, State target1, State target2) {
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
                return new StatePair(target1, target2);
            }
        }

        return null;
    }

    private boolean checkInputs(State state1, State state2, TransitionSystem sys1, TransitionSystem sys2) {
        for (Channel action : inputs2) {
            List<Transition> next2 = getInternalTransitions(state2, action, sys2, false);
            if (!next2.isEmpty()) {
                List<Transition> next1 = getInternalTransitions(state1, action, sys1, true);
                if (next1.isEmpty()) {
                    // we found an input in automaton 2 that doesn't exist in automaton 1, so refinement doesn't hold
                    return false;
                } else {
                    List<StatePair> newStates = getNewStates(next1, next2);
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

    private boolean checkOutputs(State state1, State state2, TransitionSystem sys1, TransitionSystem sys2) {
        for (Channel action : outputs1) {
            List<Transition> next1 = getInternalTransitions(state1, action, sys1, true);
            if (!next1.isEmpty()) {
                List<Transition> next2 = getInternalTransitions(state2, action, sys2, false);
                if (next2.isEmpty()) {
                    // we found an output in automaton 1 that doesn't exist in automaton 2, so refinement doesn't hold
                    return false;
                } else {
                    List<StatePair> newStates = getNewStates(next1, next2);
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

    public List<Transition> getInternalTransitions(State state, Channel action, TransitionSystem ts, boolean isFirst){
        List<Transition> result = new ArrayList<>();

        List<Transition> tempTrans = new ArrayList<>();
        List<State> tempStates = new ArrayList<>(Arrays.asList(state));
        List<State> passedInternal = new ArrayList<>(Arrays.asList(state));

        boolean checkSyncs = true;

        while (checkSyncs) {

            for (State tempState : tempStates) {
                for (Channel sync : isFirst? syncs1 : syncs2) {
                    tempTrans.addAll(ts.getNextTransitions(tempState, sync));
                }
            }

            if(tempTrans.isEmpty()) checkSyncs = false;
            else {
                // Collect all states that are target of the given transitions
                tempStates = tempTrans.stream().map(Transition::getTarget).collect(Collectors.toList());
                tempTrans = new ArrayList<>();
                // Get all states that are in passed list
                List<State> toRemove = tempStates.stream().filter(s -> passedContainsState(s, passedInternal, ts)).collect(Collectors.toList());
                // Remove all states that are already in passed
                tempStates.removeAll(toRemove);

                passedInternal.addAll(tempStates);
            }
        }

        for (State passedState : passedInternal){
            result.addAll(ts.getNextTransitions(passedState, action));
        }

        return result;
    }

    private State copyState(State state) {
        return new State(state.getLocation(), state.getZone());
    }

    private boolean passedContainsStatePair(StatePair state) {
        // keep only states that have the same locations
        List<StatePair> passedCopy = new ArrayList<>(passed);
        passedCopy.removeIf(n -> !(n.getLeft().getLocation().equals(state.getLeft().getLocation())) ||
                !(n.getRight().getLocation().equals(state.getRight().getLocation())));

        for (StatePair passedState : passedCopy) {
            // check for zone inclusion
            if (DBMLib.dbm_isSubsetEq(state.getLeft().getZone(), passedState.getLeft().getZone(), ts1.getDbmSize()) &&
                    DBMLib.dbm_isSubsetEq(state.getRight().getZone(), passedState.getRight().getZone(), ts2.getDbmSize())) {
                return true;
            }
        }

        return false;
    }

    private boolean passedContainsState(State state, List<State> passed1, TransitionSystem ts) {
        // keep only states that have the same locations

        for (State passedState : passed1) {
            // check for zone inclusion
            if (state.getLocation().equals(passedState.getLocation()) &&
                    DBMLib.dbm_isSubsetEq(state.getZone(), passedState.getZone(), ts.getDbmSize())) {
                return true;
            }
        }

        return false;
    }
}
