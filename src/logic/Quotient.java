package logic;

import models.*;

import java.util.*;
import java.util.stream.Collectors;

import static logic.Helpers.randomString;

public class Quotient extends TransitionSystem {



    /*
    Commands for compiling the DBM


    g++ -shared -o DBM.dll -I"c:\Program Files\Java\jdk1.8.0_172\include" -I"C:\Program Files\Java\jdk1.8.0_172\include\win32" -fpermissive lib_DBMLib.cpp ../dbm/libs/libdbm.a ../dbm/libs/libbase.a ../dbm/libs/libdebug.a ../dbm/libs/libhash.a ../dbm/libs/libio.a
    C:\PROGRA~1\Java\jdk1.8.0_172\bin\javac.exe DBMLib.java -h .

    261
    */

    private final TransitionSystem left, right;
    private final Set<Channel> inputs, outputs;
    private final Channel newChan;
    private Clock newClock;
    private boolean printComments = false;
    SymbolicLocation univ = new UniversalLocation();
    SymbolicLocation inc = new InconsistentLocation();

    public Quotient(TransitionSystem left, TransitionSystem right) {
        this.left = left;
        this.right = right;

        //clocks should contain the clocks of ts1, ts2 and a new clock
        newClock = new Clock("quo_new", "quo"); //TODO: get ownerName in a better way
        clocks.add(newClock);
        clocks.addAll(left.getClocks());
        clocks.addAll(right.getClocks());
        BVs.addAll(left.getBVs());
        BVs.addAll(right.getBVs());
        if (printComments)
            System.out.println("Clocks of ts1 ( " + left.getClocks() + " ) and ts2 ( " + right.getClocks() + " ) merged to + " + clocks);


        // inputs should contain inputs of ts1, outputs of ts2 and a new input
        inputs = new HashSet<>(left.getInputs());
        inputs.addAll(right.getOutputs());
        newChan = new Channel("i_new");
        inputs.add(newChan);

        Set<Channel> outputsOfSpec = new HashSet<>(left.getOutputs());
        outputsOfSpec.addAll(left.getSyncs());
        Set<Channel> outputsOfComp = new HashSet<>(right.getOutputs());
        outputsOfComp.addAll(right.getSyncs());

        Set<Channel> inputsOfCompMinusInputsOfSpec = new HashSet<>(right.getInputs());
        inputsOfCompMinusInputsOfSpec.removeAll(left.getInputs());
        outputs = new HashSet<>(outputsOfSpec);
        outputs.removeAll(outputsOfComp);
        outputs.addAll(inputsOfCompMinusInputsOfSpec);

        printComments =true;
        if (printComments)
            System.out.println("ts1.in = " + left.getInputs() + ", ts1.out = " + left.getOutputs());
        if (printComments)
            System.out.println("ts2.in = " + right.getInputs() + ", ts2.out = " + right.getOutputs());
        if (printComments) System.out.println("quotient.in = " + inputs + ", quotioent.out = " + outputs);
        printComments =false;
    }

    @Override
    public Automaton getAutomaton() {
        Automaton res = calculateQuotientAutomaton().getAutomaton();
        return res;
    }

    public SymbolicLocation getInitialLocation() {
        // the invariant of locations consisting of locations from each transition system should be true
        // which means the location has no invariants
        SymbolicLocation initLoc = getInitialLocation(new TransitionSystem[]{left, right});
        ((ComplexLocation) initLoc).removeInvariants();
        if (printComments)
            System.out.println("ts1.init = " + left.getInitialLocation() + ", ts2.init= " + right.getInitialLocation());
        if (printComments) System.out.println("quotients.init = " + initLoc);
        return initLoc;
    }

    public SimpleTransitionSystem calculateQuotientAutomaton() {
        return calculateQuotientAutomaton(false);
    }

    public SimpleTransitionSystem calculateQuotientAutomaton(boolean prepareForBisimilarityReduction) {


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

        Automaton spec = left.getAutomaton();
        Automaton comp = right.getAutomaton();

        CDD.init(CDD.maxSize, CDD.cs, CDD.stackSize);
        CDD.addClocks(clocks.getItems());
        CDD.addBddvar(BVs.getItems());
        String name = left.getSystems().get(0).getName() + "DIV" + right.getSystems().get(0).getName();

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
                                    Edge resultE = new Edge(loc, target, c, isInput, CDD.toGuardList(guard, clocks.getItems()), updatesList);
                                    edges.add(resultE);
                                }
                            }
                        }
                    }

                    System.out.println("RULE 2");
                    //Rule 2: "channels in comp not in spec"
                    for (Channel c : allChans) {
                        // for all channels that are not in the spec alphabet
                        if (!left.getOutputs().contains(c) && !left.getInputs().contains(c) && !left.getSyncs().contains(c)) {
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
                                    edges.add(new Edge(loc, target, c, isInput, CDD.toGuardList(targetInvar,clocks.getItems()), e_comp.getUpdates()));
                                }
                            }
                        }
                    }


                    System.out.println("RULE 3 and 4");
                    //Rule 3+4: "edge to univ for negated comp guards"
                    for (Channel c : right.getOutputs()) {
                        CDD collectedGuardsComp = CDD.cddFalse();
                        // collect all negated guards from c-transitions in  comp
                        for (Edge e_comp : comp.getEdgesFromLocationAndSignal(l_comp, c)) {
                            collectedGuardsComp = collectedGuardsComp.disjunction(e_comp.getTarget().getInvariantCDD().transitionBack(e_comp));
                        }
                        CDD negated = collectedGuardsComp.negation().removeNegative();
                        boolean isInput = inputs.contains(c);

                        // if guards have been collected
                        if (negated.isNotFalse())
                            edges.add(new Edge(loc, univ, c, isInput, CDD.toGuardList(negated, clocks.getItems()), new ArrayList<>()));
                    }

                    System.out.println("RULE 5");
                    // Rule 5 "transition to univ for negated comp invariant"
                    if (!l_comp.getInvariantCDD().isTrue()) {
                        // negate the comp. invariant.
                        CDD l_comp_invar_negated = l_comp.getInvariantCDD().negation().removeNegative();
                        for (Channel c : allChans) {
                            boolean isInput = inputs.contains(c);
                            edges.add(new Edge(loc, univ, c, isInput, CDD.toGuardList(l_comp_invar_negated, clocks.getItems()), new ArrayList<>()));
                        }
                    }

                    System.out.println("RULE 6");
                    // Rule 6 "edge to inconsistent for common outputs blocked in spec"
                    Set<Channel> combinedOutputs = new HashSet<>(right.getOutputs());
                    combinedOutputs.retainAll(left.getOutputs());
                    for (Channel c : combinedOutputs) {
                        if (!comp.getEdgesFromLocationAndSignal(l_comp, c).isEmpty()) {
                            // collect all guards from spec transitions, negated
                            CDD guardsOfSpec = CDD.cddFalse();
                            for (Edge e_spec : spec.getEdgesFromLocationAndSignal(l_spec, c))
                                guardsOfSpec = guardsOfSpec.disjunction(e_spec.getTarget().getInvariantCDD()).transitionBack(e_spec);
                            CDD negated = guardsOfSpec.negation();

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
                                    edges.add(new Edge(loc, inc, c, true, CDD.toGuardList(targetState, clocks.getItems()), updates));
                                }
                            }
                        }
                    }

                    System.out.println("RULE 7");
                    // Rule 7 "Edge for negated spec invariant"
                    if (!l_spec.getInvariantCDD().isTrue()) {
                        // negate the spec. invariant
                        CDD invarNegated = l_spec.getInvariantCDD().negation();

                        // merge the negation with the invariant of the component, to create each new transition
                        CDD combined = l_comp.getInvariantCDD().conjunction(invarNegated);
                        List<Update> updates = new ArrayList<Update>() {{
                            add(new ClockUpdate(newClock, 0));
                        }};
                        edges.add(new Edge(loc, inc, newChan, true, CDD.toGuardList(combined, clocks.getItems()), updates));
                    }



                    System.out.println("RULE 8");
                    //Rule 8: "independent action in spec"
                    for (Channel c : allChans) {
                        // for all channels that are not in the components alphabet
                        if (!right.getOutputs().contains(c) && !right.getInputs().contains(c) && !right.getSyncs().contains(c)) {
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
                                    edges.add(new Edge(loc, target, c, isInput, CDD.toGuardList(targetInvar,clocks.getItems()), e_spec.getUpdates()));
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
                    toAdd.add(new Edge(e.getSource(), e.getSource(), e.getChannel(), e.isInput(), e.getGuards(), e.getUpdates()));
                }
            }


        for (Edge e : toRemove)
            edges.remove(e);
        for (Edge e : toAdd)
            edges.add(e);
        }

        newClock = new Clock("quo_new", "quo");
        clocks.add(newClock);
        List <Location> locsWithNewClocks = updateClocksInLocs(new HashSet<>(locations),clocks.getItems(), clocks.getItems(),BVs.getItems(),BVs.getItems());
        List <Edge> edgesWithNewClocks = updateClocksInEdges(new HashSet<>(edges),clocks.getItems(), clocks.getItems(),BVs.getItems(), BVs.getItems());
        CDD.done();
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
        result.addAll(left.getSystems());
        result.addAll(right.getSystems());
        return result;
    }

    @Override
    public String getName() {
        return left.getName() + "//" + right.getName();
    }

    public List<Transition> getNextTransitions(State currentState, Channel channel, List<Clock> allClocks) {
        // get possible transitions from current state, for a given channel
        SymbolicLocation location = currentState.getLocation();

        List<Move> moves = getNextMoves(location, channel);
        List<Transition> result = createNewTransitions(currentState, moves, allClocks);

       // assert(!result.isEmpty());
        return result;
    }

    public List<Move> getNextMoves(SymbolicLocation location, Channel channel) {
        List<Move> resultMoves = new ArrayList<>();
        System.out.println("gettingNextMove of " + location.getName());
        if (location instanceof ComplexLocation) {
            List<SymbolicLocation> locations = ((ComplexLocation) location).getLocations();

            // symbolic locations corresponding to each TS
            SymbolicLocation locLeft = locations.get(0);
            SymbolicLocation locRight = locations.get(1);


            // rule 1 (cartesian product)
            if (left.getActions().contains(channel) && right.getActions().contains(channel)) {
                List<Move> movesLeft = left.getNextMoves(locLeft, channel);
                List<Move> movesRight = right.getNextMoves(locRight, channel);

                if (!movesLeft.isEmpty() && !movesRight.isEmpty()) {
                    List<Move> moveProduct = moveProduct(movesLeft, movesRight, true,true);
                    for (Move move : moveProduct) {
                        move.conjunctCDD(move.getEnabledPart());
                    }
                    resultMoves.addAll(moveProduct);
                    System.out.println("Rule 1");
                }
            }

            // rule 2
            if (!left.getActions().contains(channel) && right.getActions().contains(channel)) {
                List<Move> movesRight = right.getNextMoves(locRight, channel);
                List<Move> movesLeft = new ArrayList<>();
                movesLeft.add(new Move(locLeft,locLeft,new ArrayList<>())); // TODO: check that this works
                if (!movesRight.isEmpty()) {
                    List<Move> moveProduct = moveProduct(movesLeft, movesRight, true,true);
                    for (Move move : moveProduct) {
                        move.conjunctCDD(move.getEnabledPart());
                    }
                    System.out.println("Rule 2");
                    resultMoves.addAll(moveProduct);
                }
            }

            // rule 8
            if (left.getActions().contains(channel) && !right.getActions().contains(channel)) {
                List<Move> movesLeft = left.getNextMoves(locLeft, channel);
                List<Move> movesRight = new ArrayList<>();
                movesRight.add(new Move(locRight,locRight,new ArrayList<>())); // TODO: check that this works
                if (!movesLeft.isEmpty()) {
                    List<Move> moveProduct = moveProduct(movesLeft, movesRight, true,true);
                    for (Move move : moveProduct) {
                        move.conjunctCDD(move.getEnabledPart());
                    }
                    System.out.println("Rule 8");
                    resultMoves.addAll(moveProduct);
                }
            }




            // rule 7
            Move newMoveRule7 = new Move(location, inc, new ArrayList<>());
            // invariant is negation of invariant of left conjuncted with invariant of right
            CDD negatedInvar = locLeft.getInvariantCDD().negation();
            CDD combined = negatedInvar.conjunction(locRight.getInvariantCDD());
            newMoveRule7.setGuards(combined);
            newMoveRule7.setUpdates(new ArrayList<>(Collections.singletonList(new ClockUpdate(newClock, 0))));
            resultMoves.add(newMoveRule7);
            System.out.println("Rule 7");

            // rule 5
            if (getActions().contains(channel)) {
                System.out.println("Rule 5");
                Move newMoveRule5 = new Move(location, univ, new ArrayList<>());
                // negate invariant of ts2
                newMoveRule5.setGuards(locRight.getInvariantCDD().negation());
                newMoveRule5.setUpdates(new ArrayList<>(Collections.singletonList(new ClockUpdate(newClock, 0))));
                resultMoves.add(newMoveRule5);
            }



            // rule 6
            if (left.getOutputs().contains(channel) && right.getOutputs().contains(channel)) {
                List<Move> movesFromLeft = left.getNextMoves(locLeft, channel);
                List<Move> movesFromRight = right.getNextMoves(locRight, channel);

                // take all moves from left in order to gather the guards and negate them
                CDD CDDFromMovesFromLeft = CDD.cddFalse();
                for (Move moveLeft : movesFromLeft) {
                    CDDFromMovesFromLeft = CDDFromMovesFromLeft.disjunction(moveLeft.getEnabledPart());
                }
                CDD negated = CDDFromMovesFromLeft.negation().removeNegative();


                for (Move move : movesFromRight) {
                    System.out.println("Rule 6");
                    Move newMoveRule6 = new Move(location, inc, new ArrayList<>());
                    newMoveRule6.setGuards(move.getEnabledPart().conjunction(negated));
                    newMoveRule6.setUpdates(new ArrayList<>(Collections.singletonList(new ClockUpdate(newClock, 0))));
                    resultMoves.add(newMoveRule6);
                }
            }

            // rule 3+4
            if (right.getOutputs().contains(channel)) {
                List<Move> movesFromRight = right.getNextMoves(locRight, channel);
                // take all moves from right in order to gather the guards and negate them
                CDD CDDFromMovesFromRight = CDD.cddFalse();
                for (Move move : movesFromRight) {
                    CDDFromMovesFromRight = CDDFromMovesFromRight.disjunction(move.getEnabledPart());
                }
                CDDFromMovesFromRight = CDDFromMovesFromRight.negation().removeNegative();

                System.out.println("Rule 3/4");
                Move newMove4 = new Move(location, univ, new ArrayList<>());
                newMove4.setGuards(CDDFromMovesFromRight);
                resultMoves.add(newMove4);
            }

            // rule 5
            if (!right.getActions().contains(channel)) {
                List<Move> movesFrom1 = left.getNextMoves(locLeft, channel);

                for (Move move : movesFrom1) {
                    System.out.println("Rule 5");
                    SymbolicLocation newLoc = new ComplexLocation(new ArrayList<>(Arrays.asList(move.getTarget(), locRight)));
                    ((ComplexLocation) newLoc).removeInvariants();
                    Move newMove3 = new Move(location, newLoc, new ArrayList<>());
                    CDD targetInvar = move.getTarget().getInvariantCDD();
                    targetInvar = targetInvar.transitionBack(move);
                    newMove3.setGuards(move.getGuardCDD().conjunction(targetInvar));
                    newMove3.setUpdates(move.getUpdates());
                    resultMoves.add(newMove3);
                }

            }
            // Rule 10
        } else if (location instanceof InconsistentLocation) {
            if (getInputs().contains(channel)) {
                System.out.println("Rule 10");
                Move newMove = new Move(location, inc, new ArrayList<>());
                newMove.setUpdates(new ArrayList<>(Collections.singletonList(new ClockUpdate(newClock, 0))));
                resultMoves.add(newMove);
            }
            // Rule 9
        } else if (location instanceof UniversalLocation) {
            if (getActions().contains(channel)) {
                System.out.println("Rule 9");
                Move newMove = new Move(location, univ, new ArrayList<>());
                resultMoves.add(newMove);
            }
        }
        System.out.println("result moves");
        for (Move m : resultMoves)
           System.out.println(m.getSource().getName() + " -> " + /*m.getEdges().get(0).getChannel() +*/ " -> " + m.getTarget().getName());
        return resultMoves;
    }


}
