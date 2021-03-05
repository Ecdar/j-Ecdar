package models;

import java.security.URIParameter;
import java.util.*;
import java.util.stream.Collectors;

public class Automaton {
    private final String name;

    public List<Location> getLocations() {
        return locations;
    }

    public List<Edge> getEdges() {
        return edges;
    }

    public Set<Channel> getActions() {
        return actions;
    }

    private final List<Location> locations;
    private final List<Edge> edges;
    private final List<Clock> clocks;
    private Set<Channel> inputAct, outputAct, actions;
    private Location initLoc;

    public Automaton(String name, List<Location> locations, List<Edge> edges, List<Clock> clocks) {
        this(name, locations, edges, clocks, true);
    }

    public Automaton(String name, List<Location> locations, List<Edge> edges, List<Clock> clocks, boolean makeInpEnabled) {
        this.name = name;
        this.locations = locations;

        for (Location location : locations) {

            if (location.isInitial()) {
                initLoc = location;
                break;
            }
        }

        this.edges = edges;
        setActions(edges);
        this.clocks = clocks;

        if (makeInpEnabled) {
            addTargetInvariantToEdges();
            makeInputEnabled();
        }
    }

    // Copy constructor
    public Automaton(Automaton copy){
        this.name = copy.name;

        this.clocks = new ArrayList<>();
        for (Clock c : copy.clocks) {
            this.clocks.add(new Clock(c));
        }

        this.locations = new ArrayList<>();
        for (Location loc : copy.locations) {
            this.locations.add(new Location(loc, clocks));
            if(loc.isInitial()) this.initLoc = this.locations.get(this.locations.size() - 1);
        }

        this.edges = new ArrayList<>();
        for (Edge e : copy.edges) {
            int sourceIndex = this.locations.indexOf(e.getSource());
            int targetIndex = this.locations.indexOf(e.getTarget());

            this.edges.add(new Edge(e, this.clocks, locations.get(sourceIndex), locations.get(targetIndex)));
        }

        this.inputAct = copy.inputAct;
        this.outputAct = copy.outputAct;
        this.actions = copy.actions;
    }

    public List<Integer> getMaxBoundsForAllClocks(){
        List<Integer> res = new ArrayList<>(Collections.nCopies(clocks.size(), 0));

        for(int i = 0; i < clocks.size(); i++) {
            for (Edge edge : edges) {
                int clockMaxBound = edge.getMaxConstant(clocks.get(i));
                if (clockMaxBound > res.get(i)) res.set(i, clockMaxBound);
            }

            for (Location location : locations) {
                int clockMaxBound = location.getMaxConstant(clocks.get(i));
                if (clockMaxBound > res.get(i)) res.set(i, clockMaxBound);
            }
        }
        return res;
    }

    private void makeInputEnabled() {
        //System.out.println("reached makeInputEnabled: start");
        if (clocks.size() > 0) {
            for (Location loc : locations) {
                // build the zone for this location

 /*               List<Zone> zoneList = new ArrayList<>();
                List<List<Guard>> invariants = loc.getInvariant();
                //System.out.println("reached makeInputEnabled");
                // check if done correctly
                if (invariants.isEmpty())
                {
                    //System.out.println("no invar");
                    Zone zone = new Zone(clocks.size() + 1, true);
                    //zone.init(); // TODO: check if init was the right thing to do here
                    zoneList.add(zone);
                }
                else {
                    //System.out.println("yes invar" + invariants.get(0));
                    for (List<Guard> disjunction : invariants) {
                        Zone zone = new Zone(clocks.size() + 1, true);
                        //zone.init(); // TODO: check if init was the right thing to do here
                        for (Guard invariant : disjunction) {
                            zone.buildConstraintsForGuard(invariant, clocks.indexOf(invariant.getClock()) + 1);
                        }
                        zoneList.add(zone);
                    }
                }
                Federation fullFed = new Federation(zoneList);
*/
                Federation fullFed = loc.getInvariantFederation(clocks);


                // loop through all inputs
                for (Channel input : inputAct) {
                    // build federation of zones from edges // TODO: check if federations were handled correctly here!
                    List<Edge> inputEdges = getEdgesFromLocationAndSignal(loc, input);
                    List<Zone> zones = new ArrayList<>();

                    Federation resFed;
                    Federation fedOfAllInputs=null;
                    if (!inputEdges.isEmpty()) {
                        for (Edge edge : inputEdges) {
                            Federation targetFedAfterReset = edge.getTarget().getInvariantFederation(clocks);
                            for (Update u: edge.getUpdates())
                                targetFedAfterReset= targetFedAfterReset.free(getIndexOfClock(u.getClock(),clocks));

                            if (fedOfAllInputs == null) {
                                fedOfAllInputs = edge.getGuardFederation(clocks).intersect(targetFedAfterReset);
                            } else
                                fedOfAllInputs = Federation.fedPlusFed(fedOfAllInputs, edge.getGuardFederation(clocks).intersect(targetFedAfterReset));
                        }
/*

//                        Federation guardFederation = new Federation(fullFed.getZones());
                            Federation guardFederation = fullFed.getCopy();
                            //Zone guardZone = new Zone(zone);
                            if (!guardFederation.getZones().isEmpty()) {

                                for (Zone guardZone : guardFederation.getZones()) {
                                    if (edge.getGuards().isEmpty() || (edge.getGuards().size()==1 && edge.getGuards().get(0).isEmpty()))
                                    {
                                        Zone newZone = new Zone(guardZone);
                                        zones.add(newZone);
                                    } else
                                    for (List<Guard> disjunction : edge.getGuards()) {
                                        Zone newZone = new Zone(guardZone);
                                        for (Guard g : disjunction) {
                                            newZone.buildConstraintsForGuard(g, clocks.indexOf(g.getClock()) + 1);
                                        }

                                        zones.add(newZone);
                                    }

                                }
                            } else {
                                assert (false);
                                //Zone guardZone = new Zone(); // init?
                            }


                        }
*/



                        //Federation fed = new Federation(zones);
                        // subtract the federation of zones from the original fed
                        resFed = Federation.fedMinusFed(fullFed, fedOfAllInputs);
                    }
                    else {
                        resFed =fullFed;
                    }



                        for (Zone edgeZone : resFed.getZones()) {
                            // build guards from zone
                            List<List<Guard>> guardList = new ArrayList<>();
                            guardList.add(edgeZone.buildGuardsFromZone(clocks));
                            Edge newEdge = new Edge(loc, loc, input, true, guardList, new Update[]{});
                            edges.add(newEdge);
                        }

                }
            }
        }
    }


    private static int getIndexOfClock(Clock clock, List<Clock> clocks) {
        for (int i = 0; i < clocks.size(); i++) {
            if (clock.hashCode() == clocks.get(i).hashCode()) return i + 1;
        }
        return 0;
    }
    private void addTargetInvariantToEdges() {
        if (clocks.size() > 0) {
            for (Edge edge : edges) {
                Federation targetFed = edge.getTarget().getInvariantFederation(clocks);
                for (Update u : edge.getUpdates())
                    targetFed.free(getIndexOfClock(u.getClock(), clocks));
                Federation intersec = targetFed.intersect(edge.getGuardFederation(clocks));
                edge.setGuards(intersec.turnFederationToGuards(clocks));

                /*
                // if there are no resets, we should apply the invariant of the target on the guard zone
                if (edge.getUpdates().length == 0) {
                        //System.out.println("Debug: " +edge.getGuards() + edge.getTarget().getInvariant());
                        edge.addGuards(edge.getTarget().getInvariant());
                    //System.out.println("Out: " +edge.getGuards());

                }
                else
                {
                    // FIXME: 17-11-2020 make the else branch
                }*/
            }
        }
    }

    public String getName() {
        return name;
    }

    private List<Edge> getEdgesFromLocation(Location loc) {
        if (loc.isUniversal()) {
            List<Edge> resultEdges = new ArrayList<>();
            for (Channel action : actions) {
                resultEdges.add(new Edge(loc, loc, action, inputAct.contains(action), new ArrayList<>(), new Update[]{}));
            }
            return resultEdges;
        }

        return edges.stream().filter(edge -> edge.getSource().equals(loc)).collect(Collectors.toList());
    }

    public List<Edge> getEdgesFromLocationAndSignal(Location loc, Channel signal) {
        List<Edge> resultEdges = getEdgesFromLocation(loc);

        return resultEdges.stream().filter(edge -> edge.getChannel().getName().equals(signal.getName())).collect(Collectors.toList());
    }

    private void setActions(List<Edge> edges) {
        inputAct = new HashSet<>();
        outputAct = new HashSet<>();
        actions = new HashSet<>();

        for (Edge edge : edges) {
            Channel action = edge.getChannel();

            actions.add(action);

            if (edge.isInput()) {
                inputAct.add(action);
            } else {
                outputAct.add(action);
            }
        }
    }

    public List<Clock> getClocks() {
        return clocks;
    }

    public Location getInitLoc() {
        return initLoc;
    }

    public Set<Channel> getInputAct() {
        return inputAct;
    }

    public Set<Channel> getOutputAct() {
        return outputAct;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Automaton)) return false;
        Automaton automaton = (Automaton) o;


        return name.equals(automaton.name) &&
                Arrays.equals(locations.toArray(), automaton.locations.toArray()) &&
                Arrays.equals(edges.toArray(), automaton.edges.toArray()) &&
                Arrays.equals(clocks.toArray(), automaton.clocks.toArray()) &&
                Arrays.equals(inputAct.toArray(), automaton.inputAct.toArray()) &&
                Arrays.equals(outputAct.toArray(), automaton.outputAct.toArray()) &&
                initLoc.equals(automaton.initLoc);
    }

    @Override
    public String toString() {
        return "Automaton{" +
                "name='" + name + '\'' +
                ", locations=" + Arrays.toString(locations.toArray()) +
                ", edges=" + Arrays.toString(edges.toArray()) +
                ", clocks=" + Arrays.toString(clocks.toArray()) +
                ", inputAct=" + inputAct +
                ", outputAct=" + outputAct +
                ", actions=" + actions +
                ", initLoc=" + initLoc +
                '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, locations, edges, clocks, inputAct, outputAct, initLoc);
    }
}
