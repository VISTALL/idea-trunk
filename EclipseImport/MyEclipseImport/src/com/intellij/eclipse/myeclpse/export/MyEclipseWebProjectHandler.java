/*
 * Created on 08.07.2005
 */
package com.intellij.eclipse.myeclpse.export;

import com.genuitec.eclipse.j2eedt.core.IWebProject;
import com.genuitec.eclipse.j2eedt.core.J2EECore;
import com.intellij.eclipse.export.IProjectHandler;
import com.intellij.eclipse.export.IdeaProjectFileConstants;
import com.intellij.eclipse.export.ResourceUtil;
import com.intellij.eclipse.export.model.IdeaModule;
import com.intellij.eclipse.export.model.IdeaProject;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

import java.io.IOException;

/**
 * @author Sergey.Grigorchuk
 */
public class MyEclipseWebProjectHandler implements IProjectHandler, Constants {

  /* (non-Javadoc)
         * @see com.intellij.eclipse.export.IProjectHandler#canBeExported(org.eclipse.core.resources.IProject)
         */
  public boolean canBeExported(IProject project) {
    return Util.isWEBProject(project);
  }

  public void configureIdeaProject(IdeaProject ideaProject, IProject[] projects)
    throws CoreException, IOException {
    for (IProject project : projects) {
      IdeaModule ideaModule = ideaProject.getModuleFor(project);
      if (ideaModule != null) {
        if (canBeExported(project))
          configureWebModule(ideaModule, project);
      }
    }
  }

  protected void configureWebModule(IdeaModule ideaModule, IProject project) {
    J2EECore j2eeCore = J2EECore.getDefault();
    IWebProject webProject = j2eeCore.getWebProject(project);
    if (webProject != null) {
      String deploymentPath =
        ResourceUtil
          .getFullPath(webProject.getDeploymentDescriptorFile().getFullPath(), project);
      ideaModule
        .setProperty(IdeaProjectFileConstants.DEPLOYMENT_DESCRIPTOR_URL_PROPERTY,
                     deploymentPath);
      ideaModule.setType(IdeaModule.MT_J2EE_WEB);

      IPath webContextRoot = webProject.getWebRootOSPath();
      ideaModule.setWebContextRoot(webContextRoot);
    }
  }
}
