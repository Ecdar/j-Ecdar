package models;

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
}
