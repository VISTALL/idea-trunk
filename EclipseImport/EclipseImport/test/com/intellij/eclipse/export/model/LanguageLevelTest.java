package com.intellij.eclipse.export.model;

import org.eclipse.jdt.core.JavaCore;
import org.junit.Test;

public class LanguageLevelTest extends PluginTestCase {
  @Test
  public void testLanguageLevel() {
    assertLanguageLevel(LanguageLevel.JDK_1_3, JavaCore.VERSION_1_1);
    assertLanguageLevel(LanguageLevel.JDK_1_3, JavaCore.VERSION_1_2);
    assertLanguageLevel(LanguageLevel.JDK_1_3, JavaCore.VERSION_1_3);
    assertLanguageLevel(LanguageLevel.JDK_1_4, JavaCore.VERSION_1_4);
    assertLanguageLevel(LanguageLevel.JDK_1_5, JavaCore.VERSION_1_5);
    assertLanguageLevel(LanguageLevel.JDK_1_5, JavaCore.VERSION_1_6);
    assertLanguageLevel(LanguageLevel.JDK_1_5, "bla-bla-bla");
    assertLanguageLevel(null, null);
  }

  @Test
  public void testHasAssertKeyword() {
    assertTrue(LanguageLevel.JDK_1_5.hasAssertKeyword());
    assertTrue(LanguageLevel.JDK_1_4.hasAssertKeyword());
    assertFalse(LanguageLevel.JDK_1_3.hasAssertKeyword());
  }

  @Test
  public void testIsJdk15() {
    assertTrue(LanguageLevel.JDK_1_5.isJdk15());
    assertFalse(LanguageLevel.JDK_1_4.isJdk15());
    assertFalse(LanguageLevel.JDK_1_3.isJdk15());
  }

  private void assertLanguageLevel(LanguageLevel expected, String complianceLevel) {
    assertEquals(expected, LanguageLevel.fromEclipseCompilerLevel(complianceLevel));
  }
}
