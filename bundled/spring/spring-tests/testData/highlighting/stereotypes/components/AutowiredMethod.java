package components;


@org.springframework.stereotype.Service()
public class AutowiredMethod {

  @org.springframework.beans.factory.annotation.Autowired
  public void setString(java.util.List<String> st<caret>r) {

  }
  @org.springframework.beans.factory.annotation.Autowired
  public void setStringArray(String[] str) {

  }
  @org.springframework.beans.factory.annotation.Autowired
  public void setSelf(AutowiredMethod self) {

  }
}