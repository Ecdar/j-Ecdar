package logic;

import models.Clock;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ClockContainer {
    private List<Clock> clocks;

    public ClockContainer(List<Clock> clocks) {
        this.clocks = clocks;
    }

    public ClockContainer() {
        this.clocks = new ArrayList<>();
    }

    public void add(Clock clock) {
        Clock newClock = new Clock(clock); // todo: do not make a copy

        List<Clock> sameName = clocks.stream()
                .filter(c -> sameName(c, clock))
                .collect(Collectors.toList());
        if (sameName.size() != 0){
            List<Clock> sameOwner = sameName.stream().filter(c -> c.getOwnerName().equals(clock.getOwnerName())).collect(Collectors.toList());
            if(sameOwner.size() != 0){
                for(int i = 0; i < sameOwner.size(); i++){
                    sameOwner.get(i).setUniqueName(i+1);
                }
                newClock.setUniqueName(sameOwner.size()+1);
            }else{
                for(int i = 0; i < sameName.size(); i++){
                    sameName.get(i).setUniqueName();
                }
                newClock.setUniqueName();
            }
        }
        clocks.add(newClock);

    }

    private boolean sameName(Clock clock1, Clock clock2){
        return clock1.getName().equals(clock2.getName());
    }

    public List<Clock> getClocks() {
        return clocks;
    }

    public void addAll(List<Clock> clocks) {
        for (Clock clock: clocks) {
            add(clock);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClockContainer container = (ClockContainer) o;
        return Objects.equals(clocks, container.clocks);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clocks);
    }
}
