class A {
  public static void main() {
      try {
          aaa.HelloWorldServiceLocator locator = new aaa.HelloWorldServiceLocator();
          HelloWorld_PortType service = locator.();
          // If authorization is required
          //((HelloWorldSoapBindingStub)service).setUsername("user3");
          //((HelloWorldSoapBindingStub)service).setPassword("pass3");
          // invoke business method
          service.(<caret>);
      } catch (javax.xml.rpc.ServiceException ex) {
          ex.printStackTrace();
      }
      catch (java.rmi.RemoteException ex) {
          ex.printStackTrace();
      }
  }
}