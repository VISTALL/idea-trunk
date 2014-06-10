package com.intellij.eclipse.export.model;

import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.junit.Assert;
import org.junit.Test;

public class PluginLibraryTest extends PluginTestCase {
  @Test
  public void testExporting() {
    Library lib1 = new Library(createClasspath(IClasspathEntry.CPE_LIBRARY, "lib1.jar", true),
                               ideaModule.getEclipseProject());
    Library lib2 = new Library(createClasspath(IClasspathEntry.CPE_LIBRARY, "lib2.jar", false),
                               ideaModule.getEclipseProject());

    Assert.assertTrue(lib1.isExported());
    Assert.assertFalse(lib2.isExported());
  }

  @Test
  public void testLocalAndExternalLibrary() {
    addEclipseWorkspaceResource("/project/lib.jar", "c:/workspace/project/lib.jar");

    Library lib1 =
      new Library(createClasspath(IClasspathEntry.CPE_LIBRARY, "/project/lib.jar"),
                  ideaModule.getEclipseProject());
    Assert
      .assertEquals(new Resource(new Path("c:/workspace/project/lib.jar")),
                    lib1.getResource());
    Assert.assertTrue(lib1.isLocal());

    Library lib2 =
      new Library(createClasspath(IClasspathEntry.CPE_LIBRARY, "c:/temp/anotherLib.jar"),
                  ideaModule.getEclipseProject());
    Assert.assertEquals(new Resource(new Path("c:/temp/anotherLib.jar")), lib2.getResource());
    Assert.assertFalse(lib2.isLocal());
  }

  @Test
  public void testUnbindedLibrary() {
    addEclipseWorkspaceResource("/project/lib.jar", "");

    Library lib =
      new Library(createClasspath(IClasspathEntry.CPE_LIBRARY, "/anotherProject/lib.jar"),
                  ideaModule.getEclipseProject());
    Assert.assertEquals(new Resource(new Path("/anotherProject/lib.jar")), lib.getResource());
    Assert.assertFalse(lib.isLocal());
  }

  @Test
  public void testVariablePathLibrary() {
    setEclipseWorkspaceResolverdVariablePath("VAR_NAME/lib.jar", "c:/folder/lib.jar");
    Library lib =
      new Library(createClasspath(IClasspathEntry.CPE_VARIABLE, "VAR_NAME/lib.jar"),
                  ideaModule.getEclipseProject());

    Assert.assertEquals("c:/folder/lib.jar", lib.getResource().getAbsolutePath());
    Assert.assertEquals("$VAR_NAME$/lib.jar", lib.getResource().getVariablePath());
  }

  @Test
  public void testUnresolvedVariablePathLibrary() {
    Library lib =
      new Library(createClasspath(IClasspathEntry.CPE_VARIABLE, "VAR_NAME/lib.jar"),
                  ideaModule.getEclipseProject());

    Assert.assertEquals("$VAR_NAME$/lib.jar", lib.getResource().getVariablePath());
  }

  @Test
  public void testDetectingVariablePaths() {
    Library lib1 = new Library(createClasspath(IClasspathEntry.CPE_VARIABLE, "abc"),
                               ideaModule.getEclipseProject());
    Library lib2 = new Library(createClasspath(IClasspathEntry.CPE_LIBRARY, "abc"),
                               ideaModule.getEclipseProject());

    Assert.assertTrue(lib1.getResource().isVariable());
    Assert.assertFalse(lib2.getResource().isVariable());
  }
}
