package ir.php.auto;

public class MethodParameter {
  private String name;
  private String type;
  private boolean is_optional;
  private boolean is_variadic;
  private boolean has_default;
  private boolean is_passed_by_reference;
  private Object default_value;

  // getters and setters
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public boolean isOptional() {
    return is_optional;
  }

  public void setOptional(boolean is_optional) {
    this.is_optional = is_optional;
  }

  public boolean isVariadic() {
    return is_variadic;
  }

  public void setVariadic(boolean is_variadic) {
    this.is_variadic = is_variadic;
  }

  public boolean hasDefault() {
    return has_default;
  }

  public void setHasDefault(boolean has_default) {
    this.has_default = has_default;
  }

  public boolean isPassedByReference() {
    return is_passed_by_reference;
  }

  public void setPassedByReference(boolean is_passed_by_reference) {
    this.is_passed_by_reference = is_passed_by_reference;
  }

  public Object getDefaultValue() {
    return default_value;
  }

  public void setDefaultValue(Object default_value) {
    this.default_value = default_value;
  }
}
