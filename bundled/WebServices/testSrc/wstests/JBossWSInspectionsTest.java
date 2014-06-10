package wstests;

/**
 * @by maxim
 */
public class JBossWSInspectionsTest extends BaseWSInspectionsTestCase {
  protected String getTestDataPath() {
    return "highlighting/jbossws";
  }

  public void testSimple() throws Throwable {
    final String testName = getTestName();
    doHighlightingTest(testName + ".xml");
    doHighlightingTest(testName + "2.xml", testName + ".java");
  }
}