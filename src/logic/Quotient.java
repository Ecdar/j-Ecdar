package logic;

import models.*;
import java.util.*;
import java.util.stream.Collectors;

public class Quotient extends TransitionSystem {



    /*
    Commands for compiling the DBM


    g++ -shared -o DBM.dll -I"c:\Program Files\Java\jdk1.8.0_172\include" -I"C:\Program Files\Java\jdk1.8.0_172\include\win32" -fpermissive lib_DBMLib.cpp ../dbm/libs/libdbm.a ../dbm/libs/libbase.a ../dbm/libs/libdebug.a ../dbm/libs/libhash.a ../dbm/libs/libio.a
    C:\PROGRA~1\Java\jdk1.8.0_172\bin\javac.exe DBMLib.java -h .

    261
    */

    private final TransitionSystem ts_spec, ts_comp;
    private final Set<Channel> inputs, outputs;
    private final Channel newChan;
    private Clock newClock;
    private boolean printComments = false;

    public Quotient(TransitionSystem ts_spec, TransitionSystem ts_comp) {
        this.ts_spec = ts_spec;
        this.ts_comp = ts_comp;

        //clocks should contain the clocks of ts1, ts2 and a new clock
        newClock = new Clock("new");
        clocks.add(newClock);
        clocks.addAll(ts_spec.getClocks());
        clocks.addAll(ts_comp.getClocks());
        if (printComments) System.out.println("Clocks of ts1 ( " + ts_spec.getClocks() +" ) and ts2 ( " + ts_comp.getClocks() + " ) merged to + " + clocks);


        // inputs should contain inputs of ts1, outputs of ts2 and a new input
        inputs = new HashSet<>(ts_spec.getInputs());
        inputs.addAll(ts_comp.getOutputs());
        newChan = new Channel("newInput");
        inputs.add(newChan);

        Set<Channel> outputsOfSpec = new HashSet<>(ts_spec.getOutputs());
        outputsOfSpec.addAll(ts_spec.getSyncs());
        Set<Channel> outputsOfComp = new HashSet<>(ts_comp.getOutputs());
        outputsOfComp.addAll(ts_comp.getSyncs());                         // original: outputsOF1.addAll(ts2.getSyncs());


        outputs = new HashSet<>(outputsOfSpec);
        outputs.removeAll(outputsOfComp);
        System.out.println();

        if (printComments) System.out.println("ts1.in = " + ts_spec.getInputs() +", ts1.out = " + ts_spec.getOutputs());
        if (printComments) System.out.println("ts2.in = " + ts_comp.getInputs() +", ts2.out = " + ts_comp.getOutputs());
        if (printComments) System.out.println("quotient.in = " + inputs +", quotioent.out = " + outputs);

    }

    @Override
    public Automaton getAutomaton() {
        return calculateQuotientAutomaton().getAutomaton();
    }

    public SymbolicLocation getInitialLocation() {
        // the invariant of locations consisting of locations from each transition system should be true
        // which means the location has no invariants
        SymbolicLocation initLoc = getInitialLocation(new TransitionSystem[]{ts_spec, ts_comp});
        ((ComplexLocation) initLoc).removeInvariants();
        if (printComments) System.out.println("ts1.init = " + ts_spec.getInitialLocation() + ", ts2.init= " + ts_comp.getInitialLocation());
        if (printComments) System.out.println("quotients.init = " + initLoc);
        return initLoc;
    }

    public SimpleTransitionSystem calculateQuotientAutomaton() {

        String name = ts_spec.getSystems().get(0).getName() + "DIV" + ts_comp.getSystems().get(0).getName() ;

        // Lists of edges and locations for the newly built automaton
        List<Edge>  edges = new ArrayList<Edge>();
        List<Location>  locations = new ArrayList<Location>();

        // Map so I can easily access the new locations via their name
        Map<String,Location> locationMap = new HashMap<String,Location>();

        // TODO: currently I assume the init location to not be urgent
        Location init = new Location(ts_spec.getSystems().get(0).getAutomaton().getInitLoc().getName() + "DIV" + ts_comp.getSystems().get(0).getAutomaton().getInitLoc().getName() , new ArrayList<List<Guard>>() , true, ts_spec.getSystems().get(0).getAutomaton().getInitLoc().isUrgent() || ts_comp.getSystems().get(0).getAutomaton().getInitLoc().isUrgent() , false, false);

        // just an easy way to access spec and comp from here on
        // TODO: check that there is only one automaton in each, maybe implement so that several automata can be explored at once
        Automaton spec = ts_spec.getSystems().get(0).getAutomaton();
        Automaton comp = ts_comp.getSystems().get(0).getAutomaton();

        // create product of locations
        for (Location l_spec : spec.getLocations()) {
            for (Location l_comp : comp.getLocations()) {
                // TODO: I assume that if one location in the product is univ./inc., the product is univ/inc. and I do not need to create a location for them.
                if (!l_comp.getName().equals("univ") && !l_spec.getName().equals("univ") && !l_comp.getName().equals("inc") && !l_spec.getName().equals("inc")) {
                    boolean isInitial = l_spec.isInitial() && l_comp.isInitial();
                    boolean isUrgent = l_spec.isUrgent() || l_comp.isUrgent();
                    String locName = l_spec.getName() + "DIV" + l_comp.getName();
                    Location loc = new Location(locName, new ArrayList<List<Guard>>(), isInitial, isUrgent, false, false);
                    locationMap.put(locName, loc);
                    locations.add(loc);
                }
            }
        }

        // Create univ. and inc. location
        Location univ =   new Location("univ", new ArrayList<List<Guard>>(), false, false, true, false);
        Location inc = new Location("inc", new ArrayList<List<Guard>>(), false, true, false, true);

        locationMap.put("univ",univ);
        locationMap.put("inc",inc);
        locations.add(univ);
        locations.add(inc);

        Set<Channel> allChans = new HashSet<Channel>();
        allChans.addAll(inputs);
        allChans.addAll(outputs);


        // now come all the rules for building transitions
        for (Location l_spec : spec.getLocations())
            for (Location l_comp : comp.getLocations())
            {
                // loc is the location we are currently analyzing
                Location loc = locationMap.get(l_spec.getName()+"DIV"+l_comp.getName());
                //System.out.println(loc.getName());

                // selfloops for the inc / univ state will be newly created, so we do not need to take care of them here
                // TODO: I assume only the spec can have inc./univ. location. Is that true? NO!
                // TODO: Maybe that is now fixed. Check that!
                if (!l_spec.getName().equals("inc") && !l_spec.getName().equals("univ") && !l_comp.getName().equals("inc") && !l_comp.getName().equals("univ")) {

                    // Rule 2 (First transition in Figure)
                    if (!l_spec.getInvariant().isEmpty()) {
                        // negate the spec. invariant

                            CDD invarNegated = l_spec.getInvariantCDD().negation();
                                // merge the current part with the invariant of the component, to create each new transition

                                edges.add(new Edge(loc, inc, newChan, true, CDD.toGuardList(l_comp.getInvariantCDD().conjunction(invarNegated),clocks), new ArrayList<Update>(){{add(new ClockUpdate(newClock, 0));}}));
                       }

                    // Rule 1
                    if (!l_comp.getInvariant().isEmpty()) {
                        // TODO: I interpreted this rule as "for every channel". This will also create a transition for the new symbol. That is fine!
                        // negate the comp. invariant.
                        // TODO: some of the guards in the list we get here might actually be mergable
                        CDD l_comp_invar_negated = l_comp.getInvariantCDD().negation();
                        for (Channel c : allChans) {
                                boolean isInput = false;
                                if (inputs.contains(c)) isInput = true;
                                edges.add(new Edge(loc, univ, c, isInput, CDD.toGuardList(l_comp_invar_negated,clocks), new ArrayList<>()));

                        }
                    }


                    // rule 3 (cartesian product)
                    for (Channel c : allChans) {
                        // only if both spec and comp have a transition with this channel
                        if (!spec.getEdgesFromLocationAndSignal(l_spec, c).isEmpty() && !comp.getEdgesFromLocationAndSignal(l_comp, c).isEmpty()) {
                            // in case spec / comp has multiple transitions with c, we need to take every combination
                            for (Edge e_spec : spec.getEdgesFromLocationAndSignal(l_spec, c))
                                for (Edge e_comp : comp.getEdgesFromLocationAndSignal(l_comp, c)) {
                                    // find the target location. If it is inc / univ in spec, change it to the new inv/univ location
                                    Location target = locationMap.get(e_spec.getTarget().getName() + "DIV" + e_comp.getTarget().getName());
                                    if (e_spec.getTarget().getName().equals("inc"))
                                        target = inc;
                                    if (e_spec.getTarget().getName().equals("univ"))
                                        target = univ;
                                    // combine both guards
                                    CDD guard = e_spec.getGuardCDD().conjunction(e_comp.getGuardCDD());

                                    //combine both updates
                                    List<Update> updatesList = new ArrayList<Update>();
                                    updatesList.addAll(e_spec.getUpdates());
                                    updatesList.addAll(e_comp.getUpdates());
                                    //Update[] updates = updatesList.toArray(new Update[0]); // TODO: Check whether this is really the way to initialize an array from a list
                                    edges.add(new Edge(loc, target, c, !(e_comp.isInput() && !e_spec.isInput()), CDD.toGuardList(guard,clocks), updatesList));
                                }
                        }

                    }

                    // Rule 4
                    for (Channel c : ts_comp.getOutputs()) {
                        List<Guard> collectedNegationsSpec = new ArrayList<Guard>();
                        if (!comp.getEdgesFromLocationAndSignal(l_comp, c).isEmpty()) {
                            // collect all guards from spec transitions, negated
                            CDD guardsOfSpec = CDD.cddFalse();
                            for (Edge e_spec : spec.getEdgesFromLocationAndSignal(l_spec, c))
                                //collectedNegationsSpec.addAll(e_spec.getGuards().stream().map(Guard::negate).collect(Collectors.toList()));
                                 guardsOfSpec= guardsOfSpec.disjunction(e_spec.getGuardCDD());
                            CDD negated = guardsOfSpec.negation();



                            // TODO: Currently I am creating a transition if c-transitions in spec is empty.  Correct?
                            if (!collectedNegationsSpec.isEmpty() || spec.getEdgesFromLocationAndSignal(l_spec, c).isEmpty())
                                // for each c-transtion in comp, create a new transition with the negated guard
                                for (Edge e_comp : comp.getEdgesFromLocationAndSignal(l_comp, c)) {
                                    edges.add(new Edge(loc, inc, c, true,CDD.toGuardList(e_comp.getGuardCDD().conjunction(negated),clocks), new ArrayList<Update>(){{add(new ClockUpdate(newClock, 0));}}));
                                }
                        }
                    }

                    //Rule 5
                    for (Channel c : allChans) {
                        // for all channels that are not in the components alphabet
                        if (!ts_comp.getOutputs().contains(c) && !ts_comp.getInputs().contains(c)) {
                            // if the current location in spec has a transition with c
                            if (!spec.getEdgesFromLocationAndSignal(l_spec, c).isEmpty()) {
                                for (Edge e_spec : spec.getEdgesFromLocationAndSignal(l_spec, c)) {
                                    // create a transition to a new location with updated spec location, but some comp. location
                                    edges.add(new Edge(loc, locationMap.get(e_spec.getTarget().getName() + "DIV" + l_comp.getName()), c, e_spec.isInput(), e_spec.getGuards(), e_spec.getUpdates()));
                                }
                            }
                        }
                    }

                    //Rule 6
                    for (Channel c : ts_comp.getOutputs()) {
                        CDD collectedGuardsComp = CDD.cddFalse();
                        if (!spec.getEdgesFromLocationAndSignal(l_spec, c).isEmpty()) {
                            // collect all negated guards from c-transitions in  comp
                            for (Edge e_comp : comp.getEdgesFromLocationAndSignal(l_comp, c)) {
                                collectedGuardsComp = collectedGuardsComp.disjunction(e_comp.getGuardCDD());//.stream().map(Guard::negate).collect(Collectors.toList()));
                            }
                            CDD negated = collectedGuardsComp.negation();
                            // if guards have been collected, or the component didn't have c-transitions, for each spec trans create a transition
                            if (negated.isNotFalse() || comp.getEdgesFromLocationAndSignal(l_comp, c).isEmpty())
                                for (Edge e_spec : spec.getEdgesFromLocationAndSignal(l_spec, c)) {

                                    //combined.addAll(e_spec.getGuards()); //TODO: THIS IS IMPORTANT! Is this supposed to be in?????
                                    edges.add(new Edge(loc, univ, c, e_spec.isInput(), CDD.toGuardList(negated,clocks), new ArrayList<>()));
                                }
                        }
                    }


                   // for each input, create a selfloop in inc.
                   for (Channel c : getInputs()) {
                        Guard g = new ClockGuard(newClock, null, 0, Relation.LESS_EQUAL);
                        //Guard g1 = new ClockGuard(newClock, null, false, false);
                        List<Guard> guards = new ArrayList<Guard>();
                        guards.add(g);
                       // guards.add(g1);
                        List<List<Guard>> res = new ArrayList<>();
                        res.add(guards);
                        edges.add(new Edge(inc, inc, c, true, res, new ArrayList<>()));
                    }

                    // for each input or output,  create a selfloop in univ.
                    for (Channel c : getInputs()) {
                        List<List<Guard>> guards = new ArrayList<>();
                        edges.add(new Edge(univ, univ, c, true, guards, new ArrayList<>()));
                    }
                    for (Channel c : getOutputs()) {
                        List<List<Guard>> guards = new ArrayList<>();
                        edges.add(new Edge(univ, univ, c, false, guards, new ArrayList<>()));
                    }
                }
            }


        // turning transitions to univ that are not restricted by spec into selfloops
        // TODO: did I do this as we intended

        // first: collect all channels c, so that in every location of the specification, c only selfloops
        Set<Channel> channelsThatOnlySelfLoopInSpec = new HashSet<Channel>();
        for (Channel c: allChans)
        {
            // this boolean will become true if we find a transition that is not a selfloop
            boolean foundLocationsThatDoesNotLoop = false;
            for (Location l : spec.getLocations())
            {
                // if there is no c-transition, c cannot be ignored
                if (spec.getEdgesFromLocationAndSignal(l, c).isEmpty())
                    foundLocationsThatDoesNotLoop = true;
                else
                    for (Edge e : spec.getEdgesFromLocationAndSignal(l, c))
                        // if a transition with c is not a selfloop, c cannot be ignored.
                        if (!e.getSource().getName().equals(e.getTarget().getName()))
                            foundLocationsThatDoesNotLoop=true;
            }
            if (foundLocationsThatDoesNotLoop==false)
                channelsThatOnlySelfLoopInSpec.add(c);
        }

        // Cannot edit the lists of locations / transitions while we loop through them, so we need to collect the ones we want to add/remove.
        Set<Edge> toRemove = new HashSet<Edge>();
        Set<Edge> toAdd = new HashSet<Edge>();
        // for every channel that is independent, loop through all edges and transitions, to remove the ones that lead to univ and replace them by selfloops
        for (Channel c: channelsThatOnlySelfLoopInSpec)
        {
            for (Edge e: edges)
            {
                if (e.getTarget().getName().equals("univ") && e.getChannel().equals(c))
                {
                    toRemove.add(e);
                    toAdd.add(new Edge(e.getSource(), e.getSource(), e.getChannel(), e.isInput(), e.getGuards(), e.getUpdates()));
                }
            }
        }

        for (Edge e: toRemove)
            edges.remove(e);
        for (Edge e: toAdd)
            edges.add(e);


        Automaton aut = new Automaton(name,  locations,  edges, clocks, BVs, false);

        SimpleTransitionSystem simp = new SimpleTransitionSystem(aut);
        return simp;
    }




    public Set<Channel> getInputs() {
        return inputs;
    }

    public Set<Channel> getOutputs() {
        return outputs;
    }

    public List<SimpleTransitionSystem> getSystems(){
        // no idea what this is for
        List<SimpleTransitionSystem> result = new ArrayList<>();
        result.addAll(ts_spec.getSystems());
        result.addAll(ts_comp.getSystems());
        return result;
    }

    public List<Transition> getNextTransitions(State currentState, Channel channel, List<Clock> allClocks) {
        // get possible transitions from current state, for a given channel
        SymbolicLocation location = currentState.getLocation();

        List<Move> moves = getNextMoves(location, channel);
        return createNewTransitions(currentState, moves, allClocks);
    }

    public List<Move> getNextMoves(SymbolicLocation location, Channel channel) {
        List<Move> resultMoves = new ArrayList<>();

        if (location instanceof ComplexLocation) {
            List<SymbolicLocation> locations = ((ComplexLocation) location).getLocations();

            // symbolic locations corresponding to each TS
            SymbolicLocation loc1 = locations.get(0);
            SymbolicLocation loc2 = locations.get(1);

            // rule 2
            Move newMove = new Move(location, new InconsistentLocation(), new ArrayList<>());
            // invariant is negation of invariant of ts1 and invariant of ts2


            CDD negatedInvar = loc1.getInvariantCDD().negation();
            CDD combined = negatedInvar.conjunction(loc2.getInvariantCDD());
            newMove.setGuards(combined);
            newMove.setUpdates(new ArrayList<>(Collections.singletonList(new ClockUpdate(newClock, 0))));
            resultMoves.add(newMove);

            // rule 1
            if (getActions().contains(channel)) {
                Move newMove1 = new Move(location, new UniversalLocation(), new ArrayList<>());
                // negate invariant of ts2
                //assert (loc2.getInvariants().size()<=1);
                newMove1.setGuards(loc2.getInvariantCDD().negation());
                newMove1.setUpdates(new ArrayList<>(Collections.singletonList(new ClockUpdate(newClock, 0))));
                resultMoves.add(newMove1);
            }

            // rule 3 (cartesian product)
            if (ts_spec.getActions().contains(channel) && ts_comp.getActions().contains(channel)) {

                List<Move> movesFrom1 = ts_spec.getNextMoves(loc1, channel);
                List<Move> movesFrom2 = ts_comp.getNextMoves(loc2, channel);

                if (!movesFrom1.isEmpty() && !movesFrom2.isEmpty()) {
                    List<Move> moveProduct = moveProduct(movesFrom1, movesFrom2, true);
                    resultMoves.addAll(moveProduct);
                }
            }

            // rule 4 and 6
            if (ts_comp.getOutputs().contains(channel)) {
                List<Move> movesFrom1 = ts_spec.getNextMoves(loc1, channel);
                List<Move> movesFrom2 = ts_comp.getNextMoves(loc2, channel);

                // take all moves from ts1 in order to gather the guards and negate them
                List<CDD> moves = movesFrom1.stream().map(Move::getGuardCDD).collect(Collectors.toList());
                CDD movesTS1 = CDD.cddFalse();
                for (CDD cdd : moves)
                    movesTS1 = movesTS1.disjunction(cdd);
                CDD negated = movesTS1.negation();



                for (Move move : movesFrom2) {
                    Move newMove2 = new Move(location, new InconsistentLocation(), new ArrayList<>());
                    newMove2.setGuards(move.getGuardCDD().conjunction(negated));
                    newMove2.setUpdates(new ArrayList<>(Collections.singletonList(new ClockUpdate(newClock, 0))));
                    resultMoves.add(newMove2);
                }

                // take all moves from ts2 in order to gather the guards and negate them
                List<CDD> movesT2 = movesFrom2.stream().map(Move::getGuardCDD).collect(Collectors.toList());
                CDD movesTS2 = CDD.cddFalse();
                for (CDD cdd : movesT2)
                    movesTS2 = movesTS2.disjunction(cdd);
                // for doesn't make sense since we don't use anything from movesFrom1
                //for (Move move : movesFrom1) {
                    Move newMove4 = new Move(location, new UniversalLocation(), new ArrayList<>());
                    newMove4.setGuards(movesTS2);
                    resultMoves.add(newMove4);
               // }
            }

            // rule 5
            if (!ts_comp.getActions().contains(channel)) {
                List<Move> movesFrom1 = getNextMoves(loc1, channel);

                for (Move move : movesFrom1) {
                    SymbolicLocation newLoc = new ComplexLocation(new ArrayList<>(Arrays.asList(move.getTarget(), loc2)));
                    Move newMove3 = new Move(location, newLoc, new ArrayList<>());
                    newMove3.setGuards(move.getGuardCDD());
                    newMove3.setUpdates(move.getUpdates());
                    resultMoves.add(newMove3);
                }

            }
        } else if (location instanceof InconsistentLocation) {
            if (getInputs().contains(channel)) {
                Move newMove = new Move(location, new InconsistentLocation(), new ArrayList<>());
                newMove.setUpdates(new ArrayList<>(Collections.singletonList(new ClockUpdate(newClock, 0))));
                resultMoves.add(newMove);
            }
        } else if (location instanceof UniversalLocation) {
            if (getActions().contains(channel)) {
                Move newMove = new Move(location, new UniversalLocation(), new ArrayList<>());
                resultMoves.add(newMove);
            }
        }

        return resultMoves;
    }


}
