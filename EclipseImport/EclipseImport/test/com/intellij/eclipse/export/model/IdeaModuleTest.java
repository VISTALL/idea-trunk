package com.intellij.eclipse.export.model;

import static org.easymock.EasyMock.*;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.jdt.core.IClasspathContainer;
import static org.eclipse.jdt.core.IClasspathContainer.*;
import static org.eclipse.jdt.core.IClasspathEntry.*;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.IVMInstall;
import org.junit.Test;

import java.io.File;
import java.util.List;
import java.net.URL;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URI;

public class IdeaModuleTest extends PluginTestCase {
  @Test
  public void testName() {
    setEclipseProjectName("name");
    assertEquals("name", ideaModule.getName());
  }

  @Test
  public void testFileName() {
    setEclipseWorkspaceLocation("c:/workspace");
    setEclipseProjectName("name");
    setEclipseProjectLocation("c:/workspace/folder");

    assertEquals(new File("folder/name.iml"), ideaModule.getRelativeFile());
  }

  @Test
  public void testProjectPathJustInWorspace() {
    setEclipseWorkspaceLocation("c:/workspace");
    setEclipseProjectLocation("c:/workspace/project");

    assertEquals(new File("project"), ideaModule.getRelativeDirectory());
  }

  @Test
  public void testProjectPathDeepInWorspace() {
    setEclipseWorkspaceLocation("c:/workspace");
    setEclipseProjectLocation("c:/workspace/folder/project");

    assertEquals(new File("folder/project"), ideaModule.getRelativeDirectory());
  }

  @Test
  public void testProjectPathOutsideWorspace() {
    setEclipseWorkspaceLocation("c:/workspace");
    setEclipseProjectLocation("c:/folder/project");

    assertEquals(new File("project"), ideaModule.getRelativeDirectory());
  }

  @Test
  public void testProjectPathWithSchemaSpecified() {
    setEclipseWorkspaceLocation("file://c:/workspace");
    setEclipseProjectLocation("file://c:/workspace/project");

    assertEquals(new File("project"), ideaModule.getRelativeDirectory());
  }

  @Test
  public void testProjectPathWithSlashesAtEnd() {
    setEclipseWorkspaceLocation("c:/workspace/");
    setEclipseProjectLocation("c:/workspace/project/");

    assertEquals(new File("project/"), ideaModule.getRelativeDirectory());
  }

  @Test
  public void testMakingRelativePaths() {
    setEclipseProjectLocation("c:/workspace/project");

    assertEquals("file", ideaModule.relativize("c:/workspace/project/file"));
    assertEquals("../file", ideaModule.relativize("c:/workspace/file"));
    assertEquals("", ideaModule.relativize("c:/workspace/project"));
    assertEquals("../../workspace", ideaModule.relativize("c:/workspace"));

    assertEquals("d:/workspace/project/file",
                 ideaModule.relativize("d:/workspace/project/file"));
  }

  @Test
  public void testContentRoot() {
    setEclipseProjectLocation("c:/workspace/project");

    assertEquals("c:/workspace/project", ideaModule.getContentRoot());
  }

  @Test
  public void testSourceFolders() {
    setEclipseProjectLocation("c:/workspace/project");
    setEclipseProjectClasspaths(
      createClasspath(CPE_SOURCE, "c:/workspace/project/scr1"),
      createClasspath(CPE_SOURCE, "c:/workspace/project/folder/scr2"),
      createClasspath(CPE_SOURCE, "/project/scr3"));

    assertElements(ideaModule.getSourceLocations(),
                   "c:/workspace/project/scr1",
                   "c:/workspace/project/folder/scr2",
                   "c:/workspace/project/scr3");
  }

  @Test
  public void testLibraries() {
    addEclipseWorkspaceResource("/project/lib1.jar", "c:/workspace/project/lib1.jar");

    setEclipseProjectClasspaths(createClasspath(CPE_LIBRARY, "/project/lib1.jar"),
                                createClasspath(CPE_LIBRARY, "c:/workspace/lib2.jar"),
                                createClasspath(CPE_LIBRARY, "c:/temp/lib3.jar"),
                                createClasspath(CPE_VARIABLE, "VAR_NAME/lib4.jar"));

    List<Library> result = ideaModule.getLibraries();
    assertEquals(4, result.size());

    assertEquals(new Resource(new Path("c:/workspace/project/lib1.jar")),
                 result.get(0).getResource());
    assertTrue(result.get(0).isLocal());

    assertEquals(new Resource(new Path("c:/workspace/lib2.jar")),
                 result.get(1).getResource());
    assertFalse(result.get(1).isLocal());

    assertEquals(new Resource(new Path("c:/temp/lib3.jar")),
                 result.get(2).getResource());
    assertFalse(result.get(2).isLocal());

    assertEquals(new Resource(new Path("VAR_NAME/lib4.jar")),
                 result.get(3).getResource());
    assertFalse(result.get(3).isLocal());
    assertTrue(result.get(3).getResource().isVariable());
  }

  @Test
  public void testModuleLibraries() {
    IClasspathContainer container1
      = createClasspathContainer(K_APPLICATION,
                                 "user.library1.description",
                                 createClasspath(CPE_LIBRARY, "c:/temp/lib1.jar"),
                                 createClasspath(CPE_LIBRARY, "c:/temp/lib2.jar"));
    IClasspathContainer container2
      = createClasspathContainer(K_SYSTEM,
                                 "user.library2.description",
                                 createClasspath(CPE_LIBRARY, "c:/sys/lib1.jar"),
                                 createClasspath(CPE_LIBRARY, "c:/sys/lib2.jar"));

    setEclipseProjectClasspaths(createClasspath(CPE_CONTAINER, "user.library1.name"),
                                createClasspath(CPE_CONTAINER, "user.library2.name"));

    addEclipseProjectClasspathContainer("user.library1.name", container1);
    addEclipseProjectClasspathContainer("user.library2.name", container2);

    setEclipseWorkspaceLocation("c:/workspace");
    setEclipseProjectLocation("c:/workspace/project");

    List<ModuleLibrary> result = ideaModule.getModuleLibraries();
    assertEquals(2, result.size());

    assertEquals(asResources("c:/temp/lib1.jar", "c:/temp/lib2.jar"),
                 result.get(0).getResources());
    assertEquals("user.library1.description", result.get(0).getName());

    assertEquals(asResources("c:/sys/lib1.jar", "c:/sys/lib2.jar"),
                 result.get(1).getResources());
    assertEquals("user.library2.description", result.get(1).getName());
  }

  @Test
  public void testCollectionPathVariables() {
    setEclipseInstallationDirectory("c:/eclipse/");
    setEclipseProjectClasspaths(createClasspath(CPE_LIBRARY, "NOT_A_VARIABLE/lib1.jar"),
                                createClasspath(CPE_VARIABLE, "VAR_1/lib2.jar"),
                                createClasspath(CPE_VARIABLE, "VAR_2/lib3.jar"),
                                createClasspath(CPE_VARIABLE, "VAR_2/lib4.jar"),
                                createClasspath(CPE_LIBRARY, "c:/eclipse/plugins/lib5.jar"));

    assertElements(ideaModule.getPathVariables(), "VAR_1", "VAR_2", "ECLIPSE_HOME");
  }

  @Test
  public void testCollectionPathVariablesFromModuleLibraries() {
    setEclipseInstallationDirectory("c:/eclipse/");

    IClasspathContainer container
      = createClasspathContainer(K_SYSTEM,
                                 "user.library1.description",
                                 createClasspath(CPE_LIBRARY, "c:/temp/lib1.jar"),
                                 createClasspath(CPE_VARIABLE, "VAR_NAME/lib2.jar"),
                                 createClasspath(CPE_LIBRARY, "c:/eclipse/lib3.jar"));

    setEclipseProjectClasspaths(createClasspath(CPE_CONTAINER, "user.library1.name"));

    addEclipseProjectClasspathContainer("user.library1.name", container);

    assertElements(ideaModule.getPathVariables(), "VAR_NAME", "ECLIPSE_HOME");
  }

  @Test
  public void testJdkAndLanguageLevel() {
    setEclipseProjectVMAndCompilerLevel(createVMInstall("project jdk"),
                                        JavaCore.VERSION_1_4);

    assertEquals("project jdk", ideaModule.getJdkName());
    assertEquals(LanguageLevel.JDK_1_4, ideaModule.getLanguageLevel());
  }

  @Test
  public void testDoesNotUseDefaultJdkAndLangLevel() {
    setEclipseWorkspaceVMAndCompilerLevel(createVMInstall("project jdk"),
                                          JavaCore.VERSION_1_4);
    assertEquals(null, ideaModule.getJdkName());
    assertEquals(null, ideaModule.getLanguageLevel());
  }

  @Test
  public void testDefaultAndModuleJdksAreTheSame() {
    IVMInstall install = createVMInstall("project jdk");

    ideaProject.setEclipseVMInstall(install);
    ideaModule.setEclipseVMInstall(install);

    assertEquals(null, ideaModule.getJdkName());
  }

  @Test
  public void testReferencedModules() {
    IdeaModule anotherModule1 = createTestIdeaModule();
    IdeaModule anotherModule2 = createTestIdeaModule();

    setEclipseProjectReferencedProjects(anotherModule1.getEclipseProject(),
                                        anotherModule2.getEclipseProject());
    ideaProject.setModules(ideaModule, anotherModule1, anotherModule2);

    assertElements(ideaModule.getReferencedModules(),
                   anotherModule1,
                   anotherModule2);
  }

  @Test
  public void testUnknownReferencedModules() {
    IProject unknownProject = createMock(IProject.class);
    replay(unknownProject);

    setEclipseProjectReferencedProjects(unknownProject);
    ideaProject.setModules(ideaModule);

    assertTrue(ideaModule.getReferencedModules().isEmpty());
  }
}
