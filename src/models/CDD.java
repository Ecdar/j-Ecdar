package models;

import Exceptions.CddAlreadyRunningException;
import Exceptions.CddNotRunningException;
import lib.CDDLib;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class CDD {

    private long pointer;
    private static int numClocks; // includes the + 1 for initial clock
    private static boolean cddIsRunning;
    private static List<Clock> clocks = new ArrayList<>();


    public CDD(){
        this.pointer = CDDLib.allocateCdd();
    }


    public CDD(long pointer){
        this.pointer = pointer;
    }


    private int getIndexOfClock(Clock clock, List<Clock> clocks) {
        for (int i = 0; i < clocks.size(); i++){
            if(clock.hashCode() == clocks.get(i).hashCode()) return i+1;
        }
        return 0;
    }

    public CDD(List<List<Guard>> guards){
        CDD res = cddFalse();

        for (List<Guard> guardList: guards)
        {
            Zone z = new Zone(numClocks,true);
            z.init();
            for (Guard guard: guardList) {
                z.buildConstraintsForGuard(guard,getIndexOfClock(guard.getClock(),clocks));
            }
            res = res.disjunction(CDD.allocateFromDbm(z.getDbm(), numClocks));
        }
        this.pointer = res.pointer;
    }

    public static List<List<Guard>> toGuards(CDD state){
        List<List<Guard>> guards = new ArrayList<>();
        CDD copy = new CDD(state.pointer);
        while (!copy.isTerminal())
        {
            /*
            ExtractionResult res = CDD.extractDBMandCDD(copy);
            copy = res.getCDD();
            Zone z = res.getDBM();
            CDD bddPart = res.getBDDPart();
            List<Guard> guardList = z.buildGuardsFromZone(clocks);
            // guardList.add(bddPart.toGuards()); // TODO: once we have boolean
            */
        }
        return guards;
    }

    public long getPointer()
    {
        return pointer;
    }

    public static CDD cddTrue()
    {
        return new CDD(CDDLib.cddTrue());
    }

    public static CDD cddFalse()
    {
        return new CDD(CDDLib.cddFalse());
    }

    public boolean isTerminal()
    {
        return CDDLib.isTerminal(pointer);
    }

    public static int init(int maxSize, int cs, int stackSize) throws CddAlreadyRunningException {
        if(cddIsRunning){
            throw new CddAlreadyRunningException("Can't initialize when already running");
        }
        cddIsRunning = true;
        return CDDLib.cddInit(maxSize, cs, stackSize);
    }

    public boolean equiv(CDD that){
        return CDDLib.cddEquiv(this,that);
    }

    public static void done(){
        cddIsRunning = false;
        CDDLib.cddDone();
    }

    public static CDD zeroCDD()
    {
        Zone z = new Zone(numClocks,false);
        return CDD.allocateFromDbm(z.getDbm(),numClocks);
    }


    public static void addClocks(int amount) throws CddNotRunningException {
        if(!cddIsRunning){
            throw new CddNotRunningException("Can't add clocks without before running CDD.init");
        }
        CDDLib.cddAddClocks(amount);
    }

    public static int addBddvar(int amount) { return CDDLib.addBddvar(amount); }

    public static CDD allocate(){
        return new CDD();
    }

    public static CDD allocateInterval(int i, int j, int lower, int upper){
        return new CDD(CDDLib.interval(i,j,lower,upper));
    }

    public static CDD allocateFromDbm(int[] dbm, int dim){
        return new CDD(CDDLib.cddFromDbm(dbm, dim));
    }

    public static CDD allocateLower(int i, int j, int lowerBound) {
        return new CDD(CDDLib.lower(i,j,lowerBound));
    }

    public static CDD allocateUpper(int i, int j, int upperBound) {
        return new CDD(CDDLib.upper(i,j,upperBound));
    }

    public static CDD createBddNode(int level) {
        return new CDD(CDDLib.cddBddvar(level));
    }

    public static CDD createNegatedBddNode(int level) {
        return new CDD(CDDLib.cddNBddvar(level));
    }

    public static void free(CDD cdd){
        if(cdd.pointer == 0){
            throw new NullPointerException("CDD object is null");
        }
        CDDLib.freeCdd(cdd.pointer);
        cdd.pointer = 0;
    }

    public CDD copy(){
        checkForNull();
        return new CDD(CDDLib.copy(pointer));
    }

    public CDD delay()
    {
        checkForNull();
        return new CDD(CDDLib.delay(pointer));
    }

    public CDD delayInvar(CDD invariant)
    {
        checkForNull();
        return new CDD(CDDLib.delayInvar(pointer, invariant.pointer));
    }

    public CDD exist(int[] levels, int[] clocks){
        checkForNull();
        return new CDD(CDDLib.exist(pointer, levels, clocks));
    }

    public CDD past(){
        checkForNull();
        return new CDD(CDDLib.past(pointer));
    }

    public CDD removeNegative(){
        checkForNull();
        return new CDD(CDDLib.removeNegative(pointer));
    }

    public CDD applyReset(int[] clockResets, int[] clockValues, int[] boolResets, int[] boolValues){
        checkForNull();
        return new CDD(CDDLib.applyReset(pointer, clockResets, clockValues, boolResets, boolValues));
    }

    public CDD minus(CDD other){
        checkForNull();
        other.checkForNull();
        return new CDD(CDDLib.minus(pointer, other.pointer));
    }

    public CDD transition(CDD guard, int[] clockResets, int[] clockValues, int[] boolResets, int[] boolValues){
        checkForNull();
        guard.checkForNull();
        return new CDD(CDDLib.transition(pointer, guard.pointer, clockResets, clockValues, boolResets, boolValues));
    }

    public CDD conjunction(CDD other){
        checkForNull();
        other.checkForNull();
        long resultPointer = CDDLib.conjunction(pointer, other.pointer);
        return new CDD(resultPointer);
    }

    public CDD disjunction(CDD other){
        checkForNull();
        other.checkForNull();
        long resultPointer = CDDLib.disjunction(pointer, other.pointer);
        return new CDD(resultPointer);
    }

    public CDD negation() {
        checkForNull();
        long resultPointer = CDDLib.negation(pointer);
        return new CDD(resultPointer);
    }

    public CDD reduce() {
        checkForNull();
        long resultPointer = CDDLib.reduce(pointer);
        return new CDD(resultPointer);
    }

    public int getNodeCount(){
        checkForNull();
        return CDDLib.cddNodeCount(pointer);
    }

    public CDDNode getRoot(){
        checkForNull();
        long nodePointer = CDDLib.getRootNode(this.pointer);
        return new CDDNode(nodePointer);
    }

    public void printDot() {
        checkForNull();
        CDDLib.cddPrintDot(pointer);
    }

    public void printDot(String filePath){
        checkForNull();
        CDDLib.cddPrintDot(pointer, filePath);
    }

    public static CDD applyReset(CDD state, List<Update> updates)
    {
        // TODO: FUN;
        assert(false);
        return null;
    }


    private void checkForNull(){
        if(pointer == 0){
            throw new NullPointerException("CDD object is null");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CDD cdd = (CDD) o;
        return pointer == cdd.pointer;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pointer);
    }


    public CDD transition(Edge e)
    {
        int[] clockResets = new int[e.getUpdates().length];
        int[] clockValues = new int[e.getUpdates().length];
        int[] boolResets = {};
        int[] boolValues= {};
        int i=0;
        for (Update u : e.getUpdates())
        {
            clockResets[i]=getIndexOfClock(u.getClock(),clocks);
            clockValues[i]=u.getValue();
            i++;
        }

        return transition(e.getGuardCDD(),clockResets,clockValues,boolResets,boolValues);
    }

    public CDD transitionBack(Edge e)
    {
        int[] clockResets = new int[e.getUpdates().length];
        int[] clockValues = new int[e.getUpdates().length];
        int[] boolResets = {};
        int[] boolValues= {};
        int i=0;
        for (Update u : e.getUpdates())
        {
            clockResets[i]=getIndexOfClock(u.getClock(),clocks);
            clockValues[i]=u.getValue();
            i++;
        }
        assert(false); // TODO
        //return transitionBack(e.getGuardCDD(),clockResets,clockValues,boolResets,boolValues);
        return null;
    }




    private static Automaton makeInputEnabled(Automaton aut) {
        Automaton copy = new Automaton(aut);
        if (clocks.size() > 0) {
            for (Location loc : copy.getLocations()) {
                CDD fullCDD = loc.getInvariantCDD();
                // loop through all inputs
                for (Channel input : copy.getInputAct()) {
                    // build CDD of zones from edges
                    List<Edge> inputEdges = copy.getEdgesFromLocationAndSignal(loc, input);

                    CDD resCDD = cddTrue();
                    CDD cddOfAllInputs = cddFalse();
                    if (!inputEdges.isEmpty()) {
                        for (Edge edge : inputEdges) {
                            CDD target = edge.getTarget().getInvariantCDD();
                            CDD preReset = CDD.applyReset(target, Arrays.asList(edge.getUpdates()));
                            CDD preGuard = preReset.conjunction(edge.getGuardCDD());
                            CDD preGuard1 = target.transition(edge);
                            assert (preGuard1.equiv(preGuard));
                            cddOfAllInputs = cddOfAllInputs.disjunction(preGuard);
                        }

                        // subtract the federation of zones from the original fed
                        resCDD = fullCDD.minus(cddOfAllInputs);
                    } else {
                        resCDD = fullCDD;
                    }

                    Edge newEdge = new Edge(loc, loc, input, true, CDD.toGuards(resCDD), new Update[]{});
                    copy.getEdges().add(newEdge);


                }
            }
        }
        return  copy;
    }

    private static Automaton addTargetInvariantToEdges(Automaton aut) {

        Automaton copy = new Automaton(aut);
        if (clocks.size() > 0) {
            for (Edge edge : copy.getEdges()) {
                CDD targetCDD = edge.getTarget().getInvariantCDD();
                CDD past = targetCDD.transitionBack(edge);
                edge.setGuards(CDD.toGuards(past));
            }
        } // TODO: else part will be important once we have bool support
        return copy;
    }



}
