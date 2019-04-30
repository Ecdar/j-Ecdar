package logic;

import models.*;

import java.util.*;
import java.util.stream.Collectors;

public class Refinement {
    private final TransitionSystem ts1, ts2;
    private final List<Clock> allClocks;
    private final Deque<StatePair> waiting;
    private final List<StatePair> passed;
    private final Set<Channel> inputs1, inputs2, outputs1, outputs2;
    private int constant;
    private int INF = 1073741823;

    public Refinement(TransitionSystem system1, TransitionSystem system2) {
        this.ts1 = system1;
        this.ts2 = system2;
        this.waiting = new ArrayDeque<>();
        this.passed = new ArrayList<>();

        allClocks = new ArrayList<>(ts1.getClocks());
        allClocks.addAll(ts2.getClocks());

        // the first states we look at are the initial ones
        waiting.push(getInitialStatePair());

        inputs1 = ts1.getInputs();
        inputs2 = ts2.getInputs();

        outputs1 = new HashSet<>(ts1.getOutputs());
        outputs1.addAll(ts1.getSyncs());

        outputs2 = new HashSet<>(ts2.getOutputs());
        outputs2.addAll(ts2.getSyncs());

        setConstant();
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

        if(!ts1.isConsistent())
            return false;

        if(!ts2.isConsistent())
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
            }
        }

        // if we got here it means refinement property holds
        return true;
    }

    private StatePair buildStatePair(Transition t1, Transition t2) {
        State target1 = new State(t1.getTarget().getLocation(), t1.getGuardZone());

        target1.applyGuards(t2.getGuards(), allClocks);

        if (!target1.getInvZone().isValid())
            return null;

        target1.applyResets(t1.getUpdates(), allClocks);
        target1.applyResets(t2.getUpdates(), allClocks);

        target1.getInvZone().delay();

        target1.applyInvariants(target1.getInvariants(), allClocks);
        Zone invariantTest = new Zone(target1.getInvZone());
        target1.applyInvariants(t2.getTarget().getInvariants(), allClocks);

        // Check if the invariant of the other side does not cut solutions and if so, report failure
        // This also happens to be a delay check
        Federation fed = Federation.dbmMinusDbm(invariantTest, target1.getInvZone());
        if(!fed.isEmpty())
            return null;

        if (!target1.getInvZone().isValid())
            return null;

        target1.extrapolateMaxBounds(constant);

        State target2 = new State(t2.getTarget().getLocation(), target1.getInvZone());

        return new StatePair(target1, target2);
    }

    private List<StatePair> createNewStatePairs(List<Transition> trans1, List<Transition> trans2) {
        List<StatePair> pairs = new ArrayList<>();

        List<Zone> gzLeft = trans1.stream().map(Transition::getGuardZone).collect(Collectors.toList());
        List<Zone> gzRight = trans2.stream().map(Transition::getGuardZone).collect(Collectors.toList());

        Federation fedL = new Federation(gzLeft);
        Federation fedR = new Federation(gzRight);

        // If trans2 does not satisfy all solution of trans2, return empty list which should result in refinement failure
        if(!Federation.fedMinusFed(fedL, fedR).isEmpty())
            return pairs;

        for (Transition transition1 : trans1) {
            for (Transition transition2 : trans2) {
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
            List<Transition> transitions1 = isInput ? ts2.getNextTransitions(state2, action, allClocks)
                    : ts1.getNextTransitions(state1, action, allClocks);

            if (!transitions1.isEmpty()) {

                List<Transition> transitions2;
                Set<Channel> toCheck = isInput ? inputs1 : outputs2;
                if (toCheck.contains(action)) {
                    transitions2 = isInput ? ts1.getNextTransitions(state1, action, allClocks)
                            : ts2.getNextTransitions(state2, action, allClocks);

                    if (transitions2.isEmpty())
                        return false;
                } else {
                    // if action is missing in TS1 (for inputs) or in TS2 (for outputs), add a self loop for that action
                    transitions2 = new ArrayList<>();
                    Transition loop = isInput ? new Transition(state1, state1.getInvZone()) :
                            new Transition(state2, state2.getInvZone());
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

    public StatePair getInitialStatePair(){
        State left = ts1.getInitialStateRef(allClocks, ts2.getInitialLocation().getInvariants());
        State right = ts2.getInitialStateRef(allClocks, ts1.getInitialLocation().getInvariants());

        return new StatePair(left, right);
    }

    public void setConstant(){
        constant =  ts1.getMaxConstant() > ts2.getMaxConstant() ? ts1.getMaxConstant() : ts2.getMaxConstant();
    }
}
