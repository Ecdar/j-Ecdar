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
        this.isGuardDirty = true;
    }

    public CDD(long pointer) {
        this.pointer = pointer;
        this.isGuardDirty = true;
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
            cdd = CDD.allocateFromDbm(zone.getDbm(), numClocks);
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
            CddExtractionResult extraction = copy.extractBddAndDbm();
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

    public CddExtractionResult extractBddAndDbm()
            throws NullPointerException, CddNotRunningException {
        checkIfNotRunning();
        checkForNull();
        return new CddExtractionResult(CDDLib.extractBddAndDbm(pointer));
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
        isGuardDirty = true;
        return this;
    }

    public CDD delayInvar(CDD invariant)
            throws NullPointerException, CddNotRunningException {
        checkIfNotRunning();
        checkForNull();
        pointer = CDDLib.delayInvar(pointer, invariant.pointer);
        isGuardDirty = true;
        return this;
    }

    public CDD exist(int[] levels, int[] clocks)
            throws NullPointerException, CddNotRunningException {
        checkIfNotRunning();
        checkForNull();
        pointer = CDDLib.exist(pointer, levels, clocks);
        isGuardDirty = true;
        return this;
    }

    public CDD past()
            throws NullPointerException, CddNotRunningException {
        // TODO: make sure this is used at the correct spots everywhere, might have been confuces with delay
        checkIfNotRunning();
        checkForNull();
        pointer = CDDLib.past(pointer);
        isGuardDirty = true;
        return this;
    }

    public CDD removeNegative()
            throws NullPointerException, CddNotRunningException {
        checkIfNotRunning();
        checkForNull();
        pointer = CDDLib.removeNegative(pointer);
        isGuardDirty = true;
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
        isGuardDirty = true;
        return this;
    }

    public CDD transition(CDD guard, int[] clockResets, int[] clockValues, int[] boolResets, int[] boolValues)
            throws NullPointerException, CddNotRunningException {
        checkIfNotRunning();
        checkForNull();
        guard.checkForNull();
        pointer = CDDLib.transition(pointer, guard.pointer, clockResets, clockValues, boolResets, boolValues);
        removeNegative().reduce();
        isGuardDirty = true;
        return this;
    }

    public CDD predt(CDD safe) {
        checkIfNotRunning();
        checkForNull();
        safe.checkForNull();
        pointer = CDDLib.predt(pointer, safe.pointer);
        isGuardDirty = true;
        return this;
    }

    public CDD transitionBackPast(CDD guard, CDD update, int[] clockResets, int[] boolResets)
            throws NullPointerException, CddNotRunningException {
        checkIfNotRunning();
        checkForNull();
        guard.checkForNull();
        update.checkForNull();
        pointer = CDDLib.transitionBackPast(pointer, guard.pointer, update.pointer, clockResets, boolResets);
        isGuardDirty = true;
        return this;
    }

    public CDD reduce()
            throws NullPointerException, CddNotRunningException {
        checkIfNotRunning();
        checkForNull();
        pointer = CDDLib.reduce(pointer);
        isGuardDirty = true;
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

    public Federation toFederation() {
        // TODO: does not in any way take care of BDD parts (might run endless for BCDDs?)
        List<Zone> zoneList = new ArrayList<>();
        CDD copy = new CDD(this.pointer);
        while (!copy.isTerminal()) {
            copy = copy.reduce().removeNegative();
            CddExtractionResult res = copy.extractBddAndDbm();
            copy = res.getCddPart().reduce().removeNegative();
            Zone zone = new Zone(res.getDbm());
            zoneList.add(zone);
        }
        return new Federation(zoneList);
    }

    public CDD transition(Edge e) {
        if (e.getUpdates().size() == 0) {
            return this.conjunction(e.getGuardCDD());
        }
        int numBools = 0;
        int numClocks = 0;
        for (Update up : e.getUpdates()) {
            if (up instanceof ClockUpdate) numClocks++;
            if (up instanceof BoolUpdate) numBools++;
        }
        int[] clockResets = new int[numClocks];
        int[] clockValues = new int[numClocks];
        int[] boolResets = new int[numBools];
        int[] boolValues = new int[numBools];
        int cl = 0;
        int bl = 0;
        for (Update up : e.getUpdates()) {
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

        return this.transition(e.getGuardCDD(), clockResets, clockValues, boolResets, boolValues).removeNegative().reduce();
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

        int numBools = 0;
        int numClocks = 0;
        for (Update up : updates) {
            if (up instanceof ClockUpdate) numClocks++;
            if (up instanceof BoolUpdate) numBools++;
        }
        int[] clockResets = new int[numClocks];
        int[] boolResets = new int[numBools];
        int cl = 0;
        int bl = 0;
        for (Update up : updates) {
            if (up instanceof ClockUpdate) {
                ClockUpdate u = (ClockUpdate) up;
                clockResets[cl] = indexOf(u.getClock());
                cl++;
            }
            if (up instanceof BoolUpdate) {
                BoolUpdate u = (BoolUpdate) up;
                boolResets[bl] = bddStartLevel + indexOf(u.getBV());
                bl++;
            }
        }
        return this.transitionBack(guard, create(updates), clockResets, boolResets).removeNegative().reduce();
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
                res = res.conjunction(CDD.allocateInterval(indexOf(u.getClock()), 0, u.getValue(), true, u.getValue(), true));
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
        return CDD.allocateFromDbm(zone.getDbm(), numClocks);
    }

    public static CDD cddZeroDelayed() {
        Zone zone = new Zone(numClocks, false);
        zone.delay();
        return CDD.allocateFromDbm(zone.getDbm(), numClocks);
    }

    public static boolean isCddIsRunning() {
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

    @SafeVarargs
    public static void addBooleans(List<BoolVar>... BVs) {
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
    }

    public static void addBooleans(BoolVar... BVs) {
        addBooleans(
                Arrays.asList(BVs)
        );
    }

    public static CDD allocateInterval(int i, int j, int lower, boolean lower_included, int upper, boolean upper_included) {
        checkIfNotRunning();
        // TODO: Negation of lower strict should be moved to a new function allocate_interval function in the CDD library
        return new CDD(CDDLib.interval(i, j, lower, lower_included, upper, !upper_included)).removeNegative();
    }

    public static CDD allocateFromDbm(int[] dbm, int dim) {
        checkIfNotRunning();
        return new CDD(CDDLib.cddFromDbm(dbm, dim));
    }

    public static CDD allocateLower(int i, int j, int lowerBound, boolean strict) {
        checkIfNotRunning();
        return new CDD(CDDLib.lower(i, j, lowerBound, strict)).removeNegative();
    }

    public static CDD allocateUpper(int i, int j, int upperBound, boolean strict) {
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

    public static void free(CDD cdd) {
        cdd.checkForNull();
        CDDLib.freeCdd(cdd.pointer);
        cdd.pointer = 0;
    }

    public static CDD applyReset(CDD state, List<Update> list) {
        if (state.isFalse()) {
            return state;
        }

        if (list.size() == 0) {
            return state;
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
        return state.applyReset(clockResets, clockValues, boolResets, boolValues).removeNegative().reduce();
    }

    private static void checkIfNotRunning() {
        if (!cddIsRunning) {
            throw new CddNotRunningException("CDD.init() has not been called");
        }
    }

    public static CDD predt(CDD A, CDD B) {
        checkIfNotRunning();
        A.checkForNull();
        B.checkForNull();
        return new CDD(CDDLib.predt(A.pointer, B.pointer));
    }

    public static boolean canDelayIndefinitely(CDD state) {
        if (state.isTrue()) {
            return true;
        }
        if (state.isFalse()) {
            return false;
        }
        if (state.isBDD()) {
            return true;
        }

        while (!state.isTerminal()) {
            CddExtractionResult extraction = state.removeNegative().reduce().extractBddAndDbm();
            state = extraction.getCddPart().removeNegative().reduce();
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
            CddExtractionResult res = copy.removeNegative().reduce().extractBddAndDbm();
            Zone zone = new Zone(res.getDbm());
            copy = res.getCddPart().removeNegative().reduce();
            if (!zone.isUrgent()) {
                return false;
            }
        }
        return true;
    }

    public static boolean intersects(CDD A, CDD B) {
        return A.conjunction(B).isNotFalse();
    }

    public static boolean isSubset(CDD A, CDD B) {
        // TODO: check if correct
        return A.conjunction(B).equiv(A);
    }
}
