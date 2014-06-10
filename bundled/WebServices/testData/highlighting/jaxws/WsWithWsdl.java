package mypackage;

import javax.jws.WebService;

public class OutputClass {}
public class InputClass {}
 
@WebService
public class WsWithWsdl {
  void <error>doService</error>() {}
  public OutputClass doService2(InputClass cls) { return null; }
}