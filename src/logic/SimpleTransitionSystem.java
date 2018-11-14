package logic;

import models.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

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

		public State getInitialState() {
				Location init = component.getInitLoc();
				// size of DBM is clock count + 1 (x0 is reference clock)
				int[] dbm = initializeDBM();
				// delay
				dbm = delay(dbm);
				// apply invariant
				if (init.getInvariant() != null)
						dbm = applyInvariantsOrGuards(dbm, new ArrayList<>(Arrays.asList(init.getInvariant())));
				return new State(new ArrayList<>(Arrays.asList(component.getInitLoc())), dbm);
		}

		public ArrayList<State> getNextStates(State currentState, Channel channel) {
				ArrayList<State> states = new ArrayList<>();
				ArrayList<Transition> transitions = component.getTransitionsFromLocationAndSignal(currentState.getLocations().get(0), channel);
				for (Transition transition : transitions) {
						Location newLocation = transition.getTo();
						int[] dbm = currentState.getZone();
						// apply guards
						dbm = applyInvariantsOrGuards(dbm, transition.getGuards());
						// apply resets
						dbm = applyResets(dbm, transition.getUpdates());
						// delay
						dbm = delay(dbm);
						// apply invariant
						if (newLocation.getInvariant() != null)
								dbm = applyInvariantsOrGuards(dbm, new ArrayList<>(Arrays.asList(newLocation.getInvariant())));
						State newState = new State(new ArrayList<>(Arrays.asList(newLocation)), dbm);
						states.add(newState);
				}
				return states;
		}
}
