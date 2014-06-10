class A {
  public static void main() {
      try {
          org.apache.axis.client.Service service = new org.apache.axis.client.Service();
          org.apache.axis.client.Call call = (org.apache.axis.client.Call) service.createCall();
          call.setTargetEndpointAddress(new java.net.URL("http://localhost:8080/MyService"));
          call.setOperationName(new javax.xml.namespace.QName("http://localhost:8080/MyService", "businessMethod"));
          call.invoke(new Object[]{<caret>});
      } catch (javax.xml.rpc.ServiceException ex) {
          ex.printStackTrace();
      }
      catch (java.rmi.RemoteException ex) {
          ex.printStackTrace();
      }
      catch (java.net.MalformedURLException ex) {
          ex.printStackTrace();
      }
  }
}