package logic;

import models.Channel;
import java.util.*;
import java.util.stream.Collectors;

public class Conjunction extends TransitionSystem {
    private final List<TransitionSystem> systems;

    public Conjunction(List<TransitionSystem> systems) {
        super();

        this.systems = systems;

        clocks.addAll(systems.stream().map(TransitionSystem::getClocks).flatMap(List::stream).collect(Collectors.toList()));

        dbmSize = clocks.size() + 1;
    }

    public Set<Channel> getInputs() {
        Set<Channel> inputs = new HashSet<>(systems.get(0).getInputs());

        for (int i = 1; i < systems.size(); i++) {
            inputs.retainAll(systems.get(i).getInputs());
        }

        return inputs;
    }

    public Set<Channel> getOutputs() {
        Set<Channel> outputs = new HashSet<>(systems.get(0).getOutputs());

        for (int i = 1; i < systems.size(); i++) {
            outputs.retainAll(systems.get(i).getOutputs());
        }

        return outputs;
    }

    public SymbolicLocation getInitialLocation() {
        return getInitialLocation(systems);
    }

    public List<Transition> getNextTransitions(State currentState, Channel channel) {
        List<SymbolicLocation> locations = ((ComplexLocation) currentState.getLocation()).getLocations();

        // these will store the locations of the target states and the corresponding transitions
        List<Move> resultMoves = computeResultMoves(locations, channel);
        if (resultMoves.isEmpty()) return new ArrayList<>();

        return createNewTransitions(currentState, resultMoves);
    }

    public List<Move> getNextMoves(SymbolicLocation symLocation, Channel channel) {
        List<SymbolicLocation> symLocs = ((ComplexLocation) symLocation).getLocations();

        List<Move> resultMoves = computeResultMoves(symLocs, channel);
        if (resultMoves.isEmpty()) return new ArrayList<>();

        // if there are no actual moves, then return empty list
        Move move = resultMoves.get(0);
        if (move.getSource().equals(move.getTarget())) {
            return new ArrayList<>();
        }

        return resultMoves;
    }

    private List<Move> computeResultMoves(List<SymbolicLocation> locations, Channel channel) {
        List<Move> resultMoves = systems.get(0).getNextMoves(locations.get(0), channel);
        // used when there are no moves for some TS
        if (resultMoves.isEmpty())
            return new ArrayList<>();

        for (int i = 1; i < systems.size(); i++) {
            List<Move> moves = systems.get(i).getNextMoves(locations.get(i), channel);

            if (moves.isEmpty())
                return new ArrayList<>();

            resultMoves = moveProduct(resultMoves, moves, i == 1);
        }

        return resultMoves;
    }
}
