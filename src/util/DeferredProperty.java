package util;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class DeferredProperty<T> {
    /**
     * The temporarily stored value of the property.
     */
    private T value;

    /**
     * If true then the {@link DeferredProperty#value} needs to be updated.
     */
    private boolean isDirty;

    /**
     * The supplier used to automatically compute {@link DeferredProperty#value} when {@link DeferredProperty#isDirty} is true.
     */
    private final Supplier<T> supplier;

    /**
     * The collection of {@link DeferredProperty} instances that should be made dirty when this instance gets dirty.
     */
    private final List<DeferredProperty<?>> observers;

    /**
     * Constructs an instance of a {@link DeferredProperty<T>}.
     * If the provided initial value is {@code null}, then this instance will start of as dirty.
     * Otherwise, it will start clean with the provided initial value.
     *
     * @param value The initial value of this property.
     * @param supplier The non-parametric factory method for updating this property value automatically.
     * @param observers The dependent observers to which marking this instance as dirty is propagated to.
     */
    public DeferredProperty(T value, Supplier<T> supplier, List<DeferredProperty<?>> observers) {
        this.value = value;
        this.supplier = supplier;
        this.observers = observers;
        isDirty = value == null;
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
     * Constructs an instance of a {@link DeferredProperty<T>} with an initial value of {@code null} and no observers.
     */
    public DeferredProperty() {
        this(null, new ArrayList<>());
    }

    /**
     * Bypasses the dirty checks and just returns the value of {@link DeferredProperty#value}.
     *
     * @return The current value of {@link DeferredProperty#value}.
     */
    public T getUncheckedValue() {
        return value;
    }

    /**
     * Marks this instance as dirty if it was clean before the call and propagates the change to its {@link DeferredProperty#observers}.
     * If this instance was dirty before the call then no change and propagation will be made.
     *
     * @return {@code True} if this instance changed its classification from clean to dirty, and {@code False} if nothing changed, i.e., this instance was already classified as dirty.
     */
    public boolean markAsDirty() {
        // Only when this instance is clean do we want to make it dirty.
        if (isDirty()) {
            return false;
        }

        isDirty = true;

        // Mark the observers as dirty as they inherit it from this instance.
        // As it is impossible to add observers after construction, a livelock would never happen.
        for (DeferredProperty<?> property : observers) {
            property.markAsDirty();
        }

        return true;
    }

    /**
     * Sets the dirty flag as {@code false}.
     * Unlike {@link DeferredProperty#markAsDirty()} this does not propagate the cleaning to its observers. For this reason, observers that are dirty will not be cleaned.
     *
     * @return {@code true} if the dirty flag changed to {@code false}, and {@code false} if instance was already clean.
     */
    public boolean clean() {
        boolean change = isDirty();
        isDirty = false;
        return change;
    }

    /**
     * Sets the dirty flag as {@code false} by calling {@link DeferredProperty#clean()} if it cleaned this instance then we run the {@link Runnable}.
     * This should be used if the dirty state of this instance can be controlled by performing an operation rather than setting a new {@link DeferredProperty#value}.
     *
     * @param runnable The runnable which is run if we cleaned the object.
     * @return {@code true} if the dirty flag changed to {@code false}, and {@code false} if instance was already clean.
     */
    public boolean clean(Runnable runnable) {
        boolean cleaned = clean();
        if (cleaned) {
            runnable.run();
        }
        return cleaned;
    }

    /**
     * Checks if this instance is marked as dirty.
     *
     * @return {@code true} if this instance is dirty.
     */
    public boolean isDirty() {
        return isDirty;
    }

    /**
     * Checks if this instance is _not_ marked as dirty.
     *
     * @return {@code true} if this instance is not dirty.
     */
    public boolean isClean() {
        return !isDirty();
    }

    /**
     * Sets the {@link DeferredProperty#value} and cleans this instance by marking it as not dirty.
     * Calling this function when this instance is not dirty will just set {@link DeferredProperty#value}.
     *
     * @param value The value to set this property {@link DeferredProperty#value} to.
     * @return {@code true} if the instance was cleaned.
     */
    public boolean set(T value) {
        this.value = value;
        return clean();
    }

    /**
     * Sets the {@link DeferredProperty#value} but only if this is dirty.
     * Calling this function when this instance is not dirty will not set the value.
     * However, the value of this property before this call will always be returned.
     *
     * @param value The value to set this property {@link DeferredProperty#value} to.
     * @return The value of this instance even if no new assignment was made.
     */
    public T trySet(T value) {
        if (isDirty()) {
            set(value);
        }
        return getUncheckedValue();
    }

    /**
     * Sets the {@link DeferredProperty#value} but only if this is dirty.
     * The new {@link DeferredProperty#value} would be the one returned by the {@code supplier}.
     * Calling this function when this instance is not dirty will not set the value.
     * However, the value of this property before this call will always be returned.
     *
     * @param supplier The supplier that returns the new value of {@link DeferredProperty#value}.
     * @return The value of this instance value even if no new assignment was made.
     */
    public T trySet(Supplier<T> supplier) {
        return trySet(supplier.get());
    }

    /**
     * Sets a new value computed by the supplier if this instance is dirty.
     * {@link Optional<T>} is used to highlight that if this instance is dirty then {@link Optional#empty()} is returned instead of {@code null}.
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
     * @return {@code null} if this is dirty and no supplier exists.
     */
    public T getValue() {
        return tryCompute().orElse(null);
    }
}
