class A {
  public static void main() {
      try {
          MyServiceLocator locator = new MyServiceLocator();
          MyServiceName service = locator.();
          // If authorization is required
          //((MyService_Soap_BindingStub)service).setUsername("user3");
          //((MyService_Soap_BindingStub)service).setPassword("pass3");
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