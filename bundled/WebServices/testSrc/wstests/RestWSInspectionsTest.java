package wstests;

import com.intellij.testFramework.builders.JavaModuleFixtureBuilder;

import java.io.File;

/**
 * @author Konstantin Bulenkov
 */
public class RestWSInspectionsTest extends BaseWSInspectionsTestCase {
  protected String getTestDataPath() {
    return "highlighting/rest";
  }

  @Override
  protected void configureLibs(final JavaModuleFixtureBuilder moduleFixtureBuilder) {
    super.configureLibs(moduleFixtureBuilder);
    moduleFixtureBuilder.addLibraryJars("jcr311",
                                        (getPluginBasePath() +"testData/lib/").replace(File.separatorChar, '/'),
                                        "jsr311-api.jar");
  }

  public void testRestHighlights() throws Throwable {
    doHighlightingTest(true, getTestName() + ".java");
  }

  public void testRemovePathFix() throws Throwable {
    doQuickFixTest("Remove @Path annotation", getTestName() + ".java");
  }

  public void testChangeVoidToStringFix() throws Throwable {
    doQuickFixTest("Change return type to String", getTestName() + ".java");
  }
}
