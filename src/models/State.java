package models;

import java.util.ArrayList;

public class State {
		private ArrayList<Location> locations;
		private ArrayList<Clock> valuations;
		private ArrayList<Guard> zone;

		public State(ArrayList<Location> locations, ArrayList<Clock> valuations, ArrayList<Guard> zone) {
				this.locations = locations;
				this.valuations = valuations;
				this.zone = zone;
		}

		public ArrayList<Location> getLocations() {
				return locations;
		}

		public void setLocations(ArrayList<Location> locations) {
				this.locations = locations;
		}

		public ArrayList<Clock> getValuations() {
				return valuations;
		}

		public void setValuations(ArrayList<Clock> valuations) {
				this.valuations = valuations;
		}

		public ArrayList<Guard> getZone() {
				return zone;
		}

		public void setZone(ArrayList<Guard> zone) {
				this.zone = zone;
		}
}
