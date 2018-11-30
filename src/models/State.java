package models;

import lib.DBMLib;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class State {
		private List<Location> locations;
		private int[] zone;
		private int zoneSize;

		public State(List<Location> locations, int[] zone) {
				this.locations = locations;
				this.zone = zone;
				this.zoneSize = (int) Math.sqrt(zone.length);

				String fileName = "src/" + System.mapLibraryName("DBM");
				File lib = new File(fileName);
				System.load(lib.getAbsolutePath());
		}

		public List<Location> getLocations() {
				return locations;
		}

		public int[] getZone() {
				return zone;
		}

		public void delay() {
				this.zone = DBMLib.dbm_up(zone, zoneSize);
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
