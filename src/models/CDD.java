package models;

import exceptions.CddAlreadyRunningException;
import exceptions.CddNotRunningException;
import lib.CDDLib;
import log.Log;
import log.Urgency;
import util.DeferredProperty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class CDD {
    private final DeferredProperty<Long> pointerProperty;
    private final DeferredProperty<Guard> guardProperty;
    private final DeferredProperty<CddExtractionResult> extractionProperty;
    private final DeferredProperty<?> isDelayedMarker;
    private final DeferredProperty<?> isDelayedInvariantMarker;
    private final DeferredProperty<?> isPastMarker;
    private final DeferredProperty<Integer> nodeCountProperty;
    private final DeferredProperty<CDDNode> rootNodeProperty;
    private final DeferredProperty<Boolean> isBddProperty;
    private final DeferredProperty<Boolean> isTerminalProperty;
    private final DeferredProperty<Boolean> isUnrestrainedProperty;
    private final DeferredProperty<Boolean> isEquivTrueProperty;
    private final DeferredProperty<Boolean> isEquivFalseProperty;
    private final DeferredProperty<Boolean> canDelayIndefinitelyProperty;
    private final DeferredProperty<Boolean> isUrgentProperty;
    private final DeferredProperty<?> removeNegativeMarker;
    private final DeferredProperty<?> reduceMarker;

    public CDD(long pointer) {
        extractionProperty = new DeferredProperty<>();
        isDelayedMarker = new DeferredProperty<>();
        isDelayedInvariantMarker = new DeferredProperty<>();
        isPastMarker = new DeferredProperty<>();
        nodeCountProperty = new DeferredProperty<>();
        guardProperty = new DeferredProperty<>();
        rootNodeProperty = new DeferredProperty<>();
        isBddProperty = new DeferredProperty<>();
        isTerminalProperty = new DeferredProperty<>();
        isUnrestrainedProperty = new DeferredProperty<>();
        isEquivTrueProperty = new DeferredProperty<>();
        isEquivFalseProperty = new DeferredProperty<>();
        canDelayIndefinitelyProperty = new DeferredProperty<>();
        isUrgentProperty = new DeferredProperty<>();
        removeNegativeMarker = new DeferredProperty<>();
        reduceMarker = new DeferredProperty<>();
        pointerProperty = new DeferredProperty<>(
                pointer,
                extractionProperty, isDelayedMarker, isDelayedInvariantMarker,
                isPastMarker, nodeCountProperty, guardProperty, rootNodeProperty,
                isBddProperty, isTerminalProperty, isUnrestrainedProperty, isEquivTrueProperty,
                isEquivFalseProperty, canDelayIndefinitelyProperty, isUrgentProperty,
                removeNegativeMarker, reduceMarker
        );
        markAsDirty();
    }

    public CDD() {
        this(CDDLib.allocateCdd());
    }

    private void markAsDirty() {
        pointerProperty.markAsDirty();
    }

    public long getPointer() {
        return pointerProperty.getValue();
    }

    private void setPointer(long newPointer) {
        markAsDirty();
        pointerProperty.set(newPointer);
    }

    public Guard getGuard(List<Clock> relevantClocks) {
        return guardProperty.trySet(
                GuardFactory.createFrom(this, relevantClocks)
        );
    }

    public Guard getGuard() {
        return getGuard(CDDRuntime.getAllClocks());
    }

    public void setGuard(Guard guard) {
        guardProperty.set(guard);
    }

    public CddExtractionResult extract() throws NullPointerException, CddNotRunningException {
        checkIfNotRunning();
        checkForNull();
        return extractionProperty.trySet(
                new CddExtractionResult(CDDLib.extractBddAndDbm(getPointer()))
        );
    }

    public int getNodeCount() throws NullPointerException {
        checkForNull();
        return nodeCountProperty.trySet(CDDLib.cddNodeCount(getPointer()));
    }

    public CDDNode getRoot() throws NullPointerException {
        checkForNull();
        return rootNodeProperty.trySet(
                new CDDNode(CDDLib.getRootNode(getPointer()))
        );
    }

    public boolean isBDD() throws NullPointerException {
        checkForNull();
        return isBddProperty.trySet(
                CDDLib.isBDD(getPointer())
        );
    }

    public boolean isTerminal() throws NullPointerException, CddNotRunningException {
        checkIfNotRunning();
        checkForNull();
        return isTerminalProperty.trySet(
                CDDLib.isTerminal(getPointer())
        );
    }

    public boolean isUnrestrained() {
        return isUnrestrainedProperty.trySet(
                this.equiv(cddTrue())
        );
    }

    public boolean equivTrue() throws NullPointerException {
        checkForNull();
        return isEquivTrueProperty.trySet(
                CDDLib.cddEquiv(getPointer(), cddTrue().getPointer())
        );
    }

    public boolean isNotEquivTrue() throws NullPointerException {
        checkForNull();
        return !equivTrue();
    }

    public boolean equivFalse() throws NullPointerException {
        checkForNull();
        return isEquivFalseProperty.trySet(
                CDDLib.cddEquiv(getPointer(), cddFalse().getPointer())
        );
    }

    public boolean isNotEquivFalse() {
        return !equivFalse();
    }

    public void free() throws NullPointerException {
        checkForNull();
        CDDLib.freeCdd(getPointer());
        setPointer(0);
    }

    public CDD applyReset(List<Update> list) {
        if (equivFalse()) {
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
                clockResets[cl] = CDDRuntime.indexOf(u.getClock());
                clockValues[cl] = u.getValue();
                cl++;
            }
            if (up instanceof BoolUpdate) {
                BoolUpdate u = (BoolUpdate) up;
                boolResets[bl] = CDDRuntime.getBddStartLevel() + CDDRuntime.indexOf(u.getBV());
                boolValues[bl] = u.getValue() ? 1 : 0;
                bl++;
            }
        }
        return applyReset(clockResets, clockValues, boolResets, boolValues);
    }

    public CDD applyReset(int[] clockResets, int[] clockValues, int[] boolResets, int[] boolValues) throws NullPointerException, CddNotRunningException {
        checkIfNotRunning();
        checkForNull();
        if (clockResets.length != clockValues.length) {
            throw new IllegalArgumentException("The amount of clock resets and values must be the same");
        }
        if (boolResets.length != boolValues.length) {
            throw new IllegalArgumentException("The amount of boolean resets and values must be the same");
        }

        setPointer(CDDLib.applyReset(getPointer(), clockResets, clockValues, boolResets, boolValues));
        removeNegative().reduce();
        markAsDirty();
        return this;
    }

    public boolean canDelayIndefinitely() {
        return canDelayIndefinitelyProperty.trySet(() -> {
            if (equivTrue()) {
                return true;
            }
            if (equivFalse()) {
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
        });
    }

    public boolean isUrgent() {
        return isUrgentProperty.trySet(() -> {
            if (equivTrue()) {
                return false;
            }
            if (equivFalse()) {
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
        });
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
        return new CDD(getPointer());
    }

    public CDD copy() throws NullPointerException, CddNotRunningException {
        checkIfNotRunning();
        checkForNull();
        return new CDD(CDDLib.copy(getPointer()));
    }

    public CDD delay() throws NullPointerException, CddNotRunningException {
        isDelayedMarker.clean(() -> {
            checkIfNotRunning();
            checkForNull();
            setPointer(CDDLib.delay(getPointer()));
        });
        markAsDirty();
        return this;
    }

    public CDD delayInvar(CDD invariant) throws NullPointerException, CddNotRunningException {
        isDelayedInvariantMarker.clean(() -> {
            checkIfNotRunning();
            checkForNull();
            setPointer(CDDLib.delayInvar(getPointer(), invariant.getPointer()));
        });
        return this;
    }

    public CDD exist(int[] levels, int[] clocks) throws NullPointerException, CddNotRunningException {
        checkIfNotRunning();
        checkForNull();
        setPointer(CDDLib.exist(getPointer(), levels, clocks));
        return this;
    }

    public CDD past() throws NullPointerException, CddNotRunningException {
        // TODO: make sure this is used at the correct spots everywhere, might have been confuces with delay
        isPastMarker.clean(() -> {
            checkIfNotRunning();
            checkForNull();
            setPointer(CDDLib.past(getPointer()));
        });
        return this;
    }

    public CDD removeNegative() throws NullPointerException, CddNotRunningException {
        removeNegativeMarker.clean(() -> {
            checkIfNotRunning();
            checkForNull();
            setPointer(CDDLib.removeNegative(getPointer()));
        });
        return this;
    }

    public CDD transition(CDD guard, int[] clockResets, int[] clockValues, int[] boolResets, int[] boolValues) throws NullPointerException, CddNotRunningException {
        checkIfNotRunning();
        checkForNull();
        guard.checkForNull();
        setPointer(CDDLib.transition(getPointer(), guard.getPointer(), clockResets, clockValues, boolResets, boolValues));
        removeNegative().reduce();
        return this;
    }

    public CDD transitionBackPast(CDD guard, CDD update, int[] clockResets, int[] boolResets) throws NullPointerException, CddNotRunningException {
        checkIfNotRunning();
        checkForNull();
        guard.checkForNull();
        update.checkForNull();
        setPointer(CDDLib.transitionBackPast(getPointer(), guard.getPointer(), update.getPointer(), clockResets, boolResets));
        return this;
    }

    public CDD reduce() throws NullPointerException, CddNotRunningException {
        reduceMarker.clean(() -> {
            checkIfNotRunning();
            checkForNull();
            setPointer(CDDLib.reduce(getPointer()));
        });
        return this;
    }

    public CDD predt(CDD safe) {
        checkIfNotRunning();
        checkForNull();
        safe.checkForNull();
        setPointer(CDDLib.predt(getPointer(), safe.getPointer()));
        return this;
    }

    public CDD minus(CDD other) throws NullPointerException, CddNotRunningException {
        checkIfNotRunning();
        checkForNull();
        other.checkForNull();
        return new CDD(CDDLib.minus(getPointer(), other.getPointer())).removeNegative().reduce();
    }

    public CDD negation() throws NullPointerException, CddNotRunningException {
        checkIfNotRunning();
        checkForNull();
        long resultPointer = CDDLib.negation(getPointer());
        return new CDD(resultPointer);
    }

    public CDD conjunction(CDD other) throws NullPointerException, CddNotRunningException {
        checkIfNotRunning();
        checkForNull();
        other.checkForNull();
        long resultPointer = CDDLib.conjunction(getPointer(), other.getPointer());
        return new CDD(resultPointer).reduce().removeNegative(); // tried to remove the reduce and remove negative, but that made a simpleversity test fail because rule 6 in the quotient on automata level did something funky (both spec and negated spec turned out to be cddtrue)
    }

    public CDD disjunction(CDD other) throws NullPointerException, CddNotRunningException {
        checkIfNotRunning();
        checkForNull();
        other.checkForNull();
        long resultPointer = CDDLib.disjunction(getPointer(), other.getPointer());
        return new CDD(resultPointer);
    }

    public boolean intersects(CDD other) {
        return conjunction(other).isNotEquivFalse();
    }

    public boolean isSubset(CDD other) {
        CDD hardCopy = hardCopy();
        return conjunction(other).equiv(hardCopy);
    }

    public boolean equiv(CDD that) throws NullPointerException {
        checkForNull();
        return CDDLib.cddEquiv(this.getPointer(), that.getPointer());
    }

    public void printDot() throws NullPointerException {
        checkForNull();
        CDDLib.cddPrintDot(getPointer());
    }

    public void printDot(String filePath) throws NullPointerException {
        checkForNull();
        CDDLib.cddPrintDot(getPointer(), filePath);
    }

    private void checkForNull() {
        if (getPointer() == 0) {
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
                int clockIndex = CDDRuntime.indexOf(clockUpdate.getClock());
                clockResets.add(clockIndex);
                int clockValue = clockUpdate.getValue();
                clockValues.add(clockValue);
            } else if (update instanceof BoolUpdate) {
                BoolUpdate boolUpdate = (BoolUpdate) update;
                int boolIndex = CDDRuntime.getBddStartLevel() + CDDRuntime.indexOf(boolUpdate.getBV());
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

    public CDD transitionBack(CDD guard, CDD update, int[] clockResets, int[] boolResets) throws NullPointerException, CddNotRunningException {
        checkIfNotRunning();
        checkForNull();
        guard.checkForNull();
        update.checkForNull();
        return new CDD(CDDLib.transitionBack(getPointer(), guard.getPointer(), update.getPointer(), clockResets, boolResets)).removeNegative().reduce();
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
                int clockIndex = CDDRuntime.indexOf(clockUpdate.getClock());
                clockUpdates.add(clockIndex);
            } else if (update instanceof BoolUpdate) {
                BoolUpdate boolUpdate = (BoolUpdate) update;
                int boolIndex = CDDRuntime.getBddStartLevel() + CDDRuntime.indexOf(boolUpdate.getBV());
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
        return getPointer() == other.getPointer();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPointer());
    }

    public static CDD create(List<Update> updates) {
        return CDDFactory.create(updates);
    }

    public static CDD create(BoolGuard guard) {
        return CDDFactory.create(guard);
    }

    public static CDD cddUnrestrained() {
        return CDD.cddTrue().removeNegative();
    }

    public static CDD cddTrue() throws CddAlreadyRunningException {
        checkIfNotRunning();
        return new CDD(CDDLib.cddTrue());
    }

    public static CDD cddFalse() {
        checkIfNotRunning();
        return new CDD(CDDLib.cddFalse());
    }

    public static CDD cddZero() {
        Zone zone = new Zone(CDDRuntime.getNumberOfClocks(), false);
        return CDD.createFromDbm(zone.getDbm(), CDDRuntime.getNumberOfClocks());
    }

    public static CDD cddZeroDelayed() {
        Zone zone = new Zone(CDDRuntime.getNumberOfClocks(), false);
        zone.delay();
        return CDD.createFromDbm(zone.getDbm(), CDDRuntime.getNumberOfClocks());
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
        if (!CDDRuntime.isRunning()) {
            throw new CddNotRunningException("CDD.init() has not been called");
        }
    }
}
