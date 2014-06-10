/*
 * Copyright 2000-2009 JetBrains s.r.o.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.advancedtools.webservices.rest.client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

/**
 * @by Konstantin Bulenkov
 */
public class TypesCellEditor extends DefaultCellEditor {
  private final JComboBox myAcceptTypes;
  private Component currentEditor = null;
  private final List<String> myNames;


  public TypesCellEditor(String[] names, String...types) {
    super(new JTextField());
    myNames = Arrays.asList(names);
    myAcceptTypes = new JComboBox();
    for (String type : types) {
      myAcceptTypes.addItem(type);
    }
    myAcceptTypes.setBackground(Color.WHITE);
    myAcceptTypes.setEditable(true);

    KeyListener submitter = new KeyAdapter() {
      @Override
      public void keyPressed(final KeyEvent e) {
        super.keyPressed(e);
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
          stopCellEditing();
        }
      }
    };
    myAcceptTypes.getEditor().getEditorComponent().addKeyListener(submitter);
    myAcceptTypes.addKeyListener(submitter);
    myAcceptTypes.getEditor().getEditorComponent().addFocusListener(new FocusListener() {
      public void focusGained(final FocusEvent e) {
        myAcceptTypes.showPopup();
      }
      public void focusLost(final FocusEvent e) {
      }
    });
  }

  @Override
  public Component getTableCellEditorComponent(final JTable table,
                                               final Object value,
                                               final boolean isSelected,
                                               final int row,
                                               final int column) {
    NameValueTableModel model = (NameValueTableModel)table.getModel();
    if (myNames.contains(model.getName(row))) {
      myAcceptTypes.setSelectedItem(String.valueOf(value));
      currentEditor = myAcceptTypes;
    } else {
      currentEditor = super.getTableCellEditorComponent(table, value, isSelected, row, column);
    }
    return currentEditor;
  }

  @Override
  public Object getCellEditorValue() {
    return currentEditor instanceof JComboBox ? String.valueOf(((JComboBox)currentEditor).getSelectedItem()) : ((JTextField)currentEditor).getText();
  }
}
