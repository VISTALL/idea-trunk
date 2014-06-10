package com.intellij.eclipse.export.model;

import org.eclipse.core.runtime.Path;
import static org.eclipse.jdt.core.IClasspathEntry.*;
import org.junit.Test;

public class LibraryTest extends PluginTestCase {
  @Test
  public void testExporting() {

    Library lib1 = new Library(createClasspath(CPE_LIBRARY, "lib1.jar", true),
                               ideaModule.getEclipseProject());
    Library lib2 = new Library(createClasspath(CPE_LIBRARY, "lib2.jar", false),
                               ideaModule.getEclipseProject());

    assertTrue(lib1.isExported());
    assertFalse(lib2.isExported());
  }

  @Test
  public void testLocalAndExternalLibrary() {
    addEclipseWorkspaceResource("/project/lib.jar", "c:/workspace/project/lib.jar");

    Library lib1 = new Library(createClasspath(CPE_LIBRARY, "/project/lib.jar"),
                               ideaModule.getEclipseProject());
    assertEquals(new Resource(new Path("c:/workspace/project/lib.jar")), lib1.getResource());
    assertTrue(lib1.isLocal());

    Library lib2 = new Library(createClasspath(CPE_LIBRARY, "c:/temp/anotherLib.jar"),
                               ideaModule.getEclipseProject());
    assertEquals(new Resource(new Path("c:/temp/anotherLib.jar")), lib2.getResource());
    assertFalse(lib2.isLocal());
  }

  @Test
  public void testUnbindedLibrary() {
    addEclipseWorkspaceResource("/project/lib.jar", "");

    Library lib = new Library(createClasspath(CPE_LIBRARY, "/anotherProject/lib.jar"),
                              ideaModule.getEclipseProject());
    assertEquals(new Resource(new Path("/anotherProject/lib.jar")), lib.getResource());
    assertFalse(lib.isLocal());
  }

  @Test
  public void testVariablePathLibrary() {
    setEclipseWorkspaceResolverdVariablePath("VAR_NAME/lib.jar", "c:/folder/lib.jar");
    Library lib = new Library(createClasspath(CPE_VARIABLE, "VAR_NAME/lib.jar"),
                              ideaModule.getEclipseProject());

    assertEquals("c:/folder/lib.jar", lib.getResource().getAbsolutePath());
    assertEquals("$VAR_NAME$/lib.jar", lib.getResource().getVariablePath());
  }

  @Test
  public void testUnresolvedVariablePathLibrary() {
    Library lib = new Library(createClasspath(CPE_VARIABLE, "VAR_NAME/lib.jar"),
                              ideaModule.getEclipseProject());

    assertEquals("$VAR_NAME$/lib.jar", lib.getResource().getVariablePath());
  }

  @Test
  public void testDetectingVariablePaths() {
    Library lib1 = new Library(createClasspath(CPE_VARIABLE, "abc"),
                               ideaModule.getEclipseProject());
    Library lib2 = new Library(createClasspath(CPE_LIBRARY, "abc"),
                               ideaModule.getEclipseProject());

    assertTrue(lib1.getResource().isVariable());
    assertFalse(lib2.getResource().isVariable());
  }

  @Test
  public void testEclipseLibrary() {
    setEclipseInstallationDirectory("c:/eclipse");

    Library lib = new Library(createClasspath(CPE_LIBRARY, "c:/eclipse/plugins/lib.jar"),
                              ideaModule.getEclipseProject());

    assertFalse(lib.isLocal());

    Resource res = lib.getResource();

    assertTrue(res.isVariable());
    assertEquals("$ECLIPSE_HOME$/plugins/lib.jar", res.getVariablePath());
    assertEquals("ECLIPSE_HOME", res.getVariableName());

    assertEquals("c:/eclipse/plugins/lib.jar", res.getAbsolutePath());
  }
}
