package wstests;

import java.util.Arrays;

/**
 * @by maxim
 */
public class JaxRPCInspectionsTest extends BaseWSInspectionsTestCase {
  {
    myTestsWithoutJavaee.addAll(Arrays.asList("JaxRPCMapping","JaxRPCWsWithWsdl"));
  }
  protected String getTestDataPath() {
    return "highlighting/jaxrpc";
  }

  public void testJaxRPCWsWithWsdl() throws Throwable {
    final String testName = getTestName();
    doHighlightingTest(testName + ".java", testName + ".wsdl");
  }

  public void testJaxRPCMapping() throws Throwable {
    final String testName = getTestName();
    doHighlightingTest(testName + ".xml", testName + ".java");
  }

  public void testJaxRPCRuntimeConfig() throws Throwable {
    final String testName = getTestName();
    doHighlightingTest(testName + ".xml", testName + ".java");
  }
}
