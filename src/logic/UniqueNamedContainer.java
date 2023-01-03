package logic;

import models.UniquelyNamed;

import java.util.*;
import java.util.stream.Collectors;

/**
 * An unordered collection of uniquely named items.
 * This container handles the uniquely renaming of items that aren't global.
 * The renaming is defined by the {@link UniquelyNamed} implementation, and
 * for this reason this container does not ensure that the unique names are
 * in fact unique to the collection. This is handled by the implementation of {@link UniquelyNamed}
 * which must utilise that the unique index provided to the renaming function.
 * <p>
 * Global items on the other hand are never renamed and only a singe instance
 * with the same unique name can be in the container. This is used for clocks
 * like the "quo_new" which are reused between quotients.
 *
 * @param <T> The type of items in this container.
 */
public class UniqueNamedContainer<T extends UniquelyNamed> {
    /**
     * The internal list for this container.
     */
    private final List<T> items;

    /**
     * Constructs a container with an initial set of items.
     *
     * @param items the initial set of items.
     */
    public UniqueNamedContainer(List<T> items) {
        this.items = items;
    }

    /**
     * Constructs an empty container.
     */
    public UniqueNamedContainer() {
        this(new ArrayList<>());
    }

    /**
     * Adds the specified element to the end of this container.
     * <p>
     * For non singleton items renaming of its unique name will happen when:
     * There is another item with the same owner and the same original name.
     * There is another item with a different owner and the same original name.
     *
     * @param item item to be added to the end of this container.
     */
    public void add(T item) {
        T newItem = (T) item.getCopy();

        if (!item.isGlobal()) {
            List<T> sameName = items.stream()
                    .filter(it -> sameName(it, item))
                    .collect(Collectors.toList());
            if (sameName.size() != 0) {
                List<T> sameOwner = sameName.stream().filter(c -> c.getOwnerName().equals(item.getOwnerName())).collect(Collectors.toList());
                if (sameOwner.size() != 0) { // Same name, same owner
                    for (int i = 0; i < sameOwner.size(); i++) {
                        sameOwner.get(i).setUniqueName(i + 1);
                    }
                    newItem.setUniqueName(sameOwner.size() + 1);
                } else { //  Same name, different owner
                    for (int i = 0; i < sameName.size(); i++) {
                        sameName.get(i).setUniqueName();
                    }
                    newItem.setUniqueName();
                }
            }
        }

        // If the unique name is not present in the set of items then add it
        Optional<T> existing = findFirstByUniqueName(newItem.getUniqueName());
        if (existing.isEmpty()) {
            items.add(newItem);
        }
    }

    private boolean sameName(UniquelyNamed item1, UniquelyNamed item2) {
        return item1.getOriginalName().equals(item2.getOriginalName());
    }

    /**
     * Finds the first item in this container with the specified unique name.
     *
     * @param uniqueName The unique name to look for.
     * @return An optional item which is empty if an item with the unique name could not be found.
     */
    private Optional<T> findFirstByUniqueName(String uniqueName) {
        return items.stream().filter(item -> Objects.equals(item.getUniqueName(), uniqueName)).findFirst();
    }

    /**
     * Finds the first item in this container with the specified original name.
     *
     * @param originalName The original name to look for.
     * @return An optional item which is empty if an item with the original name could not be found.
     */
    public Optional<T> findAnyWithOriginalName(String originalName) {
        return items.stream().filter(item -> Objects.equals(item.getOriginalName(), originalName)).findFirst();
    }

    /**
     * @return the internal list representation of this container.
     */
    public List<T> getItems() {
        return items;
    }

    /**
     * Appends all the elements in the specified iterable to the end of this container,
     * in the order that they are returned by the specified collection's iterator.
     *
     * @param items The iterable with items which should be added to this container.
     */
    public void addAll(Iterable<T> items) {
        for (T item : items) {
            add(item);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UniqueNamedContainer container = (UniqueNamedContainer) o;
        return Objects.equals(items, container.items);
    }

    @Override
    public int hashCode() {
        return Objects.hash(items);
    }
}
