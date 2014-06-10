class AAA {
  private String property;
  private String property2;

  String <error>getProperty</error>() { return property; }
  void setProperty(String _property) { property = _property; }
  public String <error>getProperty2</error>() { return property2; }
  // public void setProperty2(String _property) { property2 = _property; }
}