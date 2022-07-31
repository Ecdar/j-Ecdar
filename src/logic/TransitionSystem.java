package logic;

import models.*;

import java.util.*;
import java.util.stream.Collectors;

import static models.CDD.indexOf;

// parent class for all TS's, so we can use it with regular TS's, composed TS's etc.
public abstract class TransitionSystem {
    final UniqueNamedContainer<Clock> clocks;
    final UniqueNamedContainer<BoolVar> BVs;
    private StringBuilder lastErr = new StringBuilder();

    TransitionSystem() {
        this.clocks = new UniqueNamedContainer<>();
        this.BVs = new UniqueNamedContainer<>();
    }

    public abstract Automaton getAutomaton();

    public List<Clock> getClocks() {
        return clocks.getItems();
    }

    public List<BoolVar> getBVs() {
        return BVs.getItems();
    };


    public State getInitialState() {
        CDD initCDD = CDD.cddZeroDelayed();
        CDD bddPart = CDD.cddTrue();
        for (BoolVar bv : BVs.getItems())
        {
            if (bv.getInitialValue())
                bddPart = bddPart.conjunction(CDD.createBddNode(CDD.bddStartLevel + indexOf(bv)));
            else {

                bddPart = bddPart.conjunction(CDD.createNegatedBddNode(CDD.bddStartLevel + indexOf(bv)));

            }
        }

        State state = new State(getInitialLocation(), initCDD.conjunction(bddPart));
        state.applyInvariants();
        return state;
    }

    public State getInitialStateRef( CDD invs) {

        CDD initCDD = CDD.cddZeroDelayed();
        CDD bddPart = CDD.cddTrue();
        for (BoolVar bv : CDD.BVs)
        {
            if (bv.getInitialValue())
                bddPart = bddPart.conjunction(CDD.createBddNode(CDD.bddStartLevel + indexOf(bv)));
            else
                bddPart = bddPart.conjunction(CDD.createNegatedBddNode(CDD.bddStartLevel + indexOf(bv)));
        }

        State state = new State(getInitialLocation(), initCDD.conjunction(bddPart));

        state.applyInvariants();
        state.applyGuards(invs);

        return state;
    }

    protected abstract SymbolicLocation getInitialLocation();

    SymbolicLocation getInitialLocation(TransitionSystem[] systems) {
        // build ComplexLocation with initial location from each TransitionSystem
        List<SymbolicLocation> initials = Arrays
                .stream(systems)
                .map(TransitionSystem::getInitialLocation)
                .collect(Collectors.toList());
        return new ComplexLocation(initials);
    }

    Transition createNewTransition(State state, Move move) {
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

    List<Transition> createNewTransitions(State currentState, List<Move> moves, List<Clock> allClocks) {
        List<Transition> transitions = new ArrayList<>();

        for (Move move : moves) {
            Transition transition = createNewTransition(
                    currentState, move
            );

            // Check if it is unreachable and if so then ignore it
            if (transition.getTarget().getInvariant().isFalse()) {
                continue;
            }

            transitions.add(
                    transition
            );
        }

        return transitions;
    }

    public abstract Set<Channel> getInputs();

    public abstract Set<Channel> getOutputs();

    public abstract List<SimpleTransitionSystem> getSystems();

    public Set<Channel> getActions() {
        Set<Channel> actions = new HashSet<>(getInputs());
        actions.addAll(getOutputs());

        return actions;
    }

    public String getLastErr() {
        return lastErr.toString();
    }

    public void clearLastErr() {
        lastErr = new StringBuilder();
    }


    public boolean isDeterministic(){
        if (!CDD.isCddIsRunning())
        {
            CDD.init(CDD.maxSize,CDD.cs,CDD.stackSize);
            CDD.addClocks(clocks.getItems());
            CDD.addBooleans(BVs.getItems());

        }

        boolean isDeterministic = true;
        List<String> nondetermTs = new ArrayList<>();

        List<SimpleTransitionSystem> systems = getSystems();

        for (SimpleTransitionSystem ts : systems){
            if(!ts.isDeterministicHelper()){
                isDeterministic = false;
                nondetermTs.add(ts.getName());
            }
        }

        if(!isDeterministic) buildErrMessage(nondetermTs, "non-deterministic");

        CDD.done();
        return isDeterministic;
    }

    public boolean isLeastConsistent(){
        boolean result = isConsistent(true);
        return result;
    }

    public abstract String getName();

    public boolean isFullyConsistent(){
        return isConsistent(false);
    }

    private boolean isConsistent(boolean canPrune) {
        boolean isDeterm = isDeterministic();
        boolean isConsistent = true;
        CDD.init(CDD.maxSize,CDD.cs,CDD.stackSize);
        CDD.addClocks(getClocks());
        CDD.addBooleans(BVs.getItems());

        List<String> inconsistentTs = new ArrayList<>();
        List<SimpleTransitionSystem> systems = getSystems();
        for (SimpleTransitionSystem ts : systems){
            if(!ts.isConsistentHelper(canPrune)) {
                isConsistent = false;
                inconsistentTs.add(ts.getName());
            }
        }
        if(!isConsistent) buildErrMessage(inconsistentTs, "inconsistent");

        CDD.done();
        return isConsistent && isDeterm;
    }

    public boolean isImplementation(){
        boolean isCons = isFullyConsistent();
        CDD.init(CDD.maxSize,CDD.cs,CDD.stackSize);
        CDD.addClocks(getClocks());
        CDD.addBooleans(BVs.getItems());
        boolean isImpl = true;
        List<String> nonImpl = new ArrayList<>();
        List<SimpleTransitionSystem> systems = getSystems();

        for (SimpleTransitionSystem ts : systems){
            if(!ts.isImplementationHelper())
                isImpl = false;
            nonImpl.add(ts.getName());
        }
        if(!isImpl) {
            buildErrMessage(nonImpl, "not output urgent");
        }
        CDD.done();
        return isImpl && isCons;
    }

    public HashMap<Clock,Integer> getMaxBounds(){
        List<SimpleTransitionSystem> systems = getSystems();
        HashMap<Clock,Integer> res = new HashMap<>();

        for (TransitionSystem ts : systems){
            res.putAll(ts.getMaxBounds());
        }
        return res;
    }

    public List<Transition> getNextTransitions(State currentState, Channel channel){
        return getNextTransitions(currentState, channel, clocks.getItems());
    }

    public abstract List<Transition> getNextTransitions(State currentState, Channel channel, List<Clock> allClocks);

    protected abstract List<Move> getNextMoves(SymbolicLocation location, Channel channel);

    protected boolean hasMove(SymbolicLocation location, Channel channel) {
        return !getNextMoves(location, channel).isEmpty();
    }

    List<Move> moveProduct(List<Move> moves1, List<Move> moves2, boolean toNest, boolean removeTargetInvars) {
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

                if (toNest) {
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
                if (removeTargetInvars) {
                    target.removeInvariants();
                }

                List<Edge> edges = new ArrayList<>();
                edges.addAll(move1.getEdges());
                edges.addAll(move2.getEdges());

                // (q1s, q2s) -...-> (q1t, q2t)
                moves.add(
                        new Move(source, target, edges)
                );
            }
        }

        return moves;
    }

    public List<Location> updateClocksInLocs(Set<Location> locs, List<Clock> newClocks, List<Clock> oldClocks, List<BoolVar> newBVs, List<BoolVar> oldBVs)
    {
        List<Location> result = new ArrayList<>();
        for (Location loc: locs)
        {
            result.add(new Location(loc, newClocks, oldClocks,newBVs,oldBVs));
        }
        return result;
    }

    public List<Edge> updateClocksInEdges(Set<Edge> edges, List<Clock> newClocks, List<Clock> oldClocks, List<BoolVar> newBVs, List<BoolVar> oldBVs)
    {
        List<Edge> result = new ArrayList<>();
        for (Edge edge: edges)
        {
            result.add(new Edge(edge, newClocks, newBVs, edge.getSource(), edge.getTarget(), oldClocks,oldBVs));
        }
        return result;
    }

    public void buildErrMessage(List<String> inc, String checkType) {
        if (! (lastErr.length()==0))
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
}