package models;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Component {
		private String name;
		private List<Location> locations;
		private List<Transition> transitions;
		private Set<Clock> clocks;
		private Set<Channel> inputAct;
		private Set<Channel> outputAct;
		private Location initLoc;

		public Component(String name, List<Location> locations, List<Transition> transitions, Set<Clock> clocks) {
				this.name = name;
				this.locations = locations;
				for (Location location : locations) {
						if (location.isInitial()) {
								initLoc = location;
								break;
						}
				}
				this.inputAct = new HashSet<>();
				this.outputAct = new HashSet<>();
				setTransitions(transitions);
				this.clocks = clocks;
		}

		public String getName(){
				return name;
		}

		public List<Location> getLocations() {
				return locations;
		}

		public List<Transition> getTransitions() {
				return transitions;
		}

		private List<Transition> getTransitionsFromLocation(Location loc) {
				List<Transition> trans = new ArrayList<>(transitions);

				trans.removeIf(n -> n.getSource() != loc);

				return trans;
		}

		public List<Transition> getTransitionsFromLocationAndSignal(Location loc, Channel signal) {
				List<Transition> trans = getTransitionsFromLocation(loc);

				trans.removeIf(n -> !n.getChannel().getName().equals(signal.getName()));

				return trans;
		}

		private void setTransitions(List<Transition> transitions) {
				this.transitions = transitions;
				for (Transition transition : transitions) {
						Channel action = transition.getChannel();
						if (transition.isInput()) {
								inputAct.add(action);
						} else {
								outputAct.add(action);
						}
				}
		}

		public Set<Clock> getClocks() {
				return clocks;
		}

		public Location getInitLoc() {
				return initLoc;
		}

		public Set<Channel> getInputAct() {
				return inputAct;
		}

		public Set<Channel> getOutputAct() {
				return outputAct;
		}
}
