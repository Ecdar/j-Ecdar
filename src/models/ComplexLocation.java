package models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ComplexLocation extends SymbolicLocation {
    private final List<SymbolicLocation> locations;
    private List<List<Guard>> invariants;

    public ComplexLocation(List<SymbolicLocation> locations) {
        this.locations = locations;
        // TODO: this does not simplyfy, if we have a conjunction of several times the same guard (actually, it does not simplify anything)
        List<List<List<Guard>>> theListOfAllInvarsWithDisjunction = new ArrayList<>();
        for (SymbolicLocation loc1 : locations)
        {
            List<List<Guard>> invarWithDisj = loc1.getInvariants();
            if (!invarWithDisj.isEmpty())
                theListOfAllInvarsWithDisjunction.add(invarWithDisj);
        }
        List<List<Guard>> allCombinations = cartesianProduct(theListOfAllInvarsWithDisjunction);
        invariants = allCombinations;
    }

    public static List<List<Guard>> cartesianProduct(List<List<List<Guard>>> lists) {

        List<List<Guard>> product = new ArrayList<List<Guard>>();

        for (List<List<Guard>> list : lists) {

            List<List<Guard>> newProduct = new ArrayList<List<Guard>>();

            for (List<Guard> listElement : list) {

                if (product.isEmpty()) {

                    List<List<Guard>> newProductList = new ArrayList<List<Guard>>();
                    newProductList.add(listElement);
                    newProduct.addAll(newProductList);
                } else {

                    for (List<Guard> productList : product) {

                        List<Guard> newProductList = new ArrayList<Guard>(productList);
                        newProductList.addAll(listElement);
                        newProduct.add(newProductList);
                    }
                }
            }

            product = newProduct;
        }

        return product;
    }


    public List<SymbolicLocation> getLocations() {
        return locations;
    }

    public List<List<Guard>> getInvariants() {
        return invariants;
    }

    public void removeInvariants() {
        invariants = new ArrayList<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ComplexLocation that = (ComplexLocation) o;
        return Arrays.equals(locations.toArray(), that.locations.toArray());
    }

    @Override
    public int hashCode() {
        return Objects.hash(locations);
    }

    @Override
    public String toString() {
        return "" + locations;
    }
}