package logic;

import models.*;
import java.util.ArrayList;
import java.util.Set;

public class Composition {

		public void compose(ArrayList<Component> machines) {
				State initialState = computeInitial(machines);

				ArrayList<State> passed = new ArrayList<>();
				ArrayList<State> waiting = new ArrayList<>();
				waiting.add(initialState);
		}

		private State computeInitial(ArrayList<Component> machines) {
				ArrayList<Location> initialLocations = new ArrayList<>();
				ArrayList<Clock> valuations = new ArrayList<>();
				ArrayList<Guard> zone = new ArrayList<>();
				for (Component machine : machines) {
						Location init = machine.getInitLoc();
						initialLocations.add(init);
						Set<Clock> clocks = machine.getClocks();
						for (Clock clock : clocks) {
								clock.setValue(0);
								valuations.add(clock);
						}
						if (init.getInvariant() != null) zone.add(init.getInvariant());
				}
				return new State(initialLocations, valuations, zone);
		}
}
