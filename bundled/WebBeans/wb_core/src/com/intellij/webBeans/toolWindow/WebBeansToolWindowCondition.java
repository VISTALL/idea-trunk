package com.intellij.webBeans.toolWindow;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Condition;
import com.intellij.webBeans.utils.WebBeansCommonUtils;

public class WebBeansToolWindowCondition implements Condition<Project> {

  public boolean value(Project project) {
    for (Module module : ModuleManager.getInstance(project).getModules()) {
      if (WebBeansCommonUtils.isWebBeansFacetDefined(module)) {
        return true;
      }
    }
    return false;
  }
}
