package wstests;

import com.intellij.testFramework.builders.JavaModuleFixtureBuilder;
import org.jetbrains.annotations.NonNls;

/**
 * @author Maxim
 */
public class WsdlCompletionTest extends BaseWSTestCase {
  public void test1() throws Throwable { doTest(); }
  public void test2() throws Throwable { doTest(); }
  public void test3() throws Throwable { doTest(); }
  public void test4() throws Throwable { doTest(); }
  public void test5() throws Throwable { doTest(); }

  private void doTest() throws Throwable {
    String ext = "wsdl";
    myFixture.testCompletion(prepareFiles(getTestName() + "." + ext),getTestName() + "_after." + ext);
  }

  protected void configureLibs(JavaModuleFixtureBuilder moduleFixtureBuilder) {
  }

  protected void configureInspections() {
  }

  protected @NonNls String getTestDataPath() {
    return "completion/wsdl";
  }
}
