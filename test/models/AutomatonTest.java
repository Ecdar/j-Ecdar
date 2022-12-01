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
        Location location = Location.create("Location", new TrueExpression(), true, false, false, false);
        List<Location> locations = new ArrayList<>();
        locations.add(location);
        List<Edge> edges = new ArrayList<>();
        edges.add(
                new Edge(location, location, channel, true, new TrueExpression(), new ArrayList<>())
        );
        edges.add(
                new Edge(location, location, channel, false, new TrueExpression(), new ArrayList<>())
        );
        List<Clock> clocks = new ArrayList<>();
        List<BoolVar> booleans = new ArrayList<>();

        // Act
        Automaton automaton = new Automaton(
                "automaton",
                locations,
                edges,
                clocks,
                booleans,
                false
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNoLocationsThrowsException() {
        // Arrange
        List<Location> locations = new ArrayList<>();
        List<Edge> edges = new ArrayList<>();
        List<Clock> clocks = new ArrayList<>();
        List<BoolVar> booleans = new ArrayList<>();

        // Act
        Automaton automaton = new Automaton(
                "automaton",
                locations,
                edges,
                clocks,
                booleans,
                false
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNoInitialLocationThrowsException() {
        // Arrange
        List<Location> locations = new ArrayList<>();
        locations.add(
                Location.create("Location", new TrueExpression(), false, false, false, false)
        );
        List<Edge> edges = new ArrayList<>();
        List<Clock> clocks = new ArrayList<>();
        List<BoolVar> booleans = new ArrayList<>();

        // Act
        Automaton automaton = new Automaton(
                "automaton",
                locations,
                edges,
                clocks,
                booleans,
                false
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMultipleInitialLocationsThrowsException() {
        // Arrange
        List<Location> locations = new ArrayList<>();
        locations.add(
                Location.create("Location", new TrueExpression(), true, false, false, false)
        );
        locations.add(
                Location.create("Location", new TrueExpression(), true, false, false, false)
        );
        List<Edge> edges = new ArrayList<>();
        List<Clock> clocks = new ArrayList<>();
        List<BoolVar> booleans = new ArrayList<>();

        // Act
        Automaton automaton = new Automaton(
                "automaton",
                locations,
                edges,
                clocks,
                booleans,
                false
        );
    }

    @Test
    public void testCopyConstructorUsesNewReferences() {
        // Arrange
        List<Location> locations = new ArrayList<>();
        locations.add(
                Location.create("Location", new TrueExpression(), true, false, false, false)
        );
        List<Edge> edges = new ArrayList<>();
        List<Clock> clocks = new ArrayList<>();
        List<BoolVar> booleans = new ArrayList<>();
        Automaton automaton = new Automaton(
                "automaton",
                locations,
                edges,
                clocks,
                booleans,
                false
        );

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
