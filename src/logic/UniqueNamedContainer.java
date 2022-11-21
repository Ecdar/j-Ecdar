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
        // Unique name naming rules:
        //   Same owner and different name: Keep it as is
        //   Same owner and original name: Keep it as is
        //   Different owner and name: Keep it as is
        //   Different owner and same original name: Add owner to name "owner.n.name" where n is a counter value
        List<T> similar = items.stream()
                .filter(current ->
                        !Objects.equals(current.getOwnerName(), item.getOwnerName()) &&
                                Objects.equals(current.getOriginalName(), item.getOriginalName())
                ).collect(Collectors.toList());
        similar.add(item);
        int similarCount = similar.size();

        for (T current : similar) {
            current.setUniqueName(similarCount);
            similarCount -= 1;
        }

        // If the unique name is not present in the set of items then add it
        Optional<T> existing = findFirstByUniqueName(item.getUniqueName());
        if (existing.isEmpty()) {
            items.add(item);
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
