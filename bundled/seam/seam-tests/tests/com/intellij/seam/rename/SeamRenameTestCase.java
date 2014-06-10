package com.intellij.seam.rename;

import com.intellij.seam.highlighting.SeamHighlightingTestCase;
import com.intellij.testFramework.builders.JavaModuleFixtureBuilder;
import com.intellij.testFramework.builders.WebModuleFixtureBuilder;
import com.intellij.openapi.application.PathManager;
import org.jetbrains.annotations.NonNls;

import java.io.File;

public abstract class SeamRenameTestCase extends SeamHighlightingTestCase<WebModuleFixtureBuilder> {

  protected Class<WebModuleFixtureBuilder> getModuleFixtureBuilderClass() {
    return WebModuleFixtureBuilder.class;
  }

  protected void configureModule(final WebModuleFixtureBuilder moduleBuilder) throws Exception {
    super.configureModule(moduleBuilder);
    moduleBuilder.setMockJdkLevel(JavaModuleFixtureBuilder.MockJdkLevel.jdk15);
    moduleBuilder.addSourceRoot("src");

    moduleBuilder.addWebRoot(myFixture.getTempDirPath(), "/");

    moduleBuilder.addLibraryJars("myfaces", PathManager.getHomePath().replace(File.separatorChar, '/') + getBasePath(), "myfaces.jar");
    moduleBuilder
      .addLibraryJars("myfaces-jsf-api", PathManager.getHomePath().replace(File.separatorChar, '/') + getBasePath(), "myfaces-jsf-api.jar");

    addSeamJar(moduleBuilder);
  }

  @NonNls
  public String getBasePath() {
    return super.getBasePath() + "rename/";
  }
}
