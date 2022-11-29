package models;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.*;
import java.util.stream.Collectors;

public class AndGuard extends Guard {
    private List<Guard> guards;

    public AndGuard(List<Guard> guards) {
        this.guards = guards;

        /* If any of the guards are AndGuards themselves,
         *   then we can decompose their guards to be contained in this */
        List<AndGuard> worklist = this.guards
                .stream()
                .filter(guard -> guard instanceof AndGuard)
                .map(guard -> (AndGuard) guard)
                .collect(Collectors.toList());
        while (!worklist.isEmpty()) {
            AndGuard current = worklist.get(0);
            worklist.remove(0);

            for (Guard guard : current.guards) {
                if (guard instanceof AndGuard) {
                    worklist.add((AndGuard) guard);
                }
                this.guards.add(guard);
            }

            this.guards.remove(current);
        }

        /* If the AndGuard contains a FalseGuard,
         *   then remove all guards and just have a single
         *   FalseGuard as it will always be false. */
        boolean hasFalseGuard = this.guards.stream().anyMatch(guard -> guard instanceof FalseGuard);
        if (hasFalseGuard) {
            /* We just know that there is at least one FalseGuard for this
             *   reason we clear all guard as it handle multiple FalseGuards
             *   then we just add a FalseGuard to account for all of them.
             *   If we left it empty then it would be interpreted as a tautology. */
            this.guards.clear();
            this.guards.add(new FalseGuard());
        }

        // Remove all ture guards
        this.guards = this.guards.stream().filter(guard -> !(guard instanceof TrueGuard)).collect(Collectors.toList());

        // If empty then it is a tautology
        if (this.guards.size() == 0) {
            this.guards.add(new TrueGuard());
        }
    }

    public AndGuard(List<Guard>... guards) {
        this(Lists.newArrayList(Iterables.concat(guards)));
    }

    public AndGuard(Guard... guards) {
        this(Arrays.asList(guards));
    }

    public AndGuard(AndGuard copy, List<Clock> newClocks, List<Clock> oldClocks, List<BoolVar> newBVs, List<BoolVar> oldBVs) {
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
        return new AndGuard(
            this, newClocks, oldClocks, newBVs, oldBVs
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof AndGuard)) {
            return false;
        }

        AndGuard other = (AndGuard) obj;
        return Arrays.equals(guards.toArray(), other.guards.toArray());
    }

    @Override
    public String toString() {
        if (guards.size() == 1) {
            return guards.get(0).toString();
        }

        return "(" +
                guards.stream().map(Guard::toString).collect(Collectors.joining(" && "))
                + ")";
    }

    @Override
    public int hashCode() {
        return Objects.hash(guards);
    }
}
