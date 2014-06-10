package wstests;

import junit.framework.TestCase;
import com.intellij.testFramework.builders.JavaModuleFixtureBuilder;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.advancedtools.webservices.utils.DeployUtils;

/**
 * @by maxim
 */
public class UpdateWebXmlTest extends BaseWSTestCase {
  protected void configureLibs(JavaModuleFixtureBuilder moduleFixtureBuilder) {
  }

  protected void configureInspections() {
  }

  protected String getTestDataPath() {
    return "webxml";
  }

  public void testWeb1() throws Throwable {
    doTest();
  }

  public void testWeb2() throws Throwable {
    doTest();
  }

  public void testWeb3() throws Throwable {
    doTest();
  }

  private void doTest() throws Throwable {
    final String testName = getTestName();
    doHighlightingTest(true, testName + ".xml");
    VirtualFile virtualFile = myFixture.getFile().getVirtualFile();
    DeployUtils.updateWebXml(myFixture.getProject(), virtualFile, "WSServlet");
    myFixture.checkResultByFile(testName + "_after.xml");
  }
}
