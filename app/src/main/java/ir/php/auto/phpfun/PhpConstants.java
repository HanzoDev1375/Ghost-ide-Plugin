package ir.php.auto.phpfun;

import java.util.Map;
import java.util.List;

public class PhpConstants {
  private Map<String, ConstantCategory> constants;

  public Map<String, ConstantCategory> getConstants() {
    return constants;
  }

  public void setConstants(Map<String, ConstantCategory> constants) {
    this.constants = constants;
  }
}

class ConstantCategory {
  private int count;
  private List<String> constants;
  public int getCount() {
    return count;
  }

  public void setCount(int count) {
    this.count = count;
  }

  public List<String> getConstants() {
    return constants;
  }

  public void setConstants(List<String> constants) {
    this.constants = constants;
  }
}
