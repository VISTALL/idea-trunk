/*
 * Created on 07.07.2005
 */
package com.intellij.eclipse.export.wizard;

import com.intellij.eclipse.export.IProjectHandler;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Sergey.Grigorchuk
 */
public class ProjectHandlerDescriptor {
  public static final String PROJECT_HANDLER_EXTENSION_POINT =
    "com.intellij.eclipse.export.ideaProjectHandler";
  public static final String HANDLER_TAG = "projectHandler";
  public static final String CLASS_NAME_ATTR = "class";

  private IConfigurationElement element;

  private ProjectHandlerDescriptor(IConfigurationElement element) {
    this.element = element;
  }

  public static ProjectHandlerDescriptor[] getContributedHandlers() {
    IExtensionRegistry registry = Platform.getExtensionRegistry();
    IConfigurationElement[] elements =
      registry.getConfigurationElementsFor(PROJECT_HANDLER_EXTENSION_POINT);
    ProjectHandlerDescriptor[] hoverDescs = createDescriptors(elements);
    // initializeFromPreferences(hoverDescs);
    return hoverDescs;
  }

  private static ProjectHandlerDescriptor[] createDescriptors(IConfigurationElement[] elements) {
    List result = new ArrayList(elements.length);
    for (IConfigurationElement element : elements) {
      if (HANDLER_TAG.equals(element.getName())) {
        ProjectHandlerDescriptor desc = new ProjectHandlerDescriptor(element);
        result.add(desc);
      }
    }
    // Collections.sort(result);
    return (ProjectHandlerDescriptor[])result
      .toArray(new ProjectHandlerDescriptor[result.size()]);
  }

  public String getClassName() {
    return element.getAttribute(CLASS_NAME_ATTR);
  }

  public IProjectHandler createProjectHandler() {
    String pluginId = element.getDeclaringExtension().getNamespace();
    boolean isPlugInActivated = Platform.getBundle(pluginId).getState() == Bundle.ACTIVE;
    if (isPlugInActivated || canActivatePlugIn()) {
      try {
        return (IProjectHandler)element.createExecutableExtension(CLASS_NAME_ATTR);
      } catch (CoreException x) {
        x.printStackTrace();
        //ExporterToIdeaPlugin.log(x); //$NON-NLS-1$
      }
    }

    return null;
  }

  public boolean canActivatePlugIn() {
    return true; // Boolean.valueOf(element.getAttribute(ACTIVATE_PLUG_IN_ATTRIBUTE)).booleanValue();
  }
}
