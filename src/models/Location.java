package models;

import java.util.Objects;

public class Location {

		private String name;
		private Guard invariant;
		private boolean isInitial;
		private boolean isUrgent;
		private boolean isUniversal;
		private boolean isInconsistent;

		public Location(String name, Guard invariant, boolean isInitial, boolean isUrgent, boolean isUniversal, boolean isInconsistent) {
				this.name = name;
				this.invariant = invariant;
				this.isInitial = isInitial;
				this.isUrgent = isUrgent;
				this.isUniversal = isUniversal;
				this.isInconsistent = isInconsistent;
		}

		public String getName() { return name; }

		public void setName(String name) { this.name = name;}

		public Guard getInvariant() {
				return invariant;
		}

		public boolean isInitial() {
				return isInitial;
		}

		public void setInitial(boolean initial) {
				isInitial = initial;
		}

		@Override
		public String toString() {
				return "Location " + this.name;
		}

		@Override
		public boolean equals(Object o) {
				if (this == o) return true;
				if (o == null || getClass() != o.getClass()) return false;
				Location location = (Location) o;
				return isInitial == location.isInitial &&
								isUrgent == location.isUrgent &&
								isUniversal == location.isUniversal &&
								isInconsistent == location.isInconsistent &&
								name.equals(location.name) &&
								invariant.equals(location.invariant);
		}

		@Override
		public int hashCode() {
				return Objects.hash(name, invariant, isInitial, isUrgent, isUniversal, isInconsistent);
		}
}
