package models;

import java.util.Objects;

public class Guard {

		private Clock clock;
		private int value;
		private boolean gt;
		private boolean gte;
		private boolean lt;
		private boolean lte;

		public Guard(Clock clock, int value, boolean gt, boolean gte, boolean lt, boolean lte) {
				this.clock = clock;
				this.value = value;
				this.gt = gt;
				this.gte = gte;
				this.lt = lt;
				this.lte = lte;
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

		public boolean isGt() {
				return gt;
		}

		public void setGt(boolean gt) {
				this.gt = gt;
		}

		public boolean isGte() {
				return gte;
		}

		public void setGte(boolean gte) {
				this.gte = gte;
		}

		public boolean isLt() {
				return lt;
		}

		public void setLt(boolean lt) {
				this.lt = lt;
		}

		public boolean isLte() {
				return lte;
		}

		public void setLte(boolean lte) {
				this.lte = lte;
		}

		public boolean isStrict() { return (gte || lte); }

		public int lowerBound() {
				if (!(gt || gte)) {
					return 0;
				} else {
						return value;
				}
		}

		public int upperBound() {
				if (!(lt || lte)) {
						return Integer.MAX_VALUE;
				} else {
						return value;
				}
		}

		@Override
		public boolean equals(Object o) {
				if (this == o) return true;
				if (o == null || getClass() != o.getClass()) return false;
				Guard guard = (Guard) o;
				return value == guard.value &&
								gt == guard.gt &&
								gte == guard.gte &&
								lt == guard.lt &&
								lte == guard.lte &&
								clock.getName() == guard.clock.getName();
		}

		@Override
		public int hashCode() {
				return Objects.hash(clock, value, gt, gte, lt, lte);
		}
}
