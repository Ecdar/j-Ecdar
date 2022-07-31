package logic;

import models.*;

import java.util.*;
import java.util.stream.Collectors;

// parent class for all TS's, so we can use it with regular TS's, composed TS's etc.
public abstract class TransitionSystem {
    final UniqueNamedContainer<Clock> clocks;
    final UniqueNamedContainer<BoolVar> BVs;
    private StringBuilder lastErr = new StringBuilder();

    TransitionSystem() {
        this.clocks = new UniqueNamedContainer<>();
        this.BVs = new UniqueNamedContainer<>();
    }

    public List<Clock> getClocks() {
        return clocks.getItems();
    }

    public List<BoolVar> getBVs() {
        return BVs.getItems();
    }

    public State getInitialState(List<BoolVar> variables) {
        // Create a CDD for the initial values of the boolean variables
        CDD bdd = CDD.cddTrue();
        for (BoolVar variable : variables) {
            int level = CDD.bddStartLevel + CDD.indexOf(variable);
            bdd = bdd.conjunction(
                    CDD.createBddNode(level, variable.getInitialValue())
            );
        }

        // Initialize the clocks and conjoin with initial values of the bdd
        CDD initial = CDD.cddZeroDelayed();
        CDD invariant = initial.conjunction(
                bdd
        );

        // Create the state and apply the invariant of the initial location
        State state = new State(getInitialLocation(), invariant);
        state.applyInvariants();

        return state;
    }

    public State getInitialState() {
        return getInitialState(BVs.getItems());
    }

    public State getInitialState(CDD guard) {
        State state = getInitialState(CDD.BVs);
        state.applyGuards(guard);
        return state;
    }

    public List<Transition> getNextTransitions(State currentState, Channel channel) {
        return getNextTransitions(currentState, channel, clocks.getItems());
    }

    public Set<Channel> getActions() {
        Set<Channel> actions = new HashSet<>(getInputs());
        actions.addAll(getOutputs());
        return actions;
    }

    public SymbolicLocation getInitialLocation(TransitionSystem[] systems) {
        // build ComplexLocation with initial location from each TransitionSystem
        List<SymbolicLocation> initials = Arrays
                .stream(systems)
                .map(TransitionSystem::getInitialLocation)
                .collect(Collectors.toList());
        return new ComplexLocation(initials);
    }

    private Transition createNewTransition(State state, Move move) {
        // Conjoined CDD of all edges in the move
        CDD edgeGuard = move.getGuardCDD();

        /* Simulate the move across the edge.
         *   Init the invariant to false, as it might be
         *   that the edge guard is false and thereby the conjunction
         *   (Applying the edge guard) will result in a contradiction. */
        CDD guardCDD = CDD.cddFalse();
        if (!edgeGuard.isFalse()) {
            guardCDD = state.getInvariant().conjunction(edgeGuard);
        }

        /* Now that we have simulated the traversal over the edge
         *   the current state of the "invariant" is the guardCDD. */
        CDD invariant = guardCDD.hardCopy();

        invariant = invariant.applyReset(
                move.getUpdates()
        );
        invariant = invariant.delay();
        invariant = invariant.conjunction(
                move.getTarget().getInvariant()
        );

        // Create the state after traversing the edge
        State targetState = new State(
                move.getTarget(), invariant
        );

        return new Transition(
                state, targetState, move, guardCDD
        );
    }

    public List<Transition> createNewTransitions(State state, List<Move> moves, List<Clock> allClocks) {
        List<Transition> transitions = new ArrayList<>();

        for (Move move : moves) {
            Transition transition = createNewTransition(state, move);

            // Check if it is unreachable and if so then ignore it
            if (transition.getTarget().getInvariant().isFalse()) {
                continue;
            }

            transitions.add(transition);
        }

        return transitions;
    }

    public boolean isDeterministic() {

        boolean initialisedCdd = false;
        if (!CDD.isRunning()) {
            CDD.init(CDD.maxSize, CDD.cs, CDD.stackSize, getClocks(), getBVs());
            initialisedCdd = true;
        }

        boolean isDeterministic = true;
        List<String> nondetermTs = new ArrayList<>();

        List<SimpleTransitionSystem> systems = getSystems();

        for (SimpleTransitionSystem ts : systems) {
            if (!ts.isDeterministicHelper()) {
                isDeterministic = false;
                nondetermTs.add(ts.getName());
            }
        }

        if (!isDeterministic) buildErrMessage(nondetermTs, "non-deterministic");

        if (initialisedCdd) {
            CDD.done();
        }
        return isDeterministic;
    }

    public boolean isLeastConsistent() {
        return isConsistent(true);
    }

    public boolean isFullyConsistent() {
        return isConsistent(false);
    }

    private boolean isConsistent(boolean canPrune) {
        boolean isDeterm = isDeterministic();
        boolean isConsistent = true;

        boolean initialisedCdd = false;
        if (!CDD.isRunning()) {
            CDD.init(CDD.maxSize, CDD.cs, CDD.stackSize, getClocks(), getBVs());
            initialisedCdd = true;
        }

        List<String> inconsistentTs = new ArrayList<>();
        for (SimpleTransitionSystem system : getSystems()) {
            if (!system.isConsistentHelper(canPrune)) {
                isConsistent = false;
                inconsistentTs.add(system.getName());
            }
        }
        if (!isConsistent) buildErrMessage(inconsistentTs, "inconsistent");

        if (initialisedCdd) {
            CDD.done();
        }

        return isConsistent && isDeterm;
    }

    public boolean isImplementation() {
        boolean isCons = isFullyConsistent();

        boolean initialisedCdd = false;
        if (!CDD.isRunning()) {
            CDD.init(CDD.maxSize, CDD.cs, CDD.stackSize, getClocks(), getBVs());
            initialisedCdd = true;
        }

        boolean isImpl = true;
        List<String> nonImpl = new ArrayList<>();
        List<SimpleTransitionSystem> systems = getSystems();

        for (SimpleTransitionSystem ts : systems) {
            if (!ts.isImplementationHelper())
                isImpl = false;
            nonImpl.add(ts.getName());
        }
        if (!isImpl) {
            buildErrMessage(nonImpl, "not output urgent");
        }
        if (initialisedCdd) {
            CDD.done();
        }
        return isImpl && isCons;
    }

    public HashMap<Clock, Integer> getMaxBounds() {
        HashMap<Clock, Integer> result = new HashMap<>();
        for (TransitionSystem system : getSystems()) {
            result.putAll(system.getMaxBounds());
        }

        return result;
    }

    public List<Move> moveProduct(List<Move> moves1, List<Move> moves2, boolean asNested, boolean removeTargetLocationInvariant) {
        List<Move> moves = new ArrayList<>();

        for (Move move1 : moves1) {
            for (Move move2 : moves2) {
                SymbolicLocation q1s = move1.getSource();
                SymbolicLocation q1t = move1.getTarget();
                SymbolicLocation q2s = move2.getSource();
                SymbolicLocation q2t = move2.getTarget();

                List<SymbolicLocation> sources = new ArrayList<>();
                List<SymbolicLocation> targets = new ArrayList<>();

                /* Important!: The order of which the locations are added are important.
                 *   First we add q1 and then q2. This is VERY important as the for aggregated
                 *   systems the indices of complex locations and systems are not interchangeable. */

                if (asNested) {
                    sources.add(q1s);
                    targets.add(q1t);
                } else {
                    sources.addAll(((ComplexLocation) q1s).getLocations());
                    targets.addAll(((ComplexLocation) q1t).getLocations());
                }

                // Always add q2 after q1
                sources.add(q2s);
                targets.add(q2t);

                ComplexLocation source = new ComplexLocation(sources);
                ComplexLocation target = new ComplexLocation(targets);

                // If true then we remove the conjoined invariant created from all "targets"
                if (removeTargetLocationInvariant) {
                    target.removeInvariants();
                }

                List<Edge> edges = new ArrayList<>();
                edges.addAll(move1.getEdges());
                edges.addAll(move2.getEdges());

                // (q1s, q2s) -...-> (q1t, q2t)
                Move move = new Move(source, target, edges);
                moves.add(move);
            }
        }

        return moves;
    }

    public List<Location> updateLocations(Set<Location> locations, List<Clock> newClocks, List<Clock> oldClocks, List<BoolVar> newBVs, List<BoolVar> oldBVs) {
        return locations
                .stream()
                .map(location -> new Location(location, newClocks, oldClocks, newBVs, oldBVs))
                .collect(Collectors.toList());
    }

    public List<Edge> updateEdges(Set<Edge> edges, List<Clock> newClocks, List<Clock> oldClocks, List<BoolVar> newBVs, List<BoolVar> oldBVs) {
        return edges
                .stream()
                .map(edge -> new Edge(edge, newClocks, newBVs, oldClocks, oldBVs))
                .collect(Collectors.toList());
    }

    public String getLastErr() {
        return lastErr.toString();
    }

    public void buildErrMessage(List<String> inc, String checkType) {
        if (!(lastErr.length() == 0))
            lastErr.append(", ");
        if (inc.size() == 1) {
            lastErr.append("Automaton ");
            lastErr.append(inc.get(0));
            lastErr.append(" is ").append(checkType).append(".");
        } else {
            lastErr.append("Automata ");
            for (int i = 0; i < inc.size(); i++) {
                if (i == inc.size() - 1)
                    lastErr.append(inc.get(i));
                else {
                    lastErr.append(inc.get(i));
                    lastErr.append(", ");
                }
            }
            lastErr.append(" are ").append(checkType).append(".");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransitionSystem that = (TransitionSystem) o;
        return clocks.equals(that.clocks);
    }

    public abstract String getName();
    public abstract Automaton getAutomaton();
    public abstract Set<Channel> getInputs();
    public abstract Set<Channel> getOutputs();
    public abstract List<SimpleTransitionSystem> getSystems();
    protected abstract SymbolicLocation getInitialLocation();
    public abstract List<Transition> getNextTransitions(State currentState, Channel channel, List<Clock> allClocks);

    protected abstract List<Move> getNextMoves(SymbolicLocation location, Channel channel);
}