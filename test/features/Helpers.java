package features;

import logic.Refinement;
import logic.SimpleTransitionSystem;
import models.Component;

public class Helpers {
    public static Refinement selfRefinesSelf(Component component) {
        return simpleRefinesSimple(component, component);
    }

    public static Refinement simpleRefinesSimple(Component component1, Component component2) {
        return new Refinement(new SimpleTransitionSystem(component1), new SimpleTransitionSystem(component2));
    }
}
