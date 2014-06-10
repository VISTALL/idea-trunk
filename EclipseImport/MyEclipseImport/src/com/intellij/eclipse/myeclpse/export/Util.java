/*
 * Created on 11.07.2005
 */
package com.intellij.eclipse.myeclpse.export;

import com.genuitec.eclipse.j2eedt.core.EARProject;
import com.genuitec.eclipse.j2eedt.core.EJBProject;
import com.genuitec.eclipse.j2eedt.core.IWebProject;
import com.genuitec.eclipse.j2eedt.core.J2EECore;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Sergey.Grigorchuk
 */
public class Util implements Constants {

  private Util() {

  }

  public static boolean isEJBProject(IProject project) {
    boolean res = false;
    try {
      res = project.getDescription().hasNature(MY_ECLIPSE_EJB_NATURE);
      if (res) {
        J2EECore j2eeCore = J2EECore.getDefault();
        EJBProject ejbProject = j2eeCore.getEJBProject(project.getName());
        res = ejbProject != null;
      }
    } catch (CoreException ce) {
      //ExporterToIdeaPlugin.log(ce);
    }
    return res;
  }

  public static boolean isWEBProject(IProject project) {
    boolean res = false;
    try {
      res = project.getDescription().hasNature(MY_ECLIPSE_WEB_NATURE);
      if (res) {
        J2EECore j2eeCore = J2EECore.getDefault();
        IWebProject webProject = j2eeCore.getWebProject(project.getName());
        res = webProject != null;
      }
    } catch (CoreException ce) {
      //ExporterToIdeaPlugin.log(ce);
    }
    return res;
  }

  public static boolean isEARProject(IProject project) {
    boolean res = false;
    try {
      res = project.getDescription().hasNature(MY_ECLIPSE_EAR_NATURE);
      if (res) {
        J2EECore j2eeCore = J2EECore.getDefault();
        EARProject earProject = j2eeCore.getEARProject(project.getName());
        res = earProject != null;
      }
    } catch (CoreException ce) {
      //ExporterToIdeaPlugin.log(ce);
    }
    return res;
  }
}
