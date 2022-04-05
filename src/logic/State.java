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

    private int getIndexOfClock(Clock clock, List<Clock> clocks) {
        for (int i = 0; i < clocks.size(); i++){
            if(clock.hashCode() == clocks.get(i).hashCode()) return i+1;
        }
        return 0;
    }

    public List<List<Guard>> getInvariants() {
        return CDD.toGuards(location.getInvariantCDD());
    }

    // TODO: I think this is finally done correctly. Check that that is true!
    public void applyGuards(CDD guardCDD) {
        invarCDD = invarCDD.conjunction(guardCDD) ;
    }

    public void applyInvariants(CDD invarCDD) {
        this.invarCDD = this.invarCDD.conjunction(invarCDD);
    }

    public void applyInvariants() {
        // should not be used anymore, i think
        assert(false);
    }

    public void applyResets(List<Update> resets) {
        invarCDD=CDD.applyReset(invarCDD, resets);
    }

    public void extrapolateMaxBounds(int[] maxBounds){
        // TODO!
//        for (Zone z : invFed.getZones())
//            z.extrapolateMaxBounds(maxBounds);
        assert(false);
    }

    @Override
    public String toString() {
        return "{" + location + ", " + invarCDD + '}';
    }
}