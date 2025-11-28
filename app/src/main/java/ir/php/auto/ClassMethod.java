package ir.php.auto;

import java.util.List;

public class ClassMethod {
  private String name;
  private String visibility;
  private boolean is_static;
  private boolean is_final;
  private boolean is_abstract;
  private List<MethodParameter> parameters;
  private String return_type;
  private boolean is_constructor;
  private boolean is_destructor;

  // getters and setters
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getVisibility() {
    return visibility;
  }

  public void setVisibility(String visibility) {
    this.visibility = visibility;
  }

  public boolean isStatic() {
    return is_static;
  }

  public void setStatic(boolean is_static) {
    this.is_static = is_static;
  }

  public boolean isFinal() {
    return is_final;
  }

  public void setFinal(boolean is_final) {
    this.is_final = is_final;
  }

  public boolean isAbstract() {
    return is_abstract;
  }

  public void setAbstract(boolean is_abstract) {
    this.is_abstract = is_abstract;
  }

  public List<MethodParameter> getParameters() {
    return parameters;
  }

  public void setParameters(List<MethodParameter> parameters) {
    this.parameters = parameters;
  }

  public String getReturnType() {
    return return_type;
  }

  public void setReturnType(String return_type) {
    this.return_type = return_type;
  }

  public boolean isConstructor() {
    return is_constructor;
  }

  public void setConstructor(boolean is_constructor) {
    this.is_constructor = is_constructor;
  }

  public boolean isDestructor() {
    return is_destructor;
  }

  public void setDestructor(boolean is_destructor) {
    this.is_destructor = is_destructor;
  }
}
