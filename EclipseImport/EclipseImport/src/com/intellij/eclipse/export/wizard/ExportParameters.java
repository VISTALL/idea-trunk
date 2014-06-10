package com.intellij.eclipse.export.wizard;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;

import java.util.ArrayList;
import java.util.List;

public class ExportParameters {
  public static final String INVALID_OUTPUT_DIRECTORY = "Output directory not specified";
  public static final String INVALID_PROJECT_NAME = "Project name not specified";
  public static final String INVALID_PROJECTS_TO_EXPORT = "Projects to export not specified";

  private String outputDirectory = "";
  private String projectName = "";

  private List<IProject> projectsToExport;
  private boolean shouldCopyContent = false;
  private boolean shouldUsePathVariables = false;
  private boolean shouldDeclareLibraries = true;

  public ExportParameters() {
    projectsToExport = getProjects();
  }

  public String getOutputDirectory() {
    return outputDirectory;
  }

  public void setOutputDirectory(String dir) {
    outputDirectory = dir;
  }

  public String getProjectName() {
    return projectName;
  }

  public void setProjectName(String name) {
    projectName = name;
  }

  public List<IProject> getProjects() {
    IWorkspace workspace = ResourcesPlugin.getWorkspace();

    List<IProject> result = new ArrayList<IProject>();
    for (IProject project : workspace.getRoot().getProjects()) {
      if (BuilderUtil.isExportable(project)) {
        result.add(project);
      }
    }
    return result;
  }

  public List<IProject> getProjectsToExport() {
    return projectsToExport;
  }

  public void setProjectsToExport(List<IProject> projectsToExport) {
    this.projectsToExport = projectsToExport;
  }

  public boolean shouldCopyContent() {
    return shouldCopyContent;
  }

  public void setShouldCopyContent(boolean should) {
    shouldCopyContent = should;
  }

  public boolean shouldUsePathVariables() {
    return shouldUsePathVariables;
  }

  public void setShouldUsePathVariables(boolean should) {
    shouldUsePathVariables = should;
  }

  public boolean shouldDeclareLibraries() {
    return shouldDeclareLibraries;
  }

  public void setShouldDeclareLibraries(boolean should) {
    shouldDeclareLibraries = should;
  }

  public boolean isValid() {
    return getErrorMessage() == null;
  }

  public String getErrorMessage() {
    String result = "";

    if (outputDirectory.length() == 0) result += INVALID_OUTPUT_DIRECTORY + "\n";
    if (projectName.length() == 0) result += INVALID_PROJECT_NAME + "\n";
    if (projectsToExport.isEmpty()) result += INVALID_PROJECTS_TO_EXPORT + "\n";

    return result.length() == 0 ? null : result.substring(0, result.length() - 1);
  }
}
