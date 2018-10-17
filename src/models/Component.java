package models;

import java.util.ArrayList;

public class Component {

  private ArrayList<Location> locations;
  private ArrayList<Edge> edges;
  private ArrayList<Clock> clocks;

  public Component(ArrayList<Location> locations, ArrayList<Edge> edges, ArrayList<Clock> clocks) {
    this.locations = locations;
    this.edges = edges;
    this.clocks = clocks;
  }

  public ArrayList<Location> getLocations() {
    return locations;
  }

  public void setLocations(ArrayList<Location> locations) {
    this.locations = locations;
  }

  public ArrayList<Edge> getEdges() {
    return edges;
  }

  public void setEdges(ArrayList<Edge> edges) {
    this.edges = edges;
  }

  public ArrayList<Clock> getClocks() {
    return clocks;
  }

  public void setClocks(ArrayList<Clock> clocks) {
    this.clocks = clocks;
  }

}
