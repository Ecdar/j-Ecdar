package models;

import exceptions.CddAlreadyRunningException;
import exceptions.CddNotRunningException;
import lib.CDDLib;
import util.DeferredProperty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class CDD {

    /**
     * This {@link CDD} converted to a {@link Guard}.
     */
    private final DeferredProperty<Guard> guardProperty = new DeferredProperty<>();

    /**
     * The {@link DeferredProperty} used as a marker to see if {@link CDD#delay()} should skip the
     * {@link CDDLib#delay(long)} call, as it won't change the current {@link CDD#pointerProperty} value.
     */
    private final DeferredProperty<?> isDelayedMarker = new DeferredProperty<>();

    /**
     * The {@link DeferredProperty} used as a marker to see if {@link CDD#delayInvar(CDD)} should skip the
     * {@link CDDLib#delayInvar(long, long)} call, as it won't change the current {@link CDD#pointerProperty} value.
     */
    private final DeferredProperty<?> isDelayedInvariantMarker = new DeferredProperty<>();

    /**
     * The {@link DeferredProperty} used as a marker to see if {@link CDD#past()} should skip the
     * {@link CDDLib#past(long)} call, as it won't change the current {@link CDD#pointerProperty} value.
     */
    private final DeferredProperty<?> isPastMarker = new DeferredProperty<>();

    /**
     * The bottom part of a {@link CDD} are BDD nodes. If {@link DeferredProperty#get()} returns  <code>true</code>
     * then we are within the BDD part of the {@link CDD}.
     *
     * If {@link DeferredProperty#get()} returns <code>null</code> then it is likely that the {@link DeferredProperty}
     * needs to be re-computed which is the case if {@link DeferredProperty#isDirty()} returns <code>true</code>.
     */
    private final DeferredProperty<Boolean> isBddProperty = new DeferredProperty<>();

    /**
     * The bottom most nodes labeled either <code>true</code> or <code>false</code> are called terminal nodes.
     * These nodes have only ingoing edges and no outgoing.
     * If {@link DeferredProperty#get()} returns <code>true</code>, then the {@link CDD#getPointer()} points at one of these terminal nodes.
     * If this is the case, then this {@link CDD} is a BDD ({@link CDD#isBDD()} is <code>true</code>).
     *
     * If {@link DeferredProperty#get()} returns <code>null</code> then it is likely that the {@link DeferredProperty}
     * needs to be re-computed which is the case if {@link DeferredProperty#isDirty()} returns <code>true</code>.
     */
    private final DeferredProperty<Boolean> isTerminalProperty = new DeferredProperty<>();

    /**
     * This property is <code>true</code> if this {@link CDD} is urgent. Otherwise, it is not urgent.
     *
     * If {@link DeferredProperty#get()} returns <code>null</code> then it is likely that the {@link DeferredProperty}
     * needs to be re-computed which is the case if {@link DeferredProperty#isDirty()} returns <code>true</code>.
     */
    private final DeferredProperty<Boolean> isUrgentProperty = new DeferredProperty<>();

    /**
     * This property is <code>true</code> if this {@link CDD} is unrestrained (I.e., {@link CDD#equivTrue()} is <code>true</code>).
     * Otherwise, it is not unrestrained.
     *
     * If {@link DeferredProperty#get()} returns <code>null</code> then it is likely that the {@link DeferredProperty}
     * needs to be re-computed which is the case if {@link DeferredProperty#isDirty()} returns <code>true</code>.
     */
    private final DeferredProperty<Boolean> isUnrestrainedProperty = new DeferredProperty<>();

    /**
     * This property is <code>true</code> if this {@link CDD} is equivalent to the {@link CDD#cddTrue()}.
     * However, if this {@link CDD} is not equivalent to {@link CDD#cddTrue()} then it is not guaranteed that it
     * is equivalent to {@link CDD#cddFalse()}.
     *
     * If {@link DeferredProperty#get()} returns <code>null</code> then it is likely that the {@link DeferredProperty}
     * needs to be re-computed which is the case if {@link DeferredProperty#isDirty()} returns <code>true</code>.
     */
    private final DeferredProperty<Boolean> isEquivTrueProperty = new DeferredProperty<>();

    /**
     * This property is <code>true</code> if this {@link CDD} is equivalent to the {@link CDD#cddFalse()}.
     * However, if this {@link CDD} is not equivalent to {@link CDD#cddFalse()} then it is not guaranteed that it
     * is equivalent to {@link CDD#cddTrue()}.
     *
     * If {@link DeferredProperty#get()} returns <code>null</code> then it is likely that the {@link DeferredProperty}
     * needs to be re-computed which is the case if {@link DeferredProperty#isDirty()} returns <code>true</code>.
     */
    private final DeferredProperty<Boolean> isEquivFalseProperty = new DeferredProperty<>();

    /**
     * This property is <code>true</code> if this {@link CDD} can indefinitely be delayed.
     * Otherwise, if <code>false</code> it cannot be indefinitely delayed.
     *
     * If {@link DeferredProperty#get()} returns <code>null</code> then it is likely that the {@link DeferredProperty}
     * needs to be re-computed which is the case if {@link DeferredProperty#isDirty()} returns <code>true</code>.
     */
    private final DeferredProperty<Boolean> canDelayIndefinitelyProperty = new DeferredProperty<>();

    /**
     * The {@link DeferredProperty} used as a marker to see if {@link CDD#removeNegative()} should skip the
     * {@link CDDLib#removeNegative(long)} call, as it won't change the current {@link CDD#pointerProperty} value.
     */
    private final DeferredProperty<?> removeNegativeMarker = new DeferredProperty<>();

    /**
     * This property stored the {@link CddExtractionResult} if this {@link CDD} has made a {@link CDD#extract()} call before {@link CDD#setPointer(long)}.
     * Otherwise, it is dirty and returns <code>null</code>.
     *
     * If {@link DeferredProperty#get()} returns <code>null</code> then it is likely that the {@link DeferredProperty}
     * needs to be re-computed which is the case if {@link DeferredProperty#isDirty()} returns <code>true</code>.
     */
    private final DeferredProperty<CddExtractionResult> extractionProperty = new DeferredProperty<>();

    /**
     * This property stores the node count of this {@link CDD}.
     *
     * If {@link DeferredProperty#get()} returns <code>null</code> then it is likely that the {@link DeferredProperty}
     * needs to be re-computed which is the case if {@link DeferredProperty#isDirty()} returns <code>true</code>.
     */
    private final DeferredProperty<Integer> nodeCountProperty = new DeferredProperty<>();

    /**
     * This property stores the {@link CDDNode} for the root node.
     *
     * If {@link DeferredProperty#get()} returns <code>null</code> then it is likely that the {@link DeferredProperty}
     * needs to be re-computed which is the case if {@link DeferredProperty#isDirty()} returns <code>true</code>.
     */
    private final DeferredProperty<CDDNode> rootNodeProperty = new DeferredProperty<>();

    /**
     * The {@link DeferredProperty} used as a marker to see if {@link CDD#reduce()} should skip the
     * {@link CDDLib#reduce(long)} call, as it won't change the current {@link CDD#pointerProperty} value.
     */
    private final DeferredProperty<?> reduceMarker = new DeferredProperty<>();

    /**
     * The pointer used in the {@link CDDLib} to represent this {@link CDD}.
     *
     * The pointer is the root {@link DeferredProperty} meaning that it manages all the observers which are
     * {@link DeferredProperty DeferredProperties} that should be marked as dirty as soon as their parent property
     * has been marked as dirty.
     *
     * When the {@link CDD#pointerProperty} gets dirty so does e.g. {@link CDD#guardProperty}.
     */
    private final DeferredProperty<Long> pointerProperty = new DeferredProperty<>(
            0L,
            guardProperty, isDelayedMarker, isDelayedInvariantMarker,
            isPastMarker, isBddProperty, isTerminalProperty, isUrgentProperty,
            isUnrestrainedProperty, isEquivTrueProperty, isEquivFalseProperty,
            canDelayIndefinitelyProperty, removeNegativeMarker, extractionProperty,
            nodeCountProperty, rootNodeProperty, reduceMarker
    );
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

    public CDD(long pointer) {
        setPointer(pointer);
    }

    public CDD() {
        this(CDDLib.allocateCdd());
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
        guardProperty.set(guard);
        setPointer(cdd.getPointer());
    }

    public Guard getGuard(List<Clock> relevantClocks) {
        // Only if the guard property is dirty do we compute a new value.
        // Always, do we return the current value of the property.
        return guardProperty.trySet(
                () -> isBDD() ? toBoolGuards() : toClockGuards(relevantClocks)
        );
    }

    public Guard getGuard() {
        return getGuard(clocks);
    }

    private Guard toClockGuards(List<Clock> relevantClocks)
            throws IllegalArgumentException {
        if (isBDD()) {
            throw new IllegalArgumentException("CDD is a BDD");
        }

        if (equivFalse()) {
            return new FalseGuard();
        }
        if (equivTrue()) {
            return new TrueGuard();
        }

        // Required as we don't want to alter the pointer value of "this".
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

        if (equivFalse()) {
            return new FalseGuard();
        }
        if (equivTrue()) {
            return new TrueGuard();
        }

        long ptr = getPointer();
        BDDArrays arrays = new BDDArrays(CDDLib.bddToArray(ptr));

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
        // We have to use getValue as we don't want to return null if the pointer property is dirty.
        return pointerProperty.getValue();
    }

    private void setPointer(long newPointer) {
        pointerProperty.markAsDirty();
        pointerProperty.set(newPointer);
    }

    public int getNodeCount()
            throws NullPointerException {
        checkForNull();
        return CDDLib.cddNodeCount(getPointer());
    }

    public CDDNode getRoot()
            throws NullPointerException {
        checkForNull();
        long nodePointer = CDDLib.getRootNode(getPointer());
        return new CDDNode(nodePointer);
    }

    public CddExtractionResult extract()
            throws NullPointerException, CddNotRunningException {
        return extractionProperty.trySet(() -> {
            checkIfNotRunning();
            checkForNull();
            return new CddExtractionResult(
                    CDDLib.extractBddAndDbm(reduce().getPointer())
            );
        });
    }

    public boolean isBDD()
            throws NullPointerException {
        return isBddProperty.trySet(() -> {
            checkForNull();
            checkIfNotRunning();
            // TODO: Make this terminal checks.
            return isTrue() || isFalse() || CDDLib.isBDD(getPointer());
        });
    }

    public boolean isTerminal()
            throws NullPointerException, CddNotRunningException {
        return isTerminalProperty.trySet(() -> {
            checkIfNotRunning();
            checkForNull();
            return CDDLib.isTerminal(getPointer());
        });
    }

    public boolean isTrue()
        throws NullPointerException, CddNotRunningException {
        checkIfNotRunning();
        checkForNull();
        return CDDLib.isTrue(getPointer());
    }

    public boolean isFalse()
        throws NullPointerException, CddNotRunningException {
        checkIfNotRunning();
        checkForNull();
        return CDDLib.isFalse(getPointer());
    }

    public boolean isUnrestrained() {
        return isUnrestrainedProperty.trySet(
                this.equiv(cddTrue())
        );
    }

    public boolean notEquivFalse() {
        return !equivFalse();
    }

    public boolean equivFalse()
            throws NullPointerException {
        return isEquivFalseProperty.trySet(() -> {
            checkIfNotRunning();
            checkForNull();

            if (isFalse()) {
                return true;
            }

            return CDDLib.cddEquiv(getPointer(), cddFalse().getPointer());
        });
    }

    public boolean notEquivTrue()
            throws NullPointerException {
        checkForNull();
        return !equivTrue();
    }

    public boolean equivTrue()
            throws NullPointerException {
        return isEquivTrueProperty.trySet(() -> {
            checkIfNotRunning();
            checkForNull();

            if (isTrue()) {
                return true;
            }

            return CDDLib.cddEquiv(getPointer(), cddTrue().getPointer());
        });
    }

    public void free()
            throws NullPointerException {
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
     * is a pass-by-value the immediate not operator invocations won't alter the pointer
     * value of the original (this.pointer) retrieved through {@link #getPointer()}.
     *
     * @return Returns a new CDD which is not created through {@link CDDLib#copy(long)} but with a pointer copy.
     */
    public CDD hardCopy() {
        return new CDD(getPointer());
    }

    public CDD copy()
            throws NullPointerException, CddNotRunningException {
        checkIfNotRunning();
        checkForNull();
        return new CDD(CDDLib.copy(getPointer()));
    }

    public CDD delay()
            throws NullPointerException, CddNotRunningException {
        isDelayedMarker.clean(() -> {
            checkIfNotRunning();
            checkForNull();
            setPointer(CDDLib.delay(getPointer()));
        });
        return this;
    }

    public CDD delayInvar(CDD invariant)
            throws NullPointerException, CddNotRunningException {
        isDelayedInvariantMarker.clean(() -> {
            checkIfNotRunning();
            checkForNull();
            setPointer(CDDLib.delayInvar(getPointer(), invariant.getPointer()));
        });
        return this;
    }

    public CDD exist(int[] levels, int[] clocks)
            throws NullPointerException, CddNotRunningException {
        checkIfNotRunning();
        checkForNull();
        setPointer(CDDLib.exist(getPointer(), levels, clocks));
        return this;
    }

    public CDD past()
            throws NullPointerException, CddNotRunningException {
        // TODO: make sure this is used at the correct spots everywhere, might have been confused with delay
        isPastMarker.clean(() -> {
            checkIfNotRunning();
            checkForNull();
            setPointer(CDDLib.past(getPointer()));
        });
        return this;
    }

    public CDD removeNegative()
            throws NullPointerException, CddNotRunningException {
        removeNegativeMarker.clean(() -> {
            checkIfNotRunning();
            checkForNull();
            setPointer(CDDLib.removeNegative(getPointer()));
        });
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

        setPointer(
                CDDLib.applyReset(getPointer(), clockResets, clockValues, boolResets, boolValues)
        );
        removeNegative().reduce();
        return this;
    }

    public CDD transition(CDD guard, int[] clockResets, int[] clockValues, int[] boolResets, int[] boolValues)
            throws NullPointerException, CddNotRunningException {
        checkIfNotRunning();
        checkForNull();
        guard.checkForNull();
        setPointer(
                CDDLib.transition(getPointer(), guard.getPointer(), clockResets, clockValues, boolResets, boolValues)
        );
        removeNegative().reduce();
        return this;
    }

    public CDD transitionBackPast(CDD guard, CDD update, int[] clockResets, int[] boolResets)
            throws NullPointerException, CddNotRunningException {
        checkIfNotRunning();
        checkForNull();
        guard.checkForNull();
        update.checkForNull();
        setPointer(
                CDDLib.transitionBackPast(getPointer(), guard.getPointer(), update.getPointer(), clockResets, boolResets)
        );
        return this;
    }

    public CDD reduce()
            throws NullPointerException, CddNotRunningException {
        checkIfNotRunning();
        checkForNull();

        if (isTerminal()) {
            return this;
        }

        setPointer(CDDLib.reduce(getPointer()));
        return this;
    }

    public CDD predt(CDD safe) {
        checkIfNotRunning();
        checkForNull();
        safe.checkForNull();
        setPointer(CDDLib.predt(getPointer(), safe.getPointer()));
        return this;
    }

    public CDD minus(CDD other)
            throws NullPointerException, CddNotRunningException {
        checkIfNotRunning();
        checkForNull();
        other.checkForNull();

        if (equivFalse() || other.equivFalse()) {
            return this;
        }

        if (other.equivTrue()) {
            return cddFalse();
        }

        if (other.getPointer() == getPointer()) {
            return cddFalse();
        }

        return new CDD(CDDLib.minus(getPointer(), other.getPointer())).removeNegative().reduce();
    }

    public CDD negation()
            throws NullPointerException, CddNotRunningException {
        checkIfNotRunning();
        checkForNull();

        if (equivFalse()) {
            return cddTrue();
        }

        if (equivTrue()) {
            return cddFalse();
        }

        return new CDD(CDDLib.negation(getPointer()));
    }

    public CDD conjunction(CDD other)
            throws NullPointerException, CddNotRunningException {
        checkIfNotRunning();
        checkForNull();
        other.checkForNull();

        if (equivFalse()) {
            return cddFalse();
        }

        if (equivTrue()) {
            return other.hardCopy();
        }

        if (other.equivFalse()) {
            return cddFalse();
        }

        if (other.equivTrue()) {
            return hardCopy();
        }

        if (other.getPointer() == getPointer()) {
            return hardCopy();
        }

        return new CDD(CDDLib.conjunction(getPointer(), other.getPointer())).reduce().removeNegative();
    }

    public CDD disjunction(CDD other)
            throws NullPointerException, CddNotRunningException {
        checkIfNotRunning();
        checkForNull();
        other.checkForNull();

        if (equivFalse()) {
            return other.hardCopy();
        }

        if (equivTrue()) {
            return cddTrue();
        }

        if (other.equivFalse()) {
            return hardCopy();
        }

        if (other.equivTrue()) {
            return cddTrue();
        }

        if (other.getPointer() == getPointer()) {
            return hardCopy();
        }

        return new CDD(CDDLib.disjunction(getPointer(), other.getPointer()));
    }

    public boolean intersects(CDD other) {
        return conjunction(other).notEquivFalse();
    }

    public boolean isSubset(CDD other) {
        CDD hardCopy = hardCopy();
        return conjunction(other).equiv(hardCopy);
    }

    public boolean equiv(CDD that)
            throws NullPointerException {
        checkForNull();
        return CDDLib.cddEquiv(this.getPointer(), that.getPointer());
    }

    public void printDot()
            throws NullPointerException {
        checkForNull();
        CDDLib.cddPrintDot(getPointer());
    }

    public void printDot(String filePath)
            throws NullPointerException {
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
        // Required as we don't want to alter the pointer value of "this".
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

        return new CDD(CDDLib.transitionBack(getPointer(), guard.getPointer(), update.getPointer(), clockResets, boolResets));
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
        return getPointer() == other.getPointer();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPointer());
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
        return CDD.cddTrue();
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
