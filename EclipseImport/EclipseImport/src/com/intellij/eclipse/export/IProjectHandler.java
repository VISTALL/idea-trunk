package com.intellij.eclipse.export;

import com.intellij.eclipse.export.model.IdeaProject;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

import java.io.IOException;

public interface IProjectHandler {
  boolean canBeExported(IProject project);

  void configureIdeaProject(IdeaProject ideaProject, IProject[] projects)
    throws CoreException, IOException;
}
