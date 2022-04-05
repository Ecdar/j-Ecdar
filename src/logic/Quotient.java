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

                            List<List<Guard>> guardList = l_spec.getInvariant();
                            List<List<Guard>> negatedInvSpec = negateGuards(guardList); //.stream().map(Guard::negate).collect(Collectors.toList());
                            // Old: since the negation will create disjunction, we need to create a transition for each part.
                            // New: Since we now support discjunction, we only need to add one transition
                            //for (List<Guard> g : negatedInvSpec) {
                                // merge the current part with the invariant of the component, to create each new transition
                                List<List<List<Guard>>> big = new ArrayList<>();
                                big.add(negatedInvSpec);
                                big.add(l_comp.getInvariant());
                                List<List<Guard>> res = cartesianProductBig(big);
                                edges.add(new Edge(loc, inc, newChan, true, res, new Update[]{new Update(newClock, 0)}));
                            //}

                    }

                    // Rule 1
                    if (!l_comp.getInvariant().isEmpty()) {
                        // TODO: I interpreted this rule as "for every channel". This will also create a transition for the new symbol. That is fine!
                        // negate the comp. invariant.
                        // TODO: some of the guards in the list we get here might actually be mergable
                        //assert(l_comp.getInvariant().size()<=1);
                        List<List<Guard>> guardList = l_comp.getInvariant();
                        List<List<Guard>> negatedInvComp = negateGuards(guardList);
                        for (Channel c : allChans) {
                            // handle disjunction
                            //for (Guard g : negatedInvComp) {
                            //    List<Guard> list = new ArrayList<Guard>();
                            //    list.add(g);
                                boolean isInput = false;
                                if (inputs.contains(c)) isInput = true;
                                edges.add(new Edge(loc, univ, c, isInput, negatedInvComp, new Update[]{}));

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
                                    List<List<List<Guard>>> big = new ArrayList<>();
                                    big.add(e_spec.getGuards());
                                    big.add(e_comp.getGuards());

                                    //combine both updates
                                    List<Update> updatesList = new ArrayList<Update>();
                                    updatesList.addAll(Arrays.asList(e_spec.getUpdates()));
                                    updatesList.addAll(Arrays.asList(e_comp.getUpdates()));
                                    Update[] updates = updatesList.toArray(new Update[0]); // TODO: Check whether this is really the way to initialize an array from a list
                                    edges.add(new Edge(loc, target, c, !(e_comp.isInput() && !e_spec.isInput()), cartesianProductBig(big), updates));
                                }
                        }

                    }

                    // Rule 4
                    for (Channel c : ts_comp.getOutputs()) {
                        List<Guard> collectedNegationsSpec = new ArrayList<Guard>();
                        if (!comp.getEdgesFromLocationAndSignal(l_comp, c).isEmpty()) {
                            // collect all guards from spec transitions, negated
                            List<List<Guard>> guardsOfSpec = new ArrayList<>();
                            for (Edge e_spec : spec.getEdgesFromLocationAndSignal(l_spec, c))
                                //collectedNegationsSpec.addAll(e_spec.getGuards().stream().map(Guard::negate).collect(Collectors.toList()));
                                 guardsOfSpec.addAll(e_spec.getGuards());
                            List<List<Guard>> negated = negateGuards(guardsOfSpec);



                            // TODO: Currently I am creating a transition if c-transitions in spec is empty.  Correct?
                            if (!collectedNegationsSpec.isEmpty() || spec.getEdgesFromLocationAndSignal(l_spec, c).isEmpty())
                                // for each c-transtion in comp, create a new transition with the negated guard
                                // G_T was a disjunction, so its negation is a conjunction and can just be added.
                                for (Edge e_comp : comp.getEdgesFromLocationAndSignal(l_comp, c)) {
                                    List<List<List<Guard>>> big = new ArrayList<>();
                                    big.add(negated);
                                    big.add(e_comp.getGuards());

                                    edges.add(new Edge(loc, inc, c, true, cartesianProductBig(big), new Update[]{new Update(newClock, 0)}));
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
                        List<List<Guard>> collectedGuardsComp = new ArrayList<>();
                        if (!spec.getEdgesFromLocationAndSignal(l_spec, c).isEmpty()) {
                            // collect all negated guards from c-transitions in  comp
                            for (Edge e_comp : comp.getEdgesFromLocationAndSignal(l_comp, c)) {
                                collectedGuardsComp.addAll(e_comp.getGuards());//.stream().map(Guard::negate).collect(Collectors.toList()));
                            }
                            List<List<Guard>> negated = negateGuards(collectedGuardsComp);
                            // if guards have been collected, or the component didn't have c-transitions, for each spec trans create a transition
                            if (!negated.isEmpty() || comp.getEdgesFromLocationAndSignal(l_comp, c).isEmpty())
                                for (Edge e_spec : spec.getEdgesFromLocationAndSignal(l_spec, c)) {
                                    List<List<Guard>> combined = new ArrayList<>();
                                    combined.addAll(negated);
                                    //combined.addAll(e_spec.getGuards()); //TODO: THIS IS IMPORTANT! Is this supposed to be in?????
                                    edges.add(new Edge(loc, univ, c, e_spec.isInput(), combined, new Update[]{}));
                                }
                        }
                    }


                   // for each input, create a selfloop in inc.
                   for (Channel c : getInputs()) {
                        Guard g = new Guard(newClock, 0, true, false);
                        Guard g1 = new Guard(newClock, 0, false, false);
                        List<Guard> guards = new ArrayList<Guard>();
                        guards.add(g);
                        guards.add(g1);
                        List<List<Guard>> res = new ArrayList<>();
                        res.add(guards);
                        edges.add(new Edge(inc, inc, c, true, res, new Update[]{}));
                    }

                    // for each input or output,  create a selfloop in univ.
                    for (Channel c : getInputs()) {
                        List<List<Guard>> guards = new ArrayList<>();
                        edges.add(new Edge(univ, univ, c, true, guards, new Update[]{}));
                    }
                    for (Channel c : getOutputs()) {
                        List<List<Guard>> guards = new ArrayList<>();
                        edges.add(new Edge(univ, univ, c, false, guards, new Update[]{}));
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


        Automaton aut = new Automaton(name,  locations,  edges, clocks,false);

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

    public List<Refinement.Transition> getNextTransitions(Refinement.State currentState, Channel channel, List<Clock> allClocks) {
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

            List<List<Guard>> newGuards = negateGuards(loc1.getInvariants());
            //newGuards.addAll(loc2.getInvariants());

            List<List<List<Guard>>> bigList = new ArrayList<>();
            bigList.add(newGuards);
            bigList.add(loc2.getInvariants());



            newMove.setGuards(cartesianProductBig(bigList));
            newMove.setUpdates(new ArrayList<>(Collections.singletonList(new Update(newClock, 0))));
            resultMoves.add(newMove);

            // rule 1
            if (getActions().contains(channel)) {
                Move newMove1 = new Move(location, new UniversalLocation(), new ArrayList<>());
                // negate invariant of ts2
                //assert (loc2.getInvariants().size()<=1);
                newMove1.setGuards(negateGuards(loc2.getInvariants()));
                newMove1.setUpdates(new ArrayList<>(Collections.singletonList(new Update(newClock, 0))));
                resultMoves.add(newMove1);
            }

            // rule 3 (cartesian product)
            if (ts_spec.getActions().contains(channel) && ts_comp.getActions().contains(channel)) {

                List<Move> movesFrom1 = ts_spec.getNextMoves(loc1, channel);
                List<Move> movesFrom2 = ts_comp.getNextMoves(loc2, channel);
                //System.out.println("loc1" + loc1);
                //System.out.println("loc2" + loc2);
                //System.out.println("movesFrom1" + movesFrom1);
                //System.out.println("movesFrom2" + movesFrom2);
                if (!movesFrom1.isEmpty() && !movesFrom2.isEmpty()) {

                    List<Move> moveProduct = moveProduct(movesFrom1, movesFrom2, true);
                    //System.out.println("here" + moveProduct);
                    resultMoves.addAll(moveProduct);
                }
            }

            // rule 4 and 6
            if (ts_comp.getOutputs().contains(channel)) {
                List<Move> movesFrom1 = ts_spec.getNextMoves(loc1, channel);
                List<Move> movesFrom2 = ts_comp.getNextMoves(loc2, channel);

                // take all moves from ts1 in order to gather the guards and negate them
                List<List<Guard>> newGuards2 = negateGuards(movesFrom1.stream().map(Move::getGuards).flatMap(List::stream).collect(Collectors.toList()));

                for (Move move : movesFrom2) {
                    Move newMove2 = new Move(location, new InconsistentLocation(), new ArrayList<>());
                    List<List<List<Guard>>> newGuards3 = new ArrayList<>();
                    newGuards3.add(move.getGuards());
                    newGuards3.add(newGuards2);
                    List<List<Guard>> result = cartesianProductBig(newGuards3);
                    newMove2.setGuards(result);
                    newMove2.setUpdates(new ArrayList<>(Collections.singletonList(new Update(newClock, 0))));
                    resultMoves.add(newMove2);
                }

                // take all moves from ts2 in order to gather the guards and negate them
                List<List<Guard>> newGuards4 = negateGuards(movesFrom2.stream().map(Move::getGuards).flatMap(List::stream).collect(Collectors.toList()));

                // doesn't make sense since we don't use anything from movesFrom1
                for (Move move : movesFrom1) {
                    Move newMove4 = new Move(location, new UniversalLocation(), new ArrayList<>());
                    newMove4.setGuards(newGuards4);
                    resultMoves.add(newMove4);
                }
            }

            // rule 5
            if (!ts_comp.getActions().contains(channel)) {
                List<Move> movesFrom1 = getNextMoves(loc1, channel);

                for (Move move : movesFrom1) {
                    SymbolicLocation newLoc = new ComplexLocation(new ArrayList<>(Arrays.asList(move.getTarget(), loc2)));
                    Move newMove3 = new Move(location, newLoc, new ArrayList<>());
                    newMove3.setGuards(move.getGuards());
                    newMove3.setUpdates(move.getUpdates());
                    resultMoves.add(newMove3);
                }

            }
        } else if (location instanceof InconsistentLocation) {
            if (getInputs().contains(channel)) {
                Move newMove = new Move(location, new InconsistentLocation(), new ArrayList<>());
                newMove.setUpdates(new ArrayList<>(Collections.singletonList(new Update(newClock, 0))));
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
    public static List<List<Guard>> negateGuards(List<List<Guard>> origGuards)
    {
//        neg ((a && b) || (c && d) || (e && f && g))

//        neg (a && b) && neg (c && d) && neg (e && f && g)

//        (neg a || neg b) && (neg c || neg d) && (neg e || neg f || neg g)

//        (neg a && neg c && neg e) || (neg a && neg c && neg f) || (neg a && neg c && neg g) || ....


        List<List<Guard>> bigListOfNegatedConstraints = new ArrayList<>();
        for (List<Guard> disj : origGuards)
        {
            List <Guard> temp = new ArrayList<>();
            for (Guard g : disj) {
                temp.add(g.negate());
            }
            bigListOfNegatedConstraints.add(temp);
        }

        return (cartesianProduct(bigListOfNegatedConstraints));

    }

    public static List<List<Guard>> cartesianProduct(List<List<Guard>> lists) {

        List<List<Guard>> product = new ArrayList<List<Guard>>();

        for (List<Guard> list : lists) {

            List<List<Guard>> newProduct = new ArrayList<List<Guard>>();

            for (Guard listElement : list) {

                if (product.isEmpty()) {

                    List<Guard> newProductList = new ArrayList<Guard>();

                    newProductList.add(listElement);
                    newProduct.add(newProductList);
                } else {

                    for (List<Guard> productList : product) {

                        List<Guard> newProductList = new ArrayList<Guard>(productList);
                        newProductList.add(listElement);
                        newProduct.add(newProductList);
                    }
                }
            }

            product = newProduct;
        }

        return product;
    }

    public static List<List<Guard>> cartesianProductBig(List<List<List<Guard>>> lists) {

        List<List<Guard>> product = new ArrayList<List<Guard>>();

        for (List<List<Guard>> list : lists) {

            List<List<Guard>> newProduct = new ArrayList<List<Guard>>();

            for (List<Guard> listElement : list) {

                if (product.isEmpty()) {

                    List<List<Guard>> newProductList = new ArrayList<List<Guard>>();
                    newProductList.add(listElement);
                    newProduct.addAll(newProductList);
                } else {

                    for (List<Guard> productList : product) {

                        List<Guard> newProductList = new ArrayList<Guard>(productList);
                        newProductList.addAll(listElement);
                        newProduct.add(newProductList);
                    }
                }
            }

            product = newProduct;
        }

        return product;
    }


}
