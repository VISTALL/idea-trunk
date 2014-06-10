package org.jetbrains.plugins.groovy.mvc.plugins;

import com.intellij.openapi.module.Module;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.SortableColumnModel;
import org.jetbrains.annotations.Nullable;

import javax.swing.table.AbstractTableModel;
import java.util.*;

/**
 * User: Dmitry.Krasilschikov
 * Date: 31.08.2008
 */
public class AvailablePluginsModel extends AbstractTableModel implements SortableColumnModel {
  private final Map<String, MvcPluginDescriptor> myPluginsMap;

  public static final int COLUMN_IS_INSTALLED = 0;
  public static final int COLUMN_NAME = 1;
  public static final int COLUMN_VERSION = 2;
  public static final int COLUMN_TITLE = 3;
  private final Module myModule;
  protected ColumnInfo[] columns;
  private int mySortColumn = 1;
  protected List<MvcPluginDescriptor> view = new ArrayList<MvcPluginDescriptor>();

  public AvailablePluginsModel(final Module module, final Map<String, MvcPluginDescriptor> installedPlugins,
                               @Nullable final Map<String, MvcPluginDescriptor> availablePlugins) {
    super();

    columns = new ColumnInfo[]{new MvcPluginIsInstalledColumnInfo(installedPlugins),
          new MvcPluginColumnInfo(module, COLUMN_NAME),
          new MvcPluginColumnInfo(module, COLUMN_VERSION),
          new MvcPluginColumnInfo(module, COLUMN_TITLE)};

    myModule = module;

    ((MvcPluginColumnInfo)getColumnInfos()[COLUMN_NAME]).setModel(this);
    ((MvcPluginColumnInfo)getColumnInfos()[COLUMN_VERSION]).setModel(this);
    ((MvcPluginColumnInfo)getColumnInfos()[COLUMN_TITLE]).setModel(this);

    myPluginsMap = new HashMap<String, MvcPluginDescriptor>();

    if (availablePlugins != null) {
      myPluginsMap.putAll(availablePlugins);
    }

    myPluginsMap.putAll(installedPlugins);

    ((MvcPluginIsInstalledColumnInfo)getColumnInfos()[COLUMN_IS_INSTALLED]).configureColumn(this);

    fireModelChange();
    sortByColumn(getSortedColumnIndex());
  }

  public void addData(final Collection<MvcPluginDescriptor> list) {
    for (MvcPluginDescriptor mvcPluginDescriptor : list) {
      myPluginsMap.put(mvcPluginDescriptor.getName(), mvcPluginDescriptor);
    }
    fireModelChange();
  }

  public void addData(final MvcPluginDescriptor mvcPluginDescriptor) {
    myPluginsMap.put(mvcPluginDescriptor.getName(), mvcPluginDescriptor);
    fireModelChange();
  }

  public void modifyData(final List<MvcPluginDescriptor> list) {
    for (MvcPluginDescriptor mvcPluginDescriptor : list) {
      myPluginsMap.put(mvcPluginDescriptor.getName(), mvcPluginDescriptor);
    }
    fireModelChange();
  }

  public void clearData() {
    myPluginsMap.clear();
    fireModelChange();
  }

  private void fireModelChange() {
    view = Arrays.asList(myPluginsMap.values().toArray(MvcPluginDescriptor.EMPTY_ARRAY));

    sortByColumn(getSortedColumnIndex());
    fireTableDataChanged();
  }

  public Module getModule() {
    return myModule;
  }

  public Map<String, MvcPluginDescriptor> getPluginsMap() {
    return myPluginsMap;
  }
public int getColumnCount() {
    return columns.length;
  }

  public ColumnInfo[] getColumnInfos() {
    return columns;
  }

  public boolean isSortable() {
    return true;
  }

  public void setSortable(boolean aBoolean) {
    // do nothing cause it's always sortable
  }

  public String getColumnName(int column) {
    return columns[column].getName();
  }

  public int getSortedColumnIndex() {
    return mySortColumn;
  }

  public int getSortingType() {
    return SORT_ASCENDING;
  }

  @Nullable
  public MvcPluginDescriptor getObjectAt (int row) {
    if (view == null) return null;
    return view.get(row);
  }

  public MvcPluginDescriptor[] getPlugins() {
    return view.toArray(new MvcPluginDescriptor[view.size()]);
  }

  public int getRowCount() {
    if (view == null) return -1;
    return view.size();
  }

  public Object getValueAt(int rowIndex, int columnIndex) {
    return columns[columnIndex].valueOf(getObjectAt(rowIndex));
  }

  public boolean isCellEditable(final int rowIndex, final int columnIndex) {
    return columns[columnIndex].isCellEditable(getObjectAt(rowIndex));
  }

  public void setValueAt(final Object aValue, final int rowIndex, final int columnIndex) {
    columns[columnIndex].setValue(getObjectAt(rowIndex), aValue);
    fireTableRowsUpdated(rowIndex, rowIndex);
  }

  public void sortByColumn(int columnIndex) {
    if (view == null) return;
    mySortColumn = columnIndex;
    Collections.sort(view, columns[columnIndex].getComparator());
    fireTableDataChanged();
  }

  public void sortByColumn(int columnIndex, int sortingType) {
    sortByColumn(columnIndex);
  }

  public List<MvcPluginDescriptor> getView() {
    return view;
  }
}
