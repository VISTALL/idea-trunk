package mypackage;

import javax.jws.WebService;
import javax.jws.Oneway;

public class OutputClass {}
public class InputClass {}
 
@WebService
public class WsWithWsdl {
  public OutputClass doService2(InputClass cls) { return null; }
}