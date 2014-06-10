package jaxws;
import javax.jws.WebService;

@WebService
public class InnerClassInWs {
  public class Ccc {}
  public static class Ccc2 {}
  public static enum En {AAA, BBB}

  public void <error>hello</error>(Ccc ccc) {}
  public void hello2(Ccc2 ccc) {}
  public En hello3(En en) {return en == En.AAA ? En.AAA : En.BBB;}
}