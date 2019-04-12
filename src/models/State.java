package models;

import java.util.List;

public class State {
    private final SymbolicLocation location;
    private Zone invZone, arrivalZone;
    // delay sum
    private int dSum;

    public State(SymbolicLocation location, Zone zone) {
        this.location = location;
        this.invZone = new Zone(zone);
        this.arrivalZone = new Zone(zone);
        this.dSum = 0;
    }

    public State(SymbolicLocation location, Zone zone, Zone arrivalZone, int sum) {
        this.location = location;
        this.invZone = new Zone(zone);
        this.arrivalZone = new Zone(arrivalZone);
        this.dSum = sum;
    }

    public State(State oldState) {
        this.location = oldState.getLocation();
        this.invZone = new Zone(oldState.getInvZone());
        this.arrivalZone = new Zone(oldState.getArrivalZone());
        this.dSum = oldState.dSum;
    }

    public SymbolicLocation getLocation() {
        return location;
    }

    public Zone getInvZone() {
        return invZone;
    }

    public Zone getArrivalZone() {
        return arrivalZone;
    }

    public int getDSum() {
        return dSum;
    }

    private int getIndexOfClock(Clock clock, List<Clock> clocks) {
        return clocks.indexOf(clock) + 1;
    }

    private List<Guard> getInvariants() {
        return location.getInvariants();
    }

    public void applyGuards(List<Guard> guards, List<Clock> clocks) {
        for (Guard guard : guards)
            invZone.buildConstraintsForGuard(guard, getIndexOfClock(guard.getClock(), clocks));
    }

    public void applyInvariants(List<Clock> clocks) {
        for (Guard invariant : getInvariants())
            invZone.buildConstraintsForGuard(invariant, getIndexOfClock(invariant.getClock(), clocks));
    }

    public void applyResets(List<Update> resets, List<Clock> clocks) {
        for (Update reset : resets)
            invZone.updateValue(getIndexOfClock(reset.getClock(), clocks), reset.getValue());
    }

    public void setArrivalZone(Zone arrivalZone) {
        this.arrivalZone = new Zone(arrivalZone);
    }

    public void updateArrivalZone(Zone timeline){
        this.arrivalZone.updateArrivalZone(timeline);
    }

    public void setDSum(int sum) {
        this.dSum = sum;
    }

    @Override
    public String toString() {
        return "{" + location + ", " + invZone + '}';
    }
}
