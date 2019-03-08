package logic;

import models.Automaton;
import models.Channel;
import models.Edge;
import models.Location;

import java.util.*;
import java.util.stream.Collectors;

public class SimpleTransitionSystem extends TransitionSystem {

    private final Automaton automaton;
    private Deque<State> waiting;
    private List<State> passed;

    public SimpleTransitionSystem(Automaton automaton) {
        this.automaton = automaton;
        clocks.addAll(Arrays.asList(automaton.getClocks()));

        this.waiting = new ArrayDeque<>();
        this.passed = new ArrayList<>();
    }

    public Set<Channel> getInputs() {
        return automaton.getInputAct();
    }

    public Set<Channel> getOutputs() {
        return automaton.getOutputAct();
    }

    public SymbolicLocation getInitialLocation() {
        return new SimpleLocation(automaton.getInitLoc());
    }

    public List<TransitionSystem> getSystems() {
        return Collections.singletonList(this);
    }

    // Checks if automaton is deterministic
    public boolean isDeterministic() {
        Set<Channel> actions = getActions();

        waiting = new ArrayDeque<>();
        passed = new ArrayList<>();
        waiting.add(getInitialState());

        while (!waiting.isEmpty()) {
            State currState = new State(waiting.pop());
            passed.add(currState);

            for (Channel action : actions) {

                List<Transition> tempTrans = getNextTransitions(currState, action);

                if (checkMovesOverlap(tempTrans))
                    return false;

                List<State> toAdd = tempTrans.stream().map(Transition::getTarget).
                        filter(s -> !passedContainsState(s)).collect(Collectors.toList());

                waiting.addAll(toAdd);
            }
        }
        return true;
    }

    // Check if zones of moves for the same action overlap, that is if there is non determinism
    public boolean checkMovesOverlap(List<Transition> trans) {
        if (trans.size() >= 2) {
            for (int i = 0; i < trans.size(); i++) {
                for (int j = i + 1; j < trans.size(); j++) {
                    if (!trans.get(i).getTarget().getLocation().equals(trans.get(j).getTarget().getLocation())) {

                        State state1 = new State(trans.get(i).getSource());
                        State state2 = new State(trans.get(j).getSource());

                        state1.applyGuards(trans.get(i).getGuards(), clocks);
                        state2.applyGuards(trans.get(j).getGuards(), clocks);

                        if (state1.getZone().isValid() && state2.getZone().isValid()) {
                            return state1.getZone().zonesIntersect(state2.getZone());
                        }
                    }
                }
            }
        }
        return false;
    }

    public boolean isConsistent() {
        if (!isDeterministic())
            return false;
        passed = new ArrayList<>();
        Set<Channel> inputs = getInputs();
        Set<Channel> outputs = getOutputs();
        return checkConsistency(getInitialState(), inputs, outputs);
    }

    public boolean checkConsistency(State currState, Set<Channel> inputs, Set<Channel> outputs) {

        if(passedContainsState(currState))
            return true;

        passed.add(currState);

        // Check if the target of every outgoing input edge ensures independent progress
        for (Channel channel : inputs) {
            List<Transition> tempTrans = getNextTransitions(currState, channel);
            for (Transition ts : tempTrans) {
                boolean inputConsistent = checkConsistency(ts.getTarget(), inputs, outputs);
                if (!inputConsistent)
                    return false;
            }
        }

        // If Delay indefinitely -> Continue;
        if (currState.getZone().canDelayIndefinetly())
            return true;
        // Else if independent progress does not hold through delaying indefinitely,
        // we must check for being able to output and satisfy independent progress
        else {
            for (Channel channel : outputs) {

                List<Transition> tempTrans = getNextTransitions(currState, channel);
                if(tempTrans.size() == 0)
                    return false;

                for (Transition ts : tempTrans) {
                    boolean outputConsistent = checkConsistency(ts.getTarget(), inputs, outputs);
                    if(outputConsistent)
                        return true;
                }
            }
        }
        return false;
    }

    private int addStatesToWaitingList(Set<Channel> channels, State state) {
        int counter = 0;

        for (Channel channel : channels) {
            List<Transition> tempTrans = getNextTransitions(state, channel);

            List<State> toAdd = tempTrans.stream().map(Transition::getTarget).
                    filter(s -> !passedContainsState(s)).collect(Collectors.toList());

            waiting.addAll(toAdd);
            counter += toAdd.size();
        }

        return counter;
    }


    private boolean passedContainsState(State state) {
        for (State passedState : passed) {
            // check for zone inclusion
            if (state.getLocation().equals(passedState.getLocation()) &&
                    state.getZone().isSubset(passedState.getZone())) {
                return true;
            }
        }

        return false;
    }

    public List<Transition> getNextTransitions(State currentState, Channel channel) {
        List<Move> moves = getNextMoves(currentState.getLocation(), channel);

        return createNewTransitions(currentState, moves);
    }

    protected List<Move> getNextMoves(SymbolicLocation symLocation, Channel channel) {
        List<Move> moves = new ArrayList<>();

        Location location = ((SimpleLocation) symLocation).getActualLocation();
        List<Edge> edges = automaton.getEdgesFromLocationAndSignal(location, channel);

        for (Edge edge : edges) {
            SymbolicLocation target = new SimpleLocation(edge.getTarget());
            Move move = new Move(symLocation, target, Collections.singletonList(edge));
            moves.add(move);
        }

        return moves;
    }
}
