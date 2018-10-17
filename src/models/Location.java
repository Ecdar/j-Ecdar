package models;

public class Location {

  private Guard invariant;
  private boolean isInitial;
  private boolean isUrgent;
  private boolean isUniversal;
  private boolean isInconsistent;

  public Location(Guard invariant, boolean isInitial, boolean isUrgent, boolean isUniversal, boolean isInconsistent) {
    this.invariant = invariant;
    this.isInitial = isInitial;
    this.isUrgent = isUrgent;
    this.isUniversal = isUniversal;
    this.isInconsistent = isInconsistent;
  }

  public Guard getInvariant() {
    return invariant;
  }

  public void setInvariant(Guard invariant) {
    this.invariant = invariant;
  }

  public boolean isInitial() {
    return isInitial;
  }

  public void setInitial(boolean initial) {
    isInitial = initial;
  }

  public boolean isUrgent() {
    return isUrgent;
  }

  public void setUrgent(boolean urgent) {
    isUrgent = urgent;
  }

  public boolean isUniversal() {
    return isUniversal;
  }

  public void setUniversal(boolean universal) {
    isUniversal = universal;
  }

  public boolean isInconsistent() {
    return isInconsistent;
  }

  public void setInconsistent(boolean inconsistent) {
    isInconsistent = inconsistent;
  }
}
