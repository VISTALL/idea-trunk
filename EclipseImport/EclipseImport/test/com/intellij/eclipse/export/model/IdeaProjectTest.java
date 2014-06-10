package com.intellij.eclipse.export.model;

import static org.easymock.EasyMock.*;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.JavaCore;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class IdeaProjectTest extends PluginTestCase {
  @Test
  public void testFileName() {
    ideaProject.setName("exportedProjectName");
    assertEquals("exportedProjectName.ipr", ideaProject.getFileName());
  }

  @Test
  public void testRelativePath() {
    setEclipseWorkspaceLocation("c:/workspace");
    setEclipseProjectLocation("c:/workspace/project/");

    ideaProject.setName("exportedProjectName");

    assertEquals(new File("project/exportedProjectName.ipr"),
                 ideaProject.getRelativeFile());
  }

  @Test
  public void testModules() {
    IdeaProject project = new IdeaProject(null);
    assertTrue(project.getModules().isEmpty());

    IProject project1 = createMock(IProject.class);
    IProject project2 = createMock(IProject.class);

    replay(project1, project2);

    project = new IdeaProject(null, project1, project2);

    List<IdeaModule> result = project.getModules();
    assertEquals(2, result.size());

    assertSame(project1, result.get(0).getEclipseProject());
    assertSame(project2, result.get(1).getEclipseProject());
  }

  @Test
  public void testJdkAndLanguageLevel() {
    setEclipseWorkspaceVMAndCompilerLevel(createVMInstall("jdk.name"), JavaCore.VERSION_1_4);

    assertEquals("jdk.name", ideaProject.getJdkName());
    assertEquals(LanguageLevel.JDK_1_4, ideaProject.getLanguageLevel());
  }

  @Test
  public void testNoJdk() {
    ideaProject.setEclipseVMInstall(null);
    assertEquals(null, ideaProject.getJdkName());
  }

  @Test
  public void testProjectLibraries() {
    ModuleLibrary lib11 = createModuleLibrary("lib1");
    ModuleLibrary lib12 = createModuleLibrary("lib2");
    ModuleLibrary lib21 = createModuleLibrary("lib2");
    ModuleLibrary lib22 = createModuleLibrary("lib3");

    ideaProject.setModules(createModuleWithModulesLibraries(lib11, lib12),
                           createModuleWithModulesLibraries(lib21, lib22));

    List<String> names = new ArrayList<String>();

    for (ProjectLibrary lib : ideaProject.getProjectLibraries()) {
      names.add(lib.getName());
    }

    assertElements(names, "lib1", "lib2", "lib3");
  }

  @Test
  public void testMergingProjectLibrariesContent() {
    ModuleLibrary lib1 = createModuleLibrary("lib1", "c:/file1", "c:/file2");
    ModuleLibrary lib2 = createModuleLibrary("lib1", "c:/file2", "c:/file3");

    ideaProject.setModules(createModuleWithModulesLibraries(lib1),
                           createModuleWithModulesLibraries(lib2));

    List<ProjectLibrary> result = ideaProject.getProjectLibraries();
    assertEquals(1, result.size());

    assertElements(result.get(0).getResources(),
                   (Object[])asResources("c:/file1", "c:/file2", "c:/file3"));
  }

  @Test
  public void testCollectingPathVariables() {
    ideaProject.setModules(createModulePathVariables("VAR_1", "VAR_2"),
                           createModulePathVariables("VAR_2", "VAR_3"));

    assertElements(ideaProject.getPathVariables(), "VAR_1", "VAR_2", "VAR_3");
  }

  private IdeaModule createModuleWithModulesLibraries(final ModuleLibrary... libs) {
    return new IdeaModule(null, null) {
      @Override
      public List<ModuleLibrary> getModuleLibraries() {
        return Arrays.asList(libs);
      }
    };
  }

  private IdeaModule createModulePathVariables(final String... vars) {
    return new IdeaModule(null, null) {
      @Override
      public List<String> getPathVariables() {
        return Arrays.asList(vars);
      }
    };
  }
}
