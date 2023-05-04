package eu.su.mas.dedaleEtu.mas.knowledge;

public class Property {
  String name;
  String to;
  String from;
  boolean generic;
  public Property(String from, String name, String to) {
    this.name = name;
    this.to = to;
    this.from = from;
    this.generic = true;
  }
  public Property(String from, String name, String to, boolean generic) {
    this.name = name;
    this.to = to;
    this.generic = generic;
    this.from = from;
  }

  public String getName() {
    return this.name;
  }

  public String getTo() {
    return this.to;
  }

  public String getFrom() {
    return this.from;
  }

  public boolean getGeneric() {
    return this.generic;
  }
}
