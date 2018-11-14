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

		public ArrayList<State> getNextStates(State currentState, String signal) {
				Channel channel = new Channel(signal);
				return getNextStates(currentState, channel);
		}

		public abstract ArrayList<State> getNextStates(State currentState, Channel channel);

		protected int[] initializeDBM() {
				// we need a DBM of size n*n, where n is the number of clocks (x0, x1, x2, ... , xn)
				// clocks x1 to xn are clocks derived from our components, while x0 is a reference clock needed by the library
				// initially dbm is an array of 0's, which is what we need
				return new int[dbmSize*dbmSize];
		}

		protected int[] buildConstraintWithX0(int[] dbm, int i, Guard g) {
				boolean strict = g.isStrict();
				int lowerBound = g.lowerBound();
				int upperBound = g.upperBound();
				if (!strict && upperBound != Integer.MAX_VALUE) upperBound++;
				dbm = DBMLib.dbm_constrain1(dbm, dbmSize, 0, i, lowerBound, true);
				dbm = DBMLib.dbm_constrain1(dbm, dbmSize, i, 0, upperBound, true);
				return dbm;
		}

		protected int[] buildConstraint(int[] dbm, int i, int j, Guard g1, Guard g2) {
				// determine constraint between 2 guards on clocks x and y by taking x's upper bound - y's lower bound
				int bound = (g1.upperBound() == Integer.MAX_VALUE) ? Integer.MAX_VALUE : g1.upperBound() - g2.lowerBound();
				// if either guard is strict, the constraint is also strict
				boolean strict = (g1.isStrict() || g2.isStrict());
				if (!strict && bound != Integer.MAX_VALUE) bound++;
				return DBMLib.dbm_constrain1(dbm, dbmSize, i, j, bound, true);
		}

		protected int[] applyInvariantsOrGuards(int[] dbm, ArrayList<Guard> guards) {
				for (int i = 0; i < guards.size(); i++) {
						// get guard and then its index in the clock array so you know the index in the DBM
						Guard g1 = guards.get(i); int a = getClocks().indexOf(g1.getClock());
						dbm = buildConstraintWithX0(dbm, (a+1), g1);

						// take 2 guards at a time in order to determine constraint (x-y and y-x)
						for (int j = (i + 1); j < guards.size(); j++) {
								Guard g2 = guards.get(j); int b = getClocks().indexOf(g2.getClock());

								// add constraints to dbm
								dbm = buildConstraint(dbm, (a+1), (b+1), g1, g2);
								dbm = buildConstraint(dbm, (b+1), (a+1), g2, g1);
						}
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
