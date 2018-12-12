package logic;

import models.Channel;
import models.Component;
import models.Location;
import models.Transition;

import java.util.*;
import java.util.stream.Collectors;

public class ComposedTransitionSystem extends TransitionSystem {
    private List<Component> machines;
    private Set<Channel> inputs, outputs, syncs;

    public ComposedTransitionSystem(List<Component> machines) {
        super(machines);
        this.machines = machines;
        inputs = new HashSet<>();
        outputs = new HashSet<>();
        syncs = new HashSet<>();

        for (int i = 0; i < machines.size(); i++) {
            Set<Channel> inputsOfI, outputsOfI, sync, outputsOfOthers, inputsOfOthers;
            inputsOfI = new HashSet<>(machines.get(i).getInputAct());
            outputsOfI = new HashSet<>(machines.get(i).getOutputAct());
            sync = new HashSet<>(machines.get(i).getOutputAct());
            outputsOfOthers = new HashSet<>();
            inputsOfOthers = new HashSet<>();
            inputs.addAll(inputsOfI);
            outputs.addAll(outputsOfI);

            for (int j = 0; j < machines.size(); j++) {
                if (i != j) {
                    // check if output actions overlap
                    Set<Channel> diff = new HashSet<>(machines.get(i).getOutputAct());
                    diff.retainAll(machines.get(j).getOutputAct());
                    if (!diff.isEmpty()) {
                        throw new IllegalArgumentException("machines cannot be composed");
                    }

                    outputsOfOthers.addAll(machines.get(j).getOutputAct());

                    inputsOfOthers.addAll(machines.get(j).getInputAct());

                    Set<Channel> syncCopy = new HashSet<>(sync);
                    syncCopy.retainAll(machines.get(j).getInputAct());
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
    public List<StateTransition> getNextTransitions(State currentState, Channel channel) {
        List<Location> locations = currentState.getLocations();
        // these will store the locations of the target states and the corresponding transitions
        List<List<Location>> locationsArr = new ArrayList<>();
        List<List<Transition>> transitionsArr = new ArrayList<>();

        if (outputs.contains(channel)) {
            // if the signal is an output, loop through the machines to find the one sending the output
            for (int i = 0; i < locations.size(); i++) {
                List<Transition> transitions = machines.get(i).getTransitionsFromLocationAndSignal(locations.get(i), channel);

                for (Transition transition : transitions) {
                    // the new locations will contain the locations of the source zone, but the location corresponding
                    // to the machine sending the output will be replaced by the location that can be reached following the output
                    List<Location> newLocations = new ArrayList<>(locations);
                    newLocations.set(i, transition.getTarget());

                    locationsArr.add(newLocations);
                    transitionsArr.add(new ArrayList<>(Arrays.asList(transition)));
                }
            }
        } else if (inputs.contains(channel) || (syncs.contains(channel))) {
            boolean checkForInputs = checkForInputs(channel, locations);

            // for syncs, we have to check if that output is being sent by a machine, otherwise we do not look at the
            // inputs in the other machines
            if (checkForInputs) {
                List<List<Location>> locationsList = new ArrayList<>();
                List<List<Transition>> transitionsList = new ArrayList<>();

                // loop through the machines to get the transitions from the corresponding location
                for (int i = 0; i < locations.size(); i++) {
                    List<Transition> transitionsForI = machines.get(i).getTransitionsFromLocationAndSignal(locations.get(i), channel);
                    if (transitionsForI.isEmpty()) {
                        // if there are no transitions, only add the current location to the list and an empty transition
                        List<Location> newLocations = new ArrayList<>();
                        newLocations.add(locations.get(i));
                        locationsList.add(newLocations);
                        List<Transition> newTransitions = new ArrayList<>();
                        newTransitions.add(null);
                        transitionsList.add(newTransitions);
                    } else {
                        // otherwise, add all transitions and build the list of new locations by taking the target of each transition
                        List<Location> newLocations = transitionsForI.stream().map(Transition::getTarget).collect(Collectors.toList());
                        locationsList.add(newLocations);
                        transitionsList.add(transitionsForI);
                    }
                }
                // use the cartesian product to build all possible combinations between locations (same for transitions
                locationsArr = cartesianProduct(locationsList);
                transitionsArr = cartesianProduct(transitionsList);
            }
        }

        return new ArrayList<>(addNewStateTransitions(currentState, locationsArr, transitionsArr));
    }

    // function that takes an arbitrary number of lists and recursively calculates their cartesian product
    private <T> List<List<T>> cartesianProduct(List<List<T>> lists) {
        List<List<T>> resultLists = new ArrayList<>();
        if (lists.size() == 0) {
            // base case; return a list containing one empty list
            resultLists.add(new ArrayList<>());
        } else {
            // take head of list
            List<T> firstList = lists.get(0);
            // apply function to tail of list
            List<List<T>> remainingLists = cartesianProduct(lists.subList(1, lists.size()));
            // combine each element of the first list with each of the remaining lists
            for (T condition : firstList) {
                for (List<T> remainingList : remainingLists) {
                    List<T> resultList = new ArrayList<>();
                    resultList.add(condition);
                    resultList.addAll(remainingList);
                    resultLists.add(resultList);
                }
            }
        }
        return resultLists;
    }

    private boolean checkForInputs(Channel channel, List<Location> locations) {
        // assume we should check for inputs
        boolean check = true;

        // for syncs, we must make sure we have an output first
        if (syncs.contains(channel)) {
            // loop through all machines to find the one sending the output
            for (int i = 0; i < machines.size(); i++) {
                if (machines.get(i).getOutputAct().contains(channel)) {
                    List<Transition> transitionsForI = machines.get(i).getTransitionsFromLocationAndSignal(locations.get(i), channel);
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