class A {
  public static void main() {
      try {
          TypeToUnmarshal unmarshalledObject = TypeToUnmarshal.Factory.parse(
                  new java.io.File(""),
                  null
          );
      } catch (java.io.IOException e) {
          e.printStackTrace();
      } catch (org.apache.xmlbeans.XmlException e) {
          e.printStackTrace();
      }
  }
}