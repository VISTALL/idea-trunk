package aaa;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.xml.ws.Service;

@WebService
class WS {
  @WebMethod
  void wsMethod();
}

class Locator extends javax.xml.ws.Service {
  WS myWS();
}