package com.intellij.webBeans.highlighting;

import com.intellij.testFramework.builders.JavaModuleFixtureBuilder;
import org.jetbrains.annotations.NonNls;

public class WebBeanSpecializesInspectionTest extends AbstractWebBeanshighlightingTest {
  protected void setUp() throws Exception {
    super.setUp();

    addJavaeeSupport();
  }

  protected void configureModule(final JavaModuleFixtureBuilder moduleBuilder) throws Exception {
    super.configureModule(moduleBuilder);

    addWebBeansJar(moduleBuilder);
  }

  public void testWithoutSpecializedBean() throws Throwable {
    myFixture.testHighlighting(true, false, false, "WithoutSpecializedBean.java");
  }

  public void testMultipleSpecializedBeans() throws Throwable {
    myFixture.testHighlighting(true, false, false, "MultipleSpecializedBeans.java");
  }

  public void testMultipleSpecializedProducerMethodBeans() throws Throwable {
    myFixture.testHighlighting(true, false, false, "MultipleSpecializedProducerMethodBeans.java");
  }

  public void testStaticSpecializedProducerMethodBeans() throws Throwable {
    myFixture.testHighlighting(true, false, false, "StaticSpecializedProducerMethodBeans.java");
  }

  public void testSpecializesMethodIsProducer() throws Throwable {
    myFixture.testHighlighting(true, false, false, "SpecializesMethodIsProducer.java");
  }

  public void testDuplicatedNamesOfSpecializesMethodProducer() throws Throwable {
    myFixture.testHighlighting(true, false, false, "DuplicatedNamesOfSpecializesMethodProducer.java");
  }

  public void testDuplicatedNamesOfSpecializesBean() throws Throwable {
    myFixture.testHighlighting(true, false, false, "DuplicatedNamesOfSpecializesBean.java");
  }

  @NonNls
  public String getBasePath() {
    return super.getBasePath() + "highlighting/WebBeanSpecializesInspection/";
  }

}