package com.intellij.eclipse.export.model;

import com.intellij.eclipse.export.IdeaProjectFileConstants;
import com.intellij.eclipse.export.model.codestyle.CodeStyle;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;

import java.io.File;
import java.util.*;

public class IdeaProject {
  private String name;
  private IProject[] eclipseProjects;
  private CodeStyle codeStyle;

  public IdeaProject(String name, IProject... projects) {
    this.name = name;
    this.eclipseProjects = projects;
  }

  public String getName() {
    return name;
  }

  public String getFileName() {
    return getName() + "." + IdeaProjectFileConstants.IDEA_PROJECT_FILE_EXT;
  }

  public File getRelativeFile() {
    return new File(getModules().get(0).getRelativeDirectory(), getFileName());
  }

  public List<IdeaModule> getModules() {
    List<IdeaModule> result = new ArrayList<IdeaModule>();
    for (IProject p : eclipseProjects) {
      result.add(new IdeaModule(this, p));
    }
    return result;
  }

  public IdeaModule getModuleFor(IProject project) {
    for (IdeaModule ideaModule : getModules()) {
      if (project.equals(ideaModule.getEclipseProject())) return ideaModule;
    }
    return null;
  }

  public String getJdkName() {
    IVMInstall vm = getEclipseVMInstall();
    return vm == null ? null : vm.getName();
  }

  public LanguageLevel getLanguageLevel() {
    return LanguageLevel.fromEclipseCompilerLevel(getEclipseCompilerLevel());
  }

  public List<ProjectLibrary> getProjectLibraries() {
    ProjectLibraryBuilder builder = new ProjectLibraryBuilder();

    for (IdeaModule module : getModules()) {
      for (ModuleLibrary lib : module.getModuleLibraries()) {
        builder.addModuleLibrary(lib);
      }
    }

    return builder.getProjectLibraries();
  }

  public Collection<String> getPathVariables() {
    Set<String> result = new HashSet<String>();

    for (IdeaModule module : getModules()) {
      result.addAll(module.getPathVariables());
    }

    return result;
  }

  public CodeStyle getCodeStyle() {
    return codeStyle;
  }

  public void setCodeStyle(CodeStyle codeStyle) {
    this.codeStyle = codeStyle;
  }

  protected IVMInstall getEclipseVMInstall() {
    return JavaRuntime.getDefaultVMInstall();
  }

  protected String getEclipseCompilerLevel() {
    return JavaCore.getOption(JavaCore.COMPILER_COMPLIANCE);
  }
}