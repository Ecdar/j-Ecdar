package logic;

import models.Edge;
import models.StatePair;

import java.util.ArrayList;
import java.util.List;

public class GraphNode {
    private StatePair statePair;
    private List<GraphEdge> successors, predecessors;
    private int nodeId;


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
}
