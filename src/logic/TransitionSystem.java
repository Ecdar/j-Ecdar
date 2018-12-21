package logic;

import lib.DBMLib;
import models.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

// parent class for all TS's, so we can use it with regular TS's, composed TS's etc.
public abstract class TransitionSystem {
    private List<Component> machines;
    private List<Clock> clocks;
    private int dbmSize;

    TransitionSystem(List<Component> machines) {
        this.machines = machines;
        this.clocks = new ArrayList<>();
        for (Component machine : machines) {
            clocks.addAll(machine.getClocks());
        }
        dbmSize = clocks.size() + 1;

        String fileName = "src/" + System.mapLibraryName("DBM");
        File lib = new File(fileName);
        System.load(lib.getAbsolutePath());
    }

    int getDbmSize() {
        return dbmSize;
    }

    public List<Clock> getClocks() {
        return clocks;
    }

    public List<Component> getMachines() { return machines; }

    public State getInitialState() {
        List<Location> initialLocations = new ArrayList<>();

        for (Component machine : machines) {
            Location init = machine.getInitLoc();
            initialLocations.add(init);
        }

        int[] zone = initializeDBM();
        State state = new State(initialLocations, zone);
        state.applyInvariants(clocks);
        state.delay();

        return state;
    }

    List<StateTransition> addNewStateTransitions(State currentState, List<List<Location>> locationsArr, List<List<Transition>> transitionsArr) {
        List<StateTransition> stateTransitions = new ArrayList<>();

        // loop through all sets of locations and transitions
        for (int n = 0; n < locationsArr.size(); n++) {
            List<Location> newLocations = locationsArr.get(n);
            List<Transition> transitions = (transitionsArr.get(n) == null) ? new ArrayList<>() : transitionsArr.get(n);
            List<Guard> guards = new ArrayList<>();
            List<Update> updates = new ArrayList<>();
            // gather all the guards and resets of one set of transitions
            for (Transition t : transitions) {
                if (t != null) {
                    guards.addAll(t.getGuards());
                    updates.addAll(t.getUpdates());
                }
            }

            // build the target state given the set of locations
            State state = new State(newLocations, currentState.getZone());
            // get the new zone by applying guards and resets on the zone of the target state
            if (!guards.isEmpty()) state.applyGuards(guards, clocks);
            if (!updates.isEmpty()) state.applyResets(updates, clocks);

            // if the zone is valid, build the transition and add it to the list
            if (isDbmValid(state.getZone())) {
                StateTransition stateTransition = new StateTransition(currentState, state, transitions);
                stateTransitions.add(stateTransition);
            }
        }

        return stateTransitions;
    }

    public abstract Set<Channel> getInputs();

    public abstract Set<Channel> getOutputs();

    public abstract List<StateTransition> getNextTransitions(State currentState, Channel channel);

    public List<Transition> getTransitionsFromLocationAndSignal(Location loc, Channel signal) {
        List<Component> components = getMachines();

        for (Component component : components) {
            if (component.getLocations().contains(loc)) {
                return component.getTransitionsFromLocationAndSignal(loc, signal);
            }
        }

        return new ArrayList<>();
    }

    int[] initializeDBM() {
        // we need a DBM of size n*n, where n is the number of clocks (x0, x1, x2, ... , xn)
        // clocks x1 to xn are clocks derived from our components, while x0 is a reference clock needed by the library
        // initially dbm is an array of 0's, which is what we need
        int[] dbm = new int[dbmSize * dbmSize];
        dbm = DBMLib.dbm_init(dbm, dbmSize);
        return dbm;
    }

    boolean isDbmValid(int[] dbm) {
        return DBMLib.dbm_isValid(dbm, dbmSize);
    }

    // function that takes an arbitrary number of lists and recursively calculates their cartesian product
    <T> List<List<T>> cartesianProduct(List<List<T>> lists) {
        List<List<T>> resultLists = new ArrayList<>();
        if (lists.size() == 0) {
            // base case; return a list containing one empty list
            resultLists.add(new ArrayList<>());
        } else {
            // take head of list
            List<T> firstList = lists.get(0);
            // apply function to tail of list
            List<List<T>> remainingLists = cartesianProduct(lists.subList(1, lists.size()));
            // combine each element of the first list with each of the remaining lists
            for (T condition : firstList) {
                for (List<T> remainingList : remainingLists) {
                    List<T> resultList = new ArrayList<>();
                    resultList.add(condition);
                    resultList.addAll(remainingList);
                    resultLists.add(resultList);
                }
            }
        }
        return resultLists;
    }
}