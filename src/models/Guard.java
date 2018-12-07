package models;

public class Guard {

		private Clock clock;
		private int upperBound;
		private int lowerBound;

		public Guard(Clock clock, int value, boolean greater, boolean strict) {
				this.clock = clock;
				if (greater) {
						upperBound = Integer.MAX_VALUE;
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
}
