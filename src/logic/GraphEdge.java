package logic;

import models.CDD;
import models.Edge;
import models.Federation;
import models.Zone;

import java.util.List;

public class GraphEdge {
    private GraphNode source, target;
    private List<Edge> edgesL, edgesR;
    private CDD subsetCDD;

    public GraphEdge(GraphNode source, GraphNode target, List<Edge> edgesL, List<Edge> edgesR) {
        this.source = source;
        this.target = target;
        this.edgesL = edgesL;
        this.edgesR = edgesR;
    }

    public GraphEdge(GraphNode source, GraphNode target, List<Edge> edgesL, List<Edge> edgesR, CDD subsetCDD) {
        this(source, target, edgesL, edgesR);
        this.subsetCDD = new CDD(subsetCDD.getPointer());
    }

    public CDD getSubsetZone() {
        return subsetCDD;
    }

    public void setSubsetFed(CDD subsetFed) {
        this.subsetCDD= subsetFed;
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
