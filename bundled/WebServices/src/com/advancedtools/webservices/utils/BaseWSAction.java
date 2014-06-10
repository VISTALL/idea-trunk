package com.advancedtools.webservices.utils;

import com.advancedtools.webservices.environmentFacade.EnvironmentFacade;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;

/**
 * @author maxim
 * Date: 21.01.2006
 */
public abstract class BaseWSAction extends AnAction {
  public void update(AnActionEvent e) {
    Project project = e.getData(PlatformDataKeys.PROJECT);
    e.getPresentation().setEnabled(
      EnvironmentFacade.getInstance() != null
      && project != null
    );
  }
}
