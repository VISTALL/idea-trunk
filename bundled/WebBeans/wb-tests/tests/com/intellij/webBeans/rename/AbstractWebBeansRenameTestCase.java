package com.intellij.webBeans.rename;

import com.intellij.testFramework.builders.JavaModuleFixtureBuilder;
import com.intellij.testFramework.builders.WebModuleFixtureBuilder;
import com.intellij.webBeans.AbstractWebBeansTestCase;
import org.jetbrains.annotations.NonNls;

public abstract class AbstractWebBeansRenameTestCase extends AbstractWebBeansTestCase<WebModuleFixtureBuilder> {

  protected Class<WebModuleFixtureBuilder> getModuleFixtureBuilderClass() {
    return WebModuleFixtureBuilder.class;
  }

  protected void configureModule(final WebModuleFixtureBuilder moduleBuilder) throws Exception {
    super.configureModule(moduleBuilder);

    moduleBuilder.setMockJdkLevel(JavaModuleFixtureBuilder.MockJdkLevel.jdk15);

    addWebBeansJar(moduleBuilder);

    configureJSF(moduleBuilder);
  }

  @NonNls
  public String getBasePath() {
    return super.getBasePath() + "rename/";
  }
}
