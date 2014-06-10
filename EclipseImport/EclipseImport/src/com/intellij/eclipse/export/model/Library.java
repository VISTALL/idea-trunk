package com.intellij.eclipse.export.model;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;

// todo possibly delete this class and leave just Resource...
public class Library {
  protected IClasspathEntry entry;
  protected IProject project;

  public Library(IClasspathEntry entry, IProject project) {
    this.entry = entry;
    this.project = project;
  }

  public Resource getResource() {
    IPath path;
    boolean isVariable = false;

    if (entry.getEntryKind() == IClasspathEntry.CPE_VARIABLE) {
      path = entry.getPath();
      isVariable = true;
    } else if (isLocal()) {
      path = getWorkspaceRoot().getFile(entry.getPath()).getLocation();
    } else {
      path = entry.getPath();
    }

    return new Resource(path, isVariable);
  }

  public boolean isLocal() {
    // todo wrong behavior!!!
    // resources may exist even for files that are outside the project
    return getWorkspaceRoot().exists(entry.getPath());
  }

  public boolean isExported() {
    return entry.isExported();
  }

  private IWorkspaceRoot getWorkspaceRoot() {
    return project.getWorkspace().getRoot();
  }
}