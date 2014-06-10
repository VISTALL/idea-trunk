package com.intellij.seam.highlighting;

import com.intellij.testFramework.builders.JavaModuleFixtureBuilder;
import org.jetbrains.annotations.NonNls;

public class SeamIllegalScopeTest extends SeamHighlightingTestCase {
  protected void setUp() throws Exception {
    super.setUp();

    addJavaeeSupport();
  }

  protected void configureModule(final JavaModuleFixtureBuilder moduleBuilder) throws Exception {
    super.configureModule(moduleBuilder);

    addSeamJar(moduleBuilder);
  }



  public void testStatelessBeans() throws Throwable {
    myFixture.testHighlighting(true, false, true, "StatelessBean.java");
    myFixture.testHighlighting(true, false, true, "StatelessBean2.java");
  }

  public void testStatefullBeans() throws Throwable {
    myFixture.testHighlighting(true, false, true, "StatefullBean.java");
  }

  public void testEntityBeans() throws Throwable {
    configeEjbDescriptor();

    myFixture.testHighlighting(true, false, true, "SeamEntityBean.java");
  }

   public void testDataModels() throws Throwable {
    myFixture.testHighlighting(true, false, true, "DataModelBean.java");
  }

  @NonNls
  public String getBasePath() {
    return super.getBasePath() + "highlighting/scopes/";
  }

}
