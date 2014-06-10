package com.intellij.seam.facet;

import org.jetbrains.annotations.NonNls;

public enum SeamVersion {

  @NonNls SEAM_1_2_1("1.2.1.GA", "1.2"),
  @NonNls SEAM_2_0_0("2.0.1.GA", "2.0"),
  @NonNls SEAM_2_1_1("2.1.1.GA", "2.1");

  private final String myName;
  private final String myShortName;

  private SeamVersion(String name, String shortName) {
    myName = name;
    myShortName = shortName;
  }

  public String getName() {
    return myName;
  }

  public String getShortName() {
    return myShortName;
  }

  public String toString() {
    return myName;
  }
}
