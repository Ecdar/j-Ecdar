package logic;

import models.*;

import java.util.*;
import java.util.stream.Collectors;

public class Pruning {

    private static boolean printComments = true;
    private Automaton aut;
    List<Clock> clocks;
    List<Edge> edges;
    List<Location> locations;
    Map<Location, Location> locMap;
    Set<Location> inc;
    Map<Location, CDD> passedPairs;


    public Pruning(SimpleTransitionSystem st) {
        aut = st.getAutomaton();
        clocks = aut.getClocks();
        edges = new ArrayList<Edge>();
        locations = new ArrayList<Location>();
        locMap = new HashMap<>();


    }

    private static int getIndexOfClock(Clock clock, List<Clock> clocks) {
        for (int i = 0; i < clocks.size(); i++) {
            if (clock.hashCode() == clocks.get(i).hashCode()) return i + 1;
        }
        return 0;
    }


    public static SimpleTransitionSystem pruneIncTimed(SimpleTransitionSystem st) {
        Pruning pr = new Pruning(st);
        return pr.prune(st);
    }

    public SimpleTransitionSystem prune(SimpleTransitionSystem st) {


        // Creating the sets of locations and transitions that will form the new automaton.
        for (Location l : aut.getLocations()) {

            Location lNew = new Location(l.getName(), l.getInvariant(), l.isInitial(), l.isUrgent(), l.isUniversal(), l.isInconsistent(), l.getX(), l.getY());
            locations.add(lNew);
            locMap.put(l, lNew);
        }
        for (Edge e : aut.getEdges()) {
            // TODO: Make sure that guards and such things are deepcopied
            // TODO: Move this to a seperate function for deepcopying an automaton
            edges.add(new Edge(locMap.get(e.getSource()), locMap.get(e.getTarget()), e.getChannel(), e.isInput(), e.getGuards(), e.getUpdates()));
        }

        // Create a list of inconsistent locations, that we can loop through
        inc = new HashSet<>(locations.stream().filter(l -> l.isInconsistent()).collect(Collectors.toList()));

        for (Location l : inc) // TODO 05.02.21: would be nicer if I could do this while parsing
            l.setInconsistentPart(CDD.getUnrestrainedCDD());

        // TODO: make sure the passedPairs check is actually working as inteded
        passedPairs = new HashMap<Location, CDD>();
        for (Location l : inc) {
            passedPairs.put(l, CDD.getUnrestrainedCDD());
        }

        boolean initIsInconsistent =false;
        // continue while there is still unprocessed inconsistent locations
        while (!inc.isEmpty()) {
            // select the first inconsistent location in the set.
            Location targetLoc = inc.iterator().next(); // TODO: speed optimization: start with the ones that are fully inconsistent

            if (targetLoc.isInitial())
            {
                CDD initial = CDD.zeroCDD();
                if (CDD.intersects(targetLoc.getInconsistentPart(),initial))
                {
                    System.out.println("Inital location is inconsistent");
                    initIsInconsistent=true;
                    break;

                }

            }



            if (printComments)
                System.out.println("Handling the new location " + targetLoc);

            // for all incoming transitions // TODO: is this really supposed to include selfloops
            for (Edge e : edges.stream().filter(e -> e.getTarget().equals(targetLoc)).collect(Collectors.toList())) { // && !e.getSource().equals(targetLoc)).collect(Collectors.toList())) {
                if (printComments)
                    System.out.println("Processing new Edge with guard " + e.getGuards());
                if (e.isInput()) {
                    handleInput(targetLoc, e);
                } else
                    handleOutput(targetLoc, e);
                inc.remove(targetLoc);
            }
        }
        if (printComments)
            System.out.println("no more inconsistent locations");

        addInconsistentPartsToInvariants();
        if (printComments)
            System.out.println("inconsistent parts integrated into invariants");

        addInvariantsToGuards();
        if (printComments)
            System.out.println("invariants integrated into guards");

        if (initIsInconsistent) {
            locations = new ArrayList<>();
            locations.add(new Location("inc", new ArrayList<List<Guard>>(), true, false, false, true));
            edges = new ArrayList<>();
        }
        Automaton resAut = new Automaton(st.getName(), locations, edges, clocks, st.BVs, false);
        return new SimpleTransitionSystem(resAut);

    }


    public void addInconsistentPartsToInvariants() {
        for (Location l : locations) {
            if (l.isInconsistent()) {
                CDD incCDD = l.getInconsistentPart();
                if (incCDD.isUnrestrained())
                    continue;                     // TODO: 04.02.21 how to treat a completely inconsistent location here?
                else {
                    CDD invarMinusIncCDD;
                    if (l.getInvariant().isEmpty()) {
                        if (printComments)
                            System.out.println("There is no invariant yet, adding the negation of the inconsistent part");
                        CDD unrestrictedCDD = CDD.getUnrestrainedCDD();
                        invarMinusIncCDD = unrestrictedCDD.minus(l.getInconsistentPart());
                    } else {
                        if (printComments)
                            System.out.println("There is an invariant, so we subtract the inconsistent part");
                        invarMinusIncCDD = l.getInvariantCDD().minus(l.getInconsistentPart());
                    }
                    l.setInvariant(CDD.toGuards(invarMinusIncCDD));
                }
            }
        }
    }

    public void addInvariantsToGuards() {
        for (Edge e : edges) {
            if (!e.getTarget().getInvariant().isEmpty()) {
                CDD target = e.getTarget().getInvariantCDD();
                CDD cddBeforeEdge = target.transitionBack(e);

                e.setGuards(CDD.toGuards(cddBeforeEdge));
            }
        }

    }

    public void handleOutput(Location targetLoc, Edge e) {

        if (printComments)
            System.out.println("Handling an output to inc.");
        // If the whole target location is inconsistent, we just remove the transition
        // else we take the inconsistent part, free clocks reset by the current transition, and strengthen the guards so it cannot reach it

        if (targetLoc.getInconsistentPart().isUnrestrained()) {
            if (printComments)
                System.out.println("fully inconsistent target");
            edges.remove(e);
        } else {
            if (printComments)
                System.out.println("partially inconsistent target");

            // strengthen the guard, so that it cannot reach the inconsistent part of the target location

            // TODO 05.02.21: include target invariant or source invariant here?

            // take the inconsistent federation and free the clocks of the output transition
            CDD target = new CDD(targetLoc.getInconsistentPart().getPointer());
            CDD afterReset = CDD.applyReset(target,e.getUpdates());

            CDD guardCDD = e.getGuardCDD();
            CDD fedAfterRemovingInconsistentPart =guardCDD.minus(afterReset);

            e.setGuards(CDD.toGuards(fedAfterRemovingInconsistentPart));
        }

        // Removed the transition / strenthening the guards might have turned the source location inconsistent
        // This happens if there is an invariant, and part of the invariant cannot delay to enable an output anymore

        // if there is no invariant, there cannot be a deadlock, and we do not care about whether there is any input or outputs leaving
        if (e.getSource().getInvariant().isEmpty()) {
            if (printComments)
                System.out.println("Source has no invariant, nothing more to do");
        } else {
            if (printComments)
                System.out.println("Processing source location to put it on the inc. stack");

            // build the federation of all transitions that could save us (= the consistent part of all output transitions) // TODO: Shoudl this be done with PREDT???
            List<Zone> emptyZoneList = new ArrayList<>();
            CDD cddThatSavesUs = CDD.cddFalse();
            for (Edge otherE : edges) {
                if (otherE.getSource().equals(e.getSource()) && !otherE.isInput()) { //&& !otherE.equals(e)) { TODO 05.02.21: I also consider the current edge, but I think this is okay
                    if (otherE.getTarget().isInconsistent()) {
                        if (printComments)
                            System.out.println("OtherEdge is inconsistent");
                        // calculate and backtrack the part that is NOT inconsistent

                        CDD incPartOfTransThatSavesUs = new CDD(otherE.getTarget().getInconsistentPart().getPointer());
                        CDD targetInvariantCDDOfTransThatSavesUs = otherE.getTarget().getInvariantCDD();
                        CDD goodPart = targetInvariantCDDOfTransThatSavesUs.minus(incPartOfTransThatSavesUs);

                        goodPart = CDD.applyReset(goodPart,otherE.getUpdates());

                        // apply guards
                        CDD guardFed = otherE.getGuardCDD();
                        goodPart = guardFed.conjunction(goodPart);

                        goodPart = goodPart.past(); // TODO 05.02.21: is it okay to do that?
                        goodPart = goodPart.conjunction(otherE.getSource().getInvariantCDD());

                        if (printComments)
                            System.out.println("Guards done");

                        cddThatSavesUs = goodPart.disjunction(cddThatSavesUs);

                    } else {
                        // simply apply guards
                        CDD cddOfGuard = otherE.getGuardCDD();
                        cddOfGuard = cddOfGuard.past(); // TODO 05.02.21: IMPORTANT!!!! Since invariants are not bound to start at 0 anymore, every time we use down we need to afterwards intersect with invariant
                        cddOfGuard = cddOfGuard.conjunction(otherE.getSource().getInvariantCDD());
                        cddThatSavesUs = cddOfGuard.disjunction(cddThatSavesUs);

                    }
                }
            }
            if (printComments)
                System.out.println("Coming to the subtraction");

            CDD newIncPart = e.getSource().getInvariantCDD().minus(cddThatSavesUs);
            processSourceLocation(e,  newIncPart);


        }


        // we need to add all the locations that could have been "saved" by this transition back to the list of inconsistent locations, because they might not be saved anymore now
        // i.e., when we had an input transition leading to an inconsistent location, we might have created a predt federation based on the output we just removed or restricted, so we need to do it again
        for (Edge e_i : edges.stream().filter(e_i -> e_i.getSource().equals(e.getSource()) && e_i.isInput() && e_i.getTarget().isInconsistent()).collect(Collectors.toList())) {
            if (printComments)
                System.out.println("Adding outputs that leave the source location back to the stack, as they might not be safed anymore");
            inc.add(e_i.getTarget());

        }

    }

    public void handleInput(Location targetLoc, Edge e) { // treating inputs now

        if (printComments)
            System.out.println("Handling an input to inc.");

        // first we need to get the Fed that leads to the inconsistent part of the target location.
        // This means making a Fed of the inconsistent part of the target, apply its invariant, then free the clocks that are updated, and finally we include the zones of the guard

        CDD incCDD = e.getTarget().getInconsistentPart();

        // apply target invariant
        CDD invarCDD = e.getTarget().getInvariantCDD();

        incCDD = invarCDD.conjunction(incCDD);


        // apply updates as guard
       incCDD = CDD.applyReset(incCDD,e.getUpdates());

        if (!incCDD.isNotFalse()) {
            // Checking for satisfiability after clocks were reset (only a problem because target invariant might now be x>4)
            // if unsatisfiable => keep edge // todo: is return the right thing here?
            if (printComments)
                System.out.println("Federation not valid");
            return;
        }

        if (printComments)
            System.out.println("Updates as guards done");

        incCDD=backExplorationOnTransition(e,incCDD);



        // if the inconsistent part cannot be reached, we can ignore the edge e, and go on
        if (!incCDD.isNotFalse()) {
            if (printComments)
                System.out.println("could not reach inconsistent part, fed is empty");
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

            incCDD= predtOfAllOutputs(e, incCDD);
            // for each "good" transition, we remove its zone from the zone leading to inc. via the predt function

             // if the bad federation was not restricted via any good transition (i.e., its the same as before)
            // we have to take its past into the federation, as ending up in its past is already dooming us
            if ((incCDD.equiv(save))) { // TODO: check that
                if (printComments)
                    System.out.println("Could not be saved by an output");
                incCDD = incCDD.past(); // TODO: Check if this works
                incCDD = incCDD.conjunction(e.getSource().getInvariantCDD());
            }

            if (printComments)
                System.out.println("Did the predt stuff");


            // Now we have the federation that can lead to inc.
            processSourceLocation(e,  incCDD);
        }

        removeTransitionIfUnsat(e, incCDD);


    }

    public CDD backExplorationOnTransition(Edge e, CDD incCDD)
    {

        incCDD = incCDD.transitionBack(e);
        incCDD = incCDD.past();

        // apply source invariant
        CDD invarCDD1 = e.getSource().getInvariantCDD();
        incCDD = invarCDD1.conjunction(incCDD);

        if (printComments)
            System.out.println("Invariants done");

        if (incCDD.isNotFalse()) {
            if (printComments)
                System.out.println("Inconsistent part is reachable with this transition. ");
        } else {
            if (printComments)
                System.out.println("Inconsistent part is not reachable, creating an empty federation");
        }
        return incCDD;
    }

    public void removeTransitionIfUnsat(Edge e,  CDD incFederation)
    {
        if (printComments)
            System.out.println("Removing transition if its not satisfiable anymore");

        CDD testForSatEdgeCDD = CDD.getUnrestrainedCDD();


        // apply target invariant
        CDD tartgetInvCDD= e.getTarget().getInvariantCDD();
        testForSatEdgeCDD = tartgetInvCDD.conjunction(testForSatEdgeCDD);

        testForSatEdgeCDD = testForSatEdgeCDD.minus(e.getTarget().getInconsistentPart());


        CDD.applyReset(testForSatEdgeCDD, e.getUpdates());

        // apply guards
        CDD guardCDD1 = e.getGuardCDD();
        testForSatEdgeCDD = guardCDD1.conjunction(testForSatEdgeCDD);

        CDD sourceInvCDD = e.getSource().getInvariantCDD();
        testForSatEdgeCDD = sourceInvCDD.conjunction(testForSatEdgeCDD);

        // remove inconsistent part

        testForSatEdgeCDD = testForSatEdgeCDD.minus(e.getSource().getInconsistentPart());


        if (!testForSatEdgeCDD.isNotFalse()) {
            edges.remove(e);
        }
        if (printComments)
            System.out.println("... done");
    }


    public void processSourceLocation(Edge e, CDD incCDD)
    {
        // If that federation is unsatisfiable, we can just ignore the transition to inc, and be done,
        // so we check for that, zone by zone. Only one zone needs to be sat.

        if (!incCDD.isNotFalse())
        {
            if (printComments)
                System.out.println("Did not add a new inconsistent part");
        } else
        // if the federation is satisfiable, we need to add it to the inconsistent part of the source of e. (We do the invariants in the very end)
        {

            // we also need to set this location as inconsistent, so that we can go further back along incoming inputs
            if (e.getSource().isInconsistent()) {
                e.getSource().setInconsistentPart(e.getSource().getInconsistentPart().disjunction(incCDD));
                if (printComments)
                    System.out.println("merged the previous and new inconsistent part of source");
            } else {
                e.getSource().setInconsistent(true);
                e.getSource().setInconsistentPart(incCDD);
            }

            // check whether we need to add the new source location
            if (passedPairs.containsKey(e.getSource()) && (passedPairs.get(e.getSource()).equiv(incCDD))) {
                // location and federation already processed
            } else {
                if (printComments)
                    System.out.println("New inc location added to the stack");
                passedPairs.put(e.getSource(), incCDD);
                inc.add(e.getSource());
            }


            if (e.getSource().isInitial()) {
                if (printComments)
                    System.out.println("Initial Location is inconsistent!");
            }

        }
    }


    public CDD predtOfAllOutputs(Edge e, CDD incCDD)
    {
        for (Edge otherEdge : edges.stream().filter(o -> o.getSource().equals(e.getSource()) && !o.isInput()).collect(Collectors.toList())) {
            if (printComments)
                System.out.println("found an output that might lead us to good");

            // Ged invariant Federation
            CDD goodCDD = otherEdge.getTarget().getInvariantCDD();

            // constrain it by the guards and invariants  of the "good transition". TODO: IMPORTANT: Check if the order of doing the target invariant first, freeing, etc. is the correct one


            // remove the parts of the target transition that are inconsistent.
            if (otherEdge.getTarget().isInconsistent()) // TODO: added 7.1.2021, check if works
            {
                if (printComments)
                    System.out.println("the target of the saving edge is at least partially inconsistent");
                if (otherEdge.getTarget().getInconsistentPart().isUnrestrained()) // if its completely inconsistent, this transition cannot save us
                    goodCDD = CDD.cddFalse();
                else
                    goodCDD = goodCDD.minus(otherEdge.getTarget().getInconsistentPart());

            }

            if (goodCDD.isNotFalse())
                goodCDD= CDD.applyReset(goodCDD,otherEdge.getUpdates());

            CDD sourceInvFed = otherEdge.getSource().getInvariantCDD();
            goodCDD = sourceInvFed.conjunction(goodCDD);




            CDD otherGuardFed = otherEdge.getGuardCDD();

            goodCDD = otherGuardFed.conjunction(goodCDD);

            // do predt.
            CDD predtFed = CDD.predt(incCDD, goodCDD);
            //System.out.println(predtFed.getZones().get(0).buildGuardsFromZone(clocks));


            // add the inconsistent Federation to it, so in case both the transition to bad and the transition to good
            // have the guard x>4, we still get the bad zone in the result // TODO: Check if this still holds if we dont mind including zeno behaviour to save us (according to group discussion on 6.1.2021)
            incCDD = predtFed.disjunction(incCDD);
            //System.out.println(incFederation.getZones().get(0).buildGuardsFromZone(clocks));

        }
        return incCDD;

    }


}
