class A {
  public static void main() {
      try {
          Client client = new Client(new java.net.URL("url"));
          Object[] results = client.invoke("methodname", new Object[]{<caret>});
      } catch (Exception e) {
          e.printStackTrace();
      }
  }
}