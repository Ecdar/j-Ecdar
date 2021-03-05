package logic;

import lib.DBMLib;
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
    Map<Location, Federation> passedPairs;


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
            l.setInconsistentPart(Federation.getUnrestrainedFed(clocks));

        // TODO: make sure the passedPairs check is actually working as inteded
        passedPairs = new HashMap<Location, Federation>();
        for (Location l : inc) {
            passedPairs.put(l, Federation.getUnrestrainedFed(clocks));
        }

        boolean initIsInconsistent =false;
        // continue while there is still unprocessed inconsistent locations
        while (!inc.isEmpty()) {
            // select the first inconsistent location in the set.
            Location targetLoc = inc.iterator().next(); // TODO: speed optimization: start with the ones that are fully inconsistent

            if (targetLoc.isInitial())
            {
                List<Zone> emptyZoneList = new ArrayList<>();
                Zone emptyZone = new Zone(clocks.size() + 1, false);
                emptyZoneList.add(emptyZone);
                Federation initial = new Federation(emptyZoneList);
                if (targetLoc.getInconsistentPart().intersects(initial))
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
        Automaton resAut = new Automaton(st.getName(), locations, edges, clocks, false);
        return new SimpleTransitionSystem(resAut);

    }


    public void addInconsistentPartsToInvariants() {
        for (Location l : locations) {
            if (l.isInconsistent()) {
                Federation incFederation = l.getInconsistentPart();
                if (incFederation.isUnrestrained(clocks))
                    continue;                     // TODO: 04.02.21 how to treat a completely inconsistent location here?
                else {
                    Federation invarMinusIncFed;
                    if (l.getInvariant().isEmpty()) {
                        if (printComments)
                            System.out.println("There is no invariant yet, adding the negation of the inconsistent part");
                        Federation unrestrictedFed = Federation.getUnrestrainedFed(clocks);
                        invarMinusIncFed = Federation.fedMinusFed(unrestrictedFed, l.getInconsistentPart());
                    } else {
                        if (printComments)
                            System.out.println("There is an invariant, so we subtract the inconsistent part");
                        invarMinusIncFed = Federation.fedMinusFed(l.getInvariantFederation(clocks), l.getInconsistentPart());
                    }
                    l.setInvariant(invarMinusIncFed.turnFederationToGuards(clocks));
                }
            }
        }
    }

    public void addInvariantsToGuards() {
        for (Edge e : edges) {
            if (!e.getTarget().getInvariant().isEmpty()) {
                Federation fedAfterReset = e.getTarget().getInvariantFederation(clocks);
                for (Update u : e.getUpdates())
                    fedAfterReset.free(getIndexOfClock(u.getClock(), clocks));

                Federation guardFed = e.getGuardFederation(clocks);
                Federation fedAfterRemovingGuard = guardFed.intersect(fedAfterReset);

                e.setGuards(fedAfterRemovingGuard.turnFederationToGuards(clocks));
            }
        }

    }

    public void handleOutput(Location targetLoc, Edge e) {

        if (printComments)
            System.out.println("Handling an output to inc.");
        // If the whole target location is inconsistent, we just remove the transition
        // else we take the inconsistent part, free clocks reset by the current transition, and strengthen the guards so it cannot reach it

        if (targetLoc.getInconsistentPart().isUnrestrained(clocks)) {
            if (printComments)
                System.out.println("fully inconsistent target");
            edges.remove(e);
        } else {
            if (printComments)
                System.out.println("partially inconsistent target");

            // strengthen the guard, so that it cannot reach the inconsistent part of the target location

            // TODO 05.02.21: include target invariant or source invariant here?

            // take the inconsistent federation and free the clocks of the output transition
            Federation fedAfterReset = targetLoc.getInconsistentPart().getCopy();
            for (Update u : e.getUpdates())
                fedAfterReset.free(getIndexOfClock(u.getClock(), clocks));

            Federation guardFed = e.getGuardFederation(clocks);
            Federation fedAfterRemovingInconsistentPart = Federation.fedMinusFed(guardFed, fedAfterReset);

            e.setGuards(fedAfterRemovingInconsistentPart.turnFederationToGuards(clocks));
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

            // build the federation of all transitions that could save us (= the consistent part of all output transitions)
            List<Zone> emptyZoneList = new ArrayList<>();
            Federation fedThatSavesUs = new Federation(emptyZoneList);
            for (Edge otherE : edges) {
                if (otherE.getSource().equals(e.getSource()) && !otherE.isInput()) { //&& !otherE.equals(e)) { TODO 05.02.21: I also consider the current edge, but I think this is okay
                    if (otherE.getTarget().isInconsistent()) {
                        if (printComments)
                            System.out.println("OtherEdge is inconsistent");
                        // calculate and backtrack the part that is NOT inconsistent

                        Federation incPartOfTransThatSavesUs = otherE.getTarget().getInconsistentPart().getCopy();
                        Federation targetInvariantFedOfTransThatSavesUs = otherE.getTarget().getInvariantFederation(clocks);
                        Federation goodPart = Federation.fedMinusFed(targetInvariantFedOfTransThatSavesUs, incPartOfTransThatSavesUs);

                        for (Update u : otherE.getUpdates())
                            goodPart.free(getIndexOfClock(u.getClock(), clocks));

                        // apply guards
                        Federation guardFed = otherE.getGuardFederation(clocks);
                        goodPart = guardFed.intersect(goodPart);

                        goodPart = goodPart.down(); // TODO 05.02.21: is it okay to do that?
                        goodPart = goodPart.intersect(otherE.getSource().getInvariantFederation(clocks));

                        if (printComments)
                            System.out.println("Guards done");

                        fedThatSavesUs = Federation.fedPlusFed(goodPart, fedThatSavesUs);

                    } else {
                        // simply apply guards
                        Federation fedOfGuard = otherE.getGuardFederation(clocks);
                        fedOfGuard = fedOfGuard.down(); // TODO 05.02.21: IMPORTANT!!!! Since invariants are not bound to start at 0 anymore, every time we use down we need to afterwards intersect with invariant
                        fedOfGuard = fedOfGuard.intersect(otherE.getSource().getInvariantFederation(clocks));
                        fedThatSavesUs = Federation.fedPlusFed(fedOfGuard, fedThatSavesUs);

                    }
                }
            }
            if (printComments)
                System.out.println("Coming to the subtraction");

            Federation newIncPart = Federation.fedMinusFed(e.getSource().getInvariantFederation(clocks), fedThatSavesUs);
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

        Federation incFederation = e.getTarget().getInconsistentPart();

        // apply target invariant
        Federation invarFed = e.getTarget().getInvariantFederation(clocks);

        incFederation = invarFed.intersect(incFederation);


        // apply updates as guard
        for (Zone z : incFederation.getZones()) {
            for (Update u : e.getUpdates()) {
                z.buildConstraintsForGuard(new Guard(u.getClock(), u.getValue(), u.getValue(), false), getIndexOfClock(u.getClock(), clocks));
            }
        }

        if (!incFederation.isValid()) {
            // Checking for satisfiability after clocks were reset (only a problem because target invariant might now be x>4)
            // if unsatisfiable => keep edge // todo: is return the right thing here?
            if (printComments)
                System.out.println("Federation not valid");
            return;
        }

        if (printComments)
            System.out.println("Updates as guards done");

        incFederation=backExplorationOnTransition(e,incFederation);



        // if the inconsistent part cannot be reached, we can ignore the edge e, and go on
        if (incFederation.isEmpty()) {
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
            Federation save = incFederation.getCopy();
            System.out.println(save.getZones().get(0).buildGuardsFromZone(clocks));
            //System.out.println(save.getZones().get(0).buildGuardsFromZone(clocks));

            incFederation = predtOfAllOutputs(e, incFederation);
            // for each "good" transition, we remove its zone from the zone leading to inc. via the predt function

            System.out.println(incFederation.getZones().get(0).buildGuardsFromZone(clocks));
            // if the bad federation was not restricted via any good transition (i.e., its the same as before)
            // we have to take its past into the federation, as ending up in its past is already dooming us
            if (Federation.fedEqFed(incFederation, save)) { // TODO: check that
                if (printComments)
                    System.out.println("Could not be saved by an output");
                incFederation = incFederation.down(); // TODO: Check if this works
                incFederation = incFederation.intersect(e.getSource().getInvariantFederation(clocks));
            }

            if (printComments)
                System.out.println("Did the predt stuff");


            // Now we have the federation that can lead to inc.

            processSourceLocation(e,  incFederation);
        }

        removeTransitionIfUnsat(e, incFederation);


    }

    public Federation backExplorationOnTransition(Edge e, Federation incFederation)
    {
        // apply updates via free
        for (Zone z : incFederation.getZones()) {
            for (Update u : e.getUpdates()) {
                z = z.freeClock(getIndexOfClock(u.getClock(), clocks));
            }
        }

        if (printComments)
            System.out.println("Updates via free done");

        // apply guards
        Federation guardFed = e.getGuardFederation(clocks);
        incFederation = incFederation.intersect(guardFed);
        if (printComments)
            System.out.println("Guards done");

        // apply source invariant
        Federation invarFed1 = e.getSource().getInvariantFederation(clocks);
        incFederation = invarFed1.intersect(incFederation);

        if (printComments)
            System.out.println("Invariants done");

        if (!incFederation.isEmpty()) {
            if (printComments)
                System.out.println("Inconsistent part is reachable with this transition. Unrestrained Fed = " + incFederation.isUnrestrained(clocks));
        } else {
            if (printComments)
                System.out.println("Inconsistent part is not reachable, creating an empty federation");
        }
        System.out.println(incFederation.getZones().get(0).buildGuardsFromZone(clocks));
        return incFederation;
    }

    public void removeTransitionIfUnsat(Edge e,  Federation incFederation)
    {
        if (printComments)
            System.out.println("Removing transition if its not satisfiable anymore");

        Federation testForSatEdgeFed = Federation.getUnrestrainedFed(clocks);


        // apply target invariant
        Federation tartgetInvFed = e.getTarget().getInvariantFederation(clocks);
        testForSatEdgeFed = tartgetInvFed.intersect(testForSatEdgeFed);

        testForSatEdgeFed = Federation.fedMinusFed(testForSatEdgeFed, e.getTarget().getInconsistentPart());


        // apply updates as guard
        for (Zone z : testForSatEdgeFed.getZones()) {
            for (Update u : e.getUpdates()) {
                if (!DBMLib.dbm_isEmpty(z.getDbm(), clocks.size() + 1))
                    z.buildConstraintsForGuard(new Guard(u.getClock(), u.getValue(), u.getValue(), false), getIndexOfClock(u.getClock(), clocks));
            }
        }


        // apply updates via free
        for (Zone z : testForSatEdgeFed.getZones()) {

            for (Update u : e.getUpdates()) {
                if (!DBMLib.dbm_isEmpty(z.getDbm(), clocks.size() + 1))
                    z = z.freeClock(getIndexOfClock(u.getClock(), clocks));
            }
        }

        // apply guards
        Federation guardFed1 = e.getGuardFederation(clocks);
        testForSatEdgeFed = guardFed1.intersect(testForSatEdgeFed);

        Federation sourceInvFed = e.getSource().getInvariantFederation(clocks);
        testForSatEdgeFed = sourceInvFed.intersect(testForSatEdgeFed);

        // remove inconsistent part

        testForSatEdgeFed = Federation.fedMinusFed(testForSatEdgeFed, e.getSource().getInconsistentPart());


        if (testForSatEdgeFed.isEmpty()) {
            edges.remove(e);
        }
        if (printComments)
            System.out.println("... done");
    }


    public void processSourceLocation(Edge e, Federation incFederation)
    {
        // If that federation is unsatisfiable, we can just ignore the transition to inc, and be done,
        // so we check for that, zone by zone. Only one zone needs to be sat.

        if (incFederation.isEmpty())
        {
            if (printComments)
                System.out.println("Did not add a new inconsistent part");
        } else
        // if the federation is satisfiable, we need to add it to the inconsistent part of the source of e. (We do the invariants in the very end)
        {

            // we also need to set this location as inconsistent, so that we can go further back along incoming inputs
            if (e.getSource().isInconsistent()) {
                e.getSource().setInconsistentPart(Federation.fedPlusFed(e.getSource().getInconsistentPart(), incFederation));
                if (printComments)
                    System.out.println("merged the previous and new inconsistent part of source");
            } else {
                e.getSource().setInconsistent(true);
                e.getSource().setInconsistentPart(incFederation);
                if (printComments)
                    System.out.println("Set new (maybe) partially inconsistent part: " + incFederation.getZones().get(0).buildGuardsFromZone(clocks));
            }

            // check whether we need to add the new source location
            if (passedPairs.containsKey(e.getSource()) && Federation.fedEqFed(passedPairs.get(e.getSource()), incFederation)) {
                // location and federation already processed
                if (printComments)
                    System.out.println("Inc location already on the stack");
            } else {
                if (printComments)
                    System.out.println("New inc location added to the stack");
                passedPairs.put(e.getSource(), incFederation);
                inc.add(e.getSource());
            }


            if (e.getSource().isInitial()) {
                if (printComments)
                    System.out.println("Initial Location is inconsistent!");
            }

        }
    }


    public Federation predtOfAllOutputs(Edge e, Federation incFederation)
    {
        for (Edge otherEdge : edges.stream().filter(o -> o.getSource().equals(e.getSource()) && !o.isInput()).collect(Collectors.toList())) {
            if (printComments)
                System.out.println("found an output that might lead us to good");

            // Ged invariant Federation
            Federation goodFed = otherEdge.getTarget().getInvariantFederation(clocks);

            // constrain it by the guards and invariants  of the "good transition". TODO: IMPORTANT: Check if the order of doing the target invariant first, freeing, etc. is the correct one


            // remove the parts of the target transition that are inconsistent.
            if (otherEdge.getTarget().isInconsistent()) // TODO: added 7.1.2021, check if works
            {
                if (printComments)
                    System.out.println("the target of the saving edge is at least partially inconsistent");
                if (otherEdge.getTarget().getInconsistentPart().isUnrestrained(clocks)) // if its completely inconsistent, this transition cannot save us
                    goodFed = new Federation(new ArrayList<>());
                else
                    goodFed = Federation.fedMinusFed(goodFed, otherEdge.getTarget().getInconsistentPart());

            }

            if (!goodFed.isEmpty())
                for (Update u : otherEdge.getUpdates())  // TODO: 04.02.2021: IMPORTANT!!!! if the transition is completely inconsistent (and I thus make an empty good federation for it), does freeing clocks destroy that?
                    goodFed.free(getIndexOfClock(u.getClock(), clocks));

            Federation sourceInvFed = otherEdge.getSource().getInvariantFederation(clocks);
            goodFed = sourceInvFed.intersect(goodFed);




            Federation otherGuardFed = otherEdge.getGuardFederation(clocks);

            goodFed = otherGuardFed.intersect(goodFed);

            // do predt.
            Federation predtFed = Federation.predt(incFederation, goodFed);
            //System.out.println(predtFed.getZones().get(0).buildGuardsFromZone(clocks));


            // add the inconsistent Federation to it, so in case both the transition to bad and the transition to good
            // have the guard x>4, we still get the bad zone in the result // TODO: Check if this still holds if we dont mind including zeno behaviour to save us (according to group discussion on 6.1.2021)
            incFederation = Federation.fedPlusFed(predtFed, incFederation);
            //System.out.println(incFederation.getZones().get(0).buildGuardsFromZone(clocks));

        }
        return incFederation;

    }


}
