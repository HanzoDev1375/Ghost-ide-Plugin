package ir.php.auto.phpfun;

import java.util.List;

public class PhpFunctions {
  private List<String> internal;
  private List<String> user;
  private int internal_count;
  private int user_count;
  public List<String> getInternal() {
    return internal;
  }

  public void setInternal(List<String> internal) {
    this.internal = internal;
  }

  public List<String> getUser() {
    return user;
  }

  public void setUser(List<String> user) {
    this.user = user;
  }

  public int getInternalCount() {
    return internal_count;
  }

  public void setInternalCount(int internal_count) {
    this.internal_count = internal_count;
  }

  public int getUserCount() {
    return user_count;
  }

  public void setUserCount(int user_count) {
    this.user_count = user_count;
  }
}
