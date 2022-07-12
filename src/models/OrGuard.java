package models;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.*;
import java.util.stream.Collectors;

public class OrGuard extends Guard {

    private List<Guard> guards;

    public OrGuard(List<Guard> guards) {
        this.guards = guards;

        /* If any of the guards are OrGuards themselves,
         *   then we can decompose their guards to be contained in this */
        List<OrGuard> worklist = this.guards
                .stream()
                .filter(guard -> guard instanceof OrGuard)
                .map(guard -> (OrGuard) guard)
                .collect(Collectors.toList());
        while (!worklist.isEmpty()) {
            OrGuard current = worklist.get(0);
            worklist.remove(0);

            for (Guard guard : current.guards) {
                if (guard instanceof OrGuard) {
                    worklist.add((OrGuard) guard);
                }
                this.guards.add(guard);
            }

            this.guards.remove(current);
        }

        // Remove all guards if there is a true guard
        boolean hasTrueGuard = this.guards.stream().anyMatch(guard -> guard instanceof TrueGuard);
        if (hasTrueGuard) {
            /* If there are one or more TrueGuards then we just need a single TrueGuard.
             *   It would be possible to just clear it and let the last predicate,
             *   ensure that the empty OrGuard is a tautology.
             *   However, this is more robust towards changes */
            this.guards.clear();
            this.guards.add(new TrueGuard());
        }

        // Remove all false guards
        this.guards = this.guards.stream().filter(guard -> !(guard instanceof FalseGuard)).collect(Collectors.toList());

        // If there are no guards then it is a tautology
        if (this.guards.size() == 0) {
            this.guards.add(new TrueGuard());
        }
    }

    public OrGuard(List<Guard>... guards) {
        this(Lists.newArrayList(Iterables.concat(guards)));
    }

    public OrGuard(Guard... guards) {
        this(Arrays.asList(guards));
    }

    public OrGuard(OrGuard copy, List<Clock> newClocks, List<Clock> oldClocks, List<BoolVar> newBVs, List<BoolVar> oldBVs) {
        // As this is the copy-constructor we need to create new instances of the guards
        this(copy.guards.stream().map(guard ->
            guard.copy(newClocks, oldClocks, newBVs, oldBVs)
        ).collect(Collectors.toList()));
    }

    public List<Guard> getGuards() {
        return guards;
    }

    @Override
    int getMaxConstant(Clock clock) {
        int max = 0;
        for (Guard guard : guards) {
            max = Math.max(max, guard.getMaxConstant(clock));
        }
        return max;
    }

    @Override
    Guard copy(List<Clock> newClocks, List<Clock> oldClocks, List<BoolVar> newBVs, List<BoolVar> oldBVs) {
        return new OrGuard(
            this, newClocks, oldClocks, newBVs, oldBVs
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof OrGuard)) {
            return false;
        }

        OrGuard other = (OrGuard) obj;
        return Arrays.equals(guards.toArray(), other.guards.toArray());
    }

    @Override
    public String toString() {
        if (guards.size() == 1) {
            return guards.get(0).toString();
        }

        return "(" +
                guards.stream()
                    .map(Guard::toString)
                    .collect(Collectors.joining(" or "))
                + ")";
    }

    @Override
    public int hashCode() {
        return Objects.hash(false);
    }
}
