package logic;

import lib.DBMLib;
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

    public Guard getInvariants(List<Clock> relevantClocks) {
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

    public void disjunctCDD(CDD other) {
        this.invarCDD=this.invarCDD.disjunction(other);
    }

    public void applyResets(List<Update> resets) {
        invarCDD=CDD.applyReset(invarCDD, resets);
    }

    public void extrapolateMaxBounds(HashMap<Clock,Integer> maxBounds, List<Clock> relevantClocks){
        if (invarCDD.isTrue())
            return;
        CDD bcddLeftToAnalyse = new CDD(invarCDD.getPointer());
        CDD resCDD = CDD.cddFalse();
        boolean print = false;
        if (bcddLeftToAnalyse.toString().contains("500")) // DEBUG PRINT
        {
            System.out.println("location " + location.getName());
            System.out.println("max bounds : " + maxBounds);
            System.out.println(CDD.toGuardList(bcddLeftToAnalyse, relevantClocks));
            print = true;
        }


        int[] bounds = new int[CDD.numClocks];
        int counter =1;
        bounds[0] = 0; // special clock
        for (Clock clk :CDD.getClocks())
        {
            if (relevantClocks.contains(clk) )
            {
                if (!maxBounds.containsKey(clk))
                {
                    System.out.println("clock: " + clk + " " + maxBounds + " relevant clocks " + relevantClocks);
                    assert false;
                    bounds[counter]=0;
                }
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
            CddExtractionResult extractResult = bcddLeftToAnalyse.reduce().removeNegative().extractBddAndDbm();
            bcddLeftToAnalyse = extractResult.getCddPart().removeNegative().reduce();

            Zone z = new Zone(extractResult.getDbm());
            CDD bddPart = extractResult.getBddPart();

            if (print)
            {
                for (int i: bounds)
                    System.out.print(i + " ");
                System.out.println();
            }
            Zone copyZone = new Zone(z);
            Zone newZone = new Zone(DBMLib.dbm_close(z.getDbm(),z.getSize()));
            newZone.extrapolateMaxBounds(bounds);
            if (print && !copyZone.equals(newZone)) copyZone.printDBM(false,false);
            if (print && !copyZone.equals(newZone)) newZone.printDBM(false,false);
            CDD extrapolatedDBMCDD = CDD.allocateFromDbm(newZone.getDbm(),CDD.numClocks);
            CDD extrapolatedCDD = bddPart.conjunction(extrapolatedDBMCDD);
            resCDD = resCDD.disjunction(extrapolatedCDD);
        }
        if (print)
            System.out.println(resCDD);
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
            System.out.println("max bounds : " + maxBounds);
            System.out.println(CDD.toGuardList(copy, relevantClocks));
            print = true;
        }
        if (copy.isBDD())
        {
            return;
        }
        else
            while (!copy.isTerminal())
            {
                CddExtractionResult extractResult = copy.reduce().removeNegative().extractBddAndDbm();
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
                    System.out.println();
                }
                z.extrapolateMaxBoundsDiag(bounds);
                if (print) z.printDBM(true,true);
                CDD extrapolatedDBMCDD = CDD.allocateFromDbm(z.getDbm(),CDD.numClocks);
                CDD extrapolatedCDD = bddPart.conjunction(extrapolatedDBMCDD);
                resCDD = resCDD.disjunction(extrapolatedCDD);

            }
        if (print)
            System.out.println(resCDD);
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