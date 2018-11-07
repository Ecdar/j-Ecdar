package models;

import java.util.Objects;

public class Update {

		private Clock clock;
		private int value;

		public Update(Clock clock, int value) {
				this.clock = clock;
				this.value = value;
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

		@Override
		public boolean equals(Object o) {
				if (this == o) return true;
				if (o == null || getClass() != o.getClass()) return false;
				Update update = (Update) o;
				return value == update.value &&
								clock.equals(update.clock);
		}

		@Override
		public int hashCode() {
				return Objects.hash(clock, value);
		}
}
