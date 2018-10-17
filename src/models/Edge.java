package models;

public class Edge {

  private Location from;
  private Location to;
  private Channel chan;
  private boolean isInput;
  private Guard guard;
  private Update update;

  public Edge(Location from, Location to, Channel chan, boolean isInput, Guard guard, Update update) {
    this.from = from;
    this.to = to;
    this.chan = chan;
    this.isInput = isInput;
    this.guard = guard;
    this.update = update;
  }

  public Location getFrom() {
    return from;
  }

  public void setFrom(Location from) {
    this.from = from;
  }

  public Location getTo() {
    return to;
  }

  public void setTo(Location to) {
    this.to = to;
  }

  public Channel getChannel() {
    return chan;
  }

  public void setChannel(Channel chan) {
    this.chan = chan;
  }

  public boolean isInput() {
    return isInput;
  }

  public void setInput(boolean input) {
    isInput = input;
  }

  public Guard getGuard() {
    return guard;
  }

  public void setGuard(Guard guard) {
    this.guard = guard;
  }

  public Update getUpdate() {
    return update;
  }

  public void setUpdate(Update update) {
    this.update = update;
  }
}
