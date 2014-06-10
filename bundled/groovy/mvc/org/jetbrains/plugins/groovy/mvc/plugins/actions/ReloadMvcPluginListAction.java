package org.jetbrains.plugins.groovy.mvc.plugins.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;
import org.jetbrains.plugins.grails.GrailsIcons;
import org.jetbrains.plugins.groovy.mvc.plugins.MvcPluginsMain;
import org.jetbrains.plugins.groovy.mvc.plugins.MvcPluginManager;
import org.jetbrains.plugins.groovy.mvc.plugins.MvcPluginDescriptor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * User: Dmitry.Krasilschikov
 * Date: 02.09.2008
 */
public class ReloadMvcPluginListAction extends AnAction implements DumbAware {
  private final MvcPluginsMain myMvcPluginsMain;

  public ReloadMvcPluginListAction(final MvcPluginsMain mvcPluginsMain) {
    super("Reload plugin list", "Reload plugin list", GrailsIcons.GRAILS_PLUGINS_REFRESH);
    myMvcPluginsMain = mvcPluginsMain;
  }

  public void actionPerformed(final AnActionEvent e) {
    final MvcPluginManager manager = myMvcPluginsMain.getManager();
    manager.reloadAvailablePlugins();
    final Map<String, MvcPluginDescriptor> pluginMap = manager.getAvailablePlugins();

    List<MvcPluginDescriptor> res = new ArrayList<MvcPluginDescriptor>();
    res.addAll(pluginMap.values());
    myMvcPluginsMain.getPluginTable().getModel().modifyData(res);

    myMvcPluginsMain.getFilter().setFilter("");
  }
}