package models;

import java.util.Objects;

public class Guard {

		private Clock clock;
		private int value;
		private boolean strict;
		private boolean greater;

		public Guard(Clock clock, int value, boolean greater, boolean strict) {
				this.clock = clock;
				this.value = value;
				this.greater = greater;
				this.strict = strict;
		}

		public Clock getClock() {
				return clock;
		}

		public void setClock(Clock clock) {
				this.clock = clock;
		}

		public int getValue() {
				return value;
		}

		public void setValue(int value) {
				this.value = value;
		}

		public boolean isStrict() { return strict; }

		public int lowerBound() {
				return greater ? value : 0;
		}

		public int upperBound() {
				return greater ? Integer.MAX_VALUE : value;
		}

		@Override
		public boolean equals(Object o) {
				if (this == o) return true;
				if (o == null || getClass() != o.getClass()) return false;
				Guard guard = (Guard) o;
				return value == guard.value &&
								greater == guard.greater &&
								strict == guard.strict &&
								clock.getName().equals(guard.clock.getName());
		}

		@Override
		public int hashCode() {
				return Objects.hash(clock, value, greater, strict);
		}
}
