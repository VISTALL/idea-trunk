package com.intellij.eclipse.export.model.stubs;

import com.intellij.eclipse.export.model.IdeaModule;
import com.intellij.eclipse.export.model.IdeaProject;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.launching.IVMInstall;

import java.util.HashMap;
import java.util.Map;

public class TestIdeaModule extends IdeaModule {
  private EclipseJavaProjectStub eclipseJavaProject;
  private Map<IPath, IClasspathContainer> classpathContainers
    = new HashMap<IPath, IClasspathContainer>();

  private IVMInstall vm;
  private String compilerLevel;

  public TestIdeaModule(IdeaProject ideaProject, EclipseWorkspaceStub workspace) {
    super(ideaProject, createEclipseProject(workspace));
    eclipseJavaProject = new EclipseJavaProjectStub();
  }

  private static IProject createEclipseProject(EclipseWorkspaceStub workspace) {
    return new EclipseProjectStub(workspace);
  }

  @Override
  public EclipseProjectStub getEclipseProject() {
    return (EclipseProjectStub)super.getEclipseProject();
  }

  @Override
  public EclipseJavaProjectStub getEclipseJavaProject() {
    return eclipseJavaProject;
  }

  @Override
  public IClasspathContainer getEclipseProjectClasspathContainer(IPath path) {
    return classpathContainers.get(path);
  }

  public void addEclipseProjectClasspathContainer(IPath path,
                                                  IClasspathContainer container) {
    classpathContainers.put(path, container);
  }

  @Override
  protected IVMInstall getEclipseVMInstall() {
    return vm;
  }

  public void setEclipseVMInstall(IVMInstall vm) {
    this.vm = vm;
  }

  @Override
  protected String getEclipseCompilerLevel() {
    return compilerLevel;
  }

  public void setEclipseCompilerLevel(String l) {
    compilerLevel = l;
  }
}
