package org.jetbrains.plugins.groovy.mvc.plugins;

import com.intellij.lang.properties.psi.PropertiesFile;
import com.intellij.lang.properties.psi.Property;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.plugins.groovy.mvc.MvcConsole;
import org.jetbrains.plugins.groovy.mvc.MvcFramework;
import org.jetbrains.plugins.groovy.mvc.MvcLoadingPanel;
import org.jetbrains.plugins.groovy.mvc.MvcModuleStructureUtil;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;

/**
 * User: Dmitry.Krasilschikov
 * Date: 27.10.2008
 */
public class InstallUninstallPluginsDialog extends DialogWrapper {
  private static final Logger LOG = Logger.getInstance("#org.jetbrains.plugins.groovy.mvc.plugins.InstallUninstallPluginsDialog");
  private final JPanel myMainPanel;

  private final Set<MvcPluginDescriptor> myToInstallServerPlugins;
  private final Set<MvcPluginDescriptor> myToRemovePlugins;

  @NonNls private static final String INSTALL_PLUGIN = "install-plugin";
  private final Module myModule;

  private JPanel myInstallPluginsPanel;
  private JPanel myRemovePluginsPanel;

  MvcLoadingPanel myLoadingPanel;
  private final Map<String, JComponent> myVersionsMap;
  @NonNls private static final String RELEASE = "(release)";
  @NonNls private static final String ZIP_RELEASE = "(zip release)";
  private final Set<MvcPluginDescriptor> myToInstallCustomPlugins;
  private final HashSet<MvcPluginDescriptor> myAllToInstallPlugins;
  @NonNls private static final String UNINSTALL_PLUGIN = "uninstall-plugin";
  private final Map<String,String> myPluginNameToPath;
  private final MvcFramework myFramework;

  public InstallUninstallPluginsDialog(final Set<MvcPluginDescriptor> toInstallServerPlugins,
                                       final Set<MvcPluginDescriptor> toInstallCustomPlugins,
                                       final Set<MvcPluginDescriptor> toRemovePlugins, final Module module,
                                       final Map<String, String> pluginNameToPath, final MvcFramework framework) {
    super(module.getProject(), false);

    myToInstallServerPlugins = toInstallServerPlugins;
    myToInstallCustomPlugins = toInstallCustomPlugins;
    myToRemovePlugins = toRemovePlugins;

    myAllToInstallPlugins = new HashSet<MvcPluginDescriptor>();
    myAllToInstallPlugins.addAll(toInstallServerPlugins);
    myAllToInstallPlugins.addAll(toInstallCustomPlugins);

    myModule = module;


    myLoadingPanel = new MvcLoadingPanel();


    setTitle("Install/Uninstall plugins");

    myMainPanel = new JPanel();
    myMainPanel.setLayout(new BorderLayout());
    myVersionsMap = new HashMap<String, JComponent>();

    if (myToRemovePlugins.size() > 0) {
      myRemovePluginsPanel = new JPanel(new GridLayout(-1, 1));
      configureRemovePanel(toRemovePlugins);
      myMainPanel.add(myRemovePluginsPanel, BorderLayout.NORTH);
    }

    myMainPanel.add(new JLabel(" "), BorderLayout.CENTER);

    if (myAllToInstallPlugins.size() > 0) {
      myInstallPluginsPanel = new JPanel(new BorderLayout());
      configureInstallPanel();
      myMainPanel.add(myInstallPluginsPanel, BorderLayout.SOUTH);
    }

    init();

    myPluginNameToPath = pluginNameToPath;
    myFramework = framework;
  }

  private void configureRemovePanel(final Set<MvcPluginDescriptor> toRemovePlugins) {
    JLabel textLabel = new JLabel();
    textLabel.setText("Plugins to remove:");
    myRemovePluginsPanel.add(textLabel);

    JLabel pluginLabel;
    for (MvcPluginDescriptor toRemovePlugin : toRemovePlugins) {
      pluginLabel = createBoldLabel();
      pluginLabel.setForeground(MvcPluginUtil.COLOR_REMOVE_PLUGIN);
      pluginLabel.setText(toRemovePlugin.getName());
      myRemovePluginsPanel.add(pluginLabel);
    }
  }

  private void configureInstallPanel() {
    JLabel label = new JLabel();
    label.setText("Plugins to install:");
    myInstallPluginsPanel.add(label, BorderLayout.NORTH);
    final JPanel versionsPanel = new JPanel(new GridLayout(-1, 2));

    JComponent component;
    //Labels and versions here
    for (MvcPluginDescriptor toInstallPlugin : myAllToInstallPlugins) {
      JLabel pluginNameLabel = createBoldLabel();
      pluginNameLabel.setForeground(MvcPluginUtil.COLOR_INSTALL_PLUGIN);

      pluginNameLabel.setText(toInstallPlugin.getName());
      versionsPanel.add(pluginNameLabel);

      component = createVersionsComponent(toInstallPlugin);
      myVersionsMap.put(toInstallPlugin.getName(), component);
      versionsPanel.add(component);
    }
    myInstallPluginsPanel.add(versionsPanel, BorderLayout.CENTER);
  }

  private static JLabel createBoldLabel() {
    JLabel label = new JLabel();
    Font f = label.getFont();


    label.setFont(f.deriveFont(f.getStyle() | Font.BOLD));
    return label;
  }

  private static JComponent createVersionsComponent(MvcPluginDescriptor mvcPluginDescriptor) {
    JComboBox comboBox = new JComboBox();

    final Ref<Pair<String, Boolean>> versionToRelease = new Ref<Pair<String, Boolean>>();
    final Ref<Pair<String, Boolean>> versionToZipRelease = new Ref<Pair<String, Boolean>>();

    String release = mvcPluginDescriptor.getRelease();
    String zipRelease = mvcPluginDescriptor.getZipRelease();

    LinkedHashSet<String> versions = mvcPluginDescriptor.getVersions();

    if (release != null) {
      versions.add(release);
      versionToRelease.set(new Pair<String, Boolean>(release, true));
    }

    if (zipRelease != null) {
      versions.add(zipRelease);
      versionToZipRelease.set(new Pair<String, Boolean>(zipRelease, true));
    }

    if (versions.size() == 0) {
      JLabel label = new JLabel();

      label.setForeground(Color.RED);
      label.setText("No version");

      return label;
    }

    //editable and adding versions
    for (String version : versions) {
      comboBox.addItem(version);
    }

    //selection
    if (release != null) {
      comboBox.setSelectedItem(release);
    }
    else if (zipRelease != null) {
      comboBox.setSelectedItem(zipRelease);
    }

    comboBox.setRenderer(new DefaultListCellRenderer() {

      public Component getListCellRendererComponent(final JList list,
                                                    final Object value,
                                                    final int index,
                                                    final boolean isSelected,
                                                    final boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        assert value != null;
        assert value instanceof String;
        String version = ((String)value);

        final Pair<String, Boolean> releasePair = versionToRelease.get();
        if (releasePair != null && value.equals(releasePair.getFirst())) {
          setText(version + RELEASE);
        }
        else {
          final Pair<String, Boolean> zipReleasePair = versionToZipRelease.get();
          if (zipReleasePair != null && value.equals(zipReleasePair.getFirst())) {
            setText(version + ZIP_RELEASE);
          } else {
            setText(version);
          }
        }
        return this;
      }
    });


    return comboBox;

  }

  public void doInstallRemove() {
    final MvcConsole console = MvcConsole.getInstance(myModule.getProject());
    deletePlugins(myToRemovePlugins, myModule, myFramework, console);
    installCustomPlugins(myFramework, console);

    for (final MvcPluginDescriptor plugin : myToInstallServerPlugins) {
      final String pluginName = plugin.getName();
      final String version = getPluginVersion(pluginName);
      console.executeProcess(myModule, myFramework.createCommand(myModule, false, INSTALL_PLUGIN, pluginName, version), null, false);
    }
  }

  protected JComponent createCenterPanel() {
    return myMainPanel;
  }

  private void installCustomPlugins(MvcFramework framework, MvcConsole console) {
    final List<VirtualFile> tempPluginPaths = new ArrayList<VirtualFile>();

    for (MvcPluginDescriptor mvcPluginDescriptor : myToInstallCustomPlugins) {
      final String pluginPath = MvcPluginUtil.cleanPath(myPluginNameToPath.get(mvcPluginDescriptor.getName()));

      final VirtualFile pluginVirtualFile = VirtualFileManager.getInstance().findFileByUrl("file://" + pluginPath);
      assert pluginVirtualFile != null;

      final VirtualFile appDirVirtualFile = framework.findAppRoot(myModule);
      assert appDirVirtualFile != null;

      ApplicationManager.getApplication().runWriteAction(new Runnable() {
        public void run() {
          try {
            tempPluginPaths.add(VfsUtil.copyFile(this, pluginVirtualFile, appDirVirtualFile));
          }
          catch (IOException e) {
            LOG.error(e);
          }
        }
      });
    }

    for (final VirtualFile tempPluginPath : tempPluginPaths) {
      console.executeProcess(myModule, framework.createCommand(myModule, false, INSTALL_PLUGIN, tempPluginPath.getName()), new Runnable() {
        public void run() {
          ApplicationManager.getApplication().runWriteAction(new Runnable() {
          public void run() {
            try {
              LocalFileSystem.getInstance().deleteFile(this, tempPluginPath);
            }
            catch (IOException e) {
              LOG.error("Cannot delete file " + tempPluginPath.getPath(), e);
            }
          }
        });
        }
      }, false);
    }
  }

  private String getPluginVersion(String pluginName) {
    final JComponent versionComponent = myVersionsMap.get(pluginName);

    String version = "'";
    if (versionComponent != null) {
      if (versionComponent instanceof JComboBox) {
        final JComboBox checkBox = (JComboBox)versionComponent;

        final Object versionStr = checkBox.getSelectedItem();
        if (versionStr instanceof String) {
          version = (String)versionStr;
        }
      }
    }
    return version;
  }

  private void deletePlugins(final Set<MvcPluginDescriptor> toRemovePlugins, final Module module, final MvcFramework framework, MvcConsole console) {
    final Project project = module.getProject();

    final PropertiesFile properties = MvcModuleStructureUtil.findApplicationProperties(module, framework);
    final VirtualFile propVFile = properties == null ? null : properties.getViewProvider().getVirtualFile();
    final Document propDocument = properties == null ? null : PsiDocumentManager.getInstance(project).getDocument(properties);

    for (final MvcPluginDescriptor plugin : toRemovePlugins) {
      final String pluginName = plugin.getName();
      console.executeProcess(module, framework.createCommand(module, false, UNINSTALL_PLUGIN, pluginName), new Runnable() {
        public void run() {
          final Set<VirtualFile> allDirs = MvcModuleStructureUtil.getAllVisiblePluginDirectories(module, framework);
          final VirtualFile pluginDir = ContainerUtil.find(allDirs, new Condition<VirtualFile>() {
            public boolean value(VirtualFile file) {
              return pluginName.equals(file.getName());
            }
          });
          new WriteCommandAction(project) {
            protected void run(Result result) throws Throwable {
              if (properties != null) {
                propVFile.refresh(false, false);
                PsiDocumentManager.getInstance(getProject()).commitDocument(propDocument);
                
                for (Property property : properties.findPropertiesByKey("plugins." + pluginName)) {
                  property.delete();
                }
                FileDocumentManager.getInstance().saveDocument(propDocument);
              }

              if (pluginDir != null) {
                pluginDir.delete(this);
              }
            }
          }.execute();
        }
      }, false);
    }
  }

}
