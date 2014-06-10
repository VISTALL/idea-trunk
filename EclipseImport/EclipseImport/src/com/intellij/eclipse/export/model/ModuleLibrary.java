package com.intellij.eclipse.export.model;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import static org.eclipse.jdt.core.IClasspathEntry.CPE_LIBRARY;
import static org.eclipse.jdt.core.IClasspathEntry.CPE_VARIABLE;

import java.util.ArrayList;
import java.util.List;

public class ModuleLibrary {
  private IClasspathEntry entry;
  private IClasspathContainer container;
  private IProject project;

  public ModuleLibrary(IClasspathEntry entry,
                       IClasspathContainer container,
                       IProject project) {
    this.entry = entry;
    this.container = container;
    this.project = project;
  }

  public String getName() {
    return container.getDescription();
  }

  public Resource[] getResources() {
    List<Resource> result = new ArrayList<Resource>();

    for (Library lib : getLibs()) {
      result.add(lib.getResource());
    }

    return result.toArray(new Resource[0]);
  }

  public boolean isExported() {
    return entry.isExported();
  }

  private List<Library> getLibs() {
    List<Library> result = new ArrayList<Library>();

    for (IClasspathEntry e : container.getClasspathEntries()) {
      // todo duplication!!!! with IdeModule.getLibraries
      if (e.getEntryKind() == CPE_LIBRARY || e.getEntryKind() == CPE_VARIABLE) {
        Library l = new Library(e, project);
        result.add(l);
      }
    }

    return result;
  }
}
