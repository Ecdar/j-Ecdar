package logic;

import global.LibLoader;
import lib.DBMLib;
import models.Channel;
import models.Guard;

import java.util.*;
import java.util.stream.Collectors;

public class Refinement {
    private final TransitionSystem ts1, ts2;
    private final Deque<StatePair> waiting;
    private final List<StatePair> passed;
    private final Set<Channel> inputs2, outputs1, syncs1, syncs2;

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

        LibLoader.load();
    }

    public boolean check() {
        // keep looking at states from Waiting as long as it contains elements
        while (!waiting.isEmpty()) {
            StatePair curr = waiting.pop();

            // ignore if the zones are included in zones belonging to pairs of states that we already visited
            if (!passedContainsStatePair(curr)) {
                // need to make deep copy
                State newState1 = copyState(curr.getLeft()); State newState2 = copyState(curr.getRight());
                // mark the pair of states as visited
                passed.add(new StatePair(newState1, newState2));

                // check that for every output in TS 1 there is a corresponding output in TS 2
                boolean holds1 = checkOutputs(curr.getLeft(), curr.getRight(), ts1, ts2);
                if (!holds1) return false;

                // check that for every input in TS 2 there is a corresponding input in TS 1
                boolean holds2 = checkInputs(curr.getLeft(), curr.getRight(), ts1, ts2);
                if (!holds2) return false;
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
                State source1 = copyState(t1.getSource()); State source2 = copyState(t2.getSource());
                State target1 = copyState(t1.getTarget()); State target2 = copyState(t2.getTarget());

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
        int maxSource1 = source1.getMinUpperBound(); int maxSource2 = source2.getMinUpperBound();
        int minSource1 = source1.getMinLowerBound(); int minSource2 = source2.getMinLowerBound();

        // check that the zones are compatible
        if (maxSource1 >= minSource2 && maxSource2 >= minSource1) {
            // delay and apply invariants on target states
            target1.delay(); target2.delay();
            target1.applyInvariants(ts1.getClocks()); target2.applyInvariants(ts2.getClocks());

            // get the max value of the clocks
            int maxTarget1 = target1.getMinUpperBound(); int maxTarget2 = target2.getMinUpperBound();
            int minTarget1 = target1.getMinLowerBound(); int minTarget2 = target2.getMinLowerBound();

            // check again that the zones are compatible
            if (maxTarget1 >= minTarget2 && maxTarget2 >= minTarget1) {
                return new StatePair(target1, target2);
            }
        }

        return null;
    }

    private boolean checkActions(boolean isInput, State state1, State state2, TransitionSystem sys1, TransitionSystem sys2) {
        for (Channel action : isInput ? inputs2 : outputs1) {
            List<Transition> next1 = isInput ? getInternalTransitions(state2, action, sys2, false) :
                    getInternalTransitions(state1, action, sys1, false);

            if (!next1.isEmpty()) {
                List<Transition> next2 = isInput ? getInternalTransitions(state1, action, sys1, true) :
                        getInternalTransitions(state2, action, sys2, true);

                // we found an input in TS 2 that doesn't exist in TS 1, so refinement doesn't hold
                if (next2.isEmpty()) return false;

                List<StatePair> newStates = isInput ? getNewStates(next2, next1): getNewStates(next1, next2);

                // if we don't get any new states, it means we found some incompatibility
                if (newStates.isEmpty()) return false;

                waiting.addAll(newStates);
            }
        }

        return true;
    }

    private boolean checkInputs(State state1, State state2, TransitionSystem sys1, TransitionSystem sys2) {
        return checkActions(true, state1, state2, sys1, sys2);
    }

    private boolean checkOutputs(State state1, State state2, TransitionSystem sys1, TransitionSystem sys2) {
        return checkActions(false, state1, state2, sys1, sys2);
    }

    private List<Transition> getInternalTransitions(State state, Channel action, TransitionSystem ts, boolean isFirst){
        List<Transition> result = new ArrayList<>();

        List<Transition> tempTrans = new ArrayList<>();
        List<State> tempStates = new ArrayList<>(Collections.singletonList(state));
        List<State> passedInternal = new ArrayList<>(Collections.singletonList(state));

        boolean checkSyncs = true;

        while (checkSyncs) {

            for (State tempState : tempStates) {
                for (Channel sync : isFirst? syncs1 : syncs2) {
                    tempTrans.addAll(ts.getNextTransitions(tempState, sync));
                }
            }

            if (tempTrans.isEmpty()) checkSyncs = false;
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
        for (StatePair passedState : passed) {
            // check for zone inclusion
            if (passedState.getLeft().getLocation().equals(state.getLeft().getLocation()) &&
                    passedState.getRight().getLocation().equals(state.getRight().getLocation())) {
                if (DBMLib.dbm_isSubsetEq(state.getLeft().getZone(), passedState.getLeft().getZone(), ts1.getDbmSize()) &&
                        DBMLib.dbm_isSubsetEq(state.getRight().getZone(), passedState.getRight().getZone(), ts2.getDbmSize())) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean passedContainsState(State state, List<State> passed1, TransitionSystem ts) {
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
