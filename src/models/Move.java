package models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Move {

    private final SymbolicLocation source, target;
    private final List<Edge> edges;
    private List<List<Guard>> guards;
    private List<Update> updates;

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

    public Move(SymbolicLocation source, SymbolicLocation target, List<Edge> edges) {
        this.source = source;
        this.target = target;
        this.edges = edges;
        List<List<List<Guard>>> guardListBig = new ArrayList<>();


        for (Edge e : edges)
        {
            List<List<Guard>> guardWithDisj = e.getGuards();
            if (!guardWithDisj.isEmpty())
                guardListBig.add(guardWithDisj);
        }

        List<List<Guard>> allCombinations = cartesianProduct(guardListBig);

        this.guards = allCombinations;

//        this.guards = edges.isEmpty() ? new ArrayList<>() : edges.stream().map(Edge::getGuards).flatMap(List::stream).collect(Collectors.toList());
        this.updates = edges.isEmpty() ? new ArrayList<>() : edges.stream().map(Edge::getUpdates).flatMap(Arrays::stream).collect(Collectors.toList());
    }

    public SymbolicLocation getSource() {
        return source;
    }

    public SymbolicLocation getTarget() {
        return target;
    }

    public List<Edge> getEdges() {
        return edges;
    }

    public List<List<Guard>> getGuards() {
        return guards;
    }

    public void setGuards(List<List<Guard>> guards) {
        this.guards = guards;
    }

    public List<Update> getUpdates() {
        return updates;
    }

    public void setUpdates(List<Update> updates) {
        this.updates = updates;
    }
}
