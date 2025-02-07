/*
 * Copyright 2000-2007 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.intellij.execution.junit2.ui;

import com.intellij.execution.testframework.Filter;
import com.intellij.execution.junit2.StateChangedEvent;
import com.intellij.execution.junit2.TestEvent;
import com.intellij.execution.junit2.TestProxy;
import com.intellij.execution.junit2.ui.model.JUnitAdapter;
import com.intellij.execution.junit2.ui.model.JUnitRunningModel;
import com.intellij.execution.ExecutionBundle;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.ListTableModel;

import java.util.ArrayList;
import java.util.Arrays;

public class StatisticsTable extends ListTableModel {
  private static final Logger LOG = Logger.getInstance("#com.intellij.execution.junit2.ui.StatisticsTable");
  private TestProxy myTest;
  private JUnitRunningModel myModel;

  public StatisticsTable(final ColumnInfo[] collumnNames) {
    super(collumnNames);
  }

  public void setModel(final JUnitRunningModel model) {
    myModel = model;
    setTest(myModel.getRoot());
    myModel.addListener(new JUnitAdapter() {
      public void doDispose() {
        myTest = null;
        resetTests();
        myModel = null;
      }

      public void onTestChanged(final TestEvent event) {
        if (event instanceof StateChangedEvent) return;
        if (event.getSource() == myTest) changeTableData();
      }
    });
  }

  public void onSelectionChanged(final TestProxy test) {
    setTest(test);
  }

  private void setTest(final TestProxy test) {
    if (myTest == test) return;
    myTest = test;
    changeTableData();
  }

  private void changeTableData() {
    if (myTest == null) {
      resetTests();
      return;
    }
    setItems(new ArrayList(Arrays.asList(myTest.selectChildren(Filter.NO_FILTER))));
  }

  private void resetTests() {
    setItems(new ArrayList());
  }

  public int getRowCount() {
    return super.getRowCount() + 1;
  }

  public Object getValueAt(final int rowIndex, final int columnIndex) {
    if (rowIndex == 0)
      return testProperty(columnIndex);
    return super.getValueAt(rowIndex - 1, columnIndex);
  }

  public void setValueAt(final Object aValue, final int rowIndex, final int columnIndex) {
    LOG.assertTrue(false, "value: " + aValue + " row: " + rowIndex + " column: " + columnIndex);
  }

  public boolean isCellEditable(final int rowIndex, final int columnIndex) {
    return false;
  }

  private Object testProperty(final int columnIndex) {
    if (columnIndex == 0)
      return ExecutionBundle.message("junit.runing.info.total.label");
    return getAspectOf(columnIndex, myTest);
  }

  public TestProxy getTestAt(final int rowIndex) {
    if (rowIndex < 0 || rowIndex > getItems().size())
      return null;
    return (rowIndex == 0) ? myTest : (TestProxy)getItems().get(rowIndex - 1);
  }

  public int getIndexOf(final Object test) {
    if (test == myTest)
      return 0;
    for (int i = 0; i < getItems().size(); i++) {
      final Object child = getItems().get(i);
      if (child == test) return i + 1;
    }
    return -1;
  }
}
