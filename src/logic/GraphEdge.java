package logic;

import models.Edge;

import java.util.List;

public class GraphEdge {
    private GraphNode source, target;
    private List<Edge> edgesL, edgesR;
    private boolean leadsToSuperset = false;

    public GraphEdge(GraphNode source, GraphNode target, List<Edge> edgesL, List<Edge> edgesR) {
        this.source = source;
        this.target = target;
        this.edgesL = edgesL;
        this.edgesR = edgesR;
    }

    public GraphEdge(GraphNode source, GraphNode target, List<Edge> edgesL, List<Edge> edgesR, boolean leadsToSuperset) {
        this(source, target, edgesL, edgesR);
        this.leadsToSuperset = leadsToSuperset;
    }

    public GraphNode getSource() {
        return source;
    }

    public void setSource(GraphNode source) {
        this.source = source;
    }

    public GraphNode getTarget() {
        return target;
    }

    public void setTarget(GraphNode target) {
        this.target = target;
    }
}
