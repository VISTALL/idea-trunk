package com.intellij.webBeans.highlighting;

import com.intellij.testFramework.builders.JavaModuleFixtureBuilder;
import org.jetbrains.annotations.NonNls;

public class WebBeanInjectionInspectionTest extends AbstractWebBeanshighlightingTest {
  protected void setUp() throws Exception {
    super.setUp();

//    addJavaeeSupport();
  }

  protected void configureModule(final JavaModuleFixtureBuilder moduleBuilder) throws Exception {
    super.configureModule(moduleBuilder);

    addWebBeansJar(moduleBuilder);
  }


  public void testInjectableFieldsModifiers() throws Throwable {
    myFixture.testHighlighting(false, false, false, "InjectableFields.java");
  }

  @NonNls
  public String getBasePath() {
    return super.getBasePath() + "highlighting/WebBeanInjectionInspection/";
  }

}