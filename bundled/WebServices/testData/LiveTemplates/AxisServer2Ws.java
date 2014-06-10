package aaa;

public interface HelloWorldService extends javax.xml.rpc.Service {
    public mypackage77.HelloWorld_PortType getHelloWorld() throws javax.xml.rpc.ServiceException;
}

public interface HelloWorld_PortType extends java.rmi.Remote {
  public java.lang.String sayHelloWorldFrom(java.lang.String from) throws java.rmi.RemoteException;
}

public class HelloWorldServiceLocator extends org.apache.axis.client.Service {
  public HelloWorld_PortType getHelloWorld() throws javax.xml.rpc.ServiceException {
  }
}

public class HelloWorldSoapBindingStub extends org.apache.axis.client.Stub implements HelloWorld_PortType {
  public java.lang.String sayHelloWorldFrom(java.lang.String from) throws java.rmi.RemoteException {
  
  }
}