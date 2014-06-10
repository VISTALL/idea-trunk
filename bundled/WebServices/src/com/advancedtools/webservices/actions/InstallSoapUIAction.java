package com.advancedtools.webservices.actions;

import com.advancedtools.webservices.utils.BaseWSAction;
import com.advancedtools.webservices.utils.SoapUI;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.project.Project;

/**
 * @author Konstantin Bulenkov
 */
public class InstallSoapUIAction extends BaseWSAction {

  @Override
  public void update(final AnActionEvent e) {
    boolean enabled = PluginManager.getPlugin(SoapUI.ID) == null;
    e.getPresentation().setEnabled(enabled);
    e.getPresentation().setVisible(enabled);
  }

  public void actionPerformed(final AnActionEvent e) {
    Project p = DataKeys.PROJECT.getData(e.getDataContext());
    SoapUI.install(p);
  }
}


