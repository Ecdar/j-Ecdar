package models;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class AutomatonTest {
    @Test(expected = IllegalArgumentException.class)
    public void testActionIsBothAnInputAndOutputThrowsException() {
        // Arrange
        Channel channel = new Channel("channel");
        Location location = Location.create("Location", new TrueGuard(), true, false, false, false);
        List<Edge> edges = new ArrayList<>();
        edges.add(new Edge(location, location, channel, true, new TrueGuard(), new ArrayList<>()));
        edges.add(new Edge(location, location, channel, false, new TrueGuard(), new ArrayList<>()));

        // Act
        new Automaton("automaton", location, edges);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyAutomatonThrowsIllegalArgumentException() {
        // Act
        new Automaton("automaton");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNoInitialLocationThrowsIllegalArgumentException() {
        // Arrange
        Location location = Location.create("Location", new TrueGuard(), false, false, false, false);

        // Act
        new Automaton("automaton", location, new ArrayList<>());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMultipleInitialLocationsThrowsException() {
        // Arrange
        List<Location> locations = new ArrayList<>();
        locations.add(Location.create("Location", new TrueGuard(), true, false, false, false));
        locations.add(Location.create("Location", new TrueGuard(), true, false, false, false));
        List<Edge> edges = new ArrayList<>();

        // Act
        new Automaton("automaton", locations, edges);
    }

    @Test
    public void testCopyConstructorUsesNewReferences() {
        // Arrange
        List<Location> locations = new ArrayList<>();
        locations.add(Location.create("Location", new TrueGuard(), true, false, false, false));
        List<Edge> edges = new ArrayList<>();
        List<Clock> clocks = new ArrayList<>();
        List<BoolVar> booleans = new ArrayList<>();
        Automaton automaton = new Automaton("automaton", locations, edges, clocks, booleans, false);

        // Act
        Automaton copy = new Automaton(automaton);

        // Assert (NotSame checks for != and not !.equals, which is for references)
        assertNotSame(automaton.getLocations(), copy.getLocations());
        assertNotSame(automaton.getEdges(), copy.getEdges());
        assertNotSame(automaton.getActions(), copy.getActions());
        assertNotSame(automaton.getName(), copy.getName());
        assertNotSame(automaton.getClocks(), copy.getClocks());
        assertNotSame(automaton.getInitial(), copy.getInitial());
        assertNotSame(automaton.getInputAct(), copy.getInputAct());
        assertNotSame(automaton.getOutputAct(), copy.getOutputAct());
        assertNotSame(automaton.getBVs(), copy.getBVs());
    }
}
