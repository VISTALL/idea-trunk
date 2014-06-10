package org.jetbrains.plugins.groovy.mvc.plugins;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogBuilder;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.groovy.mvc.MvcConsole;
import org.jetbrains.plugins.groovy.mvc.MvcFramework;
import org.jetbrains.plugins.groovy.mvc.MvcModuleStructureSynchronizer;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * User: Dmitry.Krasilschikov
 * Date: 31.08.2008
 */
public class MvcPluginsAction extends AnAction implements DumbAware {

  public void actionPerformed(final AnActionEvent e) {
    final Module module = DataKeys.MODULE.getData(e.getDataContext());
    assert module != null;
    final Project project = module.getProject();

    Runnable runnable = new Runnable() {
      public void run() {
        final DialogBuilder dialogBuilder = new DialogBuilder(project);

        dialogBuilder.addOkAction().setText("&Apply changes");

        final MvcPluginManager manager = MvcPluginManager.getInstance(module);

        final MvcPluginsMain mvcPluginsMain = new MvcPluginsMain(module, dialogBuilder, manager);
        final AvailablePluginsModel tableModel = mvcPluginsMain.getPluginTable().getModel();

        final MvcPluginIsInstalledColumnInfo pluginIsInstalledColumnInfo =
          (MvcPluginIsInstalledColumnInfo)tableModel.getColumnInfos()[AvailablePluginsModel.COLUMN_IS_INSTALLED];

        dialogBuilder.setOkActionEnabled(false);
        dialogBuilder.addCancelAction();
        dialogBuilder.setTitle(manager.getFramework().getDisplayName() + " plugins");


        dialogBuilder.setCenterPanel(mvcPluginsMain.getMainPanel());

        dialogBuilder.setOkOperation(new Runnable() {
          public void run() {

            final Map<String, String> pluginNameToPath = mvcPluginsMain.getPluginNameToPath();

            final Set<MvcPluginDescriptor> toRemovePlugins = pluginIsInstalledColumnInfo.getToRemovePlugins();
            final Set<MvcPluginDescriptor> toInstallPlugins = pluginIsInstalledColumnInfo.getToInstallPlugins();
            final Set<MvcPluginDescriptor> toInstallCustomPlugins = new HashSet<MvcPluginDescriptor>();
            final Set<MvcPluginDescriptor> serverInstallPlugins = new HashSet<MvcPluginDescriptor>();
            for (MvcPluginDescriptor toInstallPlugin : toInstallPlugins) {
              if (!pluginNameToPath.containsKey(toInstallPlugin.getName())) {
                serverInstallPlugins.add(toInstallPlugin);
              } else {
                toInstallCustomPlugins.add(toInstallPlugin);
              }
            }


            if (toInstallPlugins.size() == 0 && toRemovePlugins.size() == 0) {
              final StatusBar statusBar = WindowManager.getInstance().getStatusBar(project);
              if (statusBar != null) {
                statusBar.setInfo("There are no plugins to install or uninstall");
              }
              return;
            }


            Set<MvcPluginDescriptor> toInstallServerPlugins = Collections.emptySet();
            if (serverInstallPlugins.size() != 0) {
              toInstallServerPlugins = downloadVersions(serverInstallPlugins, manager);

              if (toInstallServerPlugins.size() == 0) {
                Messages.showWarningDialog(project, "Error while getting plugin information",
                                           "Can't determine plugin version");
                return;
              }
            }

            InstallUninstallPluginsDialog installUninstallPluginsDialog =
              new InstallUninstallPluginsDialog(toInstallServerPlugins, toInstallCustomPlugins, toRemovePlugins, mvcPluginsMain.getModule(),
                                                mvcPluginsMain.getPluginNameToPath(), manager.getFramework());

            installUninstallPluginsDialog.show();
            if (DialogWrapper.OK_EXIT_CODE == installUninstallPluginsDialog.getExitCode()) {
              dialogBuilder.getDialogWrapper().close(DialogWrapper.OK_EXIT_CODE);
              installUninstallPluginsDialog.doInstallRemove();
            }
          }
        });

        dialogBuilder.show();
      }
    };

    MvcConsole.getInstance(project).show(runnable, false);
  }

  @NotNull
  private static Set<MvcPluginDescriptor> downloadVersions(final Set<MvcPluginDescriptor> toInstallPlugins, MvcPluginManager manager) {
    final Set<MvcPluginDescriptor> updatedPlugins = new HashSet<MvcPluginDescriptor>();
    Set<String> res = new HashSet<String>();
    for (MvcPluginDescriptor toInstallPlugin : toInstallPlugins) {
      res.add(toInstallPlugin.getName());
    }

    final Map<String, MvcPluginDescriptor> infos = manager.loadAllPluginsInfo(res);

    for (String pluginName : res) {
      MvcPluginDescriptor serverPlugin = infos.get(pluginName);

      if (serverPlugin == null) {
        return Collections.emptySet();
      }
      if (serverPlugin.getRelease() == null &&
          serverPlugin.getVersions().isEmpty() &&
          serverPlugin.getZipRelease() == null) {
        return Collections.emptySet();
      }

      updatedPlugins.add(serverPlugin);
    }
    return updatedPlugins;
  }

  @Override
  public void update(final AnActionEvent event) {
    final Presentation presentation = event.getPresentation();
    final DataContext context = event.getDataContext();
    Module module = (Module)context.getData(DataKeys.MODULE.getName());
    final MvcFramework framework = MvcModuleStructureSynchronizer.getFramework(module);
    final boolean enabled = framework != null;
    presentation.setEnabled(enabled);
    presentation.setVisible(enabled);
    if (enabled) {
      presentation.setText(framework.getDisplayName() + " plugins");
    }
  }
}
