package models;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class AutomatonTest {
    @Test(expected = IllegalArgumentException.class)
    public void testActionIsBothAnInputAndOutputThrowsException() {
        // Arrange
        Channel channel = new Channel("channel");
        Location location = new Location("Location", new TrueGuard(), true, false, false, false, 0, 0);
        List<Location> locations = new ArrayList<>();
        locations.add(location);
        List<Edge> edges = new ArrayList<>();
        edges.add(
                new Edge(location, location, channel, true, new TrueGuard(), new ArrayList<>())
        );
        edges.add(
                new Edge(location, location, channel, false, new TrueGuard(), new ArrayList<>())
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
                new Location("Location", new TrueGuard(), false, false, false, false, 0, 0)
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
                new Location("Location", new TrueGuard(), true, false, false, false, 0, 0)
        );
        locations.add(
                new Location("Location", new TrueGuard(), true, false, false, false, 0, 0)
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
}
