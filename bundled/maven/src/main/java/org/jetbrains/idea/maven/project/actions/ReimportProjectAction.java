package org.jetbrains.idea.maven.project.actions;

import org.jetbrains.idea.maven.project.MavenProjectsManager;
import org.jetbrains.idea.maven.project.MavenProject;

import java.util.List;

public class ReimportProjectAction extends MavenProjectsAction {
  protected void perform(MavenProjectsManager manager, List<MavenProject> mavenProjects) {
    manager.forceUpdateProjects(mavenProjects);
  }
}