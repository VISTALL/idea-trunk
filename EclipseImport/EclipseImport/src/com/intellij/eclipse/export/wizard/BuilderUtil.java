package com.intellij.eclipse.export.wizard;

import com.intellij.eclipse.export.IProjectHandler;
import com.intellij.eclipse.export.IdeaProjectFileConstants;
import com.intellij.eclipse.export.model.IdeaProject;
import com.intellij.eclipse.export.model.codestyle.AbstractCodeStyleParam;
import com.intellij.eclipse.export.model.codestyle.CodeStyle;
import com.intellij.eclipse.export.model.codestyle.EclipseToIdeaCodeStyleMapping;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jdt.core.JavaCore;

import java.io.IOException;

public class BuilderUtil implements IdeaProjectFileConstants {
  public static IdeaProject buildIdeaProject(String name, IProject... projects)
    throws IOException, CoreException {
    IdeaProject ideaProject = new IdeaProject(name, projects);

    configureCodeStyle(ideaProject);
    for (ProjectHandlerDescriptor d : ProjectHandlerDescriptor.getContributedHandlers()) {
      IProjectHandler handler = d.createProjectHandler();
      if (handler != null) handler.configureIdeaProject(ideaProject, projects);
    }

    return ideaProject;
  }

  public static boolean isExportable(IProject project) {
    if (!project.isOpen()) return false;

    boolean res = isJavaProjectNature(project);

    ProjectHandlerDescriptor[] descriptors = ProjectHandlerDescriptor.getContributedHandlers();
    for (int i = 0; i < descriptors.length && !res; i++) {
      ProjectHandlerDescriptor descriptor = descriptors[i];
      IProjectHandler projectHandler = descriptor.createProjectHandler();
      if (projectHandler != null) {
        res = projectHandler.canBeExported(project);
      }
    }
    return res;
  }

  private static boolean isJavaProjectNature(IProject project) {
    try {
      return project.getDescription().hasNature(JavaCore.NATURE_ID);
    } catch (CoreException e) {
      throw new RuntimeException(e);
    }
  }

  private static void configureCodeStyle(IdeaProject ideaProject) {
    CodeStyle codeStyle = new CodeStyle();
    ideaProject.setCodeStyle(codeStyle);
    Preferences store = JavaCore.getPlugin().getPluginPreferences();

    EclipseToIdeaCodeStyleMapping codeStyleMapping =
      EclipseToIdeaCodeStyleMapping.getInstance();
    for (String eclipseParam : codeStyleMapping.getEclipseParamNames()) {

      String ideaParam = codeStyleMapping.eclipseToIdeaKeyName(eclipseParam);
      String value = store.getString(eclipseParam);
      AbstractCodeStyleParam codeStyleParam = codeStyle.getOrCreateParam(ideaParam, value);
    }
    codeStyle.getOrCreateParam(USE_PER_PROJECT_SETTINGS_TAG, "true");
  }
}