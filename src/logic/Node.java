package logic;

import models.StatePair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Node {
    private StatePair statePair;
    private Node parent;
    private List<Node> children = new ArrayList<>();

    public Node(StatePair statePair, Node parent) {
        this.statePair = statePair;
        this.parent = parent;
    }

    public Node(StatePair statePair) {
        this.statePair = statePair;
        this.parent = null;
    }

    public StatePair getStatePair() {
        return statePair;
    }

    public Node getParent() {
        return parent;
    }

    public void addChild(StatePair pair) {
        Node n = new Node(pair, this);
        this.children.add(n);
    }

    public Node getChild(StatePair pair) {
        List<Node> res = this.children.stream().filter(c -> c.getStatePair().equals(pair)).collect(Collectors.toList());

        if (res.isEmpty())
            return null;

        return res.get(0);
    }

    public List<Node> getChildren() {
        return children;
    }

    public List<StatePair> getTrace() {
        Node curr = this;
        List<StatePair> trace = new ArrayList<>();

        while (curr != null) {
            trace.add(curr.getStatePair());
            curr = curr.getParent();
        }

        Collections.reverse(trace);
        return trace;
    }
}
