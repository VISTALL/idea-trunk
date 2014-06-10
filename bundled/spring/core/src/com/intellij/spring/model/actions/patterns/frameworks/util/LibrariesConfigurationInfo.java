package com.intellij.spring.model.actions.patterns.frameworks.util;

import com.intellij.util.xmlb.annotations.AbstractCollection;
import com.intellij.util.xmlb.annotations.Tag;

public class LibrariesConfigurationInfo {

  @Tag("libraries")
  @AbstractCollection(surroundWithTag = false)
  public LibraryConfigurationInfo[] myInfos;

  public LibraryConfigurationInfo[] getLibraryConfigurationInfos() {
    return myInfos;
  }
}
