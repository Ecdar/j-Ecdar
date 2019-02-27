package logic;

import models.Channel;
import models.Edge;
import models.Guard;
import models.Update;

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
    }

    public boolean check() {
        int waitingAmount = 0;
        // keep looking at states from Waiting as long as it contains elements
        while (!waiting.isEmpty()) {
            if(waiting.size() > waitingAmount) waitingAmount = waiting.size();
            StatePair curr = waiting.pop();

            // ignore if the zones are included in zones belonging to pairs of states that we already visited
            if (!passedContainsStatePair(curr)) {
                State left = curr.getLeft();
                State right = curr.getRight();

                // need to make deep copy
                State newState1 = new State(left); State newState2 = new State(right);
                // mark the pair of states as visited
                passed.add(new StatePair(newState1, newState2));

                // check that for every output in TS 1 there is a corresponding output in TS 2
                boolean holds1 = checkOutputs(left, right);
                if (!holds1)
                    return false;

                // check that for every input in TS 2 there is a corresponding input in TS 1
                boolean holds2 = checkInputs(left, right);
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

                StatePair newState = buildStatePair(t1, t2);
                if (newState != null) {
                    states.add(newState);
                }
            }
        }

        return states;
    }

    private StatePair buildStatePair(Transition t1, Transition t2) {

        State source1 = new State(t1.getSource());
        State target1 = new State(t1.getTarget().getLocation(), new Zone(t1.getSource().getZone()));
        State source2 = new State(t2.getSource());
        State target2 = new State(t2.getTarget().getLocation(), new Zone(t2.getSource().getZone()));

        Zone absZone1 = source1.getZone().getAbsoluteZone(t1.getGuards(), ts1.getClocks());
        Zone absZone2 = source2.getZone().getAbsoluteZone(t1.getGuards(), ts1.getClocks());

        if(!absZone1.absoluteZonesIntersect(absZone2)) return null;

        target1.applyGuards(t1.getGuards(), ts1.getClocks());
        target2.applyGuards(t2.getGuards(), ts2.getClocks());

        // Update lower bounds with the most minimal delay one of the states has to take
        int rowMax1 = absZone1.getRawRowMax();
        int rowMax2 = absZone2.getRawRowMax();
        int rowMax = rowMax1 < rowMax2 ? rowMax1 : rowMax2;
        target1.getZone().updateLowerBounds(source1.getZone(), rowMax);
        target2.getZone().updateLowerBounds(source2.getZone(), rowMax);

        if(!target1.getZone().isValid() || !target2.getZone().isValid()) return null;

        target1.applyResets(t1.getUpdates(), ts1.getClocks());
        target2.applyResets(t2.getUpdates(), ts2.getClocks());

        target1.getZone().delay();
        target2.getZone().delay();

        target1.applyInvariants(ts1.getClocks());
        target2.applyInvariants(ts2.getClocks());

        if(!target1.getZone().isValid() || !target2.getZone().isValid()) return null;

        return new StatePair(target1, target2);

    }

    private boolean checkActions(boolean isInput, State state1, State state2) {
        for (Channel action : isInput ? inputs2 : outputs1) {
            List<Transition> next1 = isInput ? getInternalTransitions(state2, action, ts2, false) :
                    getInternalTransitions(state1, action, ts1, false);

            if (!next1.isEmpty()) {
                List<Transition> next2 = isInput ? getInternalTransitions(state1, action, ts1, true) :
                        getInternalTransitions(state2, action, ts2, true);

                // we found an input in TS 2 that doesn't exist in TS 1, so refinement doesn't hold
                if (next2.isEmpty())
                    return false;

                List<StatePair> newStates = isInput ? getNewStates(next2, next1): getNewStates(next1, next2);

                // if we don't get any new states, it means we found some incompatibility
                if (newStates.isEmpty())
                    return false;

                for (StatePair statePair : newStates) {
                    if(!passedContainsStatePair(statePair))
                    waiting.add(statePair);
                }
            }
        }

        return true;
    }

    private boolean checkInputs(State state1, State state2) {
        return checkActions(true, state1, state2);
    }

    private boolean checkOutputs(State state1, State state2) {
        return checkActions(false, state1, state2);
    }

    private List<Transition> getInternalTransitions(State state, Channel action, TransitionSystem ts, boolean isFirst) {
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
                List<State> toRemove = tempStates.stream().filter(s -> passedContainsState(s, passedInternal)).collect(Collectors.toList());
                // Remove all states that are already in passed
                tempStates.removeAll(toRemove);

                passedInternal.addAll(tempStates);
            }
        }

        for (State passedState : passedInternal) {
            result.addAll(ts.getNextTransitions(passedState, action));
        }

        return result;
    }

    private boolean passedContainsStatePair(StatePair state) {
        State currLeft = state.getLeft();
        State currRight = state.getRight();

        for (StatePair passedState : passed) {
            // check for zone inclusion
            State passedLeft = passedState.getLeft();
            State passedRight = passedState.getRight();

            if (passedLeft.getLocation().equals(currLeft.getLocation()) &&
                    passedRight.getLocation().equals(currRight.getLocation())) {
                if (currLeft.getZone().isSubset(passedLeft.getZone()) &&
                        currRight.getZone().isSubset(passedRight.getZone())) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean passedContainsState(State state, List<State> passed1) {
        for (State passedState : passed1) {
            // check for zone inclusion
            if (state.getLocation().equals(passedState.getLocation()) &&
                    state.getZone().isSubset(passedState.getZone())) {
                return true;
            }
        }

        return false;
    }
}
