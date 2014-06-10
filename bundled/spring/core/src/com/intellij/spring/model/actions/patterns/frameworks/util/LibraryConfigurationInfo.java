package com.intellij.spring.model.actions.patterns.frameworks.util;

import com.intellij.util.xmlb.annotations.Attribute;
import com.intellij.util.xmlb.annotations.Tag;

@Tag("lib")
public class LibraryConfigurationInfo {

  @Attribute("framework-id")
  public String myFrameworkId;

  @Attribute("jar-name")
  public String myJarName;

  @Attribute("version")
  public String myVersion;

  @Attribute("download-url")
  public String myDownloadUrl;

 @Attribute("presentation-url")
  public String myPresentationdUrl;

 @Attribute("required-classes")
 public String myRequiredClasses;

  public String getFrameworkId() {
    return myFrameworkId;
  }

  public String getJarName() {
    return myJarName;
  }

  public String getVersion() {
    return myVersion;
  }

  public String getDownloadUrl() {
    return myDownloadUrl;
  }

  public String getPresentationdUrl() {
    return myPresentationdUrl;
  }

  public String getRequiredClasses() {
    return myRequiredClasses;
  }
}

