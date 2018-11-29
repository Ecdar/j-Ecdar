package models;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Component {
		private String name;
		private ArrayList<Location> locations;
		private ArrayList<Transition> transitions;
		private Set<Clock> clocks;
		private Set<Channel> inputAct;
		private Set<Channel> outputAct;
		private Location initLoc;

		public Component(String name, ArrayList<Location> locations, ArrayList<Transition> transitions, Set<Clock> clocks) {
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

		public ArrayList<Location> getLocations() {
				return locations;
		}

		public ArrayList<Transition> getTransitions() {
				return transitions;
		}

		private ArrayList<Transition> getTransitionsFromLocation(Location loc) {
				ArrayList<Transition> trans = new ArrayList<>(transitions);

				trans.removeIf(n -> n.getSource() != loc);

				return trans;
		}

		public ArrayList<Transition> getTransitionsFromLocationAndSignal(Location loc, Channel signal) {
				ArrayList<Transition> trans = getTransitionsFromLocation(loc);

				trans.removeIf(n -> !n.getChannel().getName().equals(signal.getName()));

				return trans;
		}

		private void setTransitions(ArrayList<Transition> transitions) {
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

		public void setClocks(Set<Clock> clocks) {
				this.clocks = clocks;
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
