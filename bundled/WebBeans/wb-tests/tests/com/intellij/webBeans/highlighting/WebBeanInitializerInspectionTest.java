package com.intellij.webBeans.highlighting;

import com.intellij.testFramework.builders.JavaModuleFixtureBuilder;
import org.jetbrains.annotations.NonNls;

public class WebBeanInitializerInspectionTest extends AbstractWebBeanshighlightingTest {
  protected void setUp() throws Exception {
    super.setUp();

    addJavaeeSupport();
  }

  protected void configureModule(final JavaModuleFixtureBuilder moduleBuilder) throws Exception {
    super.configureModule(moduleBuilder);

    addWebBeansJar(moduleBuilder);
  }


  public void testTwoInializerConstructors() throws Throwable {
    myFixture.testHighlighting(true, false, false, "TwoInitializerConstructors.java");
  }

  public void testInializerMethods() throws Throwable {
    myFixture.testHighlighting(true, false, false, "InitializerMethodErrors.java");
  }

  @NonNls
  public String getBasePath() {
    return super.getBasePath() + "highlighting/WebBeanInitializerInspection/";
  }

}
