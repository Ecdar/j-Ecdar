package logic;

import models.*;

import java.util.ArrayList;
import java.util.List;

public class State {
    private final SymbolicLocation location;
    private CDD invarCDD;

    public State(SymbolicLocation location, CDD invarCDD) {
        this.location = location;
        this.invarCDD = new CDD(invarCDD.getPointer());

    }

    public State(State oldState) {
        this.location = oldState.getLocation();
        this.invarCDD = new CDD(oldState.getInvarCDD().getPointer());
    }

    public SymbolicLocation getLocation() {
        return location;
    }

    public CDD getInvarCDD() {
        return invarCDD;
    }


    public CDD getInvarCDDDirectlyFromInvariants() {
        return location.getInvariantCDD();
    }

    public List<List<Guard>> getInvariants(List<Clock> relevantClocks) {
        return CDD.toGuardList(location.getInvariantCDD(),relevantClocks);
    }

    // TODO: I think this is finally done correctly. Check that that is true!
    public void applyGuards(CDD guardCDD) {
        invarCDD = invarCDD.conjunction(guardCDD) ;
    }

    public void applyInvariants(CDD invarCDD) {
        this.invarCDD = this.invarCDD.conjunction(invarCDD);
    }

    public void applyInvariants() {
        this.invarCDD=this.invarCDD.conjunction(location.getInvariantCDD());
    }

    public void applyResets(List<Update> resets) {
        invarCDD=CDD.applyReset(invarCDD, resets);
    }

    public void extrapolateMaxBounds(int[] maxBounds){
        CDD copy = new CDD(invarCDD.getPointer());
        CDD resCDD = CDD.cddFalse();

        while (!copy.isTerminal())
        {
            CddExtractionResult extractResult = copy.reduce().removeNegative().extractBddAndDbm();
            copy = extractResult.getCddPart();
            //copy.printDot();
            Zone z = new Zone(extractResult.getDbm());
//            z.printDBM(true,true);
            CDD bddPart = extractResult.getBddPart();
            z.extrapolateMaxBounds(maxBounds);
            CDD extrapolatedDBMCDD = CDD.allocateFromDbm(z.getDbm(),CDD.numClocks);
            CDD extrapolatedCDD = bddPart.conjunction(extrapolatedDBMCDD);
            resCDD = resCDD.disjunction(extrapolatedCDD);
        }

        invarCDD = resCDD;
    }

    @Override
    public String toString() {
        return "{" + location + ", " + invarCDD + '}';
    }

    public void delay() {
        invarCDD = invarCDD.delay();
    }
}