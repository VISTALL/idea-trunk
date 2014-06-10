package org.jetbrains.plugins.groovy.mvc.plugins.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDialog;
import com.intellij.openapi.fileChooser.FileChooserFactory;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.plugins.groovy.mvc.plugins.MvcPluginsMain;
import org.jetbrains.plugins.groovy.mvc.plugins.AvailablePluginsModel;
import org.jetbrains.plugins.groovy.mvc.plugins.MvcPluginDescriptor;
import org.jetbrains.plugins.groovy.mvc.plugins.MvcPluginIsInstalledColumnInfo;

/**
 * User: Dmitry.Krasilschikov
 * Date: 20.10.2008
 */
public class AddCustomPluginAction extends AnAction implements DumbAware {
  private final MvcPluginsMain myMvcPluginsMain;
  private String myPathToPlugin;

  public AddCustomPluginAction(final MvcPluginsMain mvcPluginsMain) {
    super("Add custom plugin", "Add custom plugin", IconLoader.getIcon("/general/add.png"));
    myMvcPluginsMain = mvcPluginsMain;
  }

  public void actionPerformed(final AnActionEvent e) {
    final Module module = myMvcPluginsMain.getModule();

    final FileChooserDescriptor descriptor = new FileChooserDescriptor(false, false, true, false, false, false);

    final Project project = module.getProject();
    final FileChooserDialog fileChooserDialog = FileChooserFactory.getInstance().createFileChooser(descriptor, project);
    final VirtualFile[] files = fileChooserDialog.choose(null, project);

    if (files.length > 0) {
      myPathToPlugin = files[0].getPath();

      MvcPluginDescriptor plugin = myMvcPluginsMain.getManager().extractPluginInfo(myPathToPlugin);
      if (plugin == null) return;
      myMvcPluginsMain.putCustomPluginToPath(plugin.getName(), myPathToPlugin);

      final AvailablePluginsModel tableModel = myMvcPluginsMain.getPluginTable().getModel();
      tableModel.addData(plugin);

      final MvcPluginIsInstalledColumnInfo pluginIsInstalledColumnInfo =
        (MvcPluginIsInstalledColumnInfo) tableModel.getColumnInfos()[AvailablePluginsModel.COLUMN_IS_INSTALLED];

      myMvcPluginsMain.getDialogBuilder().setOkActionEnabled(true);
      pluginIsInstalledColumnInfo.getToInstallPlugins().add(plugin);
    }

    myMvcPluginsMain.getFilter().setFilter("");
  }


}
