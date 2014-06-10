class A {
  public static void main() {
      try {
          TypeToMarshal objectToMarshal = TypeToMarshal.Factory.newInstance();
          objectToMarshal.save(new java.io.File(""));
      } catch (java.io.IOException e) {
          e.printStackTrace();
      }
  }
}