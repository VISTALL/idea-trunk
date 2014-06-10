package com.intellij.eclipse.export.model;

import java.util.*;

public class ProjectLibrary {
  private String name;
  private Set<Resource> paths = new HashSet<Resource>();

  public ProjectLibrary(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public List<Resource> getResources() {
    return new ArrayList<Resource>(paths);
  }

  public void addResources(Resource[] paths) {
    this.paths.addAll(Arrays.asList(paths));
  }
}
