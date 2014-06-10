package org.jetbrains.plugins.groovy.mvc.plugins.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.grails.GrailsIcons;
import org.jetbrains.plugins.groovy.mvc.plugins.MvcPluginsMain;
import org.jetbrains.plugins.groovy.mvc.plugins.MvcPluginDescriptor;

/**
 * User: Dmitry.Krasilschikov
 * Date: 04.09.2008
 */
public class DownloadSinglePluginInfoAction extends AnAction implements DumbAware {
  private final MvcPluginsMain myMvcPluginsMain;

  public DownloadSinglePluginInfoAction(@NotNull final MvcPluginsMain mvcPluginsMain) {
    super("Download plugin information", "Download plugin information", GrailsIcons.GRAILS_PLUGIN_INFO_DOWNLOAD);
    myMvcPluginsMain = mvcPluginsMain;
  }

  public void actionPerformed(final AnActionEvent e) {
    final MvcPluginDescriptor selectedObject = myMvcPluginsMain.getPluginTable().getSelectedObject();
    assert selectedObject != null;
    final String pluginName = selectedObject.getName();
    assert pluginName != null;
    myMvcPluginsMain.getSinglePluginInfo(pluginName);
  }

  @Override
  public void update(AnActionEvent e) {
    e.getPresentation().setEnabled(myMvcPluginsMain.getPluginTable().getSelectedObject() != null);
  }
}
