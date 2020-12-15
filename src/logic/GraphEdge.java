package logic;

import models.Edge;
import models.Federation;
import models.Zone;

import java.util.List;

public class GraphEdge {
    private GraphNode source, target;
    private List<Edge> edgesL, edgesR;
    private Federation subsetFed;

    public GraphEdge(GraphNode source, GraphNode target, List<Edge> edgesL, List<Edge> edgesR) {
        this.source = source;
        this.target = target;
        this.edgesL = edgesL;
        this.edgesR = edgesR;
    }

    public GraphEdge(GraphNode source, GraphNode target, List<Edge> edgesL, List<Edge> edgesR, Federation subsetFed) {
        this(source, target, edgesL, edgesR);
        this.subsetFed = subsetFed.getCopy();
    }

    public Federation getSubsetZone() {
        return subsetFed;
    }

    public void setSubsetFed(Federation subsetFed) {
        this.subsetFed = subsetFed;
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

    public List<Edge> getEdgesL() {
        return edgesL;
    }

    public List<Edge> getEdgesR() {
        return edgesR;
    }
}
