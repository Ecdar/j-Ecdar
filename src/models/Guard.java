package models;

import java.util.Objects;

public class Guard {

		private Clock clock;
		private int upperBound;
		private int lowerBound;

		public Guard(Clock clock, int value, boolean greater, boolean strict) {
				this.clock = clock;
				if (greater) {
						upperBound = 1073741823;
						lowerBound = strict ? (value + 1) : value;
				} else {
						upperBound = strict ? (value - 1) : value;
						lowerBound = 0;
				}
		}

		public Clock getClock() {
				return clock;
		}


		public int getLowerBound() { return lowerBound; }

		public int getUpperBound() { return upperBound; }

		@Override
		public boolean equals(Object o) {
				if (this == o) return true;
				if (o == null || getClass() != o.getClass()) return false;
				Guard guard = (Guard) o;
				return upperBound == guard.upperBound &&
								lowerBound == guard.lowerBound &&
								clock.getName().equals(guard.clock.getName());
		}

		@Override
		public int hashCode() {
				return Objects.hash(clock, upperBound, lowerBound);
		}
}
