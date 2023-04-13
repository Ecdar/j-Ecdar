package util;

import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.*;

public class DeferredPropertyTest {
    /**
     * Used in {@link DeferredPropertyTest#deferredPropertyWithReferenceInstance}.
     * It is declared outside the function to ensure that the reference used in the factory lives longer than the test.
     */
    String instance = "0";

    @Test
    public void deferredPropertyIsConstructedWithValueAndNotDirty() {
        // Arrange
        Integer value = 0;
        DeferredProperty<Integer> property;

        // Act
        property = new DeferredProperty<>(value);

        // Assert
        assertEquals(property.getValue(), value);
        assertFalse(property.isDirty());
    }

    @Test
    public void deferredPropertyWithReferenceInstance() {
        // Arrange
        instance = "0";
        DeferredProperty<Integer> property = new DeferredProperty<>(instance.length(), () -> {
            instance += instance.length();
            return instance.length();
        });

        // Act
        property.markAsDirty();
        int first = property.getValue();
        property.markAsDirty();
        int second = property.getValue();
        property.markAsDirty();
        int third = property.getValue();

        // Assert
        assertEquals("0123", instance);
        assertEquals(first + 1, second);
        assertEquals(second + 1, third);
    }

    @Test
    public void deferredPropertyObserversInheritDirty() {
        // Arrange
        DeferredProperty<String> property_child_1 = new DeferredProperty<>("0");
        DeferredProperty<Float> property_child_2 = new DeferredProperty<>(1.0f);
        DeferredProperty<Integer> parent = new DeferredProperty<>(2, property_child_1, property_child_2);

        // Act
        parent.markAsDirty();

        // Assert
        assertTrue(property_child_1.isDirty());
        assertTrue(property_child_2.isDirty());
        assertTrue(parent.isDirty());
    }

    @Test
    public void deferredPropertyMarkAsDirtyReturnsTrueIfPropertyWasClean() {
        // Arrange
        DeferredProperty<Integer> property = new DeferredProperty<>(0);

        // Act
        boolean wasMarked = property.markAsDirty();

        // Assert
        assertTrue(wasMarked);
    }

    @Test
    public void deferredPropertyMarkAsDirtyReturnsFalseIfPropertyWasDirty() {
        // Arrange
        DeferredProperty<Integer> property = new DeferredProperty<>(0);
        property.markAsDirty();

        // Act
        boolean wasMarked = property.markAsDirty();

        // Assert
        assertFalse(wasMarked);
    }

    @Test
    public void deferredPropertySetMakesGetReturnTheNewValue() {
        // Arrange
        Integer expected = 1;
        DeferredProperty<Integer> property = new DeferredProperty<>(0);
        property.set(expected);

        // Act
        Integer actual = property.getValue();

        // Assert
        assertEquals(expected, actual);
    }

    @Test
    public void deferredPropertySetReturnsFalseIfPropertyWasClean() {
        // Arrange
        DeferredProperty<Integer> property = new DeferredProperty<>(0);

        // Act
        boolean wasMarked = property.set(0);

        // Assert
        assertFalse(wasMarked);
    }

    @Test
    public void deferredPropertySetReturnsTrueIfPropertyWasDirty() {
        // Arrange
        DeferredProperty<Integer> property = new DeferredProperty<>(0);
        property.markAsDirty();

        // Act
        boolean wasMarked = property.set(0);

        // Assert
        assertTrue(wasMarked);
    }

    @Test
    public void deferredPropertyTryComputeReturnsEmptyIfDirtyAndNoSupplier() {
        // Arrange
        DeferredProperty<Integer> property = new DeferredProperty<>(0);
        property.markAsDirty();

        // Act
        Optional<Integer> actual = property.tryCompute();

        // Assert
        assertTrue(actual.isEmpty());
    }

    @Test
    public void deferredPropertyTryComputeReturnsValueOfIfClean() {
        // Arrange
        Integer expected = 0;
        DeferredProperty<Integer> property = new DeferredProperty<>(expected);

        // Act
        Integer actual = property.tryCompute().get();

        // Assert
        assertEquals(expected, actual);
    }

    @Test
    public void deferredPropertyTryComputeReturnsValueOfIfDirtyAndHasSupplier() {
        // Arrange
        Integer expected = 1;
        DeferredProperty<Integer> property = new DeferredProperty<>(0, () -> expected);
        property.markAsDirty();

        // Act
        Integer actual = property.tryCompute().get();

        // Assert
        assertEquals(expected, actual);
        assertFalse(property.isDirty());
    }

    @Test
    public void deferredPropertyTryComputeDoesNotSetValueIfNotDirty() {
        // Arrange
        Integer expected = 0;
        DeferredProperty<Integer> property = new DeferredProperty<>(expected, () -> 1);

        // Act
        Integer actual = property.tryCompute().get();

        // Assert
        assertEquals(expected, actual);
    }
}
