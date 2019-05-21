package logic;

import models.Edge;
import models.StatePair;

import java.util.ArrayList;
import java.util.List;

public class GraphNode {
    private StatePair statePair;
    private List<GraphEdge> successors, predecessors;

    public GraphNode(StatePair statePair) {
        this.statePair = statePair;
        this.successors = new ArrayList<>();
        this.predecessors = new ArrayList<>();
        this.statePair.setNode(this);
    }

    public GraphEdge constructSuccessor(StatePair pair, List<Edge> edgesL, List<Edge> edgesR) {
        GraphEdge newEdge = new GraphEdge(this, new GraphNode(pair), edgesL, edgesR);
        this.successors.add(newEdge);

        return newEdge;
    }

    public void addSuccessor(GraphEdge succ) {
        this.successors.add(succ);
    }

    public void addPredecessor(GraphEdge pred) {
        this.predecessors.add(pred);
    }
}
