package logic;

import lib.DBMLib;
import models.*;

import java.util.*;

public class Refinement {
    private final TransitionSystem ts1, ts2;
    private final Deque<StatePair> waiting;
    private final List<StatePair> passed;
    private final Set<Channel> inputs1, inputs2, outputs1, outputs2;
    private boolean ref;
    private int INF = 1073741823;

    public Refinement(TransitionSystem system1, TransitionSystem system2) {
        this.ts1 = system1;
        this.ts2 = system2;
        this.waiting = new ArrayDeque<>();
        this.passed = new ArrayList<>();
        this.ref = true;

        // the first states we look at are the initial ones
        waiting.push(new StatePair(ts1.getInitialState(), ts2.getInitialState()));

        inputs1 = ts1.getInputs();
        inputs2 = ts2.getInputs();

        outputs1 = new HashSet<>(ts1.getOutputs());
        outputs1.addAll(ts1.getSyncs());

        outputs2 = new HashSet<>(ts2.getOutputs());
        outputs2.addAll(ts2.getSyncs());
    }

    public boolean check() {
        // signature check, precondition of refinement: inputs and outputs must be the same on both sides,
        // with the exception that the left side is allowed to have more outputs

        // inputs on the left must be equal to inputs on the right side
        if (!inputs1.equals(inputs2))
            return false;
        // the left side must contain all outputs from the right side
        if (!outputs1.containsAll(outputs2))
            return false;

        // Temp waiting list peak counter
        int waitingAmount = 0;
        // keep looking at states from Waiting as long as it contains elements

        if(!ts1.isConsistent() || !ts2.isConsistent())
            return false;

        while (!waiting.isEmpty()) {
            if (waiting.size() > waitingAmount) waitingAmount = waiting.size();

            StatePair curr = waiting.pop();

            // ignore if the zones are included in zones belonging to pairs of states that we already visited
            if (!passedContainsStatePair(curr)) {
                State left = curr.getLeft();
                State right = curr.getRight();

                // need to make deep copy
                State newState1 = new State(left);
                State newState2 = new State(right);
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

                // check if TS 2 can delay at least as much as TS 1
                if (!(left.getInvZone().getMaxRawDelay() <= right.getInvZone().getMaxRawDelay()))
                    return false;
            }

            if (!ref) return false;
        }

        // if we got here it means refinement property holds
        return true;
    }

    private StatePair buildStatePair(Transition t1, Transition t2) {
        State source1 = new State(t1.getSource());
        State target1 = new State(t1.getTarget().getLocation(), t1.getSource().getInvZone(), t1.getTarget().getArrivalZone(), t1.getTarget().getDSum());
        State source2 = new State(t2.getSource());
        State target2 = new State(t2.getTarget().getLocation(), t2.getSource().getInvZone(), t2.getTarget().getArrivalZone(), t2.getTarget().getDSum());

        Zone absZone1 = source1.getInvZone().getAbsoluteZone(t1.getGuards(), ts1.getClocks());
        Zone absZone2 = source2.getInvZone().getAbsoluteZone(t2.getGuards(), ts2.getClocks());

        if (!absZone1.absoluteZonesIntersect(absZone2))
            return null;

        target1.applyGuards(t1.getGuards(), ts1.getClocks());
        target2.applyGuards(t2.getGuards(), ts2.getClocks());

        Zone guardZone1 = target1.getInvZone();
        Zone guardZone2 = target2.getInvZone();

        Zone arrZone1 = source1.getArrivalZone();
        Zone arrZone2 = source2.getArrivalZone();

        int gMin1 = Math.abs(DBMLib.raw2bound(guardZone1.getRawRowMax()));
        int gMax1 = DBMLib.raw2bound(guardZone1.getRawColumnMin());

        int gMin2 = Math.abs(DBMLib.raw2bound(guardZone2.getRawRowMax()));
        int gMax2 = DBMLib.raw2bound(guardZone2.getRawColumnMin());

        int aMin1 = Math.abs(DBMLib.raw2bound(arrZone1.getRawRowMax()));
        int aMax1 = DBMLib.raw2bound(arrZone1.getRawColumnMin());

        int aMin2 = Math.abs(DBMLib.raw2bound(arrZone2.getRawRowMax()));
        int aMax2 = DBMLib.raw2bound(arrZone2.getRawColumnMin());

        // arrival zone on the left is [0, 0]
        if (aMin1 == 0 && aMax1 == 0) {
            if (source1.getDSum() == INF || gMax1 == INF)
                target1.setDSum(INF);
            else
                target1.setDSum(source1.getDSum() + gMax1);
        } else {
            target1.setDSum(gMax1);
        }

        if (aMin2 == 0 && aMax2 == 0) {
            if (source2.getDSum() == INF || gMax2 == INF)
                target2.setDSum(INF);
            else
                target2.setDSum(source2.getDSum() + gMax2);
        } else {
            target2.setDSum(gMax2);
        }

        // if we have self loop, skip the checks
        if (!t1.getEdges().isEmpty() && !t2.getEdges().isEmpty()) {

                // if left side can delay more than right side
                if (target1.getDSum() > target2.getDSum()) {
                    ref = false;
                    return null;
                }

            // if we have resets, min and max of arrival zone will both be 0
            if ((aMin1 == 0 && aMax1 == 0) || (aMin2 == 0 && aMax2 == 0)) {
                if (!(gMin1 == gMin2 && gMax1 == gMax2 && aMin1 == aMin2 && aMax1 == aMax2)) {
                    // for left side
                    int lfmin1 = valueDiff(gMin1, aMax1);
                    int lfmax1 = valueDiff(gMax1, aMin1);

                    // for right side
                    int lfmin2 = valueDiff(gMin2, aMin2);
                    int lfMax2 = valueDiff(gMax2, aMax2);

                    if (!(lfmin1 >= lfmin2) || !(lfmax1 <= lfMax2)) {
                        ref = false;
                        return null;
                    }
                }
            }
        }

        // Update lower bounds with the most minimal delay one of the states has to take
        int rowMax1 = absZone1.getRawRowMax();
        int rowMax2 = absZone2.getRawRowMax();
        int rowMax = rowMax1 < rowMax2 ? rowMax1 : rowMax2;
        target1.getInvZone().updateLowerBounds(source1.getInvZone(), rowMax);
        target2.getInvZone().updateLowerBounds(source2.getInvZone(), rowMax);

        if (!target1.getInvZone().isValid() || !target2.getInvZone().isValid())
            return null;

        target1.applyResets(t1.getUpdates(), ts1.getClocks());
        target2.applyResets(t2.getUpdates(), ts2.getClocks());

        // no edges means it's most likely a self loop so we don't need to update arrival zone
        if (!t1.getEdges().isEmpty()) target1.setArrivalZone(target1.getInvZone());
        if (!t2.getEdges().isEmpty()) target2.setArrivalZone(target2.getInvZone());

        target1.getInvZone().delay();
        target2.getInvZone().delay();

        target1.applyInvariants(ts1.getClocks());
        target2.applyInvariants(ts2.getClocks());

        if (!target1.getInvZone().isValid() || !target2.getInvZone().isValid())
            return null;

        return new StatePair(target1, target2);
    }

    private List<StatePair> createNewStatePairs(List<Transition> transitions1, List<Transition> transitions2) {
        List<StatePair> pairs = new ArrayList<>();

        for (Transition transition1 : transitions1) {
            for (Transition transition2 : transitions2) {
                StatePair pair = buildStatePair(transition1, transition2);
                if (pair != null)
                    pairs.add(pair);
            }
        }

        return pairs;
    }

    private boolean checkInputs(State state1, State state2) {
        return checkActions(state1, state2, true);
    }

    private boolean checkOutputs(State state1, State state2) {
        return checkActions(state1, state2, false);
    }

    private boolean checkActions(State state1, State state2, boolean isInput) {
        for (Channel action : (isInput ? inputs2 : outputs1)) {
            List<Transition> transitions1 = isInput ? ts2.getNextTransitions(state2, action) : ts1.getNextTransitions(state1, action);

            if (!transitions1.isEmpty()) {

                List<Transition> transitions2;
                Set<Channel> toCheck = isInput ? inputs1 : outputs2;
                if (toCheck.contains(action)) {
                    transitions2 = isInput ? ts1.getNextTransitions(state1, action) : ts2.getNextTransitions(state2, action);

                    if (transitions2.isEmpty())
                        return false;
                } else {
                    // if action is missing in TS1 (for inputs) or in TS2 (for outputs), add a self loop for that action
                    transitions2 = new ArrayList<>();
                    Transition loop = isInput ? new Transition(state1, state1.getInvZone().getSize()) :
                            new Transition(state2, state2.getInvZone().getSize());
                    transitions2.add(loop);
                }

                List<StatePair> pairs = isInput ? createNewStatePairs(transitions2, transitions1) : createNewStatePairs(transitions1, transitions2);
                if (pairs.isEmpty())
                    return false;
                waiting.addAll(pairs);
            }
        }

        return true;
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
                if (currLeft.getInvZone().isSubset(passedLeft.getInvZone()) &&
                        currRight.getInvZone().isSubset(passedRight.getInvZone())) {
                    return true;
                }
            }
        }

        return false;
    }

    // difference between two raw values (converted)
    private int valueDiff(int v1, int v2) {
        if (v1 == INF) return INF;

        if (v2 == INF) return 0;

        return v1 - v2;
    }
}
