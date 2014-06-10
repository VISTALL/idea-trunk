package com.intellij.webBeans.highlighting;

import com.intellij.testFramework.builders.JavaModuleFixtureBuilder;
import org.jetbrains.annotations.NonNls;

public class WebBeanObservesInspectionTest extends AbstractWebBeanshighlightingTest {
  protected void setUp() throws Exception {
    super.setUp();

    addJavaeeSupport();
  }

  protected void configureModule(final JavaModuleFixtureBuilder moduleBuilder) throws Exception {
    super.configureModule(moduleBuilder);

    addWebBeansJar(moduleBuilder);
  }


  public void testMultipleObservesParameters() throws Throwable {
    myFixture.configureByFiles("MultipleObservesParameters.java", "Document.java");
    myFixture.testHighlighting(true, false, false, "MultipleObservesParameters.java");
  }

  public void testDisposeParameters() throws Throwable {
    myFixture.configureByFiles("DisposeObservesParameters.java", "Document.java");
    myFixture.testHighlighting(true, false, false, "DisposeObservesParameters.java");
  }

  public void testParameterizedObservesParameter() throws Throwable {
    myFixture.configureByFiles("ParameterizedObservesParameter.java", "Document.java");
    myFixture.testHighlighting(true, false, false, "ParameterizedObservesParameter.java");
  }

  public void testProducesWithObservesParameter() throws Throwable {
    myFixture.configureByFiles("ProducesWithObservesParameter.java", "Document.java");
    myFixture.testHighlighting(true, false, false, "ProducesWithObservesParameter.java");
  }

  public void testInitializerWithObservesParameter() throws Throwable {
    myFixture.configureByFiles("InitializerWithObservesParameter.java", "Document.java");
    myFixture.testHighlighting(true, false, false, "InitializerWithObservesParameter.java");
  }

  @NonNls
  public String getBasePath() {
    return super.getBasePath() + "highlighting/WebBeanObservesInspection/";
  }

}