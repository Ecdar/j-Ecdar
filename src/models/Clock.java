package models;

public class Clock {

  private int value;

  public Clock(int value) {
    this.value = value;
  }

  public Clock() {
    this.value = 0;
  }

  public int getValue() {
    return value;
  }

  public void setValue(int value) {
    this.value = value;
  }
}
