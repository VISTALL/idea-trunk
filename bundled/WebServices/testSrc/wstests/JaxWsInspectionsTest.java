package wstests;

import java.util.Arrays;

/**
 * @by maxim
 */
public class JaxWsInspectionsTest extends BaseWSInspectionsTestCase {
  {
    myTestsWithoutJavaee.addAll(Arrays.asList("Dummy", "JaxWsRuntimeConfig","WSRefsInApplicationClientJavaEE5"));
    ourTestsWithJdk.add("JaxWs");
  }

  protected String getTestDataPath() {
    return "highlighting/jaxws";
  }

  public void testDummy() throws Throwable {
    doHighlightingTest(getTestName() + ".jsp");
  }

  public void testJaxWs() throws Throwable {
    doHighlightingTest();
  }

  public void testEmptyWs() throws Throwable {
    doHighlightingTest();
  }

  public void testInnerClassInWs() throws Throwable {
    doHighlightingTest();
  }
  
  public void testClassInDefaultPackage() throws Throwable {
    doHighlightingTest();
  }

  public void testWsWithWsdl() throws Throwable {
    doHighlightingTest(getTestName() + ".java", getTestName() + ".wsdl");
  }

  public void testOneway() throws Throwable {
    doHighlightingTest();
  }

  public void testOneway2() throws Throwable {
    doQuickFixTest("Remove Oneway annotation");
  }

  public void testOneway3() throws Throwable {
    doQuickFixTest("Set return type to be void");
  }

  public void testJaxWsRuntimeConfig() throws Throwable {
    final String testName = getTestName();
    doHighlightingTest(testName + ".xml", testName + ".java");
  }

  public void testWSRefsInApplicationClientJavaEE5() throws Throwable {
    doHighlightingTest(getTestName() + ".xml", getTestName() + ".java");
  }
}
