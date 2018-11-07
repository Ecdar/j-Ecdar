package models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class State {
		private ArrayList<Location> locations;
		private int[] zone;

		public State(ArrayList<Location> locations, int[] zone) {
				this.locations = locations;
				this.zone = zone;
		}

		public ArrayList<Location> getLocations() {
				return locations;
		}

		public void setLocations(ArrayList<Location> locations) {
				this.locations = locations;
		}

		public int[] getZone() {
				return zone;
		}

		public void setZone(int[] zone) {
				this.zone = zone;
		}

		@Override
		public String toString() {
				return "State{" +
								"locations=" + locations +
								", zone=" + Arrays.toString(zone) +
								'}';
		}

		@Override
		public boolean equals(Object o) {
				if (this == o) return true;
				if (o == null || getClass() != o.getClass()) return false;
				State state = (State) o;
				return Arrays.equals(locations.toArray(), state.locations.toArray()) &&
								Arrays.equals(zone, state.zone);
		}

		@Override
		public int hashCode() {
				int result = Objects.hash(locations);
				result = 31 * result + Arrays.hashCode(zone);
				return result;
		}
}
