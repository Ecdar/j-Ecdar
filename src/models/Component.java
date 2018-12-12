package models;

import java.util.*;

public class Component {
		private String name;
		private List<Location> locations;
		private List<Transition> transitions;
		private List<Clock> clocks;
		private Set<Channel> inputAct;
		private Set<Channel> outputAct;
		private Location initLoc;

		public Component(String name, List<Location> locations, List<Transition> transitions, List<Clock> clocks) {
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

		@Override
		public boolean equals(Object o) {
				if (this == o) return true;
				if (!(o instanceof Component)) return false;
				Component component = (Component) o;
				return name.equals(component.name) &&
								Arrays.equals(locations.toArray(), component.locations.toArray()) &&
								Arrays.equals(transitions.toArray(), component.transitions.toArray()) &&
								Arrays.equals(clocks.toArray(), component.clocks.toArray()) &&
								Arrays.equals(inputAct.toArray(), component.inputAct.toArray()) &&
								Arrays.equals(outputAct.toArray(), component.outputAct.toArray()) &&
								initLoc.equals(component.initLoc);
		}

		@Override
		public int hashCode() {
				return Objects.hash(name, locations, transitions, clocks, inputAct, outputAct, initLoc);
		}

		public String getName(){
				return name;
		}

		public List<Location> getLocations() {
				return locations;
		}

		private List<Transition> getTransitionsFromLocation(Location loc) {
				List<Transition> trans = new ArrayList<>(transitions);

				if (loc.isUniversal()) {
						Set<Channel> actions = getActions();
						for (Channel action : actions) {
								trans.add(new Transition(loc, loc, action, getInputAct().contains(action), new ArrayList<>(), new ArrayList<>()));
						}
				} else {
						trans.removeIf(n -> n.getSource() != loc);
				}

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

		public List<Clock> getClocks() {
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

		public Set<Channel> getActions() {
				Set<Channel> actions = new HashSet<>();
				actions.addAll(inputAct);
				actions.addAll(outputAct);
				return actions;
		}
}
