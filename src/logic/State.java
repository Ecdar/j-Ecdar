package logic;

import lib.DBMLib;
import log.Log;
import models.*;

import java.util.HashMap;
import java.util.List;

public class State {
    private final Location location;
    private CDD invarCDD;

    public State(Location location, CDD invarCDD) {
        this.location = location;
        this.invarCDD = new CDD(invarCDD.getPointer());

    }

    public State(State oldState) {
        this.location = oldState.getLocation();
        this.invarCDD = new CDD(oldState.getInvariant().getPointer());
    }

    public Location getLocation() {
        return location;
    }

    public CDD getInvariant() {
        return invarCDD;
    }


    public CDD getLocationInvariant() {
        return location.getInvariantCddNew();
    }

    public Guard getInvariants(List<Clock> relevantClocks) {
        return location.getInvariantCddNew().getGuard(relevantClocks);
    }

    // TODO: I think this is finally done correctly. Check that that is true!
    public void applyGuards(CDD guardCDD) {
        invarCDD = invarCDD.conjunction(guardCDD) ;
    }

    public void applyInvariants(CDD invarCDD) {
        this.invarCDD = this.invarCDD.conjunction(invarCDD);
    }

    public void applyInvariants() {
        CDD result = this.invarCDD.conjunction(location.getInvariantCddNew());
        this.invarCDD=result;
    }

    public void disjunctCDD(CDD other) {
        this.invarCDD=this.invarCDD.disjunction(other);
    }

    public void applyResets(List<Update> resets) {
        invarCDD=invarCDD.applyReset(resets);
    }

    public void extrapolateMaxBounds(HashMap<Clock,Integer> maxBounds, List<Clock> relevantClocks){
        if (invarCDD.isTrue())
            return;
        CDD bcddLeftToAnalyse = new CDD(invarCDD.getPointer());
        CDD resCDD = CDD.cddFalse();

        int[] bounds = new int[CDD.numClocks];
        int counter =1;
        bounds[0] = 0; // special clock
        for (Clock clk :CDD.getClocks())
        {
            if (relevantClocks.contains(clk) )
            {
                if (!maxBounds.containsKey(clk))
                {
                    // assert false; // TODO: should we really get into here??
                    bounds[counter]=0;
                }
                else
                    bounds[counter] =maxBounds.get(clk);
            }
            else
            {
                bounds[counter] = 0;
            }
            counter++;
        }


        if (bcddLeftToAnalyse.isBDD())
        {
            return;
        }
        else
            while (!bcddLeftToAnalyse.isTerminal())
            {
                CddExtractionResult extractResult = bcddLeftToAnalyse.removeNegative().reduce().extract();
                bcddLeftToAnalyse = extractResult.getCddPart().removeNegative().reduce();

                Zone z = new Zone(extractResult.getDbm());
                CDD bddPart = extractResult.getBddPart();
                Zone newZone = new Zone(DBMLib.dbm_close(z.getDbm(),z.getDimension()));
                newZone.extrapolateMaxBounds(bounds);
                CDD extrapolatedDBMCDD = CDD.createFromDbm(newZone.getDbm(),CDD.numClocks);
                CDD extrapolatedCDD = bddPart.conjunction(extrapolatedDBMCDD);
                resCDD = resCDD.disjunction(extrapolatedCDD);
            }

        invarCDD = resCDD;
    }

    public void extrapolateMaxBoundsDiag(HashMap<Clock,Integer> maxBounds, List<Clock> relevantClocks){
        if (invarCDD.isTrue())
            return;
        CDD copy = new CDD(invarCDD.getPointer());
        CDD resCDD = CDD.cddFalse();
        boolean print = false;
        if (copy.toString().contains("30"))
        {
            Log.debug("max bounds : " + maxBounds);
            Log.debug(copy.getGuard(relevantClocks));
            print = true;
        }
        if (copy.isBDD())
        {
            return;
        }
        else
            while (!copy.isTerminal())
            {
                CddExtractionResult extractResult = copy.reduce().removeNegative().extract();
                copy = extractResult.getCddPart().removeNegative().reduce();

                Zone z = new Zone(extractResult.getDbm());
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
                if (print)
                {
                    for (int i: bounds)
                        System.out.print(i + " ");
                    Log.debug();
                }
                z.extrapolateMaxBoundsDiagonal(bounds);
                if (print) z.printDbm(true,true);
                CDD extrapolatedDBMCDD = CDD.createFromDbm(z.getDbm(),CDD.numClocks);
                CDD extrapolatedCDD = bddPart.conjunction(extrapolatedDBMCDD);
                resCDD = resCDD.disjunction(extrapolatedCDD);

            }
        if (print)
            Log.debug(resCDD);
        invarCDD = resCDD;
    }
    @Override
    public String toString() {
        return "{" + location.getName() + ", " + invarCDD + '}';
    }

    public void delay() {
        invarCDD = invarCDD.delay();
    }
}