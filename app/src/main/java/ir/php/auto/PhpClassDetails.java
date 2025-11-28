package ir.php.auto;

import java.util.List;
import java.util.Map;

public class PhpClassDetails {
  private Map<String, List<ClassMethod>> class_methods;

  // getters and setters
  public Map<String, List<ClassMethod>> getClassMethods() {
    return class_methods;
  }

  public void setClassMethods(Map<String, List<ClassMethod>> class_methods) {
    this.class_methods = class_methods;
  }
}
