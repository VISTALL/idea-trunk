class A {
  public static void main() {
      try {
          MyLocator locator = new MyLocator();
          MyServiceName service = locator.();
          // invoke business method
          service.(<caret>);
      } catch (Exception ex) {
          ex.printStackTrace();
      }
  }
}