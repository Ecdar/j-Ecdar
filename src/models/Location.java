package models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Location {

    private final String name;
    private List<List<Guard>> invariant;
    private int x,y;

    public CDD getInconsistentPart() {
        return inconsistentPart;
    }

    public void setInconsistentPart(CDD inconsistentPart) {
        this.inconsistentPart = inconsistentPart;
    }


    public CDD getInvariantCDD()
    {
        return new CDD(invariant);
    }

    private CDD inconsistentPart;
    private boolean isInitial;

    private  boolean isUrgent;
    private  boolean isUniversal;
    private  boolean isInconsistent;

    public boolean isUrgent() {
        return isUrgent;
    }

    public boolean isInconsistent() {
        return isInconsistent;
    }

    public void setInvariant(List<List<Guard>> invariant) {
        this.invariant = invariant;
    }

    public void setInitial(boolean initial) {
        isInitial = initial;
    }

    public void setX(int x) {
        this.x = x;
    }
    public void setY(int y) {
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setUrgent(boolean urgent) {
        isUrgent = urgent;
    }

    public void setUniversal(boolean universal) {
        isUniversal = universal;
    }

    public void setInconsistent(boolean inconsistent) {
        isInconsistent = inconsistent;
    }



    public Location(String name, List<List<Guard>> invariant, boolean isInitial, boolean isUrgent, boolean isUniversal, boolean isInconsistent) {
        this.name = name;
        this.invariant = invariant;
        this.isInitial = isInitial;
        this.isUrgent = isUrgent;
        this.isUniversal = isUniversal;
        this.isInconsistent = isInconsistent || this.getName().equals("inc");
        this.inconsistentPart = null;
    }
    public Location(String name, List<List<Guard>> invariant, boolean isInitial, boolean isUrgent, boolean isUniversal, boolean isInconsistent, int x, int y) {
        this.name = name;
        this.invariant = invariant;
        this.isInitial = isInitial;
        this.isUrgent = isUrgent;
        this.isUniversal = isUniversal;
        this.isInconsistent = isInconsistent || this.getName().equals("inc");
        this.inconsistentPart = null;
        this.x=x;
        this.y=y;
    }

    public Location(Location copy, List<Clock> clocks){
        this.name = copy.name;

        this.invariant = new ArrayList<>();
        for (List<Guard> list : copy.invariant) {
            List<Guard> interm = new ArrayList<Guard>();
            for (Guard g : list) {
                interm.add(new Guard(g, clocks));
            }
            this.invariant.add(interm);
        }

        this.isInitial = copy.isInitial;
        this.isUrgent = copy.isUrgent;
        this.isUniversal = copy.isUniversal;
        this.isInconsistent = copy.isInconsistent;
    }

    public String getName() {
        return name;
    }

    public List<List<Guard>> getInvariant() {
        return invariant;
    }

    public boolean isInitial() {
        return isInitial;
    }

    public boolean isUniversal() {
        return isUniversal;
    }

    public int getMaxConstant(Clock clock){
        int constant = 0;

        for (List<Guard> list : invariant) {
            for(Guard guard : list) {
                if (clock.equals(guard.getClock())) {
                    if (guard.getActiveBound() > constant) constant = guard.getActiveBound();
                }
            }
        }

        return constant;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Location location = (Location) o;
        boolean invarsMatch = true;
        if (invariant.size()!=location.invariant.size())
            invarsMatch = false;
        else
            for (int i=0; i<invariant.size();i++)
            {
                invarsMatch = invarsMatch && Arrays.equals(invariant.get(i).toArray(), location.invariant.get(i).toArray());
            }
        assert(invarsMatch ==
                Arrays.equals(invariant.toArray(), location.invariant.toArray()) );

        return isInitial == location.isInitial &&
                isUrgent == location.isUrgent &&
                isUniversal == location.isUniversal &&
                isInconsistent == location.isInconsistent &&
                name.equals(location.name) &&
                invarsMatch;
    }

    @Override
    public String toString() {
        return name;
    }
}
