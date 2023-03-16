package util;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class DeferredProperty<T> {
    /**
     * The temporary stored value of the property
     */
    private T value;

    /**
     * If true then the {@link DeferredProperty#value} needs to be updated.
     */
    private boolean isDirty;

    /**
     * The supplier used to autoamtically compute {@link DeferredProperty#value} when {@link DeferredProperty#isDirty} is true.
     */
    private final Supplier<T> supplier;

    /**
     * The collection of {@link DeferredProperty} instances that should be made dirty when this instance gets dirty.
     */
    private final List<DeferredProperty<?>> observers;

    /**
     * Constructs an instance of a {@link DeferredProperty<T>}.
     *
     * @param value The initial value of this property.
     * @param supplier The non-parametric factory method for updating this property value automatically.
     * @param observers The dependent observers to which marking this instance as dirty is propagated to.
     */
    public DeferredProperty(T value, Supplier<T> supplier, List<DeferredProperty<?>> observers) {
        this.value = value;
        this.supplier = supplier;
        this.observers = observers;
    }

    /**
     * Constructs an instance of a {@link DeferredProperty<T>}.
     *
     * @param value The initial value of this property.
     * @param supplier The non-parametric factory method for updating this property value automatically.
     */
    public DeferredProperty(T value, Supplier<T> supplier) {
        this(value, supplier, new ArrayList<>());
    }

    /**
     * Constructs an instance of a {@link DeferredProperty<T>}.
     *
     * @param value The initial value of this property.
     * @param observers The dependent observers to which marking this instance as dirty is propagated to.
     */
    public DeferredProperty(T value, List<DeferredProperty<?>> observers) {
        this(value, null, observers);
    }

    /**
     * Constructs an instance of a {@link DeferredProperty<T>}.
     *
     * @param value The initial value of this property.
     * @param observers The dependent observers to which marking this instance as dirty is propagated to.
     */
    public DeferredProperty(T value, DeferredProperty<?> ... observers) {
        this(value, List.of(observers));
    }

    /**
     * Constructs an instance of a {@link DeferredProperty<T>}.
     *
     * @param value The initial value of this property.
     */
    public DeferredProperty(T value) {
        this(value, new ArrayList<>());
    }

    /**
     * Marks this instance as dirty and only if this instance was initially marked as dirty do we propagate this to its {@link DeferredProperty#observers}.
     *
     * @return True if we marked this instance as dirty.
     */
    public boolean markAsDirty() {
        // Only when this instance is clean do we want to make it dirty.
        if (!isDirty()) {
            isDirty = true;

            // Mark the observers as dirty as they inherit it from this instance.
            // As it is impossible to add observers after construction then a livelock would never happen.
            for (DeferredProperty<?> property : observers) {
                property.markAsDirty();
            }

            return true;
        }
        return false;
    }

    /**
     * Used to check if this instance is marked as dirty.
     *
     * @return True if this instance is dirty.
     */
    public boolean isDirty() {
        return isDirty;
    }

    /**
     * Set the {@link DeferredProperty#value} and cleans this instance by not marking it as dirty.
     * Calling this function when this instance is not dirty will just set {@link DeferredProperty#value}.
     *
     * @param value The value to set this property {@link DeferredProperty#value} to.
     * @return Returns <code>true</code> if the instance was cleaned by marking it as no longer dirty.
     */
    public boolean set(T value) {
        this.value = value;

        // Only if we clean this instance we want to return true.
        if (isDirty()) {
            isDirty = false;
            return true;
        }
        return false;
    }

    /**
     * If a supplier has be assigned and this is dirty then a new value is calcalted and returned.
     * {@link Optional<T>} is used to highlight that if this instance is dirty then {@link Optional#empty()} is returned instead of <code>null</code>.
     *
     * @return If dirty then {@link Optional#empty()} else {@link Optional#of(Object)} with {@link DeferredProperty#value}.
     */
    public Optional<T> tryCompute() {
        // Automatically update the value of this property if dirty.
        if (isDirty() && supplier != null) {
            set(supplier.get());
        }

        return isDirty() ? Optional.empty() : Optional.of(value);
    }

    /**
     * Calls {@link DeferredProperty#tryCompute()} and returns null if the {@link Optional#isEmpty()}.
     *
     * @return Returns <code>null</code> if this is dirty and no supplier exists.
     */
    public T get() {
        return tryCompute().orElse(null);
    }
}