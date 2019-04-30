package models;

import java.util.List;

public class State {
    private final SymbolicLocation location;
    private Zone invZone;

    public State(SymbolicLocation location, Zone zone) {
        this.location = location;
        this.invZone = new Zone(zone);
    }

    public State(State oldState) {
        this.location = oldState.getLocation();
        this.invZone = new Zone(oldState.getInvZone());
    }

    public SymbolicLocation getLocation() {
        return location;
    }

    public Zone getInvZone() {
        return invZone;
    }

    private int getIndexOfClock(Clock clock, List<Clock> clocks) {
        for (int i = 0; i < clocks.size(); i++){
            if(clock.hashCode() == clocks.get(i).hashCode()) return i+1;
        }
        return 0;
    }

    public List<Guard> getInvariants() {
        return location.getInvariants();
    }

    public void applyGuards(List<Guard> guards, List<Clock> clocks) {
        for (Guard guard : guards)
            invZone.buildConstraintsForGuard(guard, getIndexOfClock(guard.getClock(), clocks));
    }

    public void applyInvariants(List<Guard> invariants, List<Clock> clocks) {
        for (Guard invariant : invariants)
            invZone.buildConstraintsForGuard(invariant, getIndexOfClock(invariant.getClock(), clocks));
    }

    public void applyInvariants(List<Clock> clocks) {
        for (Guard invariant : getInvariants())
            invZone.buildConstraintsForGuard(invariant, getIndexOfClock(invariant.getClock(), clocks));
    }

    public void applyResets(List<Update> resets, List<Clock> clocks) {
        for (Update reset : resets)
            invZone.updateValue(getIndexOfClock(reset.getClock(), clocks), reset.getValue());
    }

    public void extrapolateMaxBounds(int maxConstant){
        invZone.extrapolateMaxBounds(maxConstant);
    }

    @Override
    public String toString() {
        return "{" + location + ", " + invZone + '}';
    }
}
