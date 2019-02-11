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
        super(systems.stream().map(TransitionSystem::getMachines).flatMap(t -> t.stream()).collect(Collectors.toList()));
        this.systems = systems;
        inputs = new HashSet<>();
        outputs = new HashSet<>();
        syncs = new HashSet<>();

        for (int i = 0; i < systems.size(); i++) {
            Set<Channel> inputsOfI, outputsOfI, sync, outputsOfOthers, inputsOfOthers;
            inputsOfI = new HashSet<>(systems.get(i).getInputs());
            outputsOfI = new HashSet<>(systems.get(i).getOutputs());
            sync = new HashSet<>(systems.get(i).getOutputs());
            outputsOfOthers = new HashSet<>();
            inputsOfOthers = new HashSet<>();
            inputs.addAll(inputsOfI);
            outputs.addAll(outputsOfI);

            for (int j = 0; j < systems.size(); j++) {
                if (i != j) {
                    // check if output actions overlap
                    Set<Channel> diff = new HashSet<>(systems.get(i).getOutputs());
                    diff.retainAll(systems.get(j).getOutputs());
                    if (!diff.isEmpty()) {
                        throw new IllegalArgumentException("machines cannot be composed");
                    }

                    outputsOfOthers.addAll(systems.get(j).getOutputs());

                    inputsOfOthers.addAll(systems.get(j).getInputs());

                    Set<Channel> syncCopy = new HashSet<>(sync);
                    syncCopy.retainAll(systems.get(j).getInputs());
                    syncs.addAll(syncCopy);
                }
            }

            // set difference
            inputsOfI.removeAll(outputsOfOthers);
            outputsOfI.removeAll(inputsOfOthers);
        }
        outputs.removeAll(syncs);
        inputs.removeAll(syncs);
    }

    public Set<Channel> getInputs() {
        return inputs;
    }

    public Set<Channel> getOutputs() {
        return outputs;
    }

    // build a list of transitions from a given state and a signal
    public List<Transition> getNextTransitions(State currentState, Channel channel) {
        List<Location> locations = currentState.getLocations();
        // these will store the locations of the target states and the corresponding transitions
        List<List<Location>> locationsArr = new ArrayList<>();
        List<List<Edge>> transitionsArr = new ArrayList<>();

        if (outputs.contains(channel)) {
            // if the signal is an output, loop through the machines to find the one sending the output
            for (int i = 0; i < locations.size(); i++) {
                List<Edge> edges = systems.get(i).getTransitionsFromLocationAndSignal(locations.get(i), channel);

                for (Edge edge : edges) {
                    // the new locations will contain the locations of the source zone, but the location corresponding
                    // to the machine sending the output will be replaced by the location that can be reached following the output
                    List<Location> newLocations = new ArrayList<>(locations);
                    newLocations.set(i, edge.getTarget());

                    locationsArr.add(newLocations);
                    transitionsArr.add(new ArrayList<>(Arrays.asList(edge)));
                }
            }
        } else if (inputs.contains(channel) || (syncs.contains(channel))) {
            boolean checkForInputs = checkForInputs(channel, locations);

            // for syncs, we have to check if that output is being sent by a machine, otherwise we do not look at the
            // inputs in the other machines
            if (checkForInputs) {
                List<List<Location>> locationsList = new ArrayList<>();
                List<List<Edge>> transitionsList = new ArrayList<>();

                // loop through the machines to get the transitions from the corresponding location
                for (int i = 0; i < locations.size(); i++) {
                    List<Edge> transitionsForI = systems.get(i).getTransitionsFromLocationAndSignal(locations.get(i), channel);
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

        return new ArrayList<>(addNewStateTransitions(currentState, locationsArr, transitionsArr));
    }

    private boolean checkForInputs(Channel channel, List<Location> locations) {
        // assume we should check for inputs
        boolean check = true;

        // for syncs, we must make sure we have an output first
        if (syncs.contains(channel)) {
            // loop through all machines to find the one sending the output
            for (int i = 0; i < systems.size(); i++) {
                if (systems.get(i).getOutputs().contains(channel)) {
                    List<Edge> transitionsForI = systems.get(i).getTransitionsFromLocationAndSignal(locations.get(i), channel);
                    if (transitionsForI.isEmpty()) {
                        // do not check for inputs if the state in the corresponding machine does not send that output
                        check = false;
                        break;
                    }
                }
            }
        }

        return check;
    }
}