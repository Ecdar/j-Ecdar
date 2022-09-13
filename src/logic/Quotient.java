package logic;

import models.*;

import java.util.*;

public class Quotient extends TransitionSystem {
    private final TransitionSystem t, s;
    private final Set<Channel> inputs, outputs;
    private final Channel newChan;
    private Clock newClock;
    private SymbolicLocation univ = new UniversalLocation();
    private SymbolicLocation inc = new InconsistentLocation();

    private final HashMap<Clock, Integer> maxBounds = new HashMap<>();

    public Quotient(TransitionSystem t, TransitionSystem s) {
        this.t = t;
        this.s = s;

        //clocks should contain the clocks of ts1, ts2 and a new clock
        newClock = new Clock("quo_new", "quo"); //TODO: get ownerName in a better way
        clocks.add(newClock);
        clocks.addAll(t.getClocks());
        clocks.addAll(s.getClocks());
        BVs.addAll(t.getBVs());
        BVs.addAll(s.getBVs());

        // Act_i = Act_i^T ∪ Act_o^S
        inputs = union(t.getInputs(), s.getOutputs());
        newChan = new Channel("i_new");
        inputs.add(newChan);

        // Act_o = Act_o^T \ Act_o^S ∪ Act_i^S \ Act_i^T
        outputs = union(
                difference(t.getOutputs(), s.getOutputs()),
                difference(s.getInputs(), t.getInputs())
        );

        maxBounds.putAll(t.getMaxBounds());
        maxBounds.putAll(s.getMaxBounds());
    }

    @Override
    public Automaton getAutomaton() {
        return calculateQuotientAutomaton().getAutomaton();
    }

    public SymbolicLocation getInitialLocation() {
        // the invariant of locations consisting of locations from each transition system should be true
        // which means the location has no invariants
        return getInitialLocation(new TransitionSystem[]{t, s});
    }

    public SimpleTransitionSystem calculateQuotientAutomaton() {
        return calculateQuotientAutomaton(false);
    }

    public SimpleTransitionSystem calculateQuotientAutomaton(boolean prepareForBisimilarityReduction) {
        assert false;

        // Lists of edges and locations for the newly built automaton
        List<Edge> edges = new ArrayList<Edge>();
        List<Location> locations = new ArrayList<Location>();

        // Map so I can easily access the new locations via their name
        Map<String, Location> locationMap = new HashMap<String, Location>();

        // just an easy way to access spec and comp from here on
        // TODO: check that there is only one automaton in each, maybe implement so that several automata can be explored at once
        //      assert (left.getSystems().size() == 1);
        //      assert (right.getSystems().size() == 1);

        //      Automaton spec = left.getSystems().get(0).getAutomaton();
        //      Automaton comp = right.getSystems().get(0).getAutomaton();

        Automaton spec = t.getAutomaton();
        Automaton comp = s.getAutomaton();

        boolean initialisedCdd = CDD.tryInit(clocks.getItems(), BVs.getItems());
        String name = t.getSystems().get(0).getName() + "DIV" + s.getSystems().get(0).getName();

        // create product of locations
        for (Location l_spec : spec.getLocations()) {
            for (Location l_comp : comp.getLocations()) {
                // I assume that if one location in the product is univ./inc., the product is univ/inc. and I do not need to create a location for them.
                if (!l_comp.getName().equals("univ") && !l_spec.getName().equals("univ") && !l_comp.getName().equals("inc") && !l_spec.getName().equals("inc")) {
                    boolean isInitial = l_spec.isInitial() && l_comp.isInitial();
                    boolean isUrgent = l_spec.isUrgent() || l_comp.isUrgent();
                    String locName = l_spec.getName() + "DIV" + l_comp.getName();
                    Location loc = new Location(locName, new TrueGuard(), isInitial, isUrgent, false, false);
                    locationMap.put(locName, loc);
                    locations.add(loc);
                }
            }
        }

        // Create univ. and inc. location
        Location univ = new Location("univ", new TrueGuard(), false, false, true, false);
        Location inc = new Location("inc", new TrueGuard(), false, true, false, true);

        locationMap.put("univ", univ);
        locationMap.put("inc", inc);
        locations.add(univ);
        locations.add(inc);

        Set<Channel> allChans = new HashSet<Channel>();
        allChans.addAll(inputs);
        allChans.addAll(outputs);

        // now come all the rules for building transitions
        for (Location l_spec : spec.getLocations())
            for (Location l_comp : comp.getLocations()) {
                // loc is the location we are currently analyzing
                Location loc = locationMap.get(l_spec.getName() + "DIV" + l_comp.getName());

                // selfloops for the inc / univ state will be newly created, so we do not need to take care of them here
                if (!l_spec.getName().equals("inc") && !l_spec.getName().equals("univ") && !l_comp.getName().equals("inc") && !l_comp.getName().equals("univ")) {
                    System.out.println(loc.getName());
                    System.out.println("RULE1");
                    // rule 1 "cartesian product"
                    for (Channel c : allChans) {
                        // only if both spec and comp have a transition with this channel
                        if (!spec.getEdgesFromLocationAndSignal(l_spec, c).isEmpty() && !comp.getEdgesFromLocationAndSignal(l_comp, c).isEmpty()) {
                            // in case spec / comp has multiple transitions with c, we need to take every combination
                            for (Edge e_spec : spec.getEdgesFromLocationAndSignal(l_spec, c)) {
                                for (Edge e_comp : comp.getEdgesFromLocationAndSignal(l_comp, c)) {
                                    // find the target location. If it is inc / univ in spec, change it to the new inc/univ location
                                    Location target = locationMap.get(e_spec.getTarget().getName() + "DIV" + e_comp.getTarget().getName());
                                    if (e_spec.getTarget().getName().equals("inc")) // todo: make those attributes of the location class
                                        target = inc;
                                    if (e_spec.getTarget().getName().equals("univ")) target = univ;
                                    if (e_comp.getTarget().getName().equals("inc")) // todo: make those attributes of the location class
                                        target = inc;
                                    if (e_comp.getTarget().getName().equals("univ")) target = univ;
                                    // combine both guards
                                    CDD guard = e_spec.getGuardCDD().conjunction(e_comp.getGuardCDD());
                                    guard = guard.conjunction(l_comp.getInvariantCDD());
                                    CDD compTarget = e_comp.getTarget().getInvariantCDD().transitionBack(e_comp);
                                    CDD specTarget = e_spec.getTarget().getInvariantCDD().transitionBack(e_spec);
                                    guard = guard.conjunction(compTarget).conjunction(specTarget);
                                    //combine both updates
                                    List<Update> updatesList = new ArrayList<Update>();
                                    updatesList.addAll(e_spec.getUpdates());
                                    updatesList.addAll(e_comp.getUpdates());

                                    boolean isInput = inputs.contains(e_comp.getChan());
                                    Edge resultE = new Edge(loc, target, c, isInput, guard.getGuard(clocks.getItems()), updatesList);
                                    edges.add(resultE);
                                }
                            }
                        }
                    }

                    System.out.println("RULE 2");
                    //Rule 2: "channels in comp not in spec"
                    for (Channel c : allChans) {
                        // for all channels that are not in the spec alphabet
                        if (!t.getOutputs().contains(c) && !t.getInputs().contains(c)) {
                            // if the current location in comp has a transition with c
                            if (!comp.getEdgesFromLocationAndSignal(l_comp, c).isEmpty()) {
                                for (Edge e_comp : comp.getEdgesFromLocationAndSignal(l_comp, c)) {
                                    // create a transition to a new location with updated comp location, but same spec location
                                    Location target = locationMap.get(l_spec.getName() + "DIV" + e_comp.getTarget().getName());
                                    if (e_comp.getTarget().getName().equals("inc"))
                                        target = inc;
                                    if (e_comp.getTarget().getName().equals("univ")) target = univ;

                                    CDD targetInvar = e_comp.getTarget().getInvariantCDD();
                                    targetInvar = targetInvar.transitionBack(e_comp);
                                    targetInvar = targetInvar.conjunction(e_comp.getGuardCDD());
                                    targetInvar = targetInvar.conjunction(l_comp.getInvariantCDD());
                                    targetInvar = targetInvar.conjunction(l_spec.getInvariantCDD());
                                    boolean isInput = inputs.contains(e_comp.getChan());
                                    edges.add(new Edge(loc, target, c, isInput, targetInvar.getGuard(clocks.getItems()), e_comp.getUpdates()));
                                }
                            }
                        }
                    }


                    System.out.println("RULE 3 and 4");
                    //Rule 3+4: "edge to univ for negated comp guards"
                    for (Channel c : s.getOutputs()) {
                        CDD collectedGuardsComp = CDD.cddFalse();
                        // collect all negated guards from c-transitions in  comp
                        for (Edge e_comp : comp.getEdgesFromLocationAndSignal(l_comp, c)) {
                            collectedGuardsComp = collectedGuardsComp.disjunction(e_comp.getTarget().getInvariantCDD().transitionBack(e_comp));
                        }
                        CDD negated = collectedGuardsComp.negation().removeNegative();
                        boolean isInput = inputs.contains(c);

                        // if guards have been collected
                        if (negated.isNotFalse())
                            edges.add(new Edge(loc, univ, c, isInput, negated.getGuard(clocks.getItems()), new ArrayList<>()));
                    }

                    System.out.println("RULE 5");
                    // Rule 5 "transition to univ for negated comp invariant"
                    if (!l_comp.getInvariantCDD().isTrue()) {
                        // negate the comp. invariant.
                        CDD l_comp_invar_negated = l_comp.getInvariantCDD().negation().removeNegative();
                        for (Channel c : allChans) {
                            boolean isInput = inputs.contains(c);
                            edges.add(new Edge(loc, univ, c, isInput, l_comp_invar_negated.getGuard(clocks.getItems()), new ArrayList<>()));
                        }
                    }

                    System.out.println("RULE 6");
                    // Rule 6 "edge to inconsistent for common outputs blocked in spec"
                    Set<Channel> combinedOutputs = new HashSet<>(s.getOutputs());
                    combinedOutputs.retainAll(t.getOutputs());
                    for (Channel c : combinedOutputs) {
                        if (!comp.getEdgesFromLocationAndSignal(l_comp, c).isEmpty()) {
                            // collect all guards from spec transitions, negated
                            CDD guardsOfSpec = CDD.cddFalse();
                            for (Edge e_spec : spec.getEdgesFromLocationAndSignal(l_spec, c))
                                guardsOfSpec = guardsOfSpec.disjunction(e_spec.getTarget().getInvariantCDD()).transitionBack(e_spec);
                            System.out.println("guards of spec: " + guardsOfSpec);
                            guardsOfSpec.printDot();
                            CDD negated = guardsOfSpec.negation();
                            System.out.println("negated: " + negated);
                            negated.printDot();
                            if (negated.isNotFalse())
                            {
                                // for each c-transtion in comp, create a new transition with the negated guard
                                for (Edge e_comp : comp.getEdgesFromLocationAndSignal(l_comp, c)) {
                                    CDD targetState =e_comp.getTarget().getInvariantCDD();
                                    targetState= targetState.transitionBack(e_comp);
                                    targetState= targetState.conjunction(negated);
                                    assert(inputs.contains(c));
                                    List<Update> updates = new ArrayList<Update>() {{
                                        add(new ClockUpdate(newClock, 0));
                                    }};
                                    System.out.println("adding edge");
                                    edges.add(new Edge(loc, inc, c, true, targetState.getGuard(clocks.getItems()), updates));
                                }
                            }
                        }
                    }

                    System.out.println("RULE 7");
                    // Rule 7 "Edge for negated spec invariant"
                    if (!l_spec.getInvariantCDD().isTrue()) {
                        // negate the spec. invariant
                        CDD invarNegated = l_spec.getInvariantCDD().negation();
                        System.out.println(l_spec.getInvariantCDD());
                        System.out.println(invarNegated);

                        // merge the negation with the invariant of the component, to create each new transition
                        CDD combined = l_comp.getInvariantCDD().conjunction(invarNegated);
                        List<Update> updates = new ArrayList<Update>() {{
                            add(new ClockUpdate(newClock, 0));
                        }};
                        System.out.println("adding edge " + combined);
                        edges.add(new Edge(loc, inc, newChan, true, combined.getGuard(clocks.getItems()), updates));
                    }



                    System.out.println("RULE 8");
                    //Rule 8: "independent action in spec"
                    for (Channel c : allChans) {
                        // for all channels that are not in the components alphabet
                        if (!s.getOutputs().contains(c) && !s.getInputs().contains(c)) {
                            // if the current location in spec has a transition with c
                            if (!spec.getEdgesFromLocationAndSignal(l_spec, c).isEmpty()) {
                                for (Edge e_spec : spec.getEdgesFromLocationAndSignal(l_spec, c)) {
                                    // create a transition to a new location with updated spec location, but some comp. location
                                    Location target = locationMap.get(e_spec.getTarget().getName() + "DIV" + l_comp.getName());
                                    if (e_spec.getTarget().getName().equals("inc"))
                                        target = inc;
                                    if (e_spec.getTarget().getName().equals("univ")) target = univ;

                                    CDD targetInvar = e_spec.getTarget().getInvariantCDD();
                                    targetInvar = targetInvar.transitionBack(e_spec);
                                    targetInvar = targetInvar.conjunction(l_comp.getInvariantCDD());
                                    targetInvar = targetInvar.conjunction(l_spec.getInvariantCDD());


                                    if (c.getName().equals("patent"))
                                        System.out.println("HERE " + targetInvar);
                                    boolean isInput = inputs.contains(c);
                                    edges.add(new Edge(loc, target, c, isInput, targetInvar.getGuard(clocks.getItems()), e_spec.getUpdates()));
                                }
                            }
                        }
                    }
                    System.out.println("ONE LOOP DONE");
                }
            }

        // Rule 10: for each input, create a selfloop in inc.
        for (Channel c : getInputs()) {
            Guard g = new ClockGuard(newClock, null, 0, Relation.LESS_EQUAL);
            edges.add(new Edge(inc, inc, c, true, g, new ArrayList<>()));
        }


        // Rule 9: for each input or output,  create a selfloop in univ.
        for (Channel c : getInputs()) {
            edges.add(new Edge(univ, univ, c, true, new TrueGuard(), new ArrayList<>()));
        }
        for (Channel c : getOutputs()) {
            edges.add(new Edge(univ, univ, c, false, new TrueGuard(), new ArrayList<>()));
        }

        System.out.println("Done with creating edges");
        if (prepareForBisimilarityReduction) {
            // turning transitions to univ that are not restricted by spec into selfloops
            // TODO: did I do this as we intended

            // first: collect all channels c, so that in every location of the specification, c only selfloops
            Set<Channel> channelsThatOnlySelfLoopInSpec = new HashSet<Channel>();
            for (Channel c : allChans) {
                // this boolean will become true if we find a transition that is not a selfloop
                boolean foundLocationsThatDoesNotLoop = false;
                for (Location l : spec.getLocations()) {
                    // if there is no c-transition, c cannot be ignored
                    if (spec.getEdgesFromLocationAndSignal(l, c).isEmpty())
                        foundLocationsThatDoesNotLoop = true;
                    else
                        for (Edge e : spec.getEdgesFromLocationAndSignal(l, c))
                            // if a transition with c is not a selfloop, c cannot be ignored.
                            if (!e.getSource().getName().equals(e.getTarget().getName()))
                                foundLocationsThatDoesNotLoop = true;
                }
                if (foundLocationsThatDoesNotLoop == false)
                    channelsThatOnlySelfLoopInSpec.add(c);
            }

            Set<Channel> allChannels = new HashSet<>();
            allChannels.addAll(allChans);
            allChannels.removeAll(spec.getInputAct());
            allChannels.removeAll(spec.getOutputAct());
            channelsThatOnlySelfLoopInSpec.addAll(allChannels);

            // Cannot edit the lists of locations / transitions while we loop through them, so we need to collect the ones we want to add/remove.
            Set<Edge> toRemove = new HashSet<Edge>();
            Set<Edge> toAdd = new HashSet<Edge>();
            // for every channel that is independent, loop through all edges and transitions, to remove the ones that lead to univ and replace them by selfloops
            for (Edge e : edges) {
                if (e.getTarget().getName().equals("univ") & channelsThatOnlySelfLoopInSpec.contains(e.getChannel())) {
                    toRemove.add(e);
                    toAdd.add(new Edge(e.getSource(), e.getSource(), e.getChannel(), e.isInput(), e.getGuard(), e.getUpdates()));
                }
            }


            for (Edge e : toRemove)
                edges.remove(e);
            for (Edge e : toAdd)
                edges.add(e);
        }

        newClock = new Clock("quo_new", "quo");
        clocks.add(newClock);
        List <Location> locsWithNewClocks = updateLocations(new HashSet<>(locations),clocks.getItems(), clocks.getItems(),BVs.getItems(),BVs.getItems());
        List <Edge> edgesWithNewClocks = updateEdges(new HashSet<>(edges),clocks.getItems(), clocks.getItems(),BVs.getItems(), BVs.getItems());
        if (initialisedCdd) {
            CDD.done();
        }
        Automaton aut = new Automaton(name, locsWithNewClocks, edgesWithNewClocks, clocks.getItems(), BVs.getItems(), true);

        SimpleTransitionSystem simp = new SimpleTransitionSystem(aut);

        return simp;
    }


    public Set<Channel> getInputs() {
        return inputs;
    }

    public Set<Channel> getOutputs() {
        return outputs;
    }

    public List<SimpleTransitionSystem> getSystems() {
        // no idea what this is for
        List<SimpleTransitionSystem> result = new ArrayList<>();
        result.addAll(t.getSystems());
        result.addAll(s.getSystems());
        return result;
    }

    @Override
    public String getName() {
        return t.getName() + "//" + s.getName();
    }

    public List<Transition> getNextTransitions(State currentState, Channel channel, List<Clock> allClocks) {
        // get possible transitions from current state, for a given channel
        SymbolicLocation location = currentState.getLocation();

        List<Move> moves = getNextMoves(location, channel);
        List<Transition> result = createNewTransitions(currentState, moves, allClocks);

        // assert(!result.isEmpty());
        return result;
    }

    public List<Move> getNextMoves(SymbolicLocation location, Channel a) {
        List<Move> resultMoves = new ArrayList<>();
        System.out.println("gettingNextMove of " + location.getName());
        if (location instanceof ComplexLocation) {
            List<SymbolicLocation> locations = ((ComplexLocation) location).getLocations();

            // symbolic locations corresponding to each TS
            SymbolicLocation lt = locations.get(0);
            SymbolicLocation ls = locations.get(1);

            List<Move> t_moves = t.getNextMoves(lt, a);
            List<Move> s_moves = s.getNextMoves(ls, a);

            // rule 1 (cartesian product)
            if (in(a, intersect(s.getActions(), t.getActions()))) {
                List<Move> moveProduct = moveProduct(t_moves, s_moves, true,true);
                for (Move move : moveProduct) {
                    move.conjunctCDD(move.getEnabledPart());
                }
                resultMoves.addAll(moveProduct);
            }

            // rule 2
            if (in(a, difference(s.getActions(), t.getActions()))) {
                List<Move> movesLeft = new ArrayList<>();
                movesLeft.add(new Move(lt,lt, new ArrayList<>()));

                List<Move> moveProduct = moveProduct(movesLeft, s_moves, true,true);
                for (Move move : moveProduct) {
                    move.conjunctCDD(move.getEnabledPart());
                }
                resultMoves.addAll(moveProduct);
            }

            // rule 3
            // rule 4
            // rule 5
            if (in(a, s.getOutputs())) {
                CDD guard_s = CDD.cddFalse();
                for (Move s_move : s_moves) {
                    guard_s = guard_s.disjunction(s_move.getEnabledPart());
                }
                guard_s = guard_s.negation().removeNegative().reduce();

                CDD inv_neg_inv_loc_s = ls.getInvariant().negation().removeNegative().reduce();

                CDD combined = guard_s.disjunction(inv_neg_inv_loc_s);

                Move move = new Move(location, univ, new ArrayList<>());
                move.conjunctCDD(combined);
                resultMoves.add(move);
            } else {
                CDD inv_neg_inv_loc_s = ls.getInvariant().negation().removeNegative().reduce();

                Move move = new Move(location, univ);
                move.conjunctCDD(inv_neg_inv_loc_s);
                resultMoves.add(move);
            }

            // rule 6
            if (in(a, intersect(t.getOutputs(), s.getOutputs()))) {
                // take all moves from left in order to gather the guards and negate them
                CDD CDDFromMovesFromLeft = CDD.cddFalse();
                for (Move moveLeft : t_moves) {
                    CDDFromMovesFromLeft = CDDFromMovesFromLeft.disjunction(moveLeft.getEnabledPart());
                }
                CDD negated = CDDFromMovesFromLeft.negation().removeNegative().reduce();


                for (Move move : s_moves) {
                    Move newMoveRule6 = new Move(location, inc, new ArrayList<>());
                    newMoveRule6.setGuards(move.getEnabledPart().conjunction(negated));
                    newMoveRule6.setUpdates(new ArrayList<>(Collections.singletonList(new ClockUpdate(newClock, 0))));
                    resultMoves.add(newMoveRule6);
                }
            }

            // rule 7
            if (Objects.equals(a.getName(), this.newChan.getName())) {
                Move newMoveRule7 = new Move(location, inc, new ArrayList<>());
                // invariant is negation of invariant of left conjuncted with invariant of right
                CDD negatedInvar = lt.getInvariant().negation();
                CDD combined = negatedInvar.conjunction(ls.getInvariant());

                newMoveRule7.setGuards(combined);
                newMoveRule7.setUpdates(new ArrayList<>(Collections.singletonList(new ClockUpdate(newClock, 0))));
                resultMoves.add(newMoveRule7);
                System.out.println("Rule 7");
            }

            // rule 8
            if (in(a, difference(t.getActions(), s.getActions()))) {
                List<Move> movesRight = new ArrayList<>();
                movesRight.add(new Move(ls,ls,new ArrayList<>()));
                List<Move> moveProduct = moveProduct(t_moves, movesRight, true,true);
                for (Move move : moveProduct) {
                    move.conjunctCDD(move.getEnabledPart());
                }
                resultMoves.addAll(moveProduct);
            }

            // Rule 10
        } else if (location instanceof InconsistentLocation) {
            if (getInputs().contains(a)) {
                Move newMove = new Move(location, inc, new ArrayList<>());
                newMove.setUpdates(new ArrayList<>(Collections.singletonList(new ClockUpdate(newClock, 0))));
                resultMoves.add(newMove);
            }
            // Rule 9
        } else if (location instanceof UniversalLocation) {
            if (getActions().contains(a)) {
                Move newMove = new Move(location, univ, new ArrayList<>());
                resultMoves.add(newMove);
            }
        }

        return resultMoves;
    }

    private boolean in(Channel element, Set<Channel> set) {
        return set.contains(element);
    }

    private boolean disjoint(Set<Channel> set1, Set<Channel> set2) {
        return empty(intersect(set1, set2));
    }

    private boolean empty(Set<Channel> set) {
        return set.isEmpty();
    }

    private Set<Channel> intersect(Set<Channel> set1, Set<Channel> set2) {
        Set<Channel> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);
        return intersection;
    }

    private Set<Channel> difference(Set<Channel> set1, Set<Channel> set2) {
        Set<Channel> difference = new HashSet<>(set1);
        difference.removeAll(set2);
        return difference;
    }

    private Set<Channel> union(Set<Channel> set1, Set<Channel> set2) {
        Set<Channel> union = new HashSet<>(set1);
        union.addAll(set2);
        return union;
    }
}
