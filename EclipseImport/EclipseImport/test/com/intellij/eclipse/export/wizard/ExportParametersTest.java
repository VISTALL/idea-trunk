package com.intellij.eclipse.export.wizard;

import com.intellij.eclipse.export.model.stubs.EclipseProjectStub;
import static com.intellij.eclipse.export.wizard.ExportParameters.*;
import org.eclipse.core.resources.IProject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ExportParametersTest extends Assert {
  private ExportParameters params;

  @Before
  public void setUp() {
    IProject p = new EclipseProjectStub(null);
    final List<IProject> projects = Collections.singletonList(p);

    params = new ExportParameters() {
      @Override
      public List<IProject> getProjects() {
        return projects;
      }
    };
  }

  @Test
  public void testAllProjectsAreSelected() {
    assertEquals(params.getProjectsToExport(), params.getProjects());
  }

  @Test
  public void testDefault() {
    assertFalse(params.isValid());
    assertEquals(INVALID_OUTPUT_DIRECTORY + "\n" + INVALID_PROJECT_NAME,
                 params.getErrorMessage());
  }

  @Test
  public void testEmpty() {
    params.setOutputDirectory("");
    params.setProjectName("");
    params.setProjectsToExport(new ArrayList<IProject>());

    assertFalse(params.isValid());
    assertEquals(INVALID_OUTPUT_DIRECTORY + "\n"
                 + INVALID_PROJECT_NAME + "\n"
                 + INVALID_PROJECTS_TO_EXPORT, params.getErrorMessage());
  }

  @Test
  public void testEmptyOutputDirectory() {
    params.setOutputDirectory("");
    params.setProjectName("name");
    params.setProjectsToExport(params.getProjects());

    assertFalse(params.isValid());
    assertEquals(INVALID_OUTPUT_DIRECTORY, params.getErrorMessage());
  }

  @Test
  public void testEmptyProjectName() {
    params.setOutputDirectory("output.dir");
    params.setProjectName("");
    params.setProjectsToExport(params.getProjects());

    assertFalse(params.isValid());
    assertEquals(INVALID_PROJECT_NAME, params.getErrorMessage());
  }

  @Test
  public void testEmptyProjectsToExport() {
    params.setOutputDirectory("output.dir");
    params.setProjectName("name");
    params.setProjectsToExport(new ArrayList<IProject>());

    assertFalse(params.isValid());
    assertEquals(INVALID_PROJECTS_TO_EXPORT, params.getErrorMessage());
  }
}
