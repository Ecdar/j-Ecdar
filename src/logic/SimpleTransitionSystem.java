package logic;

import models.Channel;
import models.Component;
import models.Location;
import models.Transition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class SimpleTransitionSystem extends TransitionSystem {

    private Component component;

    public SimpleTransitionSystem(Component component) {
        super(new ArrayList<>(Arrays.asList(component)));
        this.component = component;
    }

    public Set<Channel> getInputs() {
        return component.getInputAct();
    }

    public Set<Channel> getOutputs() {
        return component.getOutputAct();
    }

    public List<StateTransition> getNextTransitions(State currentState, Channel channel) {
        List<Transition> transitions = component.getTransitionsFromLocationAndSignal(currentState.getLocations().get(0), channel);

        List<List<Location>> locationsArr = transitions.stream().map(transition -> new ArrayList<>(Arrays.asList(transition.getTarget()))).collect(Collectors.toList());
        List<List<Transition>> transitionsArr = transitions.stream().map(transition -> new ArrayList<>(Arrays.asList(transition))).collect(Collectors.toList());

        return new ArrayList<>(addNewStateTransitions(currentState, locationsArr, transitionsArr));
    }
}
