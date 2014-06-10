package com.intellij.webBeans.highlighting;

import com.intellij.testFramework.builders.JavaModuleFixtureBuilder;
import org.jetbrains.annotations.NonNls;

public class WebBeanStereotypeRestrictionsInspectionTest extends AbstractWebBeanshighlightingTest {
  protected void setUp() throws Exception {
    super.setUp();
  }

  protected void configureModule(final JavaModuleFixtureBuilder moduleBuilder) throws Exception {
    super.configureModule(moduleBuilder);

    addWebBeansJar(moduleBuilder);
  }

  public void testUnsupportedStereotypedScopes() throws Throwable {
    myFixture.copyDirectoryToProject("beans", "beans");

    myFixture.testHighlighting(false, false, false, "beans/UnsupportedScopesWebBean.java");
  }

  public void testRequiredTypesStereotypes() throws Throwable {
    myFixture.copyDirectoryToProject("beans", "beans");

    myFixture.testHighlighting(false, false, false, "beans/RequiredTypesWebBean.java");
  }

  public void testMultipleStereotypesScopes() throws Throwable {
    myFixture.copyDirectoryToProject("beans", "beans");

    myFixture.testHighlighting(false, false, false, "beans/SimpleWebBean2.java");
    myFixture.testHighlighting(false, false, false, "beans/SimpleWebBean3.java");
    myFixture.testHighlighting(false, false, false, "beans/SimpleWebBean4.java");
    myFixture.testHighlighting(false, false, false, "beans/SimpleWebBean.java");
  }

  @NonNls
  public String getBasePath() {
    return super.getBasePath() + "highlighting/WebBeanStereotypeRestrictionsInspection/";
  }

}