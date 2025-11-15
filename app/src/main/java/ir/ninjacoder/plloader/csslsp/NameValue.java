package ir.ninjacoder.plloader.csslsp;

public class NameValue {
  private final String name;
  private final String value;

  public NameValue(String name, String value) {
    this.name = name;
    this.value = value;
  }

  public String getName() {
    return this.name;
  }

  public String getValue() {
    return this.value;
  }
}
