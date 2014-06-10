import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.WebParam;
import javax.jws.WebResult;
import java.util.List;

public class JaxWsResult {
  private int field;
  public java.lang.Double myDouble;
  public java.lang.Float myFloat;
  public java.lang.Integer myInteger;
  public java.lang.Character myChar;
  public java.lang.Short myShort;
  public java.lang.Byte myByte;
  public java.lang.Long myLong;
  
  public int getField() {
    return field;
  }
  
  public void setField(int value) {
    field = value;
  }
}

@WebService
class <error>JaxWs</error> {
  JaxWsResult result;
  static JaxWs instance;

  public int doIt() { return 0; }
  public JaxWsResult doIt2() {
    return new JaxWsResult() {};
  }
  private void doItImplementation() {}

  @WebMethod
  private void <error>doIt3</error>() {}
  
  @WebResult(name = "result", targetNamespace = "http://www.csapi.org/schema/parlayx/sms/send/v2_2/local")
  public String sendSms(@WebParam(name = "addresses", targetNamespace = "http://www.csapi.org/schema/parlayx/sms/send/v2_2/local") List<String> addresses,
                        @WebParam(name = "senderName", targetNamespace = "http://www.csapi.org/schema/parlayx/sms/send/v2_2/local") String senderName,
                        @WebParam(name = "message", targetNamespace = "http://www.csapi.org/schema/parlayx/sms/send/v2_2/local") String message) {
     return null;
  }
}

class MyList<Type> {}
enum C {}
