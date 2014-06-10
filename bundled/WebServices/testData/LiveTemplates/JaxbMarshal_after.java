class A {
  public static void main() {
      try {
          // create a JAXBContext capable of handling classes generated into package
          javax.xml.bind.JAXBContext jaxbContext = javax.xml.bind.JAXBContext.newInstance("");
          // create an object to marshal
          TypeToMarshal objectToMarshal = new TypeToMarshal();
          // create a Marshaller and do marshal
          javax.xml.bind.Marshaller marshaller = jaxbContext.createMarshaller();
          marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
          marshaller.marshal(objectToMarshal, new java.io.FileOutputStream(""));
      } catch (javax.xml.bind.JAXBException je) {
          je.printStackTrace();
      } catch (java.io.FileNotFoundException io) {
          io.printStackTrace();
      }
  }
}