package com.intellij.seam.highlighting;

import com.intellij.testFramework.builders.JavaModuleFixtureBuilder;
import org.jetbrains.annotations.NonNls;

/**
 * User: Sergey.Vasiliev
 */
public class SeamAnnotationsInconsistencyHighlightingTest extends SeamHighlightingTestCase {

  protected void setUp() throws Exception {
    super.setUp();

    addJavaeeSupport();
  }

  protected void configureModule(final JavaModuleFixtureBuilder moduleBuilder) throws Exception {
    super.configureModule(moduleBuilder);

    addSeamJar(moduleBuilder);
  }

  public void testIllegalSeamAnnotationsOnJavaBean() throws Throwable {
    myFixture.testHighlighting(false, false, true, "JavaBean.java");
  }

  public void testJavaBeans() throws Throwable {
    myFixture.testHighlighting(true, false, true, "SimpleBean.java");
  }

  public void testStatelessBeans() throws Throwable {
    myFixture.testHighlighting(true, false, true, "StatelessBean.java");
  }

  public void testStatefullBeans() throws Throwable {
    myFixture.testHighlighting(true, false, true, "StatefullBean.java");
  }

  public void testEntityBeans() throws Throwable {
    configeEjbDescriptor();

    myFixture.testHighlighting(true, false, true, "SeamEntityBean.java");
  }

  @NonNls
  public String getBasePath() {
    return super.getBasePath() + "highlighting/inconsistency/";
  }
}
