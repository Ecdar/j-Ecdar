package logic;

import models.*;

import java.util.*;
import java.util.stream.Collectors;

public class Composition extends AggregatedTransitionSystem {
    private Set<Channel> inputs, outputs;

    public Composition(TransitionSystem... systems)
            throws IllegalArgumentException {
        super(systems);
        inputs = new HashSet<>();
        outputs = new HashSet<>();

        /* Given a set of specifications we have a composition with:
         * - The Locations is the cartesian product of all specifications
         * - The initial location is the tuple of all initials of all specifications
         * - The actions is the disjoint union of all input/output actions of the specifications
         * - The transition system is later defined.
         * For the composition to be created it must be that:
         * - The intersection of all specification pairs output actions must be the empty set */

        /* As this aggregate can have an arbitrary amount of transition system we use "Act^i"
         *   to denote the current state of the aggregate transition system. This can be seen e.g. with:
         *   "Act_o = Act_o^i U Act_o^j" which is equivalent to union(outputs, system_j.getOutputs())
         *    here "Act_o^i" is outputs which is the current set of outputs for the aggregate. */
        for (int i = 0; i < systems.length; i++) {
            for (int j = 0; j < systems.length; j++) {
                // We don't want to have a transition pair with itself.
                if (i == j) {
                    continue;
                }

                TransitionSystem system_i = systems[i];
                TransitionSystem system_j = systems[j];

                // Same output actions check
                Set<Channel> output_intersection = intersect(
                        system_i.getOutputs(),
                        system_j.getOutputs()
                );
                if (!output_intersection.isEmpty()) {
                    throw new IllegalArgumentException(
                            "The output actions of all pairs of specifications must have an empty intersection"
                    );
                }

                // Act_o = Act_o^i U Act_o^j
                outputs = union(
                        outputs, system_j.getOutputs()
                );

                // Act_i = (Act_i^i \ Act_o^j) U (Act_i^j \ Act_o^i)
                inputs = union(
                        difference(inputs, system_j.getOutputs()),
                        difference(system_j.getInputs(), outputs)
                );
            }
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
                .collect(Collectors.joining(" || "));
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

            /* By iterating through all systems and then getting the next moves
             *   for each system we get a set of all next moves for all systems. */
            List<Move> moves = system.getNextMoves(location, channel);

            /* Previously this check was done in "getNextTransitions" by invoking "checkForOutputs".
             *   But it recomputed the set of next moves in order to check whether every system
             *   as a move for the output action. This ensures that if the channel is an output
             *   the system does have a corresponding move. */
            if (system.getOutputs().contains(channel) &&
                    moves.isEmpty()) {
                return new ArrayList<>();
            }

            // If we don't have any moves away from the locations then stay
            if (moves.isEmpty()) {
                Move stay = new Move(location, location);
                moves.add(stay);
            }

            if (i == 0) {
                resultMoves = moves;
            } else {
                // We get the cartesian product of all the moves
                resultMoves = moveProduct(resultMoves, moves, i == 1, false);
            }
        }

        return resultMoves;
    }
}