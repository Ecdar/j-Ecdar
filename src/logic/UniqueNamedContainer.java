package logic;

import models.UniquelyNamed;

import java.util.*;
import java.util.stream.Collectors;

public class UniqueNamedContainer<T extends UniquelyNamed> {
    private final List<T> items;

    public UniqueNamedContainer(List<T> items) {
        this.items = items;
    }

    public UniqueNamedContainer() {
        this(new ArrayList<>());
    }

    public void add(T item) {
        T newItem = (T) item.getCopy();

        if (!Objects.equals(item.getUniqueName(), "quo_new")) {
            // Unique name naming rules:
            //   Same owner and different name: Keep it as is
            //   Same owner and original name: Add owner to name "owner.n.name" where n is a counter value
            // Motivation: Machine <= Machine || Machine. Here the owner is Machine but their clocks are different.
            //   Different owner and name: Keep it as is
            //   Different owner and same original name: Add owner to name "owner.n.name" where n is a counter value
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

    private Optional<T> findFirstByUniqueName(String uniqueName) {
        return items.stream().filter(item -> Objects.equals(item.getUniqueName(), uniqueName)).findFirst();
    }

    public List<T> getItems() {
        return items;
    }

    public void addAll(List<T> newItems) {
        for (T item : newItems) {
            add(item);
        }
    }

    public Optional<T> findAnyWithOriginalName(String name) {
        return items.stream().filter(item -> Objects.equals(item.getOriginalName(), name)).findFirst();
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
