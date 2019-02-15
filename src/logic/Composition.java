package logic;

import models.Channel;
import models.Location;
import models.Edge;

import java.util.*;
import java.util.stream.Collectors;

public class Composition extends TransitionSystem {
    private List<TransitionSystem> systems;
    private Set<Channel> inputs, outputs, syncs;

    public Composition(List<TransitionSystem> systems) {

        // call constructor of super class
        super();

        this.systems = systems;

        for (TransitionSystem ts : systems) {
            clocks.addAll(ts.getClocks());
        }
        dbmSize = clocks.size() + 1;

        // initialize inputs, outputs and syncs
        inputs = new HashSet<>();
        outputs = new HashSet<>();
        syncs = new HashSet<>();

        // to compute inputs, outputs and syncs of composed TS, analyse all pairs of TS's
        for (int i = 0; i < systems.size(); i++) {

            // initialize inputs and outputs of TS at index i
            Set<Channel> inputsOfI = new HashSet<>(systems.get(i).getInputs());
            Set<Channel> outputsOfI = new HashSet<>(systems.get(i).getOutputs());

            // add syncs of I to global sync list
            syncs.addAll(systems.get(i).getSyncs());

            for (int j = 0; j < systems.size(); j++) {
                if (i != j) {

                    // get inputs, outputs and syncs of TS at index j
                    Set<Channel> inputsOfJ = new HashSet<>(systems.get(j).getInputs());
                    Set<Channel> outputsOfJ = new HashSet<>(systems.get(j).getOutputs());
                    Set<Channel> syncsOfJ = new HashSet<>(systems.get(j).getSyncs());

                    // we need to fetch the outputs of I again, as they might have been modified in the process
                    Set<Channel> cleanOutputsOfI = new HashSet<>(systems.get(i).getOutputs());
                    // check if output actions overlap
                    Set<Channel> diff = setIntersection(cleanOutputsOfI, outputsOfJ);
                    if (!diff.isEmpty()) {
                        throw new IllegalArgumentException("The automata cannot be composed");
                    }

                    // we need to fetch the inputs of I again, as they might have been modified in the process
                    Set<Channel> cleanInputsOfI = new HashSet<>(systems.get(i).getInputs());
                    // if some inputs of one automaton overlap with the outputs of another one, add those to the global sync list
                    syncs.addAll(setIntersection(cleanInputsOfI, outputsOfJ));

                    // apply changes to inputs and outputs of TS at index i
                    inputsOfI.removeAll(outputsOfJ); inputsOfI.removeAll(syncsOfJ);
                    outputsOfI.removeAll(inputsOfJ); outputsOfI.removeAll(syncsOfJ);
                }
            }

            // add inputs and outputs to the global lists
            inputs.addAll(inputsOfI);
            outputs.addAll(outputsOfI);
        }
    }

    public Set<Channel> getInputs() {
        return inputs;
    }

    public Set<Channel> getOutputs() {
        return outputs;
    }

    public Set<Channel> getSyncs() { return syncs; }

    public SymbolicLocation getInitialLocation() {
        return getInitialLocation(systems);
    }

    // build a list of transitions from a given state and a signal
    public List<Transition> getNextTransitions(State currentState, Channel channel) {
        List<SymbolicLocation> locations = ((ComplexLocation) currentState.getLocation()).getLocations();

        // these will store the locations of the target states and the corresponding transitions
        List<Move> resultMoves = new ArrayList<>();

        if (outputs.contains(channel)) {
            // if the signal is an output, loop through the automata to find the one sending the output
            for (int i = 0; i < locations.size(); i++) {
                List<Move> moves = systems.get(i).getNextMoves(locations.get(i), channel);

                for (Move move : moves) {
                    // the new locations will contain the locations of the source state, but the location corresponding
                    // to the TS sending the output will be replaced by the location that can be reached following the output
                    List<SymbolicLocation> newLocations = new ArrayList<>(locations);
                    newLocations.set(i, move.getTarget());
                    Move newMove = new Move(currentState.getLocation(), new ComplexLocation(newLocations), move.getEdges());

                    resultMoves.add(newMove);
                }
            }
        } else if (inputs.contains(channel) || (syncs.contains(channel))) {
            boolean checkForInputs = checkForInputs(channel, locations);

            // for syncs, we have to check if that output is being sent by a automaton, otherwise we do not look at the
            // inputs in the other automata
            if (checkForInputs) {

                resultMoves = systems.get(0).getNextMoves(locations.get(0), channel);
                // used when there are no moves for some TS
                if (resultMoves.isEmpty())
                    resultMoves = new ArrayList<>(Collections.singletonList(new Move(locations.get(0), locations.get(0), new ArrayList<>())));

                for (int i = 1; i < locations.size(); i++) {
                    List<Move> moves = systems.get(i).getNextMoves(locations.get(i), channel);
                    if (moves.isEmpty())
                        moves = new ArrayList<>(Collections.singletonList(new Move(locations.get(i), locations.get(i), new ArrayList<>())));

                    resultMoves = moveProduct(resultMoves, moves, i == 1);
                }
            }
        }

        return createNewTransitions(currentState, resultMoves);
    }

    public List<Move> getNextMoves(SymbolicLocation symLocation, Channel channel) {
        if (outputs.contains(channel) || inputs.contains(channel) || syncs.contains(channel))
            return getNextMoves(symLocation, channel, systems);
        else
            return new ArrayList<>();
    }

    private boolean checkForInputs(Channel channel, List<SymbolicLocation> locations) {
        // assume we should check for inputs
        boolean check = true;

        // for syncs, we must make sure we have an output first
        if (syncs.contains(channel)) {
            // loop through all automata to find the one sending the output
            for (int i = 0; i < systems.size(); i++) {
                if (systems.get(i).getOutputs().contains(channel)) {
                    List<Move> moves = systems.get(i).getNextMoves(locations.get(i), channel);
                    if (moves.isEmpty()) {
                        // do not check for inputs if the state in the corresponding automaton does not send that output
                        check = false;
                        break;
                    }
                }
            }
        }

        return check;
    }

    private Set<Channel> setIntersection(Set<Channel> set1, Set<Channel> set2) {
        Set<Channel> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);

        return intersection;
    }
}