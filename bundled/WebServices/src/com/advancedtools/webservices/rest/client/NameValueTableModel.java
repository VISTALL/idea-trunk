package com.advancedtools.webservices.rest.client;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

/**
 * @by Konstantin Bulenkov
 */
public class NameValueTableModel extends AbstractTableModel {
  private final List<String> names = new ArrayList<String>();
  private final List<String> values = new ArrayList<String>();
  private static final String EMPTY = "";
  private static final @NonNls String NAME = "Name";
  private static final @NonNls String VALUE = "Value";
  private final List<String> immutableHeaders = new ArrayList<String>();
  private final List<String> immutableHeadersExceptions = new ArrayList<String>();

  public int getRowCount() {
    return names.size() + 1;
  }

  public int getColumnCount() {
    return 2;
  }

  public Object getValueAt(final int rowIndex, final int columnIndex) {
    List<String> list = (columnIndex == 0) ? names : values;
    return (rowIndex < list.size()) ? list.get(rowIndex) : EMPTY;
  }

  @Override
  public String getColumnName(final int column) {
    return column == 0 ? NAME : VALUE;
  }

  @Override
  public void setValueAt(final Object aValue, final int rowIndex, final int columnIndex) {
    List<String> list = (columnIndex == 0) ? names : values;
    final String value = String.valueOf(aValue);
    if (rowIndex < list.size()) {
      list.set(rowIndex, value);
      if (names.get(rowIndex).trim().equals(EMPTY)) {
        names.remove(rowIndex);
        values.remove(rowIndex);
      }
    } else {
      if (aValue == null || value.trim().equals(EMPTY) || list.contains(value.trim())) return;      
      names.add(EMPTY);
      values.add(EMPTY);
      list.set(rowIndex, value);
    }
    fireTableDataChanged();
  }

  public void addProperty(@NotNull @NonNls String name, @NotNull @NonNls String value) {
    if (names.contains(name)) {
      values.set(getIndexByName(name), value);
    } else {
      names.add(name);
      values.add(value);
    }
  }

  @Override
  public boolean isCellEditable(final int row, final int column) {
    if (row >= names.size()) return column == 0;
    if (column == 0) {
      return ! (immutableHeaders.contains(names.get(row)));
    } else {
        if (immutableHeadersExceptions.contains(names.get(row))) return true;
        if (immutableHeaders.contains(names.get(row))) return false;
    }
    return !names.get(row).trim().equals(EMPTY);
  }
  
  @NotNull
  public String getName(int index) {
    return index < names.size() ? names.get(index) : EMPTY;
  }

  @NotNull
  public String getValue(int index) {
    return index < values.size() ? values.get(index) : EMPTY;
  }

  public int getElementsCount() {
    return names.size();
  }
  
  public void setImmutableFields(List<String> headerNames) {
    immutableHeaders.clear();
    immutableHeaders.addAll(headerNames);
  }

  public void setImmutableFields(String... fields) {
    setImmutableFields(Arrays.asList(fields));
  }

  public void setImmutableHeaderExceptions(List<String> headerNames) {
    immutableHeadersExceptions.clear();
    immutableHeadersExceptions.addAll(headerNames);
  }

  public void setImmutableHeaderExceptions(String... fields) {
    setImmutableHeaderExceptions(Arrays.asList(fields));
  }

  public int getIndexByName(String name) {
    for (int i=0; i < names.size(); i++) {
      if (names.get(i).equals(name)) return i;
    }
    return -1;
  }
}
