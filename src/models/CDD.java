package models;

import exceptions.CddAlreadyRunningException;
import exceptions.CddNotRunningException;
import lib.CDDLib;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class CDD {

    public static int maxSize = 1000;
    public static int cs = 1000;
    public static int stackSize = 1000;
    private long pointer;
    public static int numClocks; // includes the + 1 for initial clock
    public static int numBools;
    public static int bddStartLevel;

    public static boolean isCddIsRunning() {
        return cddIsRunning;
    }

    static boolean cddIsRunning;
    private static List<Clock> clocks = new ArrayList<>();
    public static List<BoolVar> BVs = new ArrayList<>();


    public CDD(){
        checkIfRunning();
        this.pointer = CDDLib.allocateCdd();
    }


    public CDD(long pointer){
        this.pointer = pointer;
    }


    public static int getIndexOfClock(Clock clock) {

        for (int i = 0; i < clocks.size(); i++){
            if(clock.hashCode() == clocks.get(i).hashCode()) return i+1;
        }
        System.out.println("clock " + clock + " not in " + clocks);
        assert(false);
        return 0;
    }

    public static int getIndexOfBV(BoolVar bv) {

        for (int i = 0; i < BVs.size(); i++){
            if(bv.equals(BVs.get(i))) return i;
        }
        assert(false);
        return 0;
    }

    public CDD(Guard guard){
        CDD res = cddFalse();
        if (guard instanceof FalseGuard) {
            res = cddFalse();
        }
        else if (guard instanceof TrueGuard) {
            res = cddTrue();
        }
        else if (guard instanceof ClockGuard) {
            Zone z = new Zone(numClocks,true);
            z.init();
            z.buildConstraintsForGuard((ClockGuard) guard, clocks);
            res = CDD.allocateFromDbm(z.getDbm(),numClocks);
        }
        else if (guard instanceof BoolGuard) {
            res = fromBoolGuard((BoolGuard) guard);
        }
        else if (guard instanceof AndGuard) {
            res = cddTrue();
            for (Guard g : ((AndGuard)guard).getGuards())
            {
                res = res.conjunction(new CDD(g));
            }
        }
        else if (guard instanceof OrGuard) {
            res = cddFalse();
            for (Guard g : ((OrGuard)guard).getGuards())
            {
                res = res.disjunction(new CDD(g));
            }
        }
        else
        {
            assert(false);
        }
        this.pointer = res.pointer;
    }

    public static CDD fromBoolGuard(BoolGuard guard)
    {
        if (guard.getValue())
            return createBddNode(bddStartLevel + getIndexOfBV(guard.getVar()));
        else
            return createNegatedBddNode(bddStartLevel + getIndexOfBV(guard.getVar()));
    }

    public static Guard toGuardList(CDD state, List<Clock> relevantClocks){
        CDD copy = new CDD(state.pointer);
        if (copy.equiv(cddFalse())) // special case for guards
        {
            return new FalseGuard();
        }
        if (copy.equiv(cddTrue())) // special case for guards
        {
            return new TrueGuard();
        }
        if (copy.isTrue()) // special case for guards
        {
            assert(false);
            //System.out.println("to true guard --> why did I not go into the first one??");
            return new TrueGuard();
        }
        if (copy.isBDD())
        {
            return CDD.toBoolGuards(copy);
        }
        else {
            List<Guard> orParts = new ArrayList<>();
            while (!copy.isTerminal()) {
                copy = copy.reduce().removeNegative();
                CddExtractionResult res = copy.extractBddAndDbm();
                copy = res.getCddPart().reduce().removeNegative();
                Zone z = new Zone(res.getDbm());
                CDD bddPart = res.getBddPart();
                List<Guard> andParts = new ArrayList<>();
                andParts.add(z.buildGuardsFromZone(clocks, relevantClocks));
                andParts.add(CDD.toBoolGuards(bddPart));
                andParts = andParts.stream().filter(e -> !(e instanceof TrueGuard)).collect(Collectors.toList());
                if (andParts.isEmpty())
                    andParts.add(new TrueGuard());
                orParts.add(new AndGuard(andParts));
            }
            return new OrGuard(orParts);
        }
    }

    public boolean isBDD()
    {
        return CDDLib.isBDD(this.pointer);
    }

    public static Guard toBoolGuards(CDD bdd){

        if (bdd.isFalse()) {
            return new FalseGuard();
        }
        if (bdd.isTrue()) {
            return new TrueGuard();
        }
        assert(bdd.isBDD());

        long ptr = bdd.getPointer();
        BDDArrays arrays = new BDDArrays(CDDLib.bddToArray(ptr,numBools));


        List<Guard> orParts = new ArrayList<>();
        for (int i=0; i< arrays.numTraces; i++)
        {
            List<Guard> andParts = new ArrayList<>();
            for (int j=0; j< arrays.numBools; j++)
            {

                int index = arrays.getVars().get(i).get(j);
                if (index>=0) {
                    BoolVar var = BVs.get(index-bddStartLevel);
                    boolean val = (arrays.getValues().get(i).get(j) == 1) ? true : false;
                    BoolGuard bg = new BoolGuard(var, "==", val);
                    andParts.add(bg);
                }
            }
            orParts.add(new AndGuard(andParts));
        }
        return new OrGuard(orParts);
    }

    public static List<Clock> getClocks() {
        return clocks;
    }

    @Override
    public String toString()
    {
        return CDD.toGuardList(this,clocks).toString();
    }


    public long getPointer()
    {
        return pointer;
    }

    public static CDD cddTrue()
    {
        checkIfRunning();
        return new CDD(CDDLib.cddTrue());
    }



    public boolean isTerminal()
    {
        checkIfRunning();
        checkForNull();
        return CDDLib.isTerminal(pointer);
    }

    public static int init(int maxSize, int cs, int stackSize) throws CddAlreadyRunningException {
        if(cddIsRunning){
            throw new CddAlreadyRunningException("Can't initialize when already running");
        }
        cddIsRunning = true;
        return CDDLib.cddInit(maxSize, cs, stackSize);
    }

    public static CDD cddFalse()
    {
        checkIfRunning();
        return new CDD(CDDLib.cddFalse());
    }

    public boolean isNotFalse() {
        return !isFalse();
    }

    public boolean isFalse() {
        return CDDLib.cddEquiv(this.pointer,cddFalse().pointer);
    }

    public boolean isTrue() {
        return CDDLib.cddEquiv(this.pointer,cddTrue().pointer);
    }


    public boolean equiv(CDD that){
        return CDDLib.cddEquiv(this.pointer,that.pointer);
    }

    public static void done(){
        cddIsRunning = false;
        numClocks = 0;
        numBools= 0;
        clocks = new ArrayList<>();
        BVs = new ArrayList<>();
        CDDLib.cddDone();
    }

    public static void ensure_done(){
        if (cddIsRunning) {
            done();
        }
    }

    public static CDD zeroCDD()
    {
        Zone z = new Zone(numClocks,false);
        return CDD.allocateFromDbm(z.getDbm(),numClocks);
    }

    public static CDD zeroCDDDelayed()
    {
        Zone z = new Zone(numClocks,false);
        z.delay();
        return CDD.allocateFromDbm(z.getDbm(),numClocks);
    }

    @SafeVarargs
    public static void addClocks(List<Clock>... clocks) {
        checkIfRunning();
        for (List<Clock> list: clocks)
            CDD.clocks.addAll(list);
        numClocks = CDD.clocks.size()+1;
        CDDLib.cddAddClocks(numClocks);
    }

    public static int addBddvar(List<BoolVar>... BVs) {
        checkIfRunning();
        for (List<BoolVar> list: BVs)
            CDD.BVs.addAll(list);
        numBools = CDD.BVs.size();
        if (numBools>0)
            bddStartLevel =  CDDLib.addBddvar(numBools);
        else
            bddStartLevel = 0;
        return bddStartLevel;
    }

    public static CDD allocate(){
        checkIfRunning();
        return new CDD();
    }

    public static CDD allocateInterval(int i, int j, int lower, boolean lower_included, int upper, boolean upper_included){
        checkIfRunning();
        // TODO: Negation of lower strict should be moved to a new function allocate_interval function in the CDD library
        return new CDD(CDDLib.interval(i,j,lower, lower_included,upper, !upper_included)).removeNegative();
    }

    public static CDD allocateFromDbm(int[] dbm, int dim){
        checkIfRunning();
        return new CDD(CDDLib.cddFromDbm(dbm, dim));
    }

    public static CDD allocateLower(int i, int j, int lowerBound, boolean strict) {
        checkIfRunning();
        return new CDD(CDDLib.lower(i,j,lowerBound,strict)).removeNegative();
    }

    public static CDD allocateUpper(int i, int j, int upperBound, boolean strict) {
        checkIfRunning();
        return new CDD(CDDLib.upper(i,j,upperBound,strict)).removeNegative();
    }

    public static CDD createBddNode(int level) {
        checkIfRunning();
        return new CDD(CDDLib.cddBddvar(level));
    }

    public static CDD createNegatedBddNode(int level) {
        checkIfRunning();
        return new CDD(CDDLib.cddNBddvar(level));
    }

    public static void free(CDD cdd){
        cdd.checkForNull();
        CDDLib.freeCdd(cdd.pointer);
        cdd.pointer = 0;
    }

    public CDD copy(){
        checkIfRunning();
        checkForNull();
        return new CDD(CDDLib.copy(pointer));
    }

    public CDD delay() {
        checkIfRunning();
        checkForNull();
        return new CDD(CDDLib.delay(pointer));
    }

    public CDD delayInvar(CDD invariant) {
        checkIfRunning();
        checkForNull();
        return new CDD(CDDLib.delayInvar(pointer, invariant.pointer));
    }

    public CDD exist(int[] levels, int[] clocks){
        checkIfRunning();
        checkForNull();
        return new CDD(CDDLib.exist(pointer, levels, clocks));
    }

    public CDD past(){ // TODO: make sure this is used at the correct spots everywhere, might have been confuces with delay
        checkIfRunning();
        checkForNull();
        return new CDD(CDDLib.past(pointer));
    }

    public CDD removeNegative(){
        checkIfRunning();
        checkForNull();
        return new CDD(CDDLib.removeNegative(pointer));
    }

    public CDD applyReset(int[] clockResets, int[] clockValues, int[] boolResets, int[] boolValues){
        checkIfRunning();
        checkForNull();
        assert(clockResets.length==clockValues.length);
        assert(boolResets.length==boolValues.length);
        return new CDD(CDDLib.applyReset(pointer, clockResets, clockValues, boolResets, boolValues)).removeNegative().reduce();
    }

    public CDD minus(CDD other){
        checkIfRunning();
        checkForNull();
        other.checkForNull();
        return new CDD(CDDLib.minus(pointer, other.pointer)).removeNegative().reduce();
    }

    public CDD transition(CDD guard, int[] clockResets, int[] clockValues, int[] boolResets, int[] boolValues){
        checkIfRunning();
        checkForNull();
        guard.checkForNull();
        return new CDD(CDDLib.transition(pointer, guard.pointer, clockResets, clockValues, boolResets, boolValues)).removeNegative().reduce();
    }

    public CDD transitionBack(CDD guard, CDD update, int[] clockResets, int[] boolResets){
        checkIfRunning();
        checkForNull();
        guard.checkForNull();
        update.checkForNull();
        return new CDD(CDDLib.transitionBack(pointer, guard.pointer, update.pointer, clockResets, boolResets)).removeNegative().reduce();
    }

    public CDD predt(CDD safe){
        checkIfRunning();
        checkForNull();
        safe.checkForNull();
        return new CDD(CDDLib.predt(pointer, safe.pointer));
    }

    public CddExtractionResult extractBddAndDbm(){
        checkIfRunning();
        checkForNull();
        return new CddExtractionResult(CDDLib.extractBddAndDbm(pointer));
    }

    public CDD transitionBackPast(CDD guard, CDD update, int[] clockResets, int[] boolResets){
        checkIfRunning();
        checkForNull();
        guard.checkForNull();
        update.checkForNull();

        return new CDD(CDDLib.transitionBackPast(pointer, guard.pointer, update.pointer, clockResets, boolResets));
    }

    public CDD conjunction(CDD other){
        checkIfRunning();
        checkForNull();
        other.checkForNull();
        long resultPointer = CDDLib.conjunction(pointer, other.pointer);
        return new CDD(resultPointer);//.reduce().removeNegative();
    }

    public CDD disjunction(CDD other){
        checkIfRunning();
        checkForNull();
        other.checkForNull();
        long resultPointer = CDDLib.disjunction(pointer, other.pointer);
        return new CDD(resultPointer);
    }

    public CDD negation() {
        checkIfRunning();
        checkForNull();
        long resultPointer = CDDLib.negation(pointer);
        return new CDD(resultPointer);
    }

    public CDD reduce() {
        checkIfRunning();
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



    public static CDD applyReset(CDD state, List<Update> list)
    {
        if (state.isFalse())
            return state;
        if (list.size()==0)
        {
            return state;
        }
        int numBools = 0;
        int numClocks = 0;
        for (Update up : list)
        {
            if (up instanceof ClockUpdate) numClocks ++;
            if (up instanceof BoolUpdate) numBools ++;
        }
        int[] clockResets = new int[numClocks];
        int[] clockValues = new int[numClocks];
        int[] boolResets = new int[numBools];
        int[] boolValues= new int[numBools];
        int cl=0;
        int bl=0;
        for (Update up : list)
        {
            if (up instanceof ClockUpdate) {
                ClockUpdate u = (ClockUpdate) up;
                clockResets[cl] = getIndexOfClock(u.getClock());
                clockValues[cl] = u.getValue();
                cl++;
            }
            if (up instanceof BoolUpdate) {
                BoolUpdate u = (BoolUpdate) up;
                boolResets[bl] = bddStartLevel+ getIndexOfBV(u.getBV());
                boolValues[bl] = u.getValue() ? 1 : 0;
                bl++;
            }
        }
        CDD res= state.applyReset(clockResets,clockValues,boolResets,boolValues).removeNegative().reduce();
        return res;
    }

    private void checkForNull(){
        if(pointer == 0){
            throw new NullPointerException("CDD object is null");
        }
    }

    private static void checkIfRunning() {
        if(!cddIsRunning){
            throw new CddNotRunningException("CDD.init() has not been called");
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




    public static CDD predt(CDD A, CDD B) {
        checkIfRunning();
        A.checkForNull();
        B.checkForNull();
        return new CDD(CDDLib.predt(A.pointer,B.pointer));
    }

    public static  boolean canDelayIndefinitely(CDD state) {
        CDD copy = new CDD(state.getPointer());
        if (copy.isTrue())
            return true;
        if (copy.isFalse())
            return false;
        if (copy.isBDD())
            return true;
        while (!copy.isTerminal())
        {
            CddExtractionResult res = copy.removeNegative().reduce().extractBddAndDbm();
            copy = res.getCddPart().removeNegative().reduce();
            Zone z = new Zone(res.getDbm());
            if (!z.canDelayIndefinitely())
                return false;
        }
        // found no states that cannot delay indefinitely
        return true;
    }

    public static  boolean isUrgent(CDD state) {
        CDD copy = new CDD(state.getPointer());
        if (copy.isTrue())
            return false;
        if (copy.isFalse())
            return true;
        if (copy.isBDD())
            return false;
        while (!copy.isTerminal())
        {
            CddExtractionResult res = copy.removeNegative().reduce().extractBddAndDbm();
            Zone z = new Zone(res.getDbm());
            copy = res.getCddPart().removeNegative().reduce();
            if (!z.isUrgent())
                return false;
        }
        return true;
    }

    public static boolean intersects(CDD A, CDD B) {
        if (A.conjunction(B).isNotFalse())
            return true;
        else return false;
    }

    public Federation toFederation() // TODO: does not in any way take care of BDD parts (might run endless for BCDDs?)
    {
        List<Zone> zoneList = new ArrayList<>();
        CDD copy = new CDD(this.pointer);
        while (!copy.isTerminal()) {
            copy = copy.reduce().removeNegative();
            CddExtractionResult res = copy.extractBddAndDbm();
            copy = res.getCddPart().reduce().removeNegative();
            Zone z = new Zone(res.getDbm());
            zoneList.add(z);
        }
        Federation fed = new Federation(zoneList);
        return fed;
    }

    public static boolean isSubset(CDD A, CDD B) { // A (= B
//        System.out.println("A : " + CDD.toGuardList(A,clocks));
//        System.out.println("B : " + CDD.toGuardList(B,clocks));
//        System.out.println("A & B : " + CDD.toGuardList(A.conjunction(B),clocks));
//        System.out.println("A & B = A: " + (A.conjunction(B).equiv(A)));
        if ((A.conjunction(B).equiv(A))) // TODO: check if correct
            return true;
        else return false;
    }

    public boolean isUnrestrained() {
        if (this.equiv(cddTrue().removeNegative())) // TODO: check if correct
            return true;
        else return false;
    }

    public static CDD getUnrestrainedCDD()
    {
        return CDD.cddTrue().removeNegative();
    }

    public  CDD transition( Edge e)
    {
        if (e.getUpdates().size()==0)
        {
            return this.conjunction(e.getGuardCDD());
        }
        int numBools = 0;
        int numClocks = 0;
        for (Update up : e.getUpdates())
        {
            if (up instanceof ClockUpdate) numClocks ++;
            if (up instanceof BoolUpdate) numBools ++;
        }
        int[] clockResets = new int[numClocks];
        int[] clockValues = new int[numClocks];
        int[] boolResets = new int[numBools];
        int[] boolValues= new int[numBools];
        int cl=0;
        int bl=0;
        for (Update up : e.getUpdates())
        {
            if (up instanceof ClockUpdate) {
                ClockUpdate u = (ClockUpdate) up;
                clockResets[cl] = getIndexOfClock(u.getClock());
                clockValues[cl] = u.getValue();
                cl++;
            }
            if (up instanceof BoolUpdate) {
                BoolUpdate u = (BoolUpdate) up;
                boolResets[bl] = bddStartLevel+ getIndexOfBV(u.getBV());
                boolValues[bl] = u.getValue() ? 1 : 0;
                bl++;
            }
        }



        return this.transition(e.getGuardCDD(),clockResets,clockValues,boolResets,boolValues).removeNegative().reduce();
    }


    public CDD transitionBack( Edge e)
    {
        if (e.getUpdates().size()==0)
        {
            return this.conjunction(e.getGuardCDD());
        }
        int numBools = 0;
        int numClocks = 0;
        for (Update up : e.getUpdates())
        {
            if (up instanceof ClockUpdate) numClocks ++;
            if (up instanceof BoolUpdate) numBools ++;
        }
        int[] clockResets = new int[numClocks];
        int[] clockValues = new int[numClocks];
        int[] boolResets = new int[numBools];
        int[] boolValues= new int[numBools];
        int cl=0;
        int bl=0;
        for (Update up : e.getUpdates())
        {
            if (up instanceof ClockUpdate) {
                ClockUpdate u = (ClockUpdate) up;
                clockResets[cl] = getIndexOfClock(u.getClock());
                clockValues[cl] = u.getValue();
                cl++;
            }
            if (up instanceof BoolUpdate) {
                BoolUpdate u = (BoolUpdate) up;
                boolResets[bl] = bddStartLevel+ getIndexOfBV(u.getBV());
                boolValues[bl] = u.getValue() ? 1 : 0;
                bl++;
            }
        }
        return this.transitionBack(e.getGuardCDD(),turnUpdatesToCDD(e.getUpdates()),clockResets,boolResets).removeNegative().reduce();
    }


    public static CDD turnUpdatesToCDD(List<Update> updates)
    {
        CDD res = cddTrue();
        for (Update up : updates) {
            if (up instanceof ClockUpdate) {
                ClockUpdate u = (ClockUpdate) up;
                res = res.conjunction(CDD.allocateInterval(getIndexOfClock(u.getClock()), 0, u.getValue(), true, u.getValue(), true));
            }
            if (up instanceof BoolUpdate) {
                BoolUpdate u = (BoolUpdate) up;
                BoolGuard bg = new BoolGuard(u.getBV(),"==",u.getValue());
                res = res.conjunction(CDD.fromBoolGuard(bg));
            }
        }
        return res.removeNegative().reduce();
    }


    public CDD transitionBack(Move e) {
        // TODO: check that this is up to date compared to the othter TransitionBack
        if (e.getUpdates().size()==0)
        {
            return this.conjunction(e.getGuardCDD());
        }
        int numBools = 0;
        int numClocks = 0;
        for (Update up : e.getUpdates())
        {
            if (up instanceof ClockUpdate) numClocks ++;
            if (up instanceof BoolUpdate) numBools ++;
        }
        int[] clockResets = new int[numClocks];
        int[] clockValues = new int[numClocks];
        int[] boolResets = new int[numBools];
        int[] boolValues= new int[numBools];
        int cl=0;
        int bl=0;
        for (Update up : e.getUpdates())
        {
            if (up instanceof ClockUpdate) {
                ClockUpdate u = (ClockUpdate) up;
                clockResets[cl] = getIndexOfClock(u.getClock());
                clockValues[cl] = u.getValue();
                cl++;
            }
            if (up instanceof BoolUpdate) {
                BoolUpdate u = (BoolUpdate) up;
                boolResets[bl] = bddStartLevel+ getIndexOfBV(u.getBV());
                boolValues[bl] = u.getValue() ? 1 : 0;
                bl++;
            }
        }
        return this.transitionBack(e.getGuardCDD(),turnUpdatesToCDD(e.getUpdates()),clockResets,boolResets).removeNegative().reduce();
    }

}
