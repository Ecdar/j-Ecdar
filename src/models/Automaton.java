package models;

import java.util.*;

public class Automaton {
    private String name;
    private List<Location> locations;
    private List<Edge> edges;
    private List<Clock> clocks;
    private Set<Channel> inputAct;
    private Set<Channel> outputAct;
    private Location initLoc;

    public Automaton(String name, List<Location> locations, List<Edge> edges, List<Clock> clocks) {
        this.name = name;
        this.locations = locations;
        for (Location location : locations) {
            if (location.isInitial()) {
                initLoc = location;
                break;
            }
        }
        this.inputAct = new HashSet<>();
        this.outputAct = new HashSet<>();
        setEdges(edges);
        this.clocks = clocks;
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
    public int hashCode() {
        return Objects.hash(name, locations, edges, clocks, inputAct, outputAct, initLoc);
    }

    public String getName() {
        return name;
    }

    public List<Location> getLocations() {
        return locations;
    }

    private List<Edge> getEdgesFromLocation(Location loc) {
        List<Edge> trans = new ArrayList<>(edges);

        if (loc.isUniversal()) {
            Set<Channel> actions = getActions();
            for (Channel action : actions) {
                trans.add(new Edge(loc, loc, action, getInputAct().contains(action), new ArrayList<>(), new ArrayList<>()));
            }
        }

        trans.removeIf(n -> n.getSource() != loc);

        return trans;
    }

    public List<Edge> getEdgesFromLocationAndSignal(Location loc, Channel signal) {
        List<Edge> trans = getEdgesFromLocation(loc);

        trans.removeIf(n -> !n.getChannel().getName().equals(signal.getName()));

        return trans;
    }

    private void setEdges(List<Edge> edges) {
        this.edges = edges;
        for (Edge edge : edges) {
            Channel action = edge.getChannel();
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

    public Set<Channel> getActions() {
        Set<Channel> actions = new HashSet<>();
        actions.addAll(inputAct);
        actions.addAll(outputAct);
        return actions;
    }
}
