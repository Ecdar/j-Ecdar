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
        super(systems.stream().map(TransitionSystem::getAutomata).flatMap(t -> t.stream()).collect(Collectors.toList()));

        this.systems = systems;

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

    // build a list of transitions from a given state and a signal
    public List<Transition> getNextTransitions(State currentState, Channel channel) {
        List<Location> locations = currentState.getLocations();
        // these will store the locations of the target states and the corresponding transitions
        List<List<Location>> locationsArr = new ArrayList<>();
        List<List<Edge>> transitionsArr = new ArrayList<>();

        if (outputs.contains(channel)) {
            // if the signal is an output, loop through the automata to find the one sending the output
            for (int i = 0; i < locations.size(); i++) {
                List<Edge> edges = systems.get(i).getEdgesFromLocationAndSignal(locations.get(i), channel);

                for (Edge edge : edges) {
                    // the new locations will contain the locations of the source zone, but the location corresponding
                    // to the automaton sending the output will be replaced by the location that can be reached following the output
                    List<Location> newLocations = new ArrayList<>(locations);
                    newLocations.set(i, edge.getTarget());

                    locationsArr.add(newLocations);
                    transitionsArr.add(new ArrayList<>(Arrays.asList(edge)));
                }
            }
        } else if (inputs.contains(channel) || (syncs.contains(channel))) {
            boolean checkForInputs = checkForInputs(channel, locations);

            // for syncs, we have to check if that output is being sent by a automaton, otherwise we do not look at the
            // inputs in the other automata
            if (checkForInputs) {
                List<List<Location>> locationsList = new ArrayList<>();
                List<List<Edge>> transitionsList = new ArrayList<>();

                // loop through the automata to get the transitions from the corresponding location
                for (int i = 0; i < locations.size(); i++) {
                    List<Edge> transitionsForI = systems.get(i).getEdgesFromLocationAndSignal(locations.get(i), channel);
                    if (transitionsForI.isEmpty()) {
                        // if there are no transitions, only add the current location to the list and an empty transition
                        List<Location> newLocations = new ArrayList<>();
                        newLocations.add(locations.get(i));
                        locationsList.add(newLocations);
                        List<Edge> newEdges = new ArrayList<>();
                        newEdges.add(null);
                        transitionsList.add(newEdges);
                    } else {
                        // otherwise, add all transitions and build the list of new locations by taking the target of each transition
                        List<Location> newLocations = transitionsForI.stream().map(Edge::getTarget).collect(Collectors.toList());
                        locationsList.add(newLocations);
                        transitionsList.add(transitionsForI);
                    }
                }
                // use the cartesian product to build all possible combinations between locations (same for transitions)
                locationsArr = cartesianProduct(locationsList);
                transitionsArr = cartesianProduct(transitionsList);
            }
        }

        return new ArrayList<>(createNewTransitions(currentState, locationsArr, transitionsArr));
    }

    private boolean checkForInputs(Channel channel, List<Location> locations) {
        // assume we should check for inputs
        boolean check = true;

        // for syncs, we must make sure we have an output first
        if (syncs.contains(channel)) {
            // loop through all automata to find the one sending the output
            for (int i = 0; i < systems.size(); i++) {
                if (systems.get(i).getOutputs().contains(channel)) {
                    List<Edge> transitionsForI = systems.get(i).getEdgesFromLocationAndSignal(locations.get(i), channel);
                    if (transitionsForI.isEmpty()) {
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