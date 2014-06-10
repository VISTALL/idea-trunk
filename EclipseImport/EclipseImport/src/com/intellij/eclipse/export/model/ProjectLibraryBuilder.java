package com.intellij.eclipse.export.model;

import java.util.ArrayList;
import java.util.List;

public class ProjectLibraryBuilder {
  private List<ProjectLibrary> result = new ArrayList<ProjectLibrary>();

  public void addModuleLibrary(ModuleLibrary lib) {
    ProjectLibrary projectLibrary = addProjectLibrary(lib.getName());
    projectLibrary.addResources(lib.getResources());
  }

  public List<ProjectLibrary> getProjectLibraries() {
    return result;
  }

  private ProjectLibrary addProjectLibrary(String name) {
    for (ProjectLibrary lib : result) {
      if (lib.getName().equals(name)) return lib;
    }

    ProjectLibrary lib = new ProjectLibrary(name);
    result.add(lib);
    return lib;
  }
}
