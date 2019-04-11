package logic;

import models.*;

import java.util.*;
import java.util.stream.Collectors;

public class Composition extends TransitionSystem {
    private final TransitionSystem[] systems;
    private final Set<Channel> inputs, outputs, syncs;

    public Composition(TransitionSystem[] systems) {
        this.systems = systems;

        clocks.addAll(Arrays.stream(systems).map(TransitionSystem::getClocks).flatMap(List::stream).collect(Collectors.toList()));

        // initialize inputs, outputs and syncs
        inputs = new HashSet<>();
        outputs = new HashSet<>();
        syncs = new HashSet<>();

        // to compute inputs, outputs and syncs of composed TS, analyse all pairs of TS's
        for (int i = 0; i < systems.length; i++) {

            // initialize inputs and outputs of TS at index i
            Set<Channel> inputsOfI = new HashSet<>(systems[i].getInputs());
            Set<Channel> outputsOfI = new HashSet<>(systems[i].getOutputs());

            // add syncs of I to global sync list
            syncs.addAll(systems[i].getSyncs());

            for (int j = 0; j < systems.length; j++) {
                if (i != j) {

                    // get inputs, outputs and syncs of TS at index j
                    Set<Channel> inputsOfJ = new HashSet<>(systems[j].getInputs());
                    Set<Channel> outputsOfJ = new HashSet<>(systems[j].getOutputs());
                    Set<Channel> syncsOfJ = new HashSet<>(systems[j].getSyncs());

                    // we need to fetch the outputs of I again, as they might have been modified in the process
                    Set<Channel> cleanOutputsOfI = new HashSet<>(systems[i].getOutputs());
                    // check if output actions overlap
                    Set<Channel> diff = setIntersection(cleanOutputsOfI, outputsOfJ);
                    if (!diff.isEmpty()) {
                        throw new IllegalArgumentException("The automata cannot be composed");
                    }

                    // we need to fetch the inputs of I again, as they might have been modified in the process
                    Set<Channel> cleanInputsOfI = new HashSet<>(systems[i].getInputs());
                    // if some inputs of one automaton overlap with the outputs of another one, add those to the global sync list
                    syncs.addAll(setIntersection(cleanInputsOfI, outputsOfJ));

                    // apply changes to inputs and outputs of TS at index i
                    inputsOfI.removeAll(outputsOfJ);
                    inputsOfI.removeAll(syncsOfJ);
                    outputsOfI.removeAll(inputsOfJ);
                    outputsOfI.removeAll(syncsOfJ);
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

    public Set<Channel> getSyncs() {
        return syncs;
    }

    public SymbolicLocation getInitialLocation() {
        return getInitialLocation(systems);
    }

    public List<TransitionSystem> getSystems(){
        List<TransitionSystem> result = new ArrayList<>();
        for(TransitionSystem ts : systems){
            result.addAll(ts.getSystems());
        }
        return result;
    }

    // build a list of transitions from a given state and a signal
    public List<Transition> getNextTransitions(State currentState, Channel channel) {
        List<SymbolicLocation> locations = ((ComplexLocation) currentState.getLocation()).getLocations();

        // these will store the locations of the target states and the corresponding transitions
        List<Move> resultMoves = new ArrayList<>();

        if (outputs.contains(channel)) {
            // if the signal is an output, loop through the TS's to find the one sending the output
            for (int i = 0; i < locations.size(); i++) {
                List<Move> moves = systems[i].getNextMoves(locations.get(i), channel);

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
            // for syncs, we have to check if that output is being sent by a TS, otherwise we do not look at the
            // inputs in the other TS's
            if (checkForInputs(channel, locations)) resultMoves = computeResultMoves(locations, channel);
        }
        return createNewTransitions(currentState, resultMoves);
    }

    public List<Move> getNextMoves(SymbolicLocation symLocation, Channel channel) {
        // Check if action belongs to this TS at all before proceeding
        if (!outputs.contains(channel) && !inputs.contains(channel) && !syncs.contains(channel))
            return new ArrayList<>();

        // If action is sync, then check if there is corresponding input and output in TS
        if (!checkForInputs(channel, ((ComplexLocation) symLocation).getLocations())) return new ArrayList<>();

        List<SymbolicLocation> symLocs = ((ComplexLocation) symLocation).getLocations();

        List<Move> resultMoves = computeResultMoves(symLocs, channel);

        // if there are no actual moves, then return empty list
        Move move = resultMoves.get(0);
        if (move.getSource().equals(move.getTarget())) {
            return new ArrayList<>();
        }

        return resultMoves;
    }

    private List<Move> computeResultMoves(List<SymbolicLocation> locations, Channel channel) {
        List<Move> resultMoves = systems[0].getNextMoves(locations.get(0), channel);
        // used when there are no moves for some TS
        if (resultMoves.isEmpty())
            resultMoves = new ArrayList<>(Collections.singletonList(new Move(locations.get(0), locations.get(0), new ArrayList<>())));

        for (int i = 1; i < systems.length; i++) {
            List<Move> moves = systems[i].getNextMoves(locations.get(i), channel);

            if (moves.isEmpty())
                moves = new ArrayList<>(Collections.singletonList(new Move(locations.get(i), locations.get(i), new ArrayList<>())));

            resultMoves = moveProduct(resultMoves, moves, i == 1);
        }

        return resultMoves;
    }

    private boolean checkForInputs(Channel channel, List<SymbolicLocation> locations) {
        // assume we should check for inputs
        boolean checkOutput = true;
        boolean checkInput = false;

        // for syncs, we must make sure we have an output first
        if (syncs.contains(channel)) {
            // loop through all automata to find the one sending the output
            for (int i = 0; i < systems.length; i++) {
                if (systems[i].getOutputs().contains(channel)) {
                    List<Move> moves = systems[i].getNextMoves(locations.get(i), channel);
                    if (moves.isEmpty()) {
                        // do not check for inputs if the state in the corresponding automaton does not send that output
                        checkOutput = false;
                        break;
                    }
                } else if (systems[i].getInputs().contains(channel)) {
                    List<Move> moves = systems[i].getNextMoves(locations.get(i), channel);
                    // do not check for inputs if the state in the corresponding automaton does not send that input
                    if (!moves.isEmpty())
                        checkInput = true;
                }
            }
        } else checkInput = true;

        return checkOutput && checkInput;
    }

    private Set<Channel> setIntersection(Set<Channel> set1, Set<Channel> set2) {
        Set<Channel> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);

        return intersection;
    }
}