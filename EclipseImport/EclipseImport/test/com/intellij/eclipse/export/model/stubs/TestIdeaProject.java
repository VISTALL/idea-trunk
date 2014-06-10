package com.intellij.eclipse.export.model.stubs;

import com.intellij.eclipse.export.model.IdeaModule;
import com.intellij.eclipse.export.model.IdeaProject;
import org.eclipse.jdt.launching.IVMInstall;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TestIdeaProject extends IdeaProject {
  private String name;
  private List<IdeaModule> modules = new ArrayList<IdeaModule>();
  private IVMInstall vm;
  private String compilerLevel;

  public TestIdeaProject() {
    super(null);
  }

  @Override
  public String getName() {
    return name;
  }

  public void setName(String n) {
    name = n;
  }

  @Override
  public List<IdeaModule> getModules() {
    return modules;
  }

  public void setModules(IdeaModule... m) {
    modules = Arrays.asList(m);
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
