package models;

import java.util.ArrayList;
import java.util.List;

public class State {
    private final SymbolicLocation location;
    private Federation invFed;

    public State(SymbolicLocation location, Federation fed) {
        this.location = location;
        // TODO: Check if this is creating a new federation
        //System.out.println("old fed size: " + fed.size());
        this.invFed = fed.getCopy();
        //System.out.println("new fed size: " + this.invFed.size());

    }

    public State(State oldState) {
        this.location = oldState.getLocation();
        // TODO: Check if this is creating a new federation
        //System.out.println("Copying state " + oldState.getLocation());

        //this.invFed = new Federation(oldState.getInvFed().getZones());
        this.invFed = oldState.getInvFed().getCopy();
        //oldState.getInvFed().getZones().get(0).printDBM(true,true);
        //this.invFed.getZones().get(0).printDBM(true,true);

        //System.out.println("Done ");

    }

    public SymbolicLocation getLocation() {
        return location;
    }

    public Federation getInvFed() {
        return invFed;
    }

    private int getIndexOfClock(Clock clock, List<Clock> clocks) {
        for (int i = 0; i < clocks.size(); i++){
            if(clock.hashCode() == clocks.get(i).hashCode()) return i+1;
        }
        return 0;
    }

    public List<List<Guard>> getInvariants() {
        return location.getInvariants();
    }

    // TODO: I think this is finally done correctly. Check that that is true!
    public void applyGuards(List<List<Guard>> guards, List<Clock> clocks) {
        List<Zone> zoneList = new ArrayList<>();
        for (List<Guard> disjunction : guards) {
            if (invFed.getZones().isEmpty())
            {
                assert(false);
                //System.out.println("up");
                Zone newZone = new Zone(clocks.size() + 1, true);
                newZone.init();
                for (Guard guard : disjunction) {
                    newZone.buildConstraintsForGuard(guard, getIndexOfClock(guard.getClock(), clocks));
                }
                zoneList.add(newZone);
            }
            else {
                //System.out.println("down");
                for (Zone z : invFed.getZones()) {
                    Zone newZone = new Zone(z);
                   //newZone.printDBM(true,true);
                    //z.printDBM(true,true);
                    for (Guard guard : disjunction) {
                        newZone.buildConstraintsForGuard(guard, getIndexOfClock(guard.getClock(), clocks));
                        //newZone.printDBM(true,true);
                    }

                    zoneList.add(newZone);
                }
            }
        }
        if (!guards.isEmpty())
            invFed = new Federation(zoneList);
    }

    public void applyInvariants(List<List<Guard>> invariants, List<Clock> clocks) {
        List<Zone> zoneList = new ArrayList<>();
        for (List<Guard> disjunction : invariants) {
            if (invFed.getZones().isEmpty())
            {
                assert(false);
                Zone newZone = new Zone(clocks.size() + 1, true);
                newZone.init();
                for (Guard guard : disjunction) {
                    newZone.buildConstraintsForGuard(guard, getIndexOfClock(guard.getClock(), clocks));
                }
                zoneList.add(newZone);
            }
            else
                for (Zone z : invFed.getZones()) {
                    Zone newZone = new Zone(z);
                    for (Guard guard : disjunction) {
                        newZone.buildConstraintsForGuard(guard, getIndexOfClock(guard.getClock(), clocks));
                    }
                    zoneList.add(newZone);
                }
        }
        if (!invariants.isEmpty())
            invFed = new Federation(zoneList);
    }

    public void applyInvariants(List<Clock> clocks) {
        List<Zone> zoneList = new ArrayList<>();
        for (List<Guard> disjunction : getInvariants()) {
            if (invFed.getZones().isEmpty())
            {
                assert(false);
                //System.out.println("was empty!");

                Zone newZone = new Zone(clocks.size() + 1, true);
                newZone.init();
                for (Guard guard : disjunction) {
                    newZone.buildConstraintsForGuard(guard, getIndexOfClock(guard.getClock(), clocks));
                }
                zoneList.add(newZone);
            }
            else {
                //System.out.println("was not empty!");

                for (Zone z : invFed.getZones()) {
                    Zone newZone = new Zone(z);
                    for (Guard guard : disjunction) {
                        newZone.buildConstraintsForGuard(guard, getIndexOfClock(guard.getClock(), clocks));
                    }
                    zoneList.add(newZone);
                }
            }
        }

//        System.out.println("before " + invFed.size());
        if (!getInvariants().isEmpty())
            invFed = new Federation(zoneList);
  //      System.out.println("then " + invFed.size());
    }

    public void applyResets(List<Update> resets, List<Clock> clocks) {
        for (Update reset : resets)
            for (Zone z : invFed.getZones())
                z.updateValue(getIndexOfClock(reset.getClock(), clocks), reset.getValue());
    }

    public void extrapolateMaxBounds(int[] maxBounds){
        for (Zone z : invFed.getZones())
            z.extrapolateMaxBounds(maxBounds);
    }

    @Override
    public String toString() {
        return "{" + location + ", " + invFed + '}';
    }
}
