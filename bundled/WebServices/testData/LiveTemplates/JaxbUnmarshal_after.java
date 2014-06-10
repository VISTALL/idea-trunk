class A {
  public static void main() {
      try {
          // create a JAXBContext capable of handling classes generated into package
          javax.xml.bind.JAXBContext jaxbContext = javax.xml.bind.JAXBContext.newInstance("");
          // create an Unmarshaller
          javax.xml.bind.Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
          // unmarshal an instance document into a tree of Java content
          // objects composed of classes from the package.
          TypeToUnmarshal unmarshalledObject = (TypeToUnmarshal) unmarshaller.unmarshal(new java.io.FileInputStream(""));
      } catch (javax.xml.bind.JAXBException je) {
          je.printStackTrace();
      } catch (java.io.IOException ioe) {
          ioe.printStackTrace();
      }
  }
}