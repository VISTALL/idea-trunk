package com.intellij.seam.highlighting;

import com.intellij.codeInspection.jsp.ELValidationInspection;
import com.intellij.testFramework.builders.JavaModuleFixtureBuilder;
import org.jetbrains.annotations.NonNls;

public class SeamComponentELHighlightingTest extends SeamHighlightingTestCase {

  protected void configureModule(final JavaModuleFixtureBuilder moduleBuilder) throws Exception {
    super.configureModule(moduleBuilder);

    addSeamJar(moduleBuilder);
    myFixture.enableInspections(ELValidationInspection.class);

  }

  public void testUndefinedContextVariables() throws Throwable {
    allowTreeAccessForFile("Blog.java", true);

    myFixture.testHighlighting(true, false, true, "ELUndefinedContextVariables.java", "Blog.java");
  }

  @NonNls
  public String getBasePath() {
    return super.getBasePath() + "highlighting/";
  }
}
