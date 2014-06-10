class A {
  public static void main() {
      MyService service = new PortLocator().();
      //invoke business method
      service.businessMethod(<caret>);
  }
}