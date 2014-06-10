package org.jetbrains.plugins.groovy.mvc.plugins.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.util.IconLoader;
import org.jetbrains.plugins.groovy.mvc.plugins.MvcPluginsMain;
import org.jetbrains.plugins.groovy.mvc.plugins.MvcPluginManager;
import org.jetbrains.plugins.groovy.mvc.plugins.AvailablePluginsModel;
import org.jetbrains.plugins.groovy.mvc.plugins.MvcPluginDescriptor;

import java.util.Arrays;
import java.util.Map;

/**
 * User: Dmitry.Krasilschikov
 * Date: 04.09.2008
 */
public class DownloadAllMvcPluginInfosAction extends AnAction implements DumbAware {
  private final MvcPluginsMain myMvcPluginsMain;

  public DownloadAllMvcPluginInfosAction(final MvcPluginsMain mvcPluginsMain) {
    super("Download information about all available plugins", "Download information about all available plugins",
          IconLoader.getIcon("/actions/get.png"));
    myMvcPluginsMain = mvcPluginsMain;
  }

  public void actionPerformed(final AnActionEvent e) {
    final MvcPluginManager pluginManager = myMvcPluginsMain.getManager();
    final Map<String, MvcPluginDescriptor> availablePlugins = pluginManager.getAvailablePlugins();
    if (availablePlugins == null) {
      return;
    }

    final Map<String, MvcPluginDescriptor> fullPluginsInfo = pluginManager.loadAllPluginsInfo(availablePlugins.keySet());

    AvailablePluginsModel model = myMvcPluginsMain.getPluginTable().getModel();
    if (fullPluginsInfo == null) {
      model.clearData();
    } else {
      model.modifyData(Arrays.asList(fullPluginsInfo.values().toArray(MvcPluginDescriptor.EMPTY_ARRAY)));
    }

    myMvcPluginsMain.getFilter().setFilter("");
  }
}
