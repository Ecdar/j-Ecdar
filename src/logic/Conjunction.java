package logic;

import models.Channel;
import models.Location;
import models.Edge;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Conjunction extends TransitionSystem {
    List<TransitionSystem> systems;

    public Conjunction(List<TransitionSystem> systems) {
        super(systems.stream().map(TransitionSystem::getMachines).flatMap(t -> t.stream()).collect(Collectors.toList()));
        this.systems = systems;
    }

    public Set<Channel> getInputs() {
        Set<Channel> inputs = new HashSet<>();
        inputs.addAll(systems.get(0).getInputs());

        for (int i = 1; i < systems.size(); i++) {
            inputs.retainAll(systems.get(i).getInputs());
        }

        return inputs;
    }

    public Set<Channel> getOutputs() {
        Set<Channel> outputs = new HashSet<>();
        outputs.addAll(systems.get(0).getOutputs());

        for (int i = 1; i < systems.size(); i++) {
            outputs.retainAll(systems.get(i).getOutputs());
        }

        return outputs;
    }

    public List<Transition> getNextTransitions(State currentState, Channel channel) {
        List<Location> locations = currentState.getLocations();

        List<List<Location>> locationsList = new ArrayList<>();
        List<List<Edge>> transitionsList = new ArrayList<>();

        for (int i = 0; i < systems.size(); i++) {
            List<Edge> transitionsForI = systems.get(i).getTransitionsFromLocationAndSignal(locations.get(i), channel);
            if (transitionsForI.isEmpty()) {
                // no transitions are possible from this state
                return new ArrayList<>();
            } else {
                // otherwise, add all transitions and build the list of new locations by taking the target of each transition
                List<Location> newLocations = transitionsForI.stream().map(Edge::getTarget).collect(Collectors.toList());
                locationsList.add(newLocations);
                transitionsList.add(transitionsForI);
            }
        }

        // use the cartesian product to build all possible combinations between locations (same for transitions)
        List<List<Location>> locationsArr = cartesianProduct(locationsList);
        List<List<Edge>> transitionsArr = cartesianProduct(transitionsList);

        return new ArrayList<>(addNewStateTransitions(currentState, locationsArr, transitionsArr));
    }
}
