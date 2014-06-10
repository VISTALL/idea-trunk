class A {
  public static void main() {
      try {
          MyLocator locator = new MyLocator();
          MyServiceName service = locator.();
          // invoke business method
          service.(<caret>);
      } catch (java.rmi.RemoteException ex) {
          ex.printStackTrace();
      }
  }
}