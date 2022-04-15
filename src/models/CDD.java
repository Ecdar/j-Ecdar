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
    public static int numClocks; // includes the + 1 for initial clock
    public static int bddStartLevel;
    private static boolean cddIsRunning;
    private static List<Clock> clocks = new ArrayList<>();
    private static List<BoolVar> BVs = new ArrayList<>();


    public CDD(){
        checkIfRunning();
        this.pointer = CDDLib.allocateCdd();
    }


    public CDD(long pointer){
        this.pointer = pointer;
    }


    private static int getIndexOfClock(Clock clock) {

        for (int i = 0; i < clocks.size(); i++){
            if(clock.hashCode() == clocks.get(i).hashCode()) return i+1;
        }
        assert(false);
        return 0;
    }

    private static int getIndexOfBV(BoolVar bv) {

        for (int i = 0; i < BVs.size(); i++){
            if(bv.hashCode() == BVs.get(i).hashCode()) return i+1;
        }
        return 0;
    }

    public CDD(List<List<Guard>> guards){
        CDD res = cddFalse();

        outerloop: for (List<Guard> guardList: guards)
        {
            Zone z = new Zone(numClocks,true);
            z.init();
            CDD bdd = cddTrue();
            for (Guard guard: guardList) {
                if (guard instanceof FalseGuard)
                {
                    res = cddFalse();
                    break outerloop;
                }
                if (guard instanceof ClockGuard) {
                    z.buildConstraintsForGuard((ClockGuard) guard, clocks);
                }
                if (guard instanceof BoolGuard)
                {
                    bdd.conjunction(fromBoolGuard((BoolGuard)guard));
                }
            }
            res = res.disjunction(CDD.allocateFromDbm(z.getDbm(), numClocks).conjunction(bdd)).removeNegative().reduce();
        }
        this.pointer = res.pointer;
        if (guards.isEmpty())
        {

            CDD unres = getUnrestrainedCDD();
            this.pointer = unres.pointer;
        }
    }

    public static CDD fromBoolGuard(BoolGuard guard)
    {
        if (guard.getValue())
            return createBddNode(bddStartLevel + getIndexOfBV(guard.getVar()));
        else
            return createBddNode(bddStartLevel + getIndexOfBV(guard.getVar())).negation();
    }

    public static List<List<Guard>> toGuardList(CDD state, List<Clock> relevantClocks){
        List<List<Guard>> guards = new ArrayList<>();
        CDD copy = new CDD(state.pointer);
        copy = copy.removeNegative().reduce();
        int counter = 0;
        if (copy.equiv(cddFalse())) // special case for guards
        {
            List<Guard> falseGuard = new ArrayList<>();
            falseGuard.add(new FalseGuard());
            guards.add(falseGuard);
            return guards;
        }
        while (!copy.isTerminal())
        {
            counter ++;
            if (counter == 5)
                return null;
            copy=copy.reduce().removeNegative();
            CddExtractionResult res = copy.extractBddAndDbm();
            copy = res.getCddPart();
            Zone z = new Zone(res.getDbm());
            CDD bddPart = res.getBddPart();
            List<Guard> guardList = z.buildGuardsFromZone(clocks, relevantClocks);
            guardList.addAll(CDD.toBoolGuards(bddPart)); // TODO: once we have boolean
            guards.add(guardList);
        }
        return guards;
    }



    public static List<Guard> toBoolGuards(CDD bdd){
        if (bdd.isFalse())
            return new ArrayList<>(){{add(new FalseGuard());}};
        if (bdd.isTrue())
            return new ArrayList<>();
        CDDNode node = bdd.getRoot();
        List<Guard> guards = new ArrayList<>();

        node.getElemIterable().forEach(
                n -> {BoolVar var = BVs.get(n.getChild().getLevel()-bddStartLevel);
                    String bits = Long.toString(n.getChild().getPointer(), 2);   //TODO: hate going for bit magic here, if anyone has a good idea, let me know!!! also, test this!
                    boolean value = bits.charAt(bits.length())=='1' ? false : true ;
                    guards.add(new BoolGuard(var,"==",value));
                }
        );
        return guards;
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
        clocks = new ArrayList<>();
        CDDLib.cddDone();
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

    public static int addBddvar(int amount) {
        checkIfRunning();
        bddStartLevel =  CDDLib.addBddvar(amount);
        return bddStartLevel;
    }

    public static CDD allocate(){
        checkIfRunning();
        return new CDD();
    }

    public static CDD allocateInterval(int i, int j, int lower, int upper){
        checkIfRunning();
        System.out.println("**************************************************** Check allocate interval ***********************");
        // TODO: Dont think this works.
        //assert(false);
        return new CDD(CDDLib.interval(i,j,lower,upper));
    }

    public static CDD allocateFromDbm(int[] dbm, int dim){
        checkIfRunning();
        return new CDD(CDDLib.cddFromDbm(dbm, dim));
    }

    public static CDD allocateLower(int i, int j, int lowerBound) {
        checkIfRunning();
        return new CDD(CDDLib.lower(i,j,lowerBound));
    }

    public static CDD allocateUpper(int i, int j, int upperBound) {
        checkIfRunning();
        return new CDD(CDDLib.upper(i,j,upperBound));
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
        return new CDD(resultPointer);
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
                boolResets[bl] = getIndexOfBV(u.getBV());
                boolValues[bl] = u.getValue() ? 1 : 0;
                bl++;
            }
        }
        System.out.println("resets " + clockResets[0] + " values " + clockValues[0]);
        return state.applyReset(clockResets,clockValues,boolResets,boolValues).removeNegative().reduce();
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
        // TODO!
        return null;
    }

    public static  boolean canDelayIndefinitely(CDD state) {

        CDD copy = new CDD(state.getPointer());
        if (copy.isTrue())
            return true;
        if (copy.isFalse())
            return false;
        while (!copy.isTerminal())
        {
            CddExtractionResult res = copy.removeNegative().reduce().extractBddAndDbm();
            copy = res.getCddPart();
            Zone z = new Zone(res.getDbm());
            if (z.canDelayIndefinitely())  // TODO: is it enough if one can do it??
                return true;
        }
        System.out.println(" found a state that cannot delay indefinitely");
        return false;
    }

    public static  boolean isUrgent(CDD state) {
        System.out.println("not sure urgent works yet!");

        CDD copy = new CDD(state.getPointer());
        if (copy.isTrue())
            return false;
        if (copy.isFalse())
            return false;
        while (!copy.isTerminal())
        {
            CddExtractionResult res = copy.removeNegative().reduce().extractBddAndDbm();
            Zone z = new Zone(res.getDbm());
            copy = res.getCddPart();
            if (z.isUrgent())
                return true; // TODO: is it enough if one is urgent?
        }
        return false;
    }

    public static boolean intersects(CDD A, CDD B) {
        if (A.conjunction(B).isNotFalse())
            return true;
        else return false;
    }


    public static boolean isSubset(CDD A, CDD B) { // A (= B
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
                boolResets[bl] = getIndexOfBV(u.getBV());
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
                boolResets[bl] = getIndexOfBV(u.getBV());
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
                res = res.conjunction(CDD.allocateInterval(getIndexOfClock(u.getClock()), 0, u.getValue(), u.getValue()));
            }
            if (up instanceof BoolUpdate) {
                BoolUpdate u = (BoolUpdate) up;
                BoolGuard bg = new BoolGuard(u.getBV(),"==",u.getValue());
                res = res.conjunction(CDD.fromBoolGuard(bg));
            }
        }
        return res.removeNegative().reduce();
    }

    public static Automaton makeInputEnabled(Automaton aut) {
        Automaton copy = addTargetInvariantToEdges(aut); //new Automaton(aut);
        if (clocks.size() > 0) {
            for (Location loc : copy.getLocations()) {
                CDD sourceInvariantCDD = loc.getInvariantCDD();
                // loop through all inputs
                for (Channel input : copy.getInputAct()) {
                    // build CDD of zones from edges
                    List<Edge> inputEdges = copy.getEdgesFromLocationAndSignal(loc, input);
                    CDD resCDD;
                    CDD cddOfAllInputs = cddFalse();
                    if (!inputEdges.isEmpty()) {
                        for (Edge edge : inputEdges) {
                            CDD target = edge.getTarget().getInvariantCDD();
                            //CDD preReset = CDD.applyReset(target, edge.getUpdates()); // Does not work for back propagation
                            //CDD preGuard = preReset.conjunction(edge.getGuardCDD());
                            CDD preGuard1 = target.transitionBack(edge);
                            //assert (preGuard1.equiv(preGuard));
                            cddOfAllInputs = cddOfAllInputs.disjunction(preGuard1);
                        }

                        // subtract the federation of zones from the original fed
                        resCDD = sourceInvariantCDD.minus(cddOfAllInputs);
                    } else {
                        resCDD = sourceInvariantCDD;
                    }

                    Edge newEdge = new Edge(loc, loc, input, true, CDD.toGuardList(resCDD, aut.getClocks()), new ArrayList<>());
                    copy.getEdges().add(newEdge);


                }
            }
        }
        return  copy;
    }

    public static Automaton addTargetInvariantToEdges(Automaton aut) {

        Automaton copy = aut;
        if (clocks.size() > 0) {
            for (Edge edge : copy.getEdges()) {
                CDD targetCDD = edge.getTarget().getInvariantCDD();
                CDD past = targetCDD.transitionBack(edge);
                edge.setGuards(CDD.toGuardList(past, aut.getClocks()));
            }
        } // TODO: else part will be important once we have bool support
        return copy;
    }



}
