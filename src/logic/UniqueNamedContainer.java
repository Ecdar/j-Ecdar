package logic;

import models.UniquelyNamed;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class UniqueNamedContainer<T extends UniquelyNamed> {
    private List<T> items;

    public UniqueNamedContainer(List<T> items) {
        this.items = items;
    }

    public UniqueNamedContainer() {
        this.items = new ArrayList<>();
    }

    public void add(T item) {
        T newItem = (T) item.getCopy();

        List<T> sameName = items.stream()
                .filter(it -> sameName(it, item))
                .collect(Collectors.toList());
        if (sameName.size() != 0){
            List<T> sameOwner = sameName.stream().filter(c -> c.getOwnerName().equals(item.getOwnerName())).collect(Collectors.toList());
            if(sameOwner.size() != 0){          // Same name, same owner
                for(int i = 0; i < sameOwner.size(); i++){
                    sameOwner.get(i).setUniqueName(i+1);
                }
                newItem.setUniqueName(sameOwner.size()+1);
            }else{                              // Same name, different owner
                for(int i = 0; i < sameName.size(); i++){
                    sameName.get(i).setUniqueName();
                }
                newItem.setUniqueName();
            }
        }
        items.add(newItem);

    }

    private boolean sameName(UniquelyNamed item1, UniquelyNamed item2){
        return item1.getOriginalName().equals(item2.getOriginalName());
    }

    public List<T> getItems() {
        return items;
    }

    public void addAll(List<T> newItems) {
        for (T item: newItems) {
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
