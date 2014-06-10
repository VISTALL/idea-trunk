package com.intellij.webBeans.highlighting;

import com.intellij.testFramework.builders.JavaModuleFixtureBuilder;
import org.jetbrains.annotations.NonNls;

public class WebBeanDecoratorInspectionTest extends AbstractWebBeanshighlightingTest {
  protected void setUp() throws Exception {
    super.setUp();

    addJavaeeSupport();
  }

  protected void configureModule(final JavaModuleFixtureBuilder moduleBuilder) throws Exception {
    super.configureModule(moduleBuilder);

    addWebBeansJar(moduleBuilder);
  }

  public void testValidDecorator() throws Throwable {
    myFixture.configureByFiles("Account.java", "FooDecorator.java");
    myFixture.testHighlighting(true, false, false, "FooDecorator.java");
  }

  public void testNoDelegateAttributesForDecorator() throws Throwable {
    myFixture.configureByFiles("Account.java", "FooDecorator2.java");
    myFixture.testHighlighting(true, false, false, "FooDecorator2.java");
  }

  public void testTooManyDelegateAttributesForDecorator() throws Throwable {
    myFixture.configureByFiles("Account.java", "FooDecorator3.java");
    myFixture.testHighlighting(true, false, false, "FooDecorator3.java");
  }

  public void testDelegateAttributeNotInterface() throws Throwable {
    myFixture.configureByFiles("Account.java", "FooDecorator4.java");
    myFixture.testHighlighting(true, false, false, "FooDecorator4.java");
  }

  public void testDelegateAttributeNotImplementedInterface() throws Throwable {
    myFixture.configureByFiles("Account.java", "FooDecorator5.java");
    myFixture.testHighlighting(true, false, false, "FooDecorator5.java");
  }

  @NonNls
  public String getBasePath() {
    return super.getBasePath() + "highlighting/WebBeanDecoratorInspection/";
  }

}