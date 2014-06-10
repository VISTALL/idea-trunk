package wstests;

/**
 * @by maxim
 */
public class RestSupportTest extends BaseWSInspectionsTestCase {
  protected String getTestDataPath() {
    return "highlighting/rest";
  }

  public void testWadl() throws Throwable {
    doHighlightingTest(true, getTestName() + ".wadl");
  }
}
