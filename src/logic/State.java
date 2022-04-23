package logic;

import models.*;

import java.util.ArrayList;
import java.util.HashMap;
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
        CDD result = this.invarCDD.conjunction(location.getInvariantCDD());
        this.invarCDD=result;
    }

    public void applyResets(List<Update> resets) {
        invarCDD=CDD.applyReset(invarCDD, resets);
    }

    public void extrapolateMaxBounds(HashMap<Clock,Integer> maxBounds, List<Clock> relevantClocks){
        if (invarCDD.isTrue())
            return;
        CDD copy = new CDD(invarCDD.getPointer());
        CDD resCDD = CDD.cddFalse();
        //System.out.println("max bounds : "  + maxBounds);
        //System.out.println(CDD.toGuardList(copy,relevantClocks));
        while (!copy.isTerminal())
        {
            CddExtractionResult extractResult = copy.reduce().removeNegative().extractBddAndDbm();
            copy = extractResult.getCddPart().removeNegative().reduce();
            //copy.printDot();
            Zone z = new Zone(extractResult.getDbm());
//            z.printDBM(true,true);
            CDD bddPart = extractResult.getBddPart();
            int[] bounds = new int[CDD.numClocks];
            int counter =1;
            bounds[0] = 0; // special clock
            for (Clock clk :CDD.getClocks())
            {
                if (!maxBounds.containsKey(clk))
                {
                    bounds[counter]=0;
                }
                else if (relevantClocks.contains(clk) )
                {
                    bounds[counter] =maxBounds.get(clk);
                }
                else
                {
                    bounds[counter] = 0;
                }
                counter++;
            }
            CDD before = CDD.allocateFromDbm(z.getDbm(),CDD.numClocks);
            z.extrapolateMaxBounds(bounds);
            CDD extrapolatedDBMCDD = CDD.allocateFromDbm(z.getDbm(),CDD.numClocks);
            //System.out.println(CDD.toGuardList(before,relevantClocks));
            //System.out.println(CDD.toGuardList( extrapolatedDBMCDD, relevantClocks));
            CDD extrapolatedCDD = bddPart.conjunction(extrapolatedDBMCDD);
            //System.out.println(CDD.toGuardList( extrapolatedCDD, relevantClocks));
            resCDD = resCDD.disjunction(extrapolatedCDD);
            //System.out.println(CDD.toGuardList( resCDD, relevantClocks));
        }
        //System.out.println(CDD.toGuardList( resCDD, relevantClocks));
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