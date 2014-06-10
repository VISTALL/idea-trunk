package com.intellij.seam.highlighting;

import com.intellij.testFramework.builders.JavaModuleFixtureBuilder;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NonNls;

/**
 * User: Sergey.Vasiliev
 */
public class SeamDomHighlightingTest extends SeamHighlightingTestCase {
  protected void setUp() throws Exception {
    super.setUp();

    addJavaeeSupport();
  }

  protected void configureModule(final JavaModuleFixtureBuilder moduleBuilder) throws Exception {
    super.configureModule(moduleBuilder);

    addSeamJar(moduleBuilder);
  }

  public void test_IDEADEV_26145() throws Throwable {
    myFixture.copyFileToProject("FooComponent.java");
    myFixture.copyFileToProject("actions//FooComponentDefinedInComponentsXml.java");

    VirtualFile file = getFile(myFixture.getTempDirPath() + "/FooComponent.java");
    assertNotNull(file);

    myFixture.allowTreeAccessForFile(file);

    myFixture.testHighlighting(true, false, true, "components.xml");
  }

  public void test_IDEADEV_32572() throws Throwable {
    myFixture.copyFileToProject("actions//FooComponentDefinedInComponentsXml.java");
    myFixture.copyFileToProject("components.xml");

    myFixture.testHighlighting(true, false, true, "FooTestComponent.java");
  }

  @NonNls
  public String getBasePath() {
    return super.getBasePath() + "highlighting/dom/";
  }

}
