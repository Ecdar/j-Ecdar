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
 *
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
     * @param items The initial set of items.
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
     *
     * For non singleton items renaming of its unique name will happen when:
     *   There is another item with the same owner and the same original name.
     *   There is another item with a different owner and the same original name.
     *
     * @param item Item to be added to the end of this container.
     */
    public void add(T item) {
        T newItem = (T) item.getCopy();

        if (!item.isGlobal()) {
            // Unique name naming rules:
            //   Same owner and different name: keep it as is.
            //   Different owner and name: keep it as is.
            //   Different or the same owner but the same original name: add owner to name "owner.n.name" where n is a counter value.
            // Motivation: Machine <= Machine || Machine. Here the owner is Machine but their clocks are different.
            List<T> similarOriginalName = items.stream()
                    .filter(current -> Objects.equals(current.getOriginalName(), newItem.getOriginalName()))
                    .collect(Collectors.toList());

            if (similarOriginalName.size() != 0) {

                List<T> similarOwner = similarOriginalName.stream()
                        .filter(current -> Objects.equals(current.getOwnerName(), newItem.getOwnerName()))
                        .collect(Collectors.toList());

                if (similarOwner.size() > 0) {
                    for (int i = 0; i < similarOwner.size(); i++) {
                        similarOwner.get(i).setUniqueName(i + 1);
                    }
                    newItem.setUniqueName(similarOwner.size() + 1);
                } else {
                    for (T current : similarOriginalName) {
                        current.setUniqueName();
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
     * @return The internal list representation of this container.
     */
    public List<T> getItems() {
        return items;
    }

    /**
     * Appends all the elements in the specified iterable to the end of this container,
     *   in the order that they are returned by the specified collection's iterator.
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
