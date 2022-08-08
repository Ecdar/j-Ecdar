package models;

import exceptions.CddAlreadyRunningException;
import exceptions.CddNotRunningException;
import lib.CDDLib;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class CDD {
    private long pointer;

    private Guard guard;
    private boolean isGuardDirty;

    private CddExtractionResult extraction;
    private boolean isExtractionDirty;

    private static boolean cddIsRunning;
    private static List<Clock> clocks = new ArrayList<>();

    // includes the + 1 for initial clock
    public static int numClocks;
    public static int maxSize = 1000;
    public static int cs = 1000;
    public static int stackSize = 1000;
    public static int numBools;
    public static int bddStartLevel;
    public static List<BoolVar> BVs = new ArrayList<>();

    public CDD() {
        checkIfNotRunning();
        this.pointer = CDDLib.allocateCdd();
        setDirty();
    }

    public CDD(long pointer) {
        this.pointer = pointer;
        setDirty();
    }

    public CDD(Guard guard)
            throws IllegalArgumentException {
        CDD cdd;
        if (guard instanceof FalseGuard) {
            cdd = cddFalse();
        } else if (guard instanceof TrueGuard) {
            cdd = cddTrue();
        } else if (guard instanceof ClockGuard) {
            Zone zone = new Zone(numClocks, true);
            zone.init();
            zone.buildConstraintsForGuard((ClockGuard) guard, clocks);
            cdd = CDD.createFromDbm(zone.getDbm(), numClocks);
        } else if (guard instanceof BoolGuard) {
            cdd = create((BoolGuard) guard);
        } else if (guard instanceof AndGuard) {
            cdd = cddTrue();
            for (Guard g : ((AndGuard) guard).getGuards()) {
                cdd = cdd.conjunction(new CDD(g));
            }
        } else if (guard instanceof OrGuard) {
            cdd = cddFalse();
            for (Guard g : ((OrGuard) guard).getGuards()) {
                cdd = cdd.disjunction(new CDD(g));
            }
        } else {
            throw new IllegalArgumentException("Guard instance is not supported");
        }
        this.pointer = cdd.pointer;
        this.guard = guard;
    }

    private void setDirty() {
        isGuardDirty = true;
        isExtractionDirty = true;
    }

    public Guard getGuard(List<Clock> relevantClocks) {
        if (isGuardDirty) {
            guard = isBDD() ? toBoolGuards() : toClockGuards(relevantClocks);
            isGuardDirty = false;
        }

        return guard;
    }

    public Guard getGuard() {
        return getGuard(clocks);
    }

    private Guard toClockGuards(List<Clock> relevantClocks)
            throws IllegalArgumentException {
        if (isBDD()) {
            throw new IllegalArgumentException("CDD is a BDD");
        }

        if (isFalse()) {
            return new FalseGuard();
        }
        if (isTrue()) {
            return new TrueGuard();
        }

        CDD copy = hardCopy();

        List<Guard> orParts = new ArrayList<>();
        while (!copy.isTerminal()) {
            copy.reduce().removeNegative();
            CddExtractionResult extraction = copy.extract();
            copy = extraction.getCddPart().reduce().removeNegative();

            Zone zone = new Zone(extraction.getDbm());
            CDD bdd = extraction.getBddPart();

            List<Guard> andParts = new ArrayList<>();
            // Adds normal guards and diagonal constraints
            andParts.add(
                    zone.buildGuardsFromZone(clocks, relevantClocks)
            );
            // Adds boolean constraints (var == val)
            andParts.add(
                    bdd.toBoolGuards()
            );
            // Removes all TrueGuards
            andParts = andParts.stream()
                    .filter(guard -> !(guard instanceof TrueGuard))
                    .collect(Collectors.toList());

            orParts.add(
                    new AndGuard(andParts)
            );
        }

        return new OrGuard(orParts);
    }

    private Guard toBoolGuards()
            throws IllegalArgumentException {
        if (!isBDD()) {
            throw new IllegalArgumentException("CDD is not a BDD");
        }

        if (isFalse()) {
            return new FalseGuard();
        }
        if (isTrue()) {
            return new TrueGuard();
        }

        long ptr = getPointer();
        BDDArrays arrays = new BDDArrays(CDDLib.bddToArray(ptr, numBools));

        List<Guard> orParts = new ArrayList<>();
        for (int i = 0; i < arrays.traceCount; i++) {

            List<Guard> andParts = new ArrayList<>();
            for (int j = 0; j < arrays.booleanCount; j++) {

                int index = arrays.getVariables().get(i).get(j);
                if (index >= 0) {
                    BoolVar var = BVs.get(index - bddStartLevel);
                    boolean val = arrays.getValues().get(i).get(j) == 1;
                    BoolGuard bg = new BoolGuard(var, Relation.EQUAL, val);

                    andParts.add(bg);
                }
            }

            orParts.add(new AndGuard(andParts));
        }
        return new OrGuard(orParts);
    }

    public long getPointer() {
        return pointer;
    }

    public int getNodeCount()
            throws NullPointerException {
        checkForNull();
        return CDDLib.cddNodeCount(pointer);
    }

    public CDDNode getRoot()
            throws NullPointerException {
        checkForNull();
        long nodePointer = CDDLib.getRootNode(this.pointer);
        return new CDDNode(nodePointer);
    }

    public CddExtractionResult extract()
            throws NullPointerException, CddNotRunningException {
        if (isExtractionDirty) {
            checkIfNotRunning();
            checkForNull();
            extraction = new CddExtractionResult(
                    CDDLib.extractBddAndDbm(pointer)
            );
            isExtractionDirty = false;
        }

        return extraction;
    }

    public boolean isBDD()
            throws NullPointerException {
        checkForNull();
        // CDDLib.isBDD does not recognise cddFalse and cddTrue as BDDs
        return CDDLib.isBDD(this.pointer) || isFalse() || isTrue();
    }

    public boolean isTerminal()
            throws NullPointerException, CddNotRunningException {
        checkIfNotRunning();
        checkForNull();
        return CDDLib.isTerminal(pointer);
    }

    public boolean isUnrestrained() {
        // TODO: check if correct
        return this.equiv(cddTrue());
    }

    public boolean isNotFalse() {
        return !isFalse();
    }

    public boolean isFalse()
            throws NullPointerException {
        checkForNull();
        return CDDLib.cddEquiv(this.pointer, cddFalse().pointer);
    }

    public boolean isNotTrue()
            throws NullPointerException {
        checkForNull();
        return !isTrue();
    }

    public boolean isTrue()
            throws NullPointerException {
        checkForNull();
        return CDDLib.cddEquiv(this.pointer, cddTrue().pointer);
    }

    public void free()
            throws NullPointerException {
        checkForNull();
        CDDLib.freeCdd(pointer);
        pointer = 0;
    }

    public CDD applyReset(List<Update> list) {
        if (isFalse()) {
            return this;
        }

        if (list.size() == 0) {
            return this;
        }

        int numBools = 0;
        int numClocks = 0;
        for (Update up : list) {
            if (up instanceof ClockUpdate) numClocks++;
            if (up instanceof BoolUpdate) numBools++;
        }
        int[] clockResets = new int[numClocks];
        int[] clockValues = new int[numClocks];
        int[] boolResets = new int[numBools];
        int[] boolValues = new int[numBools];
        int cl = 0;
        int bl = 0;
        for (Update up : list) {
            if (up instanceof ClockUpdate) {
                ClockUpdate u = (ClockUpdate) up;
                clockResets[cl] = indexOf(u.getClock());
                clockValues[cl] = u.getValue();
                cl++;
            }
            if (up instanceof BoolUpdate) {
                BoolUpdate u = (BoolUpdate) up;
                boolResets[bl] = bddStartLevel + indexOf(u.getBV());
                boolValues[bl] = u.getValue() ? 1 : 0;
                bl++;
            }
        }
        return applyReset(clockResets, clockValues, boolResets, boolValues).removeNegative().reduce();
    }

    public boolean canDelayIndefinitely() {
        if (isTrue()) {
            return true;
        }
        if (isFalse()) {
            return false;
        }
        if (isBDD()) {
            return true;
        }

        CDD copy = hardCopy();

        while (!copy.isTerminal()) {
            CddExtractionResult extraction = copy.removeNegative().reduce().extract();
            copy = extraction.getCddPart().removeNegative().reduce();
            Zone zone = new Zone(extraction.getDbm());

            if (!zone.canDelayIndefinitely()) {
                return false;
            }
        }
        // found no states that cannot delay indefinitely
        return true;
    }

    public boolean isUrgent() {
        if (isTrue()) {
            return false;
        }
        if (isFalse()) {
            return true;
        }
        if (isBDD()) {
            return false;
        }

        // Required as we don't want to alter the pointer value of "this"
        CDD copy = hardCopy();

        while (!copy.isTerminal()) {
            CddExtractionResult res = copy.removeNegative().reduce().extract();
            Zone zone = new Zone(res.getDbm());
            copy = res.getCddPart().removeNegative().reduce();
            if (!zone.isUrgent()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns a new instance of this CDD but with the same pointer.
     * In contrast to {@link #copy()} this does not create a completely
     * new CDD instance by invoking the {@link CDDLib#copy(long)}. The usefulness
     * of {@link #hardCopy()} is its lightweight nature and as the pointer
     * is a pass-by-value then immediate not oeprator invocations wont alter the pointer
     * value of the original (this.pointer) retrieved through {@link #getPointer()}.
     *
     * @return Returns a new CDD which is not created through {@link CDDLib#copy(long)} but with a pointer copy.
     */
    public CDD hardCopy() {
        return new CDD(pointer);
    }

    public CDD copy()
            throws NullPointerException, CddNotRunningException {
        checkIfNotRunning();
        checkForNull();
        return new CDD(CDDLib.copy(pointer));
    }

    public CDD delay()
            throws NullPointerException, CddNotRunningException {
        checkIfNotRunning();
        checkForNull();
        pointer = CDDLib.delay(pointer);
        setDirty();
        return this;
    }

    public CDD delayInvar(CDD invariant)
            throws NullPointerException, CddNotRunningException {
        checkIfNotRunning();
        checkForNull();
        pointer = CDDLib.delayInvar(pointer, invariant.pointer);
        setDirty();
        return this;
    }

    public CDD exist(int[] levels, int[] clocks)
            throws NullPointerException, CddNotRunningException {
        checkIfNotRunning();
        checkForNull();
        pointer = CDDLib.exist(pointer, levels, clocks);
        setDirty();
        return this;
    }

    public CDD past()
            throws NullPointerException, CddNotRunningException {
        // TODO: make sure this is used at the correct spots everywhere, might have been confuces with delay
        checkIfNotRunning();
        checkForNull();
        pointer = CDDLib.past(pointer);
        setDirty();
        return this;
    }

    public CDD removeNegative()
            throws NullPointerException, CddNotRunningException {
        checkIfNotRunning();
        checkForNull();
        pointer = CDDLib.removeNegative(pointer);
        setDirty();
        return this;
    }

    public CDD applyReset(int[] clockResets, int[] clockValues, int[] boolResets, int[] boolValues)
            throws NullPointerException, CddNotRunningException {
        checkIfNotRunning();
        checkForNull();
        if (clockResets.length != clockValues.length) {
            throw new IllegalArgumentException("The amount of clock resets and values must be the same");
        }
        if (boolResets.length != boolValues.length) {
            throw new IllegalArgumentException("The amount of boolean resets and values must be the same");
        }

        pointer = CDDLib.applyReset(pointer, clockResets, clockValues, boolResets, boolValues);
        removeNegative().reduce();
        setDirty();
        return this;
    }

    public CDD transition(CDD guard, int[] clockResets, int[] clockValues, int[] boolResets, int[] boolValues)
            throws NullPointerException, CddNotRunningException {
        checkIfNotRunning();
        checkForNull();
        guard.checkForNull();
        pointer = CDDLib.transition(pointer, guard.pointer, clockResets, clockValues, boolResets, boolValues);
        removeNegative().reduce();
        setDirty();
        return this;
    }

    public CDD transitionBackPast(CDD guard, CDD update, int[] clockResets, int[] boolResets)
            throws NullPointerException, CddNotRunningException {
        checkIfNotRunning();
        checkForNull();
        guard.checkForNull();
        update.checkForNull();
        pointer = CDDLib.transitionBackPast(pointer, guard.pointer, update.pointer, clockResets, boolResets);
        setDirty();
        return this;
    }

    public CDD reduce()
            throws NullPointerException, CddNotRunningException {
        checkIfNotRunning();
        checkForNull();
        pointer = CDDLib.reduce(pointer);
        setDirty();
        return this;
    }

    public CDD predt(CDD safe) {
        checkIfNotRunning();
        checkForNull();
        safe.checkForNull();
        pointer = CDDLib.predt(pointer, safe.pointer);
        setDirty();
        return this;
    }

    public CDD minus(CDD other)
            throws NullPointerException, CddNotRunningException {
        checkIfNotRunning();
        checkForNull();
        other.checkForNull();
        return new CDD(CDDLib.minus(pointer, other.pointer)).removeNegative().reduce();
    }

    public CDD negation()
            throws NullPointerException, CddNotRunningException {
        checkIfNotRunning();
        checkForNull();
        long resultPointer = CDDLib.negation(pointer);
        return new CDD(resultPointer);
    }

    public CDD conjunction(CDD other)
            throws NullPointerException, CddNotRunningException {
        checkIfNotRunning();
        checkForNull();
        other.checkForNull();
        long resultPointer = CDDLib.conjunction(pointer, other.pointer);
        return new CDD(resultPointer).reduce().removeNegative(); // tried to remove the reduce and remove negative, but that made a simpleversity test fail because rule 6 in the quotient on automata level did something funky (both spec and negated spec turned out to be cddtrue)
    }

    public CDD disjunction(CDD other)
            throws NullPointerException, CddNotRunningException {
        checkIfNotRunning();
        checkForNull();
        other.checkForNull();
        long resultPointer = CDDLib.disjunction(pointer, other.pointer);
        return new CDD(resultPointer);
    }

    public boolean intersects(CDD other) {
        return conjunction(other).isNotFalse();
    }

    public boolean isSubset(CDD other) {
        CDD hardCopy = hardCopy();
        return conjunction(other).equiv(hardCopy);
    }

    public boolean equiv(CDD that)
            throws NullPointerException {
        checkForNull();
        return CDDLib.cddEquiv(this.pointer, that.pointer);
    }

    public void printDot()
            throws NullPointerException {
        checkForNull();
        CDDLib.cddPrintDot(pointer);
    }

    public void printDot(String filePath)
            throws NullPointerException {
        checkForNull();
        CDDLib.cddPrintDot(pointer, filePath);
    }

    private void checkForNull() {
        if (pointer == 0) {
            throw new NullPointerException("CDD object is null");
        }
    }

    public Federation getFederation() {
        // TODO: does not in any way take care of BDD parts (might run endless for BCDDs?)
        List<Zone> zoneList = new ArrayList<>();
        CDD copy = hardCopy();

        while (!copy.isTerminal()) {
            copy.reduce().removeNegative();
            CddExtractionResult extraction = copy.extract();
            copy = extraction.getCddPart().reduce().removeNegative();
            Zone zone = new Zone(extraction.getDbm());
            zoneList.add(zone);
        }
        return new Federation(zoneList);
    }

    public CDD transition(Edge e) {
        if (e.getUpdates().size() == 0) {
            return this.conjunction(e.getGuardCDD());
        }

        List<Integer> clockResets = new ArrayList<>();
        List<Integer> clockValues = new ArrayList<>();
        List<Integer> boolResets = new ArrayList<>();
        List<Integer> boolValues = new ArrayList<>();

        // For each of the updates, then based on their instance type
        //   we need to find its index and value and then add it to their collection
        for (Update update : e.getUpdates()) {
            if (update instanceof ClockUpdate) {
                ClockUpdate clockUpdate = (ClockUpdate) update;
                int clockIndex = indexOf(clockUpdate.getClock());
                clockResets.add(clockIndex);
                int clockValue = clockUpdate.getValue();
                clockValues.add(clockValue);
            } else if (update instanceof BoolUpdate) {
                BoolUpdate boolUpdate = (BoolUpdate) update;
                int boolIndex = bddStartLevel + indexOf(boolUpdate.getBV());
                boolResets.add(boolIndex);
                int boolValue = boolUpdate.getValue() ? 1 : 0;
                boolValues.add(boolValue);
            }
        }

        // transition requires int[] and not List<Integer> here we are converting
        int[] arrClockResets = clockResets.stream().mapToInt(Integer::intValue).toArray();
        int[] arrClockValues = clockValues.stream().mapToInt(Integer::intValue).toArray();
        int[] arrBoolResets = boolResets.stream().mapToInt(Integer::intValue).toArray();
        int[] arrBoolValues = boolValues.stream().mapToInt(Integer::intValue).toArray();

        return transition(e.getGuardCDD(), arrClockResets, arrClockValues, arrBoolResets, arrBoolValues).removeNegative().reduce();
    }

    public CDD transitionBack(CDD guard, CDD update, int[] clockResets, int[] boolResets)
            throws NullPointerException, CddNotRunningException {
        checkIfNotRunning();
        checkForNull();
        guard.checkForNull();
        update.checkForNull();
        return new CDD(CDDLib.transitionBack(pointer, guard.pointer, update.pointer, clockResets, boolResets)).removeNegative().reduce();
    }

    private CDD transitionBack(CDD guard, List<Update> updates) {
        if (updates.size() == 0) {
            return this.conjunction(guard);
        }

        List<Integer> clockUpdates = new ArrayList<>();
        List<Integer> boolUpdates = new ArrayList<>();

        // For each of the updates, then based on their instance type
        //   we need to find its index and then add it to their collection
        for (Update update : updates) {
            if (update instanceof ClockUpdate) {
                ClockUpdate clockUpdate = (ClockUpdate) update;
                int clockIndex = indexOf(clockUpdate.getClock());
                clockUpdates.add(clockIndex);
            } else if (update instanceof BoolUpdate) {
                BoolUpdate boolUpdate = (BoolUpdate) update;
                int boolIndex = bddStartLevel + indexOf(boolUpdate.getBV());
                boolUpdates.add(boolIndex);
            }
        }

        // transitionBack requires int[] and not List<Integer> here we are converting
        int[] arrClockUpdates = clockUpdates.stream().mapToInt(Integer::intValue).toArray();
        int[] arrBoolUpdates = boolUpdates.stream().mapToInt(Integer::intValue).toArray();

        return transitionBack(guard, create(updates), arrClockUpdates, arrBoolUpdates).removeNegative().reduce();
    }

    public CDD transitionBack(Edge e) {
        return transitionBack(e.getGuardCDD(), e.getUpdates());
    }

    public CDD transitionBack(Move e) {
        return transitionBack(e.getGuardCDD(), e.getUpdates());
    }

    @Override
    public String toString() {
        return getGuard().toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof CDD)) {
            return false;
        }

        CDD other = (CDD) obj;
        return pointer == other.pointer;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pointer);
    }

    public static CDD create(List<Update> updates) {
        CDD res = cddTrue();
        for (Update up : updates) {
            if (up instanceof ClockUpdate) {
                ClockUpdate u = (ClockUpdate) up;
                res = res.conjunction(CDD.createInterval(indexOf(u.getClock()), 0, u.getValue(), true, u.getValue(), true));
            }
            if (up instanceof BoolUpdate) {
                BoolUpdate u = (BoolUpdate) up;
                BoolGuard bg = new BoolGuard(u.getBV(), Relation.EQUAL, u.getValue());
                res = res.conjunction(CDD.create(bg));
            }
        }
        return res.removeNegative().reduce();
    }

    public static CDD create(BoolGuard guard) {
        if (guard.getValue()) {
            return createBddNode(bddStartLevel + indexOf(guard.getVar()));
        }
        return createNegatedBddNode(bddStartLevel + indexOf(guard.getVar()));
    }

    public static CDD cddUnrestrained() {
        return CDD.cddTrue().removeNegative();
    }

    public static CDD cddTrue()
            throws CddAlreadyRunningException {
        checkIfNotRunning();
        return new CDD(CDDLib.cddTrue());
    }

    public static CDD cddFalse() {
        checkIfNotRunning();
        return new CDD(CDDLib.cddFalse());
    }

    public static CDD cddZero() {
        Zone zone = new Zone(numClocks, false);
        return CDD.createFromDbm(zone.getDbm(), numClocks);
    }

    public static CDD cddZeroDelayed() {
        Zone zone = new Zone(numClocks, false);
        zone.delay();
        return CDD.createFromDbm(zone.getDbm(), numClocks);
    }

    public static boolean isRunning() {
        return cddIsRunning;
    }

    public static int indexOf(Clock clock)
            throws IllegalArgumentException {
        for (int i = 0; i < clocks.size(); i++) {
            if (clock.hashCode() == clocks.get(i).hashCode()) {
                return i + 1;
            }
        }
        return -1;
    }

    public static int indexOf(BoolVar bv)
            throws IllegalArgumentException {
        for (int i = 0; i < BVs.size(); i++) {
            if (bv.equals(BVs.get(i))) {
                return i;
            }
        }
        return -1;
    }

    public static List<Clock> getClocks() {
        return clocks;
    }

    public static int init(int maxSize, int cs, int stackSize)
            throws CddAlreadyRunningException {
        if (cddIsRunning) {
            throw new CddAlreadyRunningException("Can't initialize when already running");
        }
        cddIsRunning = true;
        return CDDLib.cddInit(maxSize, cs, stackSize);
    }

    public static int init(int maxSize, int cs, int stackSize, List<Clock> clocks, List<BoolVar> booleans) {
        int initialisation = init(maxSize, cs, stackSize);
        addClocks(clocks);
        addBooleans(booleans);
        return initialisation;
    }

    public static int init(List<Clock> clocks, List<BoolVar> booleans) {
        return init(maxSize, cs, stackSize, clocks, booleans);
    }

    public static boolean tryInit(int maxSize, int cs, int stackSize, List<Clock> clocks, List<BoolVar> booleans) {
        if (cddIsRunning) {
            return false;
        }
        init(maxSize, cs, stackSize, clocks, booleans);
        return true;
    }

    public static boolean tryInit(List<Clock> clocks, List<BoolVar> booleans) {
        return tryInit(maxSize, cs, stackSize, clocks, booleans);
    }

    public static void done() {
        cddIsRunning = false;
        numClocks = 0;
        numBools = 0;
        clocks = new ArrayList<>();
        BVs = new ArrayList<>();
        CDDLib.cddDone();
    }

    public static void ensureDone() {
        if (cddIsRunning) {
            done();
        }
    }

    @SafeVarargs
    public static void addClocks(List<Clock>... clocks) {
        checkIfNotRunning();
        for (List<Clock> list : clocks) {
            CDD.clocks.addAll(list);
        }
        numClocks = CDD.clocks.size() + 1;
        CDDLib.cddAddClocks(numClocks);
    }

    public static void addClocks(Clock... clocks) {
        addClocks(
                Arrays.asList(clocks)
        );
    }

    public static void addClocks() {
        addClocks(
                new ArrayList<>()
        );
    }

    @SafeVarargs
    public static int addBooleans(List<BoolVar>... BVs) {
        checkIfNotRunning();
        for (List<BoolVar> list : BVs) {
            CDD.BVs.addAll(list);
        }

        numBools = CDD.BVs.size();
        if (numBools > 0) {
            bddStartLevel = CDDLib.addBddvar(numBools);
        } else {
            bddStartLevel = 0;
        }
        return bddStartLevel;
    }

    public static int addBooleans(BoolVar... BVs) {
        return addBooleans(
                Arrays.asList(BVs)
        );
    }

    public static int addBooleans() {
        return addBooleans(
                new ArrayList<>()
        );
    }

    public static CDD createInterval(int i, int j, int lower, boolean lower_included, int upper, boolean upper_included) {
        checkIfNotRunning();
        // TODO: Negation of lower strict should be moved to a new function allocate_interval function in the CDD library
        return new CDD(CDDLib.interval(i, j, lower, lower_included, upper, !upper_included)).removeNegative();
    }

    public static CDD createFromDbm(int[] dbm, int dim) {
        checkIfNotRunning();
        return new CDD(CDDLib.cddFromDbm(dbm, dim));
    }

    public static CDD createLower(int i, int j, int lowerBound, boolean strict) {
        checkIfNotRunning();
        return new CDD(CDDLib.lower(i, j, lowerBound, strict)).removeNegative();
    }

    public static CDD createUpper(int i, int j, int upperBound, boolean strict) {
        checkIfNotRunning();
        return new CDD(CDDLib.upper(i, j, upperBound, strict)).removeNegative();
    }

    public static CDD createBddNode(int level) {
        checkIfNotRunning();
        return new CDD(CDDLib.cddBddvar(level));
    }

    public static CDD createNegatedBddNode(int level) {
        checkIfNotRunning();
        return new CDD(CDDLib.cddNBddvar(level));
    }

    public static CDD createBddNode(int level, boolean value) {
        checkIfNotRunning();
        if (value) {
            return createBddNode(level);
        }
        return createNegatedBddNode(level);
    }

    private static void checkIfNotRunning() {
        if (!cddIsRunning) {
            throw new CddNotRunningException("CDD.init() has not been called");
        }
    }
}
