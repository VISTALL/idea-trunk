package com.intellij.eclipse.export.model;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class ProjectLibraryBuilderTest extends PluginTestCase {
  private ProjectLibraryBuilder builder;

  @Before
  public void setUp() {
    builder = new ProjectLibraryBuilder();
  }

  @Test
  public void testBuildingEmpty() {
    assertTrue(getBuildingResultFor().isEmpty());
  }

  @Test
  public void testBuilding() {
    List<ProjectLibrary> result = getBuildingResultFor(createModuleLibrary("lib1", "path1"),
                                                       createModuleLibrary("lib2", "path2"));

    assertEquals(2, result.size());
    assertEquals("lib1", result.get(0).getName());
    assertElements(result.get(0).getResources(),
                   (Object[])asResources("path1"));

    assertEquals("lib2", result.get(1).getName());
    assertElements(result.get(1).getResources(),
                   (Object[])asResources("path2"));
  }

  @Test
  public void testBuildingEmptyLibrary() {
    List<ProjectLibrary> result = getBuildingResultFor(createModuleLibrary("lib"));
    assertTrue(result.get(0).getResources().isEmpty());
  }

  @Test
  public void testBuildingWithSameName() {
    List<ProjectLibrary> result = getBuildingResultFor(createModuleLibrary("lib1"),
                                                       createModuleLibrary("lib1"));

    assertEquals(1, result.size());
    assertEquals("lib1", result.get(0).getName());
  }

  @Test
  public void testBuildingWithSameNameAndDifferentContent() {
    ModuleLibrary lib1 = createModuleLibrary("lib1", "path1", "path2");
    ModuleLibrary lib2 = createModuleLibrary("lib1", "path2", "path3");

    List<ProjectLibrary> result = getBuildingResultFor(lib1, lib2);

    assertEquals(1, result.size());
    assertEquals("lib1", result.get(0).getName());

    assertElements(result.get(0).getResources(),
                   (Object[])asResources("path1", "path2", "path3"));
  }

  private List<ProjectLibrary> getBuildingResultFor(ModuleLibrary... libs) {
    for (ModuleLibrary lib : libs) {
      builder.addModuleLibrary(lib);
    }
    return builder.getProjectLibraries();
  }
}
