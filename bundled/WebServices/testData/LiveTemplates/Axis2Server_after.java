class A {
  public static void main() {
      try {
          MyServiceStub stub = new MyServiceStub();
          // invoke business method
          stub.(<caret>);
      } catch (Exception ex) {
          ex.printStackTrace();
      }

  }
}