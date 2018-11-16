package logic;

import lib.DBMLib;
import models.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Set;

// parent class for all TS's, so we can use it with regular TS's, composed TS's etc.
public abstract class TransitionSystem {
		protected ArrayList<Clock> clocks;
		protected int dbmSize;

		public TransitionSystem(ArrayList<Component> machines) {
				this.clocks = new ArrayList<>();
				for (Component machine : machines) {
						clocks.addAll(machine.getClocks());
				}
				dbmSize = clocks.size() + 1;

				String fileName = "src/" + System.mapLibraryName("DBM");
				File lib = new File(fileName);
				System.load(lib.getAbsolutePath());
		}

		public int getDbmSize() {
				return dbmSize;
		}

		public ArrayList<Clock> getClocks() {
				return clocks;
		}

		public abstract State getInitialState();

		public abstract Set<Channel> getInputs();

		public abstract Set<Channel> getOutputs();

		public abstract ArrayList<State> getNextStates(State currentState, Channel channel);

		public abstract ArrayList<StateTransition> getTransitionsFrom(State currentState, Channel channel);

		protected int[] initializeDBM() {
				// we need a DBM of size n*n, where n is the number of clocks (x0, x1, x2, ... , xn)
				// clocks x1 to xn are clocks derived from our components, while x0 is a reference clock needed by the library
				// initially dbm is an array of 0's, which is what we need
				int[] dbm = new int[dbmSize*dbmSize];
				dbm = DBMLib.dbm_init(dbm, dbmSize);
				return dbm;
		}

		protected boolean isDbmValid(int[] dbm) {
				return DBMLib.dbm_isValid(dbm, dbmSize);
		}

		protected int[] buildConstraintsForGuard(int[] dbm, int i, Guard g) {
				boolean strict = g.isStrict();
				int max = 1073741823;

				int lowerBoundI = g.lowerBound();
				int upperBoundI = (g.upperBound() == Integer.MAX_VALUE) ? max : g.upperBound();

				if (strict) {
						if (upperBoundI < max) upperBoundI++;

						lowerBoundI--;
				}

				for (int a = 0; a < dbmSize; a++) {
						if (a != i && a == 0) {
								int lowerBoundA = dbm[a] * (-1);
								int upperBoundA = dbm[a * dbmSize];

								dbm = DBMLib.dbm_constrain1(dbm, dbmSize, i, a, (upperBoundI == max) ? max : upperBoundI - lowerBoundA, false);
								dbm = DBMLib.dbm_constrain1(dbm, dbmSize, a, i, (upperBoundA == max) ? max : upperBoundA - lowerBoundI, false);
						}
				}

				return dbm;
		}

		protected int[] buildConstraintWithX0(int[] dbm, int i, Guard g) {
				boolean strict = g.isStrict();
				int max = 1073741823;
				int lowerBound = g.lowerBound();
				int upperBound = (g.upperBound() == Integer.MAX_VALUE) ? max : g.upperBound();

				if (strict) {
						if (upperBound < max) upperBound++;

						lowerBound--;
				}

				lowerBound = lowerBound * (-1);

				dbm = DBMLib.dbm_constrain1(dbm, dbmSize, 0, i, lowerBound, false);
				dbm = DBMLib.dbm_constrain1(dbm, dbmSize, i, 0, upperBound, false);

				return dbm;
		}

		protected int[] applyInvariantsOrGuards(int[] dbm, ArrayList<Guard> guards) {
				for (int i = 0; i < guards.size(); i++) {
						// get guard and then its index in the clock array so you know the index in the DBM
						Guard g1 = guards.get(i); int a = getClocks().indexOf(g1.getClock());
						dbm = buildConstraintsForGuard(dbm, (a + 1), g1);
				}
				return dbm;
		}

		protected int[] applyResets(int[] dbm, ArrayList<Update> resets) {
				for (Update reset : resets) {
						int index = clocks.indexOf(reset.getClock());

						dbm = DBMLib.dbm_updateValue(dbm, dbmSize, (index + 1), reset.getValue());
				}

				return dbm;
		}

		protected int[] delay(int[] dbm) {
				return DBMLib.dbm_up(dbm, dbmSize);
		}
}