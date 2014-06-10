package com.advancedtools.webservices.index;

import com.intellij.openapi.vfs.VirtualFileFilter;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;

/**
 * @by Konstantin Bulenkov
 */
public class ProjectBasedFileFilter implements VirtualFileFilter {
  private final Project project;

  public ProjectBasedFileFilter(Project project) {
    this.project = project;
  }

  public boolean accept(final VirtualFile file) {
    return ProjectRootManager.getInstance(project).getFileIndex().isInContent(file);
  }
}
