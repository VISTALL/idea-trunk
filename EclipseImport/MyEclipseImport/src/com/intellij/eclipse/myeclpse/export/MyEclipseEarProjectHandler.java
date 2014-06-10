/*
 * Created on 08.07.2005
 */
package com.intellij.eclipse.myeclpse.export;

import com.genuitec.eclipse.j2eedt.core.IEARProject;
import com.genuitec.eclipse.j2eedt.core.J2EECore;
import com.intellij.eclipse.export.IProjectHandler;
import com.intellij.eclipse.export.IdeaProjectFileConstants;
import com.intellij.eclipse.export.ResourceUtil;
import com.intellij.eclipse.export.model.IdeaModule;
import com.intellij.eclipse.export.model.IdeaProject;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

import java.io.IOException;

/**
 * @author Sergey.Grigorchuk
 */
public class MyEclipseEarProjectHandler implements IProjectHandler, Constants {

  /* (non-Javadoc)
         * @see com.intellij.eclipse.export.IProjectHandler#canBeExported(org.eclipse.core.resources.IProject)
         */
  public boolean canBeExported(IProject project) {
    return Util.isEARProject(project);
  }

  /* (non-Javadoc)
         * @see com.intellij.eclipse.export.IProjectHandler#buildIdeaProject(com.intellij.eclipse.export.model.IdeaProject, org.eclipse.core.resources.IProject[])
         */
  public void configureIdeaProject(IdeaProject ideaProject, IProject[] projects)
    throws CoreException, IOException {
    for (IProject project : projects) {
      IdeaModule ideaModule = ideaProject.getModuleFor(project);
      if (ideaModule != null) {
        if (canBeExported(project))
          configureEARModule(ideaModule, project);
      }
    }
  }

  protected void configureEARModule(IdeaModule ideaModule, IProject project) {
    J2EECore j2eeCore = J2EECore.getDefault();
    IEARProject earProject = j2eeCore.getEARProject(project.getName());
    if (earProject != null) {
      ideaModule.setProperty(IdeaProjectFileConstants.DEPLOYMENT_DESCRIPTOR_URL_PROPERTY,
                             ResourceUtil.getFullPath(
                               earProject.getDeploymentDescriptorFile().getFullPath(),
                               project));
      ideaModule.setType(IdeaModule.MT_J2EE_EAR);
    }
  }

  /*
        protected void configureDependencies(IdeaProject ideaProject,
                        IProject[] projects,) throws CoreException {
                for (int i = 0; i < projects.length; i++) {
                        IProject project = projects[i];
                        if(Util.isEJBProject(project)) {

                        } else if(Util.isWEBProject(project)) {

                        }
                        IdeaModule ideaModule = ideaProject.getModuleByName(project
                                        .getName());
                        if (ideaModule != null) {
                                IProject[] refProjects = project.getReferencedProjects();
                                for (int j = 0; j < refProjects.length; j++) {
                                        IProject refProject = refProjects[j];
                                        IdeaModule refModule = ideaProject
                                                        .getModuleByName(refProject.getName());
                                        if (refModule != null) {
                                                ideaModule.addReferencedModule(refModule);
                                        }
                                }
                        }
                }
        }
        //*/
}
