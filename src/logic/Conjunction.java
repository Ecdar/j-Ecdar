package logic;

import models.*;

import java.util.*;
import java.util.stream.Collectors;

public class Conjunction extends TransitionSystem {
    private final TransitionSystem[] systems;

    public Conjunction(TransitionSystem[] systems) {
        this.systems = systems;

        clocks.addAll(Arrays.stream(systems).map(TransitionSystem::getClocks).flatMap(List::stream).collect(Collectors.toList()));
    }

    public Set<Channel> getInputs() {
        Set<Channel> inputs = new HashSet<>(systems[0].getInputs());

        for (int i = 1; i < systems.length; i++) {
            inputs.retainAll(systems[i].getInputs());
        }

        return inputs;
    }

    public Set<Channel> getOutputs() {
        Set<Channel> outputs = new HashSet<>(systems[0].getOutputs());

        for (int i = 1; i < systems.length; i++) {
            outputs.retainAll(systems[i].getOutputs());
        }

        return outputs;
    }

    public List<SimpleTransitionSystem> getSystems(){
        List<SimpleTransitionSystem> result = new ArrayList<>();
        for(TransitionSystem ts : systems){
            result.addAll(ts.getSystems());
        }
        return result;
    }

    public SymbolicLocation getInitialLocation() {
        return getInitialLocation(systems);
    }

    public List<Transition> getNextTransitions(State currentState, Channel channel, List<Clock> allClocks) {
        List<SymbolicLocation> locations = ((ComplexLocation) currentState.getLocation()).getLocations();

        // these will store the locations of the target states and the corresponding transitions
        List<Move> resultMoves = computeResultMoves(locations, channel);
        if (resultMoves.isEmpty()) return new ArrayList<>();

        return createNewTransitions(currentState, resultMoves, allClocks);
    }

    public List<Move> getNextMoves(SymbolicLocation symLocation, Channel channel) {
        List<SymbolicLocation> symLocs = ((ComplexLocation) symLocation).getLocations();

        return computeResultMoves(symLocs, channel);
    }

    private List<Move> computeResultMoves(List<SymbolicLocation> locations, Channel channel) {
        List<Move> resultMoves = systems[0].getNextMoves(locations.get(0), channel);

        // used when there are no moves for some TS
        if (resultMoves.isEmpty())
            return new ArrayList<>();

        for (int i = 1; i < systems.length; i++) {
            List<Move> moves = systems[i].getNextMoves(locations.get(i), channel);

            if (moves.isEmpty())
                return new ArrayList<>();

            resultMoves = moveProduct(resultMoves, moves, i == 1);
        }

        return resultMoves;
    }
}
