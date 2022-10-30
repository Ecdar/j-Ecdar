package logic;

import models.Edge;

import java.util.ArrayList;
import java.util.List;

public class GraphNode {
    private StatePair statePair;
    private List<GraphEdge> successors, predecessors;
    private int nodeId;
    public boolean wasLast;
    public boolean brokeRefinement;
    public boolean marked=false;


    public GraphNode(StatePair statePair) {
        this.statePair = statePair;
        this.successors = new ArrayList<>();
        this.predecessors = new ArrayList<>();
        this.statePair.setNode(this);
        this.nodeId = Refinement.NODE_ID;
        Refinement.NODE_ID ++;
    }

    public GraphEdge constructSuccessor(StatePair pair, List<Edge> edgesL, List<Edge> edgesR) {
        GraphEdge newEdge = new GraphEdge(this, new GraphNode(pair), edgesL, edgesR);
        this.successors.add(newEdge);
        newEdge.getTarget().addPredecessor(newEdge);

        return newEdge;
    }

    public StatePair getStatePair() {
        return statePair;
    }

    public int getNodeId() {
        return nodeId;
    }

    public void addSuccessor(GraphEdge succ) {
        this.successors.add(succ);
    }

    public void addPredecessor(GraphEdge pred) {
        this.predecessors.add(pred);
    }

    public List<GraphEdge> getSuccessors() {
        return successors;
    }

    public String toDot()
    {
        unmark();
        StringBuilder sb = new StringBuilder();
        sb.append("digraph G {\n");
        sb.append(toString());
        sb.append("}\n");
        return sb.toString();
    }

    public void unmark()
    {
        if (marked == true)
            for (GraphEdge e: successors)
                e.getTarget().unmark();
        marked=false;
    }

    public String toString() {
        if (marked == false) {
            String color;
            if (wasLast)
                color = "red";
            else
                color = "white";
            if (brokeRefinement)
                color = "blue";
            String ret = statePair.getLeft().getLocation().getName() + "" + statePair.getRight().getLocation().getName()
                    + "[shape=box, label=\"" + statePair.getLeft().getLocation().getName() + "" + statePair.getRight().getLocation().getName() + "\", style=filled, height=0.3, width=0.3, color = " + color + "];\n";
            for (GraphEdge e : successors) {
                String label;
                if (e.getEdgesL().isEmpty() && e.getEdgesR().isEmpty())
                    label = "undefined";
                else
                    label = e.getEdgesL().isEmpty() ? e.getEdgesR().get(0).getChan().toString() : e.getEdgesL().get(0).getChan().toString();
                if (e.getTarget().statePair.getRight().getLocation().getName() != null) {
                    ret += e.getTarget().toString();
                    ret += statePair.getLeft().getLocation().getName() + "" + statePair.getRight().getLocation().getName() + " -> " +
                            e.getTarget().getStatePair().getLeft().getLocation().getName() + "" + e.getTarget().statePair.getRight().getLocation().getName() + "[style=\"filled\", label=\"" + label + "\"]\n";
                } else {
                    ret += statePair.getLeft().getLocation().getName() + "" + statePair.getRight().getLocation().getName() + " -> " + " VIOLATION " + "[style=\"filled\", label=\"" + label + "\"]\n";
                }
            }
            marked=true;
            return ret;
        }
        else
            return "";
    }
}
