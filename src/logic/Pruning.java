package logic;

import log.Log;
import models.*;

import java.util.*;
import java.util.stream.Collectors;

public class Pruning {

    private static boolean printComments = true;



    public static SimpleTransitionSystem adversarialPruning(TransitionSystem ts) {

        Automaton aut = new Automaton(ts.getAutomaton());
        List<Clock> clocks = aut.getClocks();
        List<BoolVar> BVs = aut.getBVs();
        List<Edge> edges = aut.getEdges();
        List<Location> locations = aut.getLocations();

        Set<Location> inconsistentLocations;
        Map<Location, CDD> passedInconsistentStates;

        boolean initialisedCdd = CDDRuntime.tryInit(clocks, BVs);


        for (Location l : locations) {
            l.setInconsistentPart(CDD.cddFalse());
        }

        // Create a set of inconsistent locations, that we can loop through
        inconsistentLocations = getInitiallyInconsistentLocations(locations);

        setInconsistentCDDs(inconsistentLocations);


        // TODO: make sure the passedInconsistentStates check is actually working as intended
        passedInconsistentStates = new HashMap<Location, CDD>();
        for (Location l : inconsistentLocations) {
            passedInconsistentStates.put(l, l.getInconsistentPart());
        }

        boolean initialStateIsInconsistent =false;

        Queue<Location> inconsistentQueue = new ArrayDeque<>(inconsistentLocations);
        // continue while there is still unprocessed inconsistent locations
        while (!inconsistentQueue.isEmpty()) {
            // select the first inconsistent location in the set.
            Location targetLoc = inconsistentQueue.remove();

            initialStateIsInconsistent = checkInitialState(targetLoc);
            if (initialStateIsInconsistent)
                break;

            if (printComments)
                Log.debug("Handling the new location " + targetLoc);

            // for all incoming transitions // TODO: is this really supposed to include selfloops
            for (Edge e : edges.stream().filter(e -> e.getTarget().equals(targetLoc)).collect(Collectors.toList())) { // && !e.getSource().equals(targetLoc)).collect(Collectors.toList())) {
                if (e.isInput()) {
                    handleInput(targetLoc, e, edges,passedInconsistentStates,inconsistentQueue);
                } else
                    handleOutput(targetLoc, e, edges, clocks, passedInconsistentStates, inconsistentQueue);
            }
        }

        if (printComments)
            Log.debug("no more inconsistent locations");

        addInconsistentPartsToInvariants(locations,clocks);

        if (printComments)
            Log.debug("inconsistent parts integrated into invariants");

        addInvariantsToGuards(edges,clocks);

        if (printComments)
            Log.debug("invariants integrated into guards");

        if (initialStateIsInconsistent) {
            locations = new ArrayList<>();
            locations.add(Location.create("inc", new TrueGuard(), true, false, false, true));
            edges = new ArrayList<>();
        }

        if (initialisedCdd) {
            CDDRuntime.done();
        }

        Automaton resAut = new Automaton(aut.getName(), locations, edges, clocks, aut.getBVs(), true);
        return new SimpleTransitionSystem(resAut);
    }

    /**
     * Checks if the current location is the initial one, and if it is inconsistent without delaying.
     * @param targetLoc the current location to be checked
     * @return true if the location is initial and inconsistent
     */
    private static boolean checkInitialState(Location targetLoc) {
        if (targetLoc.isInitial())
        {
            CDD initial = CDD.cddZero();
            if (targetLoc.getInconsistentPart().intersects(initial))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Set the inconsistent part of each initially inconsistent location to true
     * @param inconsistentLocations all initially inconsistent locations
     */
    private static void setInconsistentCDDs(Set<Location> inconsistentLocations) {
        for (Location l : inconsistentLocations)
            l.setInconsistentPart(CDD.cddUnrestrained());
    }

    /**
     * Finds and returns all inconsistent locations
     * @param locations The list of all locations
     * @return The list of inconsistent locations
     */
    private static Set<Location> getInitiallyInconsistentLocations(List<Location> locations) {
        HashSet<Location> inconsistentLocations = new HashSet<>(locations.stream().filter(l -> l.isInconsistent()).collect(Collectors.toList()));
        return  inconsistentLocations;
    }

    /**
     *  Iterates through all locations, and modifies the invariants to exclude all inconsistent parts
     * @param locations List of all locations
     * @param clocks List of all clocks
     */
    public static void addInconsistentPartsToInvariants(List<Location> locations, List<Clock> clocks) {
        for (Location l : locations) {
            if (l.isInconsistent()) {
                CDD incCDD = l.getInconsistentPart();
                if (incCDD.isUnrestrained()) {
                    l.setInvariantGuard(new FalseGuard());
                }
                else {
                    CDD invariantCDD = CDDFactory.createFrom(l.getInvariantGuard());
                    CDD invarMinusIncCDD = invariantCDD.minus(l.getInconsistentPart());
                    l.setInvariantGuard(invarMinusIncCDD.getGuard(clocks));
                }
            }
        }
    }

    /**
     * Adds all invariants to the guards, just as input enabling does.
     * @param edges List of all edges
     * @param clocks List of all clocks
     */
    public static void addInvariantsToGuards(List<Edge> edges, List<Clock> clocks) {
        for (Edge e : edges) {
            if (!(e.getTarget().getInvariantGuard() instanceof TrueGuard)) {
                if (!(e.getTarget().getInvariantGuard() instanceof FalseGuard)) {
                    CDD target = CDDFactory.createFrom(e.getTarget().getInvariantGuard());
                    CDD cddBeforeEdge = target.transitionBack(e);
                    CDD sourceInvariantCdd = CDDFactory.createFrom(e.getSource().getInvariantGuard());
                    e.setGuard(cddBeforeEdge.conjunction(sourceInvariantCdd).getGuard(clocks));
                }
            }
        }
    }

    /**
     * Handle an output towards an inconsistent location
     * @param targetLoc the target location
     * @param e the edge towards that location
     * @param edges the set of all edges in the automaton
     * @param clocks the set of all clocks
     * @param passedPairs the list of passed inconsistent states
     * @param inconsistentQueue the queue of inconsistent states to process
     */
    public static void handleOutput(Location targetLoc, Edge e, List<Edge>edges, List<Clock> clocks,  Map<Location, CDD> passedPairs, Queue<Location> inconsistentQueue ) {

        if (printComments)
            Log.debug("Handling an output to inc.");
        // If the whole target location is inconsistent, we just remove the transition
        // else we take the inconsistent part, free clocks reset by the current transition, and strengthen the guards so it cannot reach it

        if (targetLoc.getInconsistentPart().isUnrestrained()) {
            if (printComments)
                Log.debug("fully inconsistent target");
            edges.remove(e);
        } else {
            if (printComments)
                Log.debug("partially inconsistent target");

            // strengthen the guard, so that it cannot reach the inconsistent part of the target location

            // TODO 05.02.21: include target invariant or source invariant here?

            // take the inconsistent federation and free the clocks of the output transition
            CDD target = new CDD(targetLoc.getInconsistentPart().getPointer());
            target = target.transitionBack(e);
            CDD guardCDD = e.getGuardCDD();
            CDD fedAfterRemovingInconsistentPart =guardCDD.minus(target);

            e.setGuard(fedAfterRemovingInconsistentPart.getGuard(clocks));
        }

        // Removing the transition / strenthening the guards might have turned the source location inconsistent
        // This happens if there is an invariant, and part of the invariant cannot delay to enable an output anymore

        // if there is no invariant, there cannot be a deadlock, and we do not care about whether there is any input or outputs leaving
        if (e.getSource().getInvariantGuard() instanceof TrueGuard) {
            if (printComments)
                Log.debug("Source has no invariant, nothing more to do");
        } else {
            if (printComments)
                Log.debug("Processing source location to put it on the inconsistent location queue");

            // build the federation of all transitions that could save us (= the consistent part of all output transitions) // TODO: Shoudl this be done with PREDT???
            List<Zone> emptyZoneList = new ArrayList<>();
            CDD cddThatSavesUs = CDD.cddFalse();
            for (Edge otherE : edges) {
                if (otherE.getSource().equals(e.getSource()) && !otherE.isInput()) { //&& !otherE.equals(e)) { TODO 05.02.21: I also consider the current edge, but I think this is okay
                    if (otherE.getTarget().isInconsistent()) {
                        if (printComments)
                            Log.debug("OtherEdge is inconsistent");
                        // calculate and backtrack the part that is NOT inconsistent

                        CDD incPartOfTransThatSavesUs = new CDD(otherE.getTarget().getInconsistentPart().getPointer());
                        CDD targetInvariantCDDOfTransThatSavesUs = CDDFactory.createFrom(
                                otherE.getTarget().getInvariantGuard()
                        );
                        CDD goodPart = targetInvariantCDDOfTransThatSavesUs.minus(incPartOfTransThatSavesUs);

                        CDD doubleCheck = goodPart.transitionBack(otherE);
                        goodPart = goodPart.applyReset(otherE.getUpdates());

                        // apply guards
                        CDD guardFed = otherE.getGuardCDD();
                        goodPart = guardFed.conjunction(goodPart);
                        assert(doubleCheck.equiv(goodPart));

                        goodPart = goodPart.past(); // TODO 05.02.21: is it okay to do that?
                        goodPart = goodPart.conjunction(CDDFactory.createFrom(
                                otherE.getSource().getInvariantGuard()
                        ));

                        if (printComments)
                            Log.debug("Guards done");

                        cddThatSavesUs = goodPart.disjunction(cddThatSavesUs);

                    } else {
                        // simply apply guards
                        CDD cddOfGuard = otherE.getGuardCDD();
                        cddOfGuard = cddOfGuard.past(); // TODO 05.02.21: IMPORTANT!!!! Since invariants are not bound to start at 0 anymore, every time we use down we need to afterwards intersect with invariant
                        CDD sourceInvariant = CDDFactory.createFrom(otherE.getSource().getInvariantGuard());
                        cddOfGuard = cddOfGuard.conjunction(sourceInvariant);
                        cddThatSavesUs = cddOfGuard.disjunction(cddThatSavesUs);

                    }
                }
            }
            if (printComments)
                Log.debug("Coming to the subtraction");

            CDD sourceInvariantCdd = CDDFactory.createFrom(e.getSource().getInvariantGuard());
            CDD newIncPart = sourceInvariantCdd.minus(cddThatSavesUs);
            processSourceLocation(e,  newIncPart,passedPairs, inconsistentQueue);


        }


        // we need to add all the locations that could have been "saved" by this transition back to the list of inconsistent locations, because they might not be saved anymore now
        // i.e., when we had an input transition leading to an inconsistent location, we might have created a predt federation based on the output we just removed or restricted, so we need to do it again
        for (Edge e_i : edges.stream().filter(e_i -> e_i.getSource().equals(e.getSource()) && e_i.isInput() && e_i.getTarget().isInconsistent()).collect(Collectors.toList())) {
            if (printComments)
                Log.debug("Adding outputs that leave the source location back to the stack, as they might not be safed anymore");
            inconsistentQueue.add(e_i.getTarget());

        }

    }

    public static void handleInput(Location targetLoc, Edge e, List<Edge> edges, Map<Location, CDD> passedPairs, Queue<Location> inconsistentQueue ) { // treating inputs now

        if (printComments)
            Log.debug("Handling an input to inc.");

        // first we need to get the Fed that leads to the inconsistent part of the target location.
        // This means making a Fed of the inconsistent part of the target, apply its invariant, then free the clocks that are updated, and finally we include the zones of the guard
        CDD incCDD = e.getTarget().getInconsistentPart();

        // apply target invariant
        CDD invarCDD = CDDFactory.createFrom(e.getTarget().getInvariantGuard());
        incCDD = invarCDD.conjunction(incCDD);

        incCDD = incCDD.transitionBack(e);


        // apply updates as guard
        //incCDD = CDD.applyReset(incCDD,e.getUpdates());

        if (incCDD.equivFalse()) {
            // Checking for satisfiability after clocks were reset (only a problem because target invariant might now be x>4)
            // if unsatisfiable => keep edge // todo: is return the right thing here?
            if (printComments)
                Log.debug("Federation not valid");
            return;
        }

        if (printComments)
            Log.debug("Updates as guards done");

        //incCDD=backExplorationOnTransition(e,incCDD);



        // if the inconsistent part cannot be reached, we can ignore the edge e, and go on
        if (incCDD.equivFalse()) {
            if (printComments)
                Log.debug("could not reach inconsistent part, fed is empty");
        } else {

            // in the next step, we need to check whether there is output transitions that could lead us away from the inconsistent state
            // such a transition needs to
            // a) have the same source as e
            // b) not be a selfloop  TODO: 07.01.2021: We need to do some check for loops that can only safe us via zeno behaviour
            //                       TODO: 15.01.21: after group meeting we decided that is not needed
            // c) be an output
            // d) not lead to the inconsistent part of a state itself

            // we keep a copy of the inc. Federation, so we can do comparison to it later
            CDD save =  new CDD(incCDD.getPointer());

            incCDD= predtOfAllOutputs(e, incCDD, edges);
            // for each "good" transition, we remove its zone from the zone leading to inc. via the predt function

            // if the bad federation was not restricted via any good transition (i.e., its the same as before)
            // we have to take its past into the federation, as ending up in its past is already dooming us
            if ((incCDD.equiv(save))) { // TODO: check that
                if (printComments)
                    Log.debug("Could not be saved by an output");
                incCDD = incCDD.past(); // TODO: Check if this works
                incCDD = incCDD.conjunction(CDDFactory.createFrom(e.getSource().getInvariantGuard()));
            }

            if (printComments)
                Log.debug("Did the predt stuff");


            // Now we have the federation that can lead to inc.
            processSourceLocation(e,  incCDD, passedPairs, inconsistentQueue);
        }

        removeTransitionIfUnsat(e, incCDD, edges);


    }

    public CDD backExplorationOnTransition(Edge e, CDD incCDD)
    {

        incCDD = incCDD.transitionBack(e);
        incCDD = incCDD.past();

        // apply source invariant
        CDD invarCDD1 = CDDFactory.createFrom(e.getSource().getInvariantGuard());
        incCDD = invarCDD1.conjunction(incCDD);

        if (printComments)
            Log.debug("Invariants done");

        if (incCDD.notEquivFalse()) {
            if (printComments)
                Log.debug("Inconsistent part is reachable with this transition. ");
        } else {
            if (printComments)
                Log.debug("Inconsistent part is not reachable, creating an empty federation");
        }
        return incCDD;
    }

    public static void removeTransitionIfUnsat(Edge e,  CDD incCDD, List<Edge> edges)
    {
        if (printComments)
            Log.debug("Removing transition if its not satisfiable anymore");

        CDD testForSatEdgeCDD = CDD.cddUnrestrained();


        // apply target invariant
        CDD tartgetInvCDD= CDDFactory.createFrom(
                e.getTarget().getInvariantGuard()
        );
        testForSatEdgeCDD = tartgetInvCDD.conjunction(testForSatEdgeCDD);

        testForSatEdgeCDD = testForSatEdgeCDD.minus(e.getTarget().getInconsistentPart());


        testForSatEdgeCDD.applyReset(e.getUpdates());

        // apply guards
        CDD guardCDD1 = e.getGuardCDD();
        testForSatEdgeCDD = guardCDD1.conjunction(testForSatEdgeCDD);

        CDD sourceInvCDD = CDDFactory.createFrom(
                e.getSource().getInvariantGuard()
        );
        testForSatEdgeCDD = sourceInvCDD.conjunction(testForSatEdgeCDD);

        // remove inconsistent part

        testForSatEdgeCDD = testForSatEdgeCDD.minus(e.getSource().getInconsistentPart());


        if (!testForSatEdgeCDD.notEquivFalse()) {
            edges.remove(e);
        }
        if (printComments)
            Log.debug("... done");
    }


    public static void processSourceLocation(Edge e, CDD incCDD, Map<Location, CDD> passedPairs, Queue<Location> inconsistentQueue )
    {
        // If that federation is unsatisfiable, we can just ignore the transition to inc, and be done,
        // so we check for that, zone by zone. Only one zone needs to be sat.

        if (incCDD.equivFalse())
        {
            if (printComments)
                Log.debug("Did not add a new inconsistent part");
        } else
        // if the federation is satisfiable, we need to add it to the inconsistent part of the source of e. (We do the invariants in the very end)
        {

            // we also need to set this location as inconsistent, so that we can go further back along incoming inputs
            if (e.getSource().isInconsistent()) {
                e.getSource().setInconsistentPart(e.getSource().getInconsistentPart().disjunction(incCDD));
                if (printComments)
                    Log.debug("merged the previous and new inconsistent part of source");
            } else {
                Log.debug("INCCDD: " + incCDD);
                e.getSource().setInconsistent(true);
                e.getSource().setInconsistentPart(incCDD);
            }

            // check whether we need to add the new source location
            if (passedPairs.containsKey(e.getSource()) && (passedPairs.get(e.getSource()).equiv(incCDD))) {
                // location and federation already processed
            } else {
                if (printComments)
                    Log.debug("New inc location added to the stack");
                passedPairs.put(e.getSource(), incCDD);
                inconsistentQueue.add(e.getSource());
            }


            if (e.getSource().isInitial()) {
                if (printComments)
                    Log.debug("Initial Location is inconsistent!");
            }

        }
    }


    public static CDD predtOfAllOutputs(Edge e, CDD incCDD, List<Edge> edges)
    {
        CDD allGoodCDDs = CDD.cddFalse();
        for (Edge otherEdge : edges.stream().filter(o -> o.getSource().equals(e.getSource()) && !o.isInput()).collect(Collectors.toList())) {
            if (printComments)
                Log.debug("found an output that might lead us to good");

            // Ged invariant Federation
            CDD goodCDD = CDDFactory.createFrom(
                    otherEdge.getTarget().getInvariantGuard()
            );
            goodCDD = goodCDD.minus(otherEdge.getTarget().getInconsistentPart());

            // constrain it by the guards and invariants  of the "good transition". TODO: IMPORTANT: Check if the order of doing the target invariant first, freeing, etc. is the correct one

            if (goodCDD.notEquivFalse()) {
                goodCDD = goodCDD.transitionBack(otherEdge);
                //goodCDD = CDD.applyReset(goodCDD, otherEdge.getUpdates());

                CDD sourceInvFed = CDDFactory.createFrom(
                        otherEdge.getSource().getInvariantGuard()
                );
                goodCDD = sourceInvFed.conjunction(goodCDD);
                allGoodCDDs = allGoodCDDs.disjunction(goodCDD);
                //Log.debug(incFederation.getZones().get(0).buildGuardsFromZone(clocks));
            }
        }

        // do predt.

        CDD predtFed = incCDD.predt(allGoodCDDs);
        Log.debug("predtFed   " + predtFed + " " + incCDD + " " + allGoodCDDs);

        // add the inconsistent Federation to it, so in case both the transition to bad and the transition to good
        // have the guard x>4, we still get the bad zone in the result // TODO: Check if this still holds if we dont mind including zeno behaviour to save us (according to group discussion on 6.1.2021)
        incCDD = predtFed.disjunction(incCDD);
        return incCDD;

    }


}
