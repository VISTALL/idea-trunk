package mypackage;

import javax.jws.WebService;
import javax.jws.Oneway;

public class OutputClass {}
public class InputClass {}
 
@WebService
public class WsWithWsdl {
  @Oneway
  public <error>OutputClass</error> doService2(InputClass cls) { return null; }
}