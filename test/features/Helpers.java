package features;

import logic.Refinement;
import logic.SimpleTransitionSystem;
import models.Automaton;

public class Helpers {
    public static Refinement selfRefinesSelf(Automaton automaton) {
        return simpleRefinesSimple(automaton, automaton);
    }

    public static Refinement simpleRefinesSimple(Automaton automaton1, Automaton automaton2) {
        return new Refinement(new SimpleTransitionSystem(automaton1), new SimpleTransitionSystem(automaton2));
    }
}
