package models;

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

		public int getValue() {
				return value;
		}
}
