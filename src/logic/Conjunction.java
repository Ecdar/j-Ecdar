package logic;

import models.*;

import java.util.*;
import java.util.stream.Collectors;

public class Conjunction extends AggregatedTransitionSystem {
    private Set<Channel> inputs, outputs;

    public Conjunction(TransitionSystem... systems) {
        super(systems);

        inputs = new HashSet<>(systems[0].getInputs());
        outputs = new HashSet<>(systems[0].getOutputs());

        for (TransitionSystem system : systems) {
            inputs = intersect(inputs, system.getInputs());
            outputs = intersect(outputs, system.getOutputs());
        }
    }

    @Override
    public Set<Channel> getInputs() {
        return inputs;
    }

    @Override
    public Set<Channel> getOutputs() {
        return outputs;
    }

    @Override
    public String getName() {
        return getRootSystems()
                .stream()
                .map(TransitionSystem::getName)
                .collect(Collectors.joining(" && "));
    }

    @Override
    protected List<Move> computeResultMoves(List<SymbolicLocation> locations, Channel channel) {
        if (locations.size() != getRootSystems().size()) {
            throw new IllegalArgumentException("There must be exactly the same amount of locations as systems");
        }

        List<Move> resultMoves = new ArrayList<>();
        for (int i = 0; i < getRootSystems().size(); i++) {

            /* Here "i" is not only the index of the system,
             *   but the order of the locations must also follow
             *   the one of the composition meaning that the i'th
             *   location is also for the i'th system. */
            TransitionSystem system = getRootSystems().get(i);
            SymbolicLocation location = locations.get(i);

            List<Move> moves = system.getNextMoves(location, channel);

            if (i == 0) {
                resultMoves = moves;
            } else {
                // We need to pass the "resultMoves" as they might force another move with a single move.
                resultMoves = moveProduct(resultMoves, moves, i == 1, false);
            }
        }

        return resultMoves;
    }
}
