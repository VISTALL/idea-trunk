package org.jetbrains.plugins.groovy.mvc.plugins;


import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.ColoredTableCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.util.ui.ColumnInfo;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.util.Comparator;

/**
 * User: Dmitry.Krasilschikov
 * Date: 31.08.2008
 */
public class MvcPluginColumnInfo extends ColumnInfo<MvcPluginDescriptor, String> {

  public static final String[] COLUMNS = {"Name", "Version", "Title",};

  private final Module myModule;
  private final int columnIdx;
  private AvailablePluginsModel myModel;

  public MvcPluginColumnInfo(Module module, int columnIdx) {
    //First colomn is 'installed'
    super(COLUMNS[columnIdx - 1]);
    myModule = module;
    this.columnIdx = columnIdx;
  }

  public void setModel(final AvailablePluginsModel model) {
    myModel = model;
  }

  public String valueOf(MvcPluginDescriptor mvcPluginDescriptor) {
    if (columnIdx == AvailablePluginsModel.COLUMN_NAME) {
      return mvcPluginDescriptor.getName();
    }
    else if (columnIdx == AvailablePluginsModel.COLUMN_VERSION) {
      return mvcPluginDescriptor.getRelease();
    }
    if (columnIdx == AvailablePluginsModel.COLUMN_TITLE) {
      return mvcPluginDescriptor.getTitle();
    }
    else {
      return "";
    }
  }

  public Comparator<MvcPluginDescriptor> getComparator() {

    switch (columnIdx) {
      case AvailablePluginsModel.COLUMN_NAME:
        return new Comparator<MvcPluginDescriptor>() {
          public int compare(MvcPluginDescriptor o1, MvcPluginDescriptor o2) {
            return compareStrings(o1.getName(), o2.getName());
          }
        };

      case AvailablePluginsModel.COLUMN_VERSION:
        return new Comparator<MvcPluginDescriptor>() {
          public int compare(MvcPluginDescriptor o1, MvcPluginDescriptor o2) {
            return compareStrings(o1.getRelease(), o2.getRelease());
          }
        };

      case AvailablePluginsModel.COLUMN_TITLE:
        return new Comparator<MvcPluginDescriptor>() {
          public int compare(MvcPluginDescriptor o1, MvcPluginDescriptor o2) {
            return compareStrings(o1.getTitle(), o2.getTitle());
          }
        };

      default:
        return new Comparator<MvcPluginDescriptor>() {
          public int compare(MvcPluginDescriptor o, MvcPluginDescriptor o1) {
            return 0;
          }
        };
    }
  }

  public static int compareStrings(String str1, String str2) {
    if (str1 == null && str2 == null) {
      return 0;
    }
    else if (str1 == null) {
      return -1;
    }
    else if (str2 == null) {
      return 1;
    }
    else {
      return str1.compareToIgnoreCase(str2);
    }
  }

  public Module getModule() {
    return myModule;
  }

  public TableCellRenderer getRenderer(MvcPluginDescriptor o) {
    return new MvcPluginCellRenderer(myModel);
  }

  public Class getColumnClass() {
    //For all columns class is 'String'
    return String.class;
  }

  private static class MvcPluginCellRenderer extends ColoredTableCellRenderer {
    private final AvailablePluginsModel myModel;

    private MvcPluginCellRenderer(final AvailablePluginsModel model) {
      myModel = model;
    }

    protected void customizeCellRenderer(final JTable table,
                                         final Object value,
                                         final boolean selected,
                                         final boolean hasFocus,
                                         final int row,
                                         final int column) {
      Object object = ((MvcPluginsTable)table).getPluginAt(row);
      assert object instanceof MvcPluginDescriptor;

      final MvcPluginDescriptor mvcPluginDescriptor = (MvcPluginDescriptor)object;


      if (column == AvailablePluginsModel.COLUMN_NAME) {
        setIcon(IconLoader.getIcon("/nodes/pluginnotinstalled.png"));

        appendRenderedText(mvcPluginDescriptor, mvcPluginDescriptor.getName());
      }
      else if (column == AvailablePluginsModel.COLUMN_TITLE) {
        appendRenderedText(mvcPluginDescriptor, mvcPluginDescriptor.getTitle());
      }
      else if (column == AvailablePluginsModel.COLUMN_VERSION) {
        appendRenderedText(mvcPluginDescriptor, mvcPluginDescriptor.getRelease());
      }
    }

    private void appendRenderedText(MvcPluginDescriptor mvcPluginDescriptor, String text) {
      if (text == null) text = "";

      final MvcPluginIsInstalledColumnInfo isInstalledColumnInfo =
        (MvcPluginIsInstalledColumnInfo)myModel.getColumnInfos()[AvailablePluginsModel.COLUMN_IS_INSTALLED];

      if (isInstalledColumnInfo.getToRemovePlugins().contains(mvcPluginDescriptor)) {
        SimpleTextAttributes deleteSimpleTextAttributes = new SimpleTextAttributes(SimpleTextAttributes.STYLE_PLAIN, MvcPluginUtil.COLOR_REMOVE_PLUGIN);
        append(text, deleteSimpleTextAttributes);
      }
      else if (isInstalledColumnInfo.getToInstallPlugins().contains(mvcPluginDescriptor)) {
        SimpleTextAttributes installSimpleTextAttributes = new SimpleTextAttributes(SimpleTextAttributes.STYLE_PLAIN, MvcPluginUtil.COLOR_INSTALL_PLUGIN);
        append(text, installSimpleTextAttributes);
      }
      else {
        append(text, SimpleTextAttributes.SIMPLE_CELL_ATTRIBUTES);
      }
    }
  }

  @Override
  public int getWidth(JTable table) {
    if (columnIdx == AvailablePluginsModel.COLUMN_NAME) {
      return 150;
    }
    else if (columnIdx == AvailablePluginsModel.COLUMN_VERSION) {
      return 20;
    }
    else if (columnIdx == AvailablePluginsModel.COLUMN_TITLE) {
      return 150;
    }

    return -1;
  }
}
