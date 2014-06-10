package com.intellij.eclipse.export.model;

import org.eclipse.jdt.core.JavaCore;

public enum LanguageLevel {
  JDK_1_3 {
    public boolean hasAssertKeyword() { return false; }
    public boolean isJdk15() { return false; }
  },
  JDK_1_4 {
    public boolean isJdk15() { return false; }
  },
  JDK_1_5;

  public boolean hasAssertKeyword() {
    return true;
  }

  public boolean isJdk15() {
    return true;
  }

  public static LanguageLevel fromEclipseCompilerLevel(String compilerLevel) {
    if (compilerLevel == null) return null;

    if (compilerLevel.equals(JavaCore.VERSION_1_1)
        || compilerLevel.equals(JavaCore.VERSION_1_2)
        || compilerLevel.equals(JavaCore.VERSION_1_3)) return JDK_1_3;

    if (compilerLevel.equals(JavaCore.VERSION_1_4)) return JDK_1_4;

    return JDK_1_5;
  }
}
