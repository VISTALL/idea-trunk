package com.intellij.eclipse.export.model;

import static org.eclipse.jdt.core.IClasspathContainer.*;
import org.eclipse.jdt.core.IClasspathEntry;
import static org.eclipse.jdt.core.IClasspathEntry.*;
import org.junit.Test;

public class ModuleLibraryTest extends PluginTestCase {
  @Test
  public void testExporting() {

    ModuleLibrary lib1
      = new ModuleLibrary(createClasspath(CPE_CONTAINER, "path1", true),
                          createClasspathContainer(K_APPLICATION, "name1"),
                          ideaModule.getEclipseProject());

    ModuleLibrary lib2
      = new ModuleLibrary(createClasspath(CPE_CONTAINER, "path2", false),
                          createClasspathContainer(K_APPLICATION, "name2"),
                          ideaModule.getEclipseProject());

    assertTrue(lib1.isExported());
    assertFalse(lib2.isExported());
  }

  @Test
  public void testComposingResources() {

    IClasspathEntry e1 = createClasspath(CPE_LIBRARY, "lib1");
    IClasspathEntry e2 = createClasspath(CPE_LIBRARY, "lib2");

    ModuleLibrary lib
      = new ModuleLibrary(createClasspath(CPE_CONTAINER, "path"),
                          createClasspathContainer(K_APPLICATION, "name", e1, e2),
                          ideaModule.getEclipseProject());

    assertEquals("name", lib.getName());
    assertEquals(asResources("lib1", "lib2"), lib.getResources());
  }
}
