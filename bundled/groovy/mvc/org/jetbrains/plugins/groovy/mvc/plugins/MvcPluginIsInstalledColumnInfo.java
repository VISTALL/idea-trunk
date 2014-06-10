package org.jetbrains.plugins.groovy.mvc.plugins;

import com.intellij.ui.BooleanTableCellEditor;
import com.intellij.ui.BooleanTableCellRenderer;
import com.intellij.util.ui.ColumnInfo;
import org.jetbrains.plugins.groovy.mvc.plugins.AvailablePluginsModel;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.util.*;

/**
 * User: Dmitry.Krasilschikov
 * Date: 20.10.2008
 */
public class MvcPluginIsInstalledColumnInfo extends ColumnInfo<MvcPluginDescriptor, Boolean> {

  private final Set<MvcPluginDescriptor> myInstalledPlugins = new HashSet<MvcPluginDescriptor>();

  private final Set<MvcPluginDescriptor> toInstallPlugins = new HashSet<MvcPluginDescriptor>();
  private final Set<MvcPluginDescriptor> toRemovePlugins = new HashSet<MvcPluginDescriptor>();
  private final Set<String> myInitialPlugins;

  public MvcPluginIsInstalledColumnInfo(final Map<String, MvcPluginDescriptor> installedPlugins) {
    super("Enable");
    myInitialPlugins = installedPlugins.keySet();
  }

  public Boolean valueOf(MvcPluginDescriptor mvcPluginDescriptor) {
    if (toInstallPlugins.contains(mvcPluginDescriptor)) {
      return true;
    }
    else if (toRemovePlugins.contains(mvcPluginDescriptor)) {
      return false;
    }

    return myInstalledPlugins.contains(mvcPluginDescriptor);
  }

  public boolean isCellEditable(final MvcPluginDescriptor mvcPluginDescriptor) {
    return true;
  }

  public Class getColumnClass() {
    return Boolean.class;
  }

  public TableCellEditor getEditor(final MvcPluginDescriptor mvcPluginDescriptor) {
    return new BooleanTableCellEditor();
  }

  public TableCellRenderer getRenderer(final MvcPluginDescriptor mvcPluginDescriptor) {
    return new BooleanTableCellRenderer();
  }

  public void setValue(final MvcPluginDescriptor mvcPluginDescriptor, final Boolean value) {

    final String name = mvcPluginDescriptor.getName();
    if (value.booleanValue()) {
      if (!containsInstalledPlugin(name)){
        toInstallPlugins.add(mvcPluginDescriptor);
      }

      toRemovePlugins.remove(mvcPluginDescriptor);
    }
    else {
      if (containsInstalledPlugin(name)){
        toRemovePlugins.add(mvcPluginDescriptor);
      }

      toInstallPlugins.remove(mvcPluginDescriptor);
    }
  }

  public Comparator<MvcPluginDescriptor> getComparator() {
    final boolean sortDirection = true;
    return new Comparator<MvcPluginDescriptor>() {
      public int compare(final MvcPluginDescriptor mvcPlugin1Descriptor, final MvcPluginDescriptor mvcPlugin2Descriptor) {
        if (isPluginInstalled(mvcPlugin1Descriptor)) {
          if (isPluginInstalled(mvcPlugin2Descriptor)) {
            return 0;
          }
          return sortDirection ? -1 : 1;
        }
        else {
          if (!isPluginInstalled(mvcPlugin2Descriptor)) {
            return 0;
          }
          return sortDirection ? 1 : -1;
        }
      }
    };
  }

  private boolean isPluginInstalled(MvcPluginDescriptor plugin) {
    return myInitialPlugins.contains(plugin.getName());
  }

  @Override
  public int getWidth(final JTable table) {
    return 10;
  }

  public Set<MvcPluginDescriptor> getToInstallPlugins() {
    return toInstallPlugins;
  }

  public Set<MvcPluginDescriptor> getToRemovePlugins() {
    return toRemovePlugins;
  }

  public void configureColumn(final AvailablePluginsModel availablePluginsModel) {

    final Collection<MvcPluginDescriptor> mvcPluginListDescriptor = availablePluginsModel.getPluginsMap().values();

    for (MvcPluginDescriptor mvcPluginDescriptor : mvcPluginListDescriptor) {
      if (isPluginInstalled(mvcPluginDescriptor)) {
        myInstalledPlugins.add(mvcPluginDescriptor);
      }
    }
  }

  private boolean containsInstalledPlugin(String name) {
    for (MvcPluginDescriptor installedPlugin : myInstalledPlugins) {
      if (name.equals(installedPlugin.getName())) {
        return true;
      }
    }
    return false;
  }
}
