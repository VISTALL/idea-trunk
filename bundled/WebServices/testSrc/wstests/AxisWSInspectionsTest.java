package wstests;

/**
 * @by maxim
 */
public class AxisWSInspectionsTest extends BaseWSInspectionsTestCase {
  protected String getTestDataPath() {
    return "highlighting/axis";
  }

  public void testAxis2Ws() throws Throwable {
    doHighlightingTest(true, getTestName() + ".java","services.xml");
  }

  public void testAxis2WsQuickFix() throws Throwable {
    doQuickFixTest("Annotate class with @WebService", getTestName() + ".java", "services.xml");
  }

  public void testAxis2WsQuickFix2() throws Throwable {
    doQuickFixTest("Annotate method as @WebMethod", getTestName() + ".java", "services.xml");
  }

  public void testAxis2Ws2() throws Throwable {
    doHighlightingTest(true, getTestName() + ".java","services.xml");
  }

  public void testAxisWs() throws Throwable {
    doHighlightingTest(true, getTestName() + ".java","deploy.wsdd");
  }

  public void testAxisWs_2() throws Throwable {
    doHighlightingTest(true, "AxisWs.java","deploy2.wsdd");
  }

  public void testAxisWs2() throws Throwable {
    doHighlightingTest(true, getTestName() + ".java","deploy.wsdd");
  }

  public void testAxis2ServicesXmlHighlighting() throws Throwable {
    doHighlightingTest(true, "badservices.xml", "badservices.java");
  }

  public void testAxisWsddHighlighting() throws Throwable {
    doHighlightingTest(true, "badwsdd.wsdd", "badwsdd.java");
  }

  public void testAxisWebMethodInspections() throws Throwable {
    doHighlightingTest(true, true, getJavaTestName(), "deploy.wsdd");
  }

  public void testAxisWebMethodCheck() throws Throwable {
    doHighlightingTest(true, true, getJavaTestName(), "deploy.wsdd");
  }
}
