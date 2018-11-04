package lib;

public class Constraint {
		private int i, j;
		private int value;

		public Constraint(int i, int j, int value) {
				this.i = i;
				this.j = j;
				this.value = value;
		}

		public int getI() {
				return i;
		}

		public void setI(int i) {
				this.i = i;
		}

		public int getJ() {
				return j;
		}

		public void setJ(int j) {
				this.j = j;
		}

		public int getValue() {
				return value;
		}

		public void setValue(int value) {
				this.value = value;
		}

		@Override
		public String toString() {
				return "Constraint{" +
								"i=" + i +
								", j=" + j +
								", value=" + value +
								'}';
		}
}
