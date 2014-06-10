package org.jetbrains.plugins.groovy.mvc.plugins;

import com.intellij.ui.TableUtil;
import com.intellij.util.ArrayUtil;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.Table;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.groovy.mvc.plugins.AvailablePluginsModel;

import javax.swing.*;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * User: Dmitry.Krasilschikov
 * Date: 31.08.2008
 */
public class MvcPluginsTable extends Table {
  public MvcPluginsTable(final AvailablePluginsModel model) {
    super(model);

    initializeHeader(model);

    for (int i = 0; i < model.getColumnCount(); i++) {
      TableColumn column = getColumnModel().getColumn(i);
      final ColumnInfo columnInfo = model.getColumnInfos()[i];
      column.setCellEditor(columnInfo.getEditor(null));
      if (i == 0 || i == 2) {
        String name = columnInfo.getName();
        if (i == 2) {
          name += name;
        }
        final int width;
        final FontMetrics fontMetrics = getFontMetrics(getFont());
        width = fontMetrics.stringWidth(" " + name + " ") + 10;

        column.setWidth(width);
        column.setPreferredWidth(width);
        column.setMaxWidth(width);
        column.setMinWidth(width);
      }
    }

    setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    setShowGrid(false);
  }

  public void setValueAt(final Object aValue, final int row, final int column) {
    super.setValueAt(aValue, row, column);
    repaint(); //in order to update invalid plugins
  }

  @Nullable
  public TableCellRenderer getCellRenderer(final int row, final int column) {
    final ColumnInfo columnInfo = getModel().getColumnInfos()[column];
    return columnInfo.getRenderer(getModel().getObjectAt(row));
  }

  private void initializeHeader(final AvailablePluginsModel model) {
    final JTableHeader header = getTableHeader();

    header.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        int column = getTableHeader().getColumnModel().getColumnIndexAtX(e.getX());

        if (model.getSortedColumnIndex() != column) {
          model.sortByColumn(column);
        }

        final MvcPluginDescriptor[] selectedObjects = getSelectedObjects();
        model.sortByColumn(column);
        if (selectedObjects != null){
          select(selectedObjects);
        }

        header.repaint();
      }
    });
    header.setReorderingAllowed(false);
  }

  public Object[] getElements() {
    return getModel().getPlugins();
  }

  @Nullable
  public MvcPluginDescriptor getPluginAt(int row) {
    return getModel().getObjectAt(row);
  }

  @Override
  public AvailablePluginsModel getModel() {
    return (AvailablePluginsModel)super.getModel();
  }

  public void select(MvcPluginDescriptor... descriptors) {
    AvailablePluginsModel tableModel = getModel();
    getSelectionModel().clearSelection();
    for (int i=0; i<tableModel.getRowCount();i++) {
      MvcPluginDescriptor descriptorAt = tableModel.getObjectAt(i);
      if (ArrayUtil.find(descriptors,descriptorAt) != -1) {
        getSelectionModel().addSelectionInterval(i, i);
      }
    }
    TableUtil.scrollSelectionToVisible(this);
  }

  @Nullable
  public MvcPluginDescriptor getSelectedObject() {
    MvcPluginDescriptor selected = null;
    if (getSelectedRowCount() > 0) {
      selected = getPluginAt(getSelectedRow());
    }
    return selected;
  }

  @Nullable
  public MvcPluginDescriptor[] getSelectedObjects() {
    MvcPluginDescriptor[] selection = null;
    if (getSelectedRowCount() > 0) {
      int[] poses = getSelectedRows();
      selection = new MvcPluginDescriptor[poses.length];
      for (int i = 0; i < poses.length; i++) {
        selection[i] = getPluginAt(poses[i]);
      }
    }
    return selection;
  }
}