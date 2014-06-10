package wstests;

/**
 * @by maxim
 */
public class JaxB2WSInspectionsTest extends BaseWSInspectionsTestCase {
  protected String getTestDataPath() {
    return "highlighting/jaxb2";
  }

  public void testJaxB2Mapped() throws Throwable {
    doHighlightingTest(getTestName() + ".java");
  }

  public void testJaxB2Mapped2() throws Throwable {
    doHighlightingTest(getTestName() + ".java");
  }
}
