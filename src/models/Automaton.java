package models;

import java.util.*;
import java.util.stream.Collectors;

public class Automaton {
    private final String name;
    private final Location[] locations;
    private final List<Edge> edges;
    private final List<Clock> clocks;
    private Set<Channel> inputAct, outputAct, actions;
    private Location initLoc;

    public Automaton(String name, Location[] locations, List<Edge> edges, List<Clock> clocks) {
        this(name, locations, edges, clocks, true);
    }

    public Automaton(String name, Location[] locations, List<Edge> edges, List<Clock> clocks, boolean makeInpEnabled) {
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

        this.locations = new Location[copy.locations.length];
        for (int i = 0; i < copy.locations.length; i++) {
            this.locations[i] = new Location(copy.locations[i], clocks);
            if(this.locations[i].isInitial()) this.initLoc = this.locations[i];
        }

        this.edges = new ArrayList<>();
        for (Edge e : copy.edges) {
            this.edges.add(new Edge(e, this.clocks));
        }

        this.inputAct = copy.inputAct;
        this.outputAct = copy.outputAct;
        this.actions = copy.actions;
    }

    public int getMaxConstant(){
        int constant = 0;

        for(Edge edge : edges){
            if(edge.getMaxConstant() > constant) constant = edge.getMaxConstant();
        }

        for(Location location : locations){
            if(location.getMaxConstant() > constant) constant = location.getMaxConstant();
        }

        return constant;
    }

    private void makeInputEnabled() {
        if (clocks.size() > 0) {
            for (Location loc : locations) {
                // build the zone for this location
                Zone zone = new Zone(clocks.size() + 1, true);
                List<Guard> invariants = loc.getInvariant();
                for (Guard invariant : invariants) {
                    zone.buildConstraintsForGuard(invariant, clocks.indexOf(invariant.getClock()) + 1);
                }
                Federation fullFed = new Federation(new ArrayList<>(Collections.singletonList(zone)));

                // loop through all inputs
                for (Channel input : inputAct) {
                    // build federation of zones from edges
                    List<Edge> inputEdges = getEdgesFromLocationAndSignal(loc, input);
                    List<Zone> zones = new ArrayList<>();
                    for (Edge edge : inputEdges) {
                        Zone guardZone = new Zone(zone);

                        for (Guard g : edge.getGuards()) {
                            guardZone.buildConstraintsForGuard(g, clocks.indexOf(g.getClock()) + 1);
                        }
                        zones.add(guardZone);
                    }
                    Federation fed = new Federation(zones);

                    // subtract the federation of zones from the original zone
                    Federation resFed = Federation.fedMinusFed(fullFed, fed);
                    for (Zone edgeZone : resFed.getZones()) {
                        // build guards from zone
                        Edge newEdge = new Edge(loc, loc, input, true, edgeZone.buildGuardsFromZone(clocks), new Update[]{});
                        edges.add(newEdge);
                    }
                }
            }
        }
    }

    private void addTargetInvariantToEdges() {
        if (clocks.size() > 0) {
            for (Edge edge : edges) {
                // if there are no resets, we should apply the invariant of the target on the guard zone
                if (edge.getUpdates().length == 0) {
                    edge.addGuards(edge.getTarget().getInvariant());
                }
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
                Arrays.equals(locations, automaton.locations) &&
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
                ", locations=" + Arrays.toString(locations) +
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
