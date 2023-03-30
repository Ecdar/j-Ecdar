package models;

import exceptions.CddAlreadyRunningException;
import exceptions.CddNotRunningException;
import lib.CDDLib;

import java.util.ArrayList;
import java.util.List;

public class CDDRuntime {
    private static boolean isRunning = false;

    private static final List<Clock> clocks = new ArrayList<>();
    private static final List<BoolVar> booleanVariables = new ArrayList<>();
    private static int bddStartLevel;

    private static int maxSize = 1000;
    private static int cs = 1000;
    private static int stackSize = 1000;

    public static boolean isRunning() {
        return isRunning;
    }

    public static int getBddStartLevel() {
        return bddStartLevel;
    }

    public static int getNumberOfClocks() {
        return clocks.size() + 1;
    }

    public static Iterable<Clock> getClocks() {
        return clocks;
    }

    public static List<Clock> getAllClocks() {
        List<Clock> allClocks = new ArrayList<>();
        getClocks().forEach(allClocks::add);
        return allClocks;
    }

    public static int indexOf(Clock clock) {
        int index = 1;
        for (Clock current : getClocks()) {
            if (current.hashCode() == clock.hashCode()) {
                return index;
            }
            index += 1;
        }

        return -1;
    }

    public static int getNumberOfBooleanVariable() {
        return booleanVariables.size();
    }

    public static Iterable<BoolVar> getBooleanVariables() {
        return booleanVariables;
    }

    public static List<BoolVar> getAllBooleanVariables() {
        List<BoolVar> allBooleanVariables = new ArrayList<>();
        getBooleanVariables().forEach(allBooleanVariables::add);
        return allBooleanVariables;
    }

    public static int indexOf(BoolVar booleanVariable) {
        int index = 0;
        for (BoolVar current : getBooleanVariables()) {
            if (booleanVariable.equals(current)) {
                return index;
            }
            index += 1;
        }

        return -1;
    }

    public static int init(int maxSize, int cs, int stackSize) {
        if (isRunning()) {
            throw new CddAlreadyRunningException("Can't initialize when already running");
        }

        int initalisation = CDDLib.cddInit(maxSize, cs, stackSize);
        CDDRuntime.maxSize = maxSize;
        CDDRuntime.cs = cs;
        CDDRuntime.stackSize = stackSize;
        isRunning = true;
        return initalisation;
    }

    public static int init() throws CddAlreadyRunningException {
        return init(maxSize, cs, stackSize);
    }

    private static void setClocks(List<Clock> clocks) {
        CDDRuntime.clocks.clear();
        addClocks(clocks);
    }

    private static void setBooleanVariables(List<BoolVar> booleanVariables) {
        CDDRuntime.booleanVariables.clear();
        addBooleanVariables(booleanVariables);
    }

    public static int init(List<Clock> clocks, List<BoolVar> booleanVariables) {
        int initialisation = init();
        setClocks(clocks);
        setBooleanVariables(booleanVariables);
        return initialisation;
    }

    public static boolean tryInit(List<Clock> clocks, List<BoolVar> booleanVariables) {
        if (isRunning()) {
            return false;
        }
        init(clocks, booleanVariables);
        return true;
    }

    @SafeVarargs
    public static void addClocks(List<Clock>... listOfClockLists) {
        if (!isRunning()) {
            throw new CddNotRunningException("Cannot add clocks to an uninitialised CDD runtime.");
        }

        for (List<Clock> clocks : listOfClockLists) {
            CDDRuntime.clocks.addAll(clocks);
        }

        // FIXME (Andreas): This does not seem right as we are "setting" the amount and not "adding".
        CDDLib.cddAddClocks(getNumberOfClocks());
    }

    public static void addClocks(Clock... clocks) {
        addClocks(List.of(clocks));
    }

    @SafeVarargs
    public static int addBooleanVariables(List<BoolVar>... listOfBooleanVariablesLists) {
        if (!isRunning()) {
            throw new CddNotRunningException("Cannot add boolean variables to an uninitialised CDD runtime.");
        }

        for (List<BoolVar> booleanVariables : listOfBooleanVariablesLists) {
            CDDRuntime.booleanVariables.addAll(booleanVariables);
        }

        if (getNumberOfBooleanVariable() > 0) {
            // FIXME (Andreas): This does not seem right as we are "setting" the amount and not "adding".
            bddStartLevel = CDDLib.addBddvar(getNumberOfBooleanVariable());
        } else {
            bddStartLevel = 0;
        }

        return bddStartLevel;
    }

    public static int addBooleanVariables(BoolVar... booleanVariables) {
        return addBooleanVariables(List.of(booleanVariables));
    }

    public static void done() {
        if (!isRunning) {
            // TODO (Andreas): Should calling done when not running be a failure?
            return;
        }

        clocks.clear();
        booleanVariables.clear();
        CDDLib.cddDone();
        isRunning = false;
    }

    public static void ensureDone() {
        if (isRunning()) {
            done();
        }
    }
}