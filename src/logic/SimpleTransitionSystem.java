package logic;

import models.Automaton;
import models.Channel;
import models.Location;
import models.Edge;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class SimpleTransitionSystem extends TransitionSystem {

    private Automaton automaton;

    public SimpleTransitionSystem(Automaton automaton) {
        super(new ArrayList<>(Arrays.asList(automaton)));
        this.automaton = automaton;
    }

    public Set<Channel> getInputs() {
        return automaton.getInputAct();
    }

    public Set<Channel> getOutputs() {
        return automaton.getOutputAct();
    }

    public List<Transition> getNextTransitions(State currentState, Channel channel) {
        List<Edge> edges = automaton.getTransitionsFromLocationAndSignal(currentState.getLocations().get(0), channel);

        List<List<Location>> locationsArr = edges.stream().map(transition -> new ArrayList<>(Arrays.asList(transition.getTarget()))).collect(Collectors.toList());
        List<List<Edge>> transitionsArr = edges.stream().map(transition -> new ArrayList<>(Arrays.asList(transition))).collect(Collectors.toList());

        return new ArrayList<>(addNewStateTransitions(currentState, locationsArr, transitionsArr));
    }
}
