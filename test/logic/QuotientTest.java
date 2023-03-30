package logic;

import models.*;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.junit.Assert.*;

public class QuotientTest {
    @Test
    public void quotientConstructorShouldAddANewClockWithNameQuo_new() {
        // Arrange
        Location t_initial_location = Location.createInitialLocation("t_initial", new TrueGuard(), false, false, false);
        List<Location> t_locations = new ArrayList<>();
        t_locations.add(t_initial_location);
        Automaton t = new Automaton("t", t_locations, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), false);

        Location s_initial_location = Location.createInitialLocation("t_initial", new TrueGuard(), false, false, false);
        List<Location> s_locations = new ArrayList<>();
        s_locations.add(s_initial_location);
        Automaton s = new Automaton("s", s_locations, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), false);

        // Act
        Quotient quotient = new Quotient(t, s);

        // Assert
        boolean contains_clock_quo_new = quotient.clocks.getItems().stream().anyMatch(
                clock -> Objects.equals(clock.getOriginalName(), "quo_new")
        );

        assertEquals(quotient.getClocks().size(), 1);
        assertTrue(contains_clock_quo_new);
    }

    @Test
    public void quotientConstructorShouldAddANewChannel() {
        // Arrange
        Location t_initial_location = Location.createInitialLocation("t_initial", new TrueGuard(), false, false, false);
        List<Location> t_locations = new ArrayList<>();
        t_locations.add(t_initial_location);
        Automaton t = new Automaton("t", t_locations, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), false);

        Location s_initial_location = Location.createInitialLocation("t_initial", new TrueGuard(), false, false, false);
        List<Location> s_locations = new ArrayList<>();
        s_locations.add(s_initial_location);
        Automaton s = new Automaton("s", s_locations, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), false);

        // Act
        Quotient quotient = new Quotient(t, s);

        // Assert
        boolean contains_channel_i_new = quotient.getInputs().stream().anyMatch(
                channel -> Objects.equals(channel.getName(), "i_new")
        );

        assertEquals(quotient.getInputs().size(), 1);
        assertTrue(contains_channel_i_new);
    }

    @Test
    public void quotientShouldHaveInputsAsUnionOfLhsAndRhs() {
        // Arrange
        Channel a = new Channel("a");
        Channel b = new Channel("a");

        Location t_initial_location = Location.createInitialLocation("t_initial", new TrueGuard(), false, false, false);

        Edge t_initial_location_looping_edge = new Edge(t_initial_location, t_initial_location, a, true, new TrueGuard(), new ArrayList<>());
        List<Edge> t_edges = new ArrayList<>();
        t_edges.add(t_initial_location_looping_edge);

        Automaton t = new Automaton("t", t_initial_location, t_edges);

        Location s_initial_location = Location.createInitialLocation("t_initial", new TrueGuard(), false, false, false);

        Edge s_initial_location_looping_edge = new Edge(s_initial_location, s_initial_location, b, true, new TrueGuard(), new ArrayList<>());
        List<Edge> s_edges = new ArrayList<>();
        s_edges.add(s_initial_location_looping_edge);

        Automaton s = new Automaton("s", s_initial_location, s_edges);

        // Act
        Quotient quotient = new Quotient(t, s);

        // Assert
        assertTrue(quotient.getInputs().contains(a));
        assertTrue(quotient.getInputs().contains(b));
        assertEquals(quotient.getInputs().size(), 2);
    }

    @Test
    public void quotientShouldHaveCorrectChannels() {
        // Arrange
        Channel a = new Channel("a");
        Channel b = new Channel("b");
        Channel c = new Channel("c");
        Channel d = new Channel("d");
        Channel e = new Channel("e");

        Location t_initial_location = Location.createInitialLocation("t_initial", new TrueGuard(), false, false, false);
        List<Location> t_locations = new ArrayList<>();
        t_locations.add(t_initial_location);

        Edge t_edge_out_a = new Edge(t_initial_location, t_initial_location, a, false, new TrueGuard(), new ArrayList<>());
        Edge t_edge_out_b = new Edge(t_initial_location, t_initial_location, b, false, new TrueGuard(), new ArrayList<>());
        Edge t_edge_in_d = new Edge(t_initial_location, t_initial_location, d, true, new TrueGuard(), new ArrayList<>());
        List<Edge> t_edges = new ArrayList<>();
        t_edges.add(t_edge_out_a);
        t_edges.add(t_edge_out_b);
        t_edges.add(t_edge_in_d);

        Automaton t = new Automaton("t", t_locations, t_edges);

        Location s_initial_location = Location.createInitialLocation("t_initial", new TrueGuard(), false, false, false);
        List<Location> s_locations = new ArrayList<>();
        s_locations.add(s_initial_location);

        Edge s_edge_out_b = new Edge(s_initial_location, s_initial_location, b, false, new TrueGuard(), new ArrayList<>());
        Edge s_edge_out_c = new Edge(s_initial_location, s_initial_location, c, false, new TrueGuard(), new ArrayList<>());
        Edge s_edge_in_d = new Edge(s_initial_location, s_initial_location, d, true, new TrueGuard(), new ArrayList<>());
        Edge s_edge_in_e = new Edge(s_initial_location, s_initial_location, e, true, new TrueGuard(), new ArrayList<>());
        List<Edge> s_edges = new ArrayList<>();
        s_edges.add(s_edge_out_b);
        s_edges.add(s_edge_out_c);
        s_edges.add(s_edge_in_d);
        s_edges.add(s_edge_in_e);

        Automaton s = new Automaton("s", s_locations, s_edges);

        // Act
        Quotient quotient = new Quotient(t, s);

        // Assert
        // Act_o = Act_o^T \ Act_o^S ∪ Act_i^S \ Act_i^T
        // Act_o = {a, b}  \ {b, c}  ∪ {d, e}  \ {d}
        // Act_o = {a}               ∪ {e}
        // Act_o = {a, e}
        assertTrue(quotient.getOutputs().contains(a));
        assertTrue(quotient.getOutputs().contains(e));
        assertEquals(quotient.getOutputs().size(), 2);

        // Act_i = Act_i^T ∪ Act_o^S ∪ i_new
        // Act_i = {d}     ∪ {b, c}  ∪ i_new
        // Act_i = {d, b, c, i_new}
        assertTrue(quotient.getInputs().contains(d));
        assertTrue(quotient.getInputs().contains(b));
        assertTrue(quotient.getInputs().contains(c));
        assertTrue(quotient.getInputs().stream().anyMatch(channel -> Objects.equals(channel.getName(), "i_new")));
        assertEquals(quotient.getInputs().size(), 4);
    }

    @Test
    public void doubleQuotientShouldReuseQuotientClock() {
        // Arrange
        Location t_initial_location = Location.createInitialLocation("t_initial", new TrueGuard(), false, false, false);
        List<Location> t_locations = new ArrayList<>();
        t_locations.add(t_initial_location);
        Automaton automaton = new Automaton("t", t_locations, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), false);
        TransitionSystem transitionSystem = new SimpleTransitionSystem(automaton);

        // Act
        Quotient quotient = new Quotient(new Quotient(transitionSystem, transitionSystem), new Quotient(transitionSystem, transitionSystem));

        // Assert
        assertEquals(quotient.getClocks().size(), 1);
    }

    @Test
    public void doubleQuotientShouldReuseQuotientInputAction() {
        // Arrange
        Location t_initial_location = Location.createInitialLocation("t_initial", new TrueGuard(), false, false, false);
        List<Location> t_locations = new ArrayList<>();
        t_locations.add(t_initial_location);
        Automaton automaton = new Automaton("t", t_locations, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), false);
        TransitionSystem transitionSystem = new SimpleTransitionSystem(automaton);

        // Act
        Quotient quotient = new Quotient(new Quotient(transitionSystem, transitionSystem), new Quotient(transitionSystem, transitionSystem));

        // Assert
        assertEquals(quotient.getInputs().size(), 1);
    }
}
