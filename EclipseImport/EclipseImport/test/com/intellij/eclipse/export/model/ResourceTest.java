package com.intellij.eclipse.export.model;

import org.eclipse.core.runtime.Path;
import org.junit.Test;

public class ResourceTest extends PluginTestCase {
  @Test
  public void testEquality() {
    Resource abc = new Resource(new Path("abc"));

    assertEquals(abc, new Resource(new Path("abc")));
    assertEquals(abc.hashCode(), new Resource(new Path("abc")).hashCode());

    assertFalse(abc.equals(new Resource(new Path("cba"))));
    assertFalse(abc.hashCode() == new Resource(new Path("cba")).hashCode());

    assertFalse(abc.equals(null));
    assertFalse(abc.equals(new Object()));
  }

  @Test
  public void testAbsolutePath() {
    assertEquals("abc", new Resource(new Path("abc")).getAbsolutePath());
    assertEquals("", new Resource(new Path("")).getAbsolutePath());
  }

  @Test
  public void testAbsolutePathForVariables() {
    setEclipseWorkspaceResolverdVariablePath("VAR_NAME/abc", "c:/folder/abc");

    assertEquals("c:/folder/abc",
                 new Resource(new Path("VAR_NAME/abc"), true).getAbsolutePath());

    assertEquals("UNKNOWN_VAR_NAME/abc",
                 new Resource(new Path("UNKNOWN_VAR_NAME/abc"), true).getAbsolutePath());

    assertEquals("", new Resource(new Path(""), true).getAbsolutePath());
  }

  @Test
  public void testVariablePath() {
    assertEquals("$VAR_NAME$/abc",
                 new Resource(new Path("VAR_NAME/abc"), true).getVariablePath());

    assertEquals("c:/abc", new Resource(new Path("c:/abc"), false).getVariablePath());
    assertEquals("", new Resource(new Path(""), true).getVariablePath());
  }

  @Test
  public void testVariableName() {
    assertEquals("VAR_NAME",
                 new Resource(new Path("VAR_NAME/abc"), true).getVariableName());
  }

  @Test
  public void testEclipseLibrary() {
    setEclipseInstallationDirectory("c:/eclipse");

    assertTrue(new Resource(new Path("c:/eclipse/lib.jar")).isEclipseLibrary());

    assertFalse(new Resource(new Path("d:/eclipse/lib.jar")).isEclipseLibrary());
    assertFalse(new Resource(new Path("c:/abc")).isEclipseLibrary());

    Resource res = new Resource(new Path("c:/eclipse/plugins/lib.jar"));

    assertTrue(res.isEclipseLibrary());
    assertTrue(res.isVariable());

    assertEquals("ECLIPSE_HOME", res.getVariableName());

    assertEquals("c:/eclipse/plugins/lib.jar", res.getAbsolutePath());
    assertEquals("$ECLIPSE_HOME$/plugins/lib.jar", res.getVariablePath());

  }

  @Test
  public void testEmptyEclipseLibrary() {
    eclipseServices.setEclipseInstallationLocation(null);

    Resource res = new Resource(new Path("c:/eclipse/plugins/lib.jar"));

    assertFalse(res.isEclipseLibrary());
    assertEquals("c:/eclipse/plugins/lib.jar", res.getAbsolutePath());
    assertEquals("c:/eclipse/plugins/lib.jar", res.getVariablePath());
  }
}
