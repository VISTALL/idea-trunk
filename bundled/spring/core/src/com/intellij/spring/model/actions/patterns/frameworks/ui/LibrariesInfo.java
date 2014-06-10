package com.intellij.spring.model.actions.patterns.frameworks.ui;

import com.intellij.facet.ui.libraries.LibraryInfo;
import com.intellij.openapi.module.Module;

public class LibrariesInfo {
    private LibraryInfo[] myLibs;
  private Module myModule;
  private String myName;

  public LibrariesInfo(final LibraryInfo[] libs, final Module module, final String name) {
    myLibs = libs;
    myModule = module;
    myName = name;
  }

  public LibraryInfo[] getLibs() {
    return myLibs;
  }

  public void setLibs(final LibraryInfo[] libs) {
    myLibs = libs;
  }

  public Module getModule() {
    return myModule;
  }

  public void setModule(final Module module) {
    myModule = module;
  }

  public String getName() {
    return myName;
  }

  public void setName(final String name) {
    myName = name;
  }
}
