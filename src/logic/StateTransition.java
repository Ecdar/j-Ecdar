package logic;

import models.Guard;
import models.Transition;
import java.util.ArrayList;
import java.util.List;

public class StateTransition {
		private State source, target;
		private List<Transition> transitions;

		public StateTransition(State source, State target, List<Transition> transitions) {
				this.source = source;
				this.target = target;
				this.transitions = transitions;
		}

		public State getSource() {
				return source;
		}

		public State getTarget() {
				return target;
		}

		public List<Guard> getGuards() {
				List<Guard> guards = new ArrayList<>();
				for (Transition t : transitions) {
						if (t != null) guards.addAll(t.getGuards());
				}
				return guards;
		}
}