/*
 * Created on 05.07.2005
 */
package com.intellij.eclipse.myeclpse.export;

import com.genuitec.eclipse.j2eedt.core.EJBProject;
import com.genuitec.eclipse.j2eedt.core.J2EECore;
import com.intellij.eclipse.export.IProjectHandler;
import com.intellij.eclipse.export.IdeaProjectFileConstants;
import com.intellij.eclipse.export.ResourceUtil;
import com.intellij.eclipse.export.model.IdeaModule;
import com.intellij.eclipse.export.model.IdeaProject;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Sergey.Grigorchuk
 */
public class MyEclipseEjbProjectHandler implements IProjectHandler, Constants {

  /* (non-Javadoc)
         * @see com.intellij.eclipse.export.IProjectHandler#canExported(org.eclipse.core.resources.IProject)
         */
  public boolean canBeExported(IProject project) {
    return Util.isEJBProject(project);
  }

  /* (non-Javadoc)
         * @see com.intellij.eclipse.export.IProjectHandler#buildIdeaProject(com.intellij.eclipse.export.model.IdeaProject, org.eclipse.core.resources.IProject[])
         */
  public void configureIdeaProject(IdeaProject ideaProject, IProject[] projects)
    throws CoreException {
    for (IProject project : projects) {
      IdeaModule ideaModule = ideaProject.getModuleFor(project);
      if (ideaModule != null) {
        if (canBeExported(project))
          configureEJBModule(ideaModule, project);
      }
    }
  }

  protected void configureEJBModule(IdeaModule ideaModule, IProject project) {
    J2EECore j2eeCore = J2EECore.getDefault();
    EJBProject ejbProject = j2eeCore.getEJBProject(project.getName());
    if (ejbProject != null) {
      ideaModule.setProperty(IdeaProjectFileConstants.DEPLOYMENT_DESCRIPTOR_URL_PROPERTY,
                             ResourceUtil.getFullPath(
                               ejbProject.getDeploymentDescriptorFile().getFullPath(),
                               project));
      ideaModule.setType(IdeaModule.MT_J2EE_EJB);
    }
  }
}
