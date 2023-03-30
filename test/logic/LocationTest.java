package logic;

import models.*;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class LocationTest {
    @Test
    public void createLocation() {
        // Arrange
        String name = "location";
        Guard invariant = new TrueGuard();
        boolean isInitial = false;
        boolean isUrgent = false;
        boolean isUniversal = false;
        boolean isInconsistent = false;
        int x = 0;
        int y = 0;
        List<Location> children = new ArrayList<>();

        // Act
        Location location = Location.create(
                name, invariant, isInitial, isUrgent, isUniversal, isInconsistent, x, y
        );

        // Assert
        assertEquals(location.getName(), name);
        assertEquals(location.isInitial(), isInitial);
        assertEquals(location.isUrgent(), isUrgent);
        assertEquals(location.isUniversal(), isUniversal);
        assertEquals(location.isInconsistent(), isInconsistent);
        assertEquals(location.getX(), x);
        assertEquals(location.getY(), y);
        assertEquals(location.getChildren(), children);
    }

    @Test
    public void createLocationDefaultsCoordinatesToZeroZero() {
        // Arrange
        String name = "location";
        Guard invariant = new TrueGuard();
        boolean isInitial = false;
        boolean isUrgent = false;
        boolean isUniversal = false;
        boolean isInconsistent = false;
        int x = 0;
        int y = 0;
        List<Location> children = new ArrayList<>();

        // Act
        Location location = Location.create(
                name, invariant, isInitial, isUrgent, isUniversal, isInconsistent
        );

        // Assert
        assertEquals(location.getName(), name);
        assertEquals(location.isInitial(), isInitial);
        assertEquals(location.isUrgent(), isUrgent);
        assertEquals(location.isUniversal(), isUniversal);
        assertEquals(location.isInconsistent(), isInconsistent);
        assertEquals(location.getX(), x);
        assertEquals(location.getY(), y);
        assertEquals(location.getChildren(), children);
    }

    @Test
    public void createInitialLocationDefaultsCoordinatesToZeroZero() {
        // Arrange
        String name = "initial location";
        Guard invariant = new TrueGuard();
        boolean isInitial = true;
        boolean isUrgent = false;
        boolean isUniversal = false;
        boolean isInconsistent = false;
        int x = 0;
        int y = 0;
        List<Location> children = new ArrayList<>();

        // Act
        Location initialLocation = Location.createInitialLocation(
                name, invariant, isUrgent, isUniversal, isInconsistent
        );

        // Assert
        assertEquals(initialLocation.getName(), name);
        assertEquals(initialLocation.isInitial(), isInitial);
        assertEquals(initialLocation.isUrgent(), isUrgent);
        assertEquals(initialLocation.isUniversal(), isUniversal);
        assertEquals(initialLocation.isInconsistent(), isInconsistent);
        assertEquals(initialLocation.getX(), x);
        assertEquals(initialLocation.getY(), y);
        assertEquals(initialLocation.getChildren(), children);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createCompositionLocationShouldThrowIllegalArgumentIfNoChildrenWereProvided() {
        // Arrange
        List<Location> children = new ArrayList<>();

        // Act
        Location.createComposition(children);
    }

    @Test
    public void createRandomCompositionLocation() {
        // Arrange
        Random random = new Random();
        List<Location> expectedChildren = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Guard invariant = new TrueGuard();
            String name = Integer.toString(random.nextInt());
            boolean isInitial = random.nextBoolean();
            boolean isUrgent = random.nextBoolean();
            boolean isUniversal = random.nextBoolean();
            boolean isInconsistent = random.nextBoolean();
            int x = random.nextInt(1000);
            int y = random.nextInt(1000);
            Location location = Location.create(
                    name, invariant, isInitial, isUrgent, isUniversal, isInconsistent, x, y
            );
            expectedChildren.add(location);
        }

        Guard expectedInvariant = new AndGuard(
                expectedChildren.stream().map(Location::getInvariantGuard).collect(Collectors.toList())
        );
        String expectedName = expectedChildren.stream().map(Location::getName).collect(Collectors.joining());
        boolean expectedIsInitial = expectedChildren.stream().allMatch(Location::isInitial);
        boolean expectedIsUrgent = expectedChildren.stream().anyMatch(Location::isUniversal);
        boolean expectedIsUniversal = expectedChildren.stream().allMatch(Location::isUrgent);
        boolean expectedIsInconsistent = expectedChildren.stream().anyMatch(Location::isInconsistent);
        int expectedX = expectedChildren.stream().map(Location::getX).reduce(0, Integer::sum) / expectedChildren.size();
        int expectedY = expectedChildren.stream().map(Location::getY).reduce(0, Integer::sum) / expectedChildren.size();

        // Act
        Location composition = Location.createComposition(expectedChildren);

        // Assert
        assertEquals(expectedInvariant, composition.getInvariantGuard());
        assertEquals(expectedName, composition.getName());
        assertEquals(expectedIsInitial, composition.isInitial());
        assertEquals(expectedIsUrgent, composition.isUrgent());
        assertEquals(expectedIsUniversal, composition.isUniversal());
        assertEquals(expectedIsInconsistent, composition.isInconsistent());
        assertEquals(expectedX, composition.getX());
        assertEquals(expectedY, composition.getY());
        assertEquals(expectedChildren, composition.getChildren());
    }

    @Test
    public void createUniversalLocation() {
        // Arrange
        String name = "universal location";
        Guard invariant = new TrueGuard();
        boolean isInitial = true;
        boolean isUrgent = false;
        boolean isUniversal = true;
        boolean isInconsistent = false;
        int x = 0;
        int y = 0;
        List<Location> children = new ArrayList<>();

        // Act
        Location universalLocation = Location.createUniversalLocation(
                name, isInitial, isUrgent, x, y
        );

        // Assert
        assertEquals(universalLocation.getName(), name);
        assertEquals(universalLocation.getInvariantGuard(), invariant);
        assertEquals(universalLocation.isInitial(), isInitial);
        assertEquals(universalLocation.isUrgent(), isUrgent);
        assertEquals(universalLocation.isUniversal(), isUniversal);
        assertEquals(universalLocation.isInconsistent(), isInconsistent);
        assertEquals(universalLocation.getX(), x);
        assertEquals(universalLocation.getY(), y);
        assertEquals(universalLocation.getChildren(), children);
    }

    @Test
    public void createInconsistentLocation() {
        // Arrange
        String name = "inconsistent location";
        boolean isInitial = false;
        boolean isUrgent = false;
        int x = 0;
        int y = 0;
        Clock clock = new Clock("inconsistent clock", "location test");
        Guard clockGuard = new ClockGuard(clock, 0, Relation.LESS_EQUAL);
        List<Location> children = new ArrayList<>();

        // Act
        Location inconsistentLocation = Location.createInconsistentLocation(
                name, isInitial, isUrgent, x, y, clock
        );

        // Assert
        assertEquals(inconsistentLocation.getName(), name);
        assertEquals(inconsistentLocation.isInitial(), isInitial);
        assertEquals(inconsistentLocation.isUrgent(), isUrgent);
        assertEquals(inconsistentLocation.getX(), x);
        assertEquals(inconsistentLocation.getY(), y);
        assertEquals(inconsistentLocation.getInvariantGuard(), clockGuard);
        assertEquals(inconsistentLocation.getChildren(), children);
    }

    @Test
    public void createSimpleLocation() {
        // Arrange
        String name = "universal location";
        Guard invariant = new TrueGuard();
        boolean isInitial = true;
        boolean isUrgent = false;
        boolean isUniversal = true;
        boolean isInconsistent = false;
        int x = 0;
        int y = 0;
        Location child = Location.create(name, invariant, isInitial, isUrgent, isUniversal, isInconsistent, x, y);
        List<Location> children = new ArrayList<>();
        children.add(child);

        // Act
        Location simpleLocation = Location.createSimple(child);

        // Assert
        assertEquals(simpleLocation.getName(), name);
        assertEquals(simpleLocation.isInitial(), isInitial);
        assertEquals(simpleLocation.isUrgent(), isUrgent);
        assertEquals(simpleLocation.getX(), x);
        assertEquals(simpleLocation.getY(), y);
        assertEquals(simpleLocation.getInvariantGuard(), invariant);
        assertEquals(simpleLocation.getChildren(), children);
    }
}
