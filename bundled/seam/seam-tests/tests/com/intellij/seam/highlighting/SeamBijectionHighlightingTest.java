package com.intellij.seam.highlighting;

import com.intellij.openapi.application.PathManager;
import com.intellij.testFramework.builders.JavaModuleFixtureBuilder;
import org.jetbrains.annotations.NonNls;

import java.io.File;

public class SeamBijectionHighlightingTest extends SeamHighlightingTestCase {

  protected void configureModule(final JavaModuleFixtureBuilder moduleBuilder) throws Exception {
    super.configureModule(moduleBuilder);

    addSeamJar(moduleBuilder);

    if (getName().equals("testContextVariablesTypeMisMatch")) {
      moduleBuilder.addLibraryJars("ejb3-persistence", PathManager.getHomePath().replace(File.separatorChar, '/') + super.getBasePath(),
                                   "libs/ejb3-persistence.jar");
      moduleBuilder.addLibraryJars("hibernate-search", PathManager.getHomePath().replace(File.separatorChar, '/') + super.getBasePath(),
                                   "libs/hibernate-search.jar");
    }
  }

  public void testIncorrectSignatureBijections() throws Throwable {
    allowTreeAccessForFile("Blog.java", true);

    myFixture.testHighlighting(false, false, true, "IncorrectSignatureBijections.java", "Blog.java");
  }


  public void testUndefinedContextVariables() throws Throwable {
    allowTreeAccessForFile("Blog.java", true);

    myFixture.testHighlighting(true, false, true, "BijectionNonDefinedContextVariable.java", "Blog.java");
  }

  public void testContextVariablesTypeMisMatch() throws Throwable {
    allowTreeAccessForFile("Blog.java", true);
    allowTreeAccessForFile("BlogChild.java", true);

    myFixture.testHighlighting(false, false, true, "BijectionTypeMismatch.java", "Blog.java", "BlogChild.java");
  }

  public void testIllegalScopeDeclaration() throws Throwable {
    allowTreeAccessForFile("Blog.java", true);

    myFixture.testHighlighting(false, false, true, "BijectionIllegalScopeDeclaration.java", "Blog.java");
  }

  @NonNls
  public String getBasePath() {
    return super.getBasePath() + "highlighting/";
  }

}
