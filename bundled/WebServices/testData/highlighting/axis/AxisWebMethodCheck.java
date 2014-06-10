package axis;

import javax.jws.WebMethod;
import javax.jws.WebService;

@WebService
public class AxisWsWebMethodCheck {
  @WebMethod
  public void foo(int i) {}

  public Boolean bar(Boolean b) {
    return Boolean.TRUE;
  }
}