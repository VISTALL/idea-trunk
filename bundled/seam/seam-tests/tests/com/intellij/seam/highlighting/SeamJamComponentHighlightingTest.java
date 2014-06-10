package com.intellij.seam.highlighting;

import com.intellij.testFramework.builders.JavaModuleFixtureBuilder;
import org.jetbrains.annotations.NonNls;

public class SeamJamComponentHighlightingTest extends SeamHighlightingTestCase {

  protected void configureModule(final JavaModuleFixtureBuilder moduleBuilder) throws Exception {
    super.configureModule(moduleBuilder);

    addSeamJar(moduleBuilder);

  }

  public void testIncorrectPsiClassForNameAnnotation() throws Throwable {
    myFixture.testHighlighting(true, false, true, "IncorrectNameAnnotatedClass.java");
  }

  public void testIncorrectDataModelSignature() throws Throwable {
    myFixture.testHighlighting(true, false, true, "IncorrectDataModelSignature.java");
  }

  public void testIncorrectFactorySignature() throws Throwable {
    myFixture.testHighlighting(true, false, true, "IncorrectFactorySignature.java");
  }

  public void testIncorrectUnwrapSignature() throws Throwable {
    myFixture.testHighlighting(true, false, true, "IncorrectUnwrapSignature.java");
  }

  public void testDuplicatedCreateAndDestroyAnno() throws Throwable {
    myFixture.testHighlighting(true, false, true, "DublicatedCreateAndDestroyAnnotations.java");
  }

  public void testIncorrectCreateAndDestroySignature() throws Throwable {
    myFixture.testHighlighting(true, false, true, "IncorrectCreateDestroyAnnoSignature1.java");
    myFixture.testHighlighting(true, false, true, "IncorrectCreateDestroyAnnoSignature2.java");
    myFixture.testHighlighting(true, false, true, "IncorrectCreateDestroyAnnoSignature3.java");
  }

  @NonNls
  public String getBasePath() {
    return super.getBasePath() + "highlighting/";
  }

}
