package logic;

import models.Automaton;
import models.Channel;
import models.Location;
import models.Edge;

import java.util.*;

public class SimpleTransitionSystem extends TransitionSystem {

    private final Automaton automaton;

    public SimpleTransitionSystem(Automaton automaton) {
        this.automaton = automaton;
        clocks.addAll(Arrays.asList(automaton.getClocks()));
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
