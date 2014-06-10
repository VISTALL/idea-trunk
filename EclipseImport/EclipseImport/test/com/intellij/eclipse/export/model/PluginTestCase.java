package com.intellij.eclipse.export.model;

import com.intellij.eclipse.export.model.stubs.EclipseServicesImplStub;
import com.intellij.eclipse.export.model.stubs.EclipseWorkspaceStub;
import com.intellij.eclipse.export.model.stubs.TestIdeaModule;
import com.intellij.eclipse.export.model.stubs.TestIdeaProject;
import static org.easymock.EasyMock.*;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.launching.IVMInstall;
import org.junit.Assert;
import org.junit.Before;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class PluginTestCase extends Assert {
  protected EclipseServicesImplStub eclipseServices;
  private EclipseWorkspaceStub eclipseWorkspace;

  protected TestIdeaProject ideaProject;
  protected TestIdeaModule ideaModule;

  @Before
  public void setUp() {
    createEclipseWorkspace();
    createEclipseServices();
    createTestIdeaProject();
  }

  private void createEclipseWorkspace() {
    eclipseWorkspace = new EclipseWorkspaceStub();
  }

  private void createEclipseServices() {
    eclipseServices = new EclipseServicesImplStub();

    EclipseServices.setImpl(eclipseServices);
  }

  private void createTestIdeaProject() {
    ideaProject = new TestIdeaProject();
    ideaModule = createTestIdeaModule();

    ideaProject.setModules(ideaModule);
  }

  protected TestIdeaModule createTestIdeaModule() {
    return new TestIdeaModule(ideaProject, eclipseWorkspace);
  }

  protected void setEclipseInstallationDirectory(String dir) {
    eclipseServices.setEclipseInstallationLocation(new Path(dir));
  }

  protected void setEclipseWorkspaceLocation(String s) {
    eclipseWorkspace.getRoot().setLocation(new Path(s));
  }

  protected void addEclipseWorkspaceResource(String path, String location) {
    IFile file = createMock(IFile.class);
    expect(file.getLocation()).andReturn(new Path(location));
    replay(file);

    eclipseWorkspace.getRoot().addFile(new Path(path), file);
  }

  protected void setEclipseWorkspaceVMAndCompilerLevel(IVMInstall install, String level) {
    ideaProject.setEclipseVMInstall(install);
    ideaProject.setEclipseCompilerLevel(level);
  }

  protected void setEclipseWorkspaceResolverdVariablePath(String variablePath,
                                                          String resolvedPath) {
    eclipseServices.addResolvedVariablePath(new Path(variablePath),
                                            new Path(resolvedPath));
  }

  protected void setEclipseProjectName(String s) {
    ideaModule.getEclipseProject().setName(s);
  }

  protected void setEclipseProjectLocation(String s) {
    ideaModule.getEclipseProject().setLocation(new Path(s));
  }

  protected void setEclipseProjectReferencedProjects(IProject... projects) {
    ideaModule.getEclipseProject().setReferencedProjects(projects);
  }

  protected void setEclipseProjectClasspaths(IClasspathEntry... entries) {
    ideaModule.getEclipseJavaProject().setRawClasspath(entries);
  }

  protected void addEclipseProjectClasspathContainer(String path,
                                                     IClasspathContainer container) {
    ideaModule.addEclipseProjectClasspathContainer(new Path(path), container);
  }

  protected void setEclipseProjectVMAndCompilerLevel(IVMInstall install, String level) {
    ideaModule.setEclipseVMInstall(install);
    ideaModule.setEclipseCompilerLevel(level);
  }

  protected IVMInstall createVMInstall(String name) {
    IVMInstall mock = createMock(IVMInstall.class);
    expect(mock.getName()).andStubReturn(name);
    replay(mock);
    return mock;
  }

  protected IClasspathEntry createClasspath(int type, String location) {
    return createClasspath(type, location, false);
  }

  protected IClasspathEntry createClasspath(int type, String location, boolean isExported) {
    IClasspathEntry mock = createMock(IClasspathEntry.class);

    expect(mock.getEntryKind()).andStubReturn(type);
    expect(mock.getPath()).andStubReturn(new Path(location));
    expect(mock.isExported()).andStubReturn(isExported);

    replay(mock);

    return mock;
  }

  protected IClasspathContainer createClasspathContainer(int type,
                                                         String description,
                                                         IClasspathEntry... entries) {
    IClasspathContainer mock = createMock(IClasspathContainer.class);

    expect(mock.getKind()).andStubReturn(type);
    expect(mock.getDescription()).andStubReturn(description);
    expect(mock.getClasspathEntries()).andStubReturn(entries);

    replay(mock);

    return mock;
  }

  protected ModuleLibrary createModuleLibrary(final String name,
                                              final String... paths) {
    return new ModuleLibrary(null, createClasspathContainer(0, ""), null) {
      @Override
      public String getName() { return name; }

      @Override
      public Resource[] getResources() {
        List<Resource> result = new ArrayList<Resource>();
        for (String path : paths) {
          result.add(new Resource(new Path(path)));
        }
        return result.toArray(new Resource[0]);
      }
    };
  }

  protected Resource[] asResources(String... paths) {
    List<Resource> result = new ArrayList<Resource>();
    for (String path : paths) {
      result.add(new Resource(new Path(path)));
    }
    return result.toArray(new Resource[0]);
  }

  protected void assertElements(Collection actual, Object... expected) {
    assertEquals(expected.length, actual.size());
    for (Object o : expected) {
      assertTrue(actual.contains(o));
    }
  }
}
