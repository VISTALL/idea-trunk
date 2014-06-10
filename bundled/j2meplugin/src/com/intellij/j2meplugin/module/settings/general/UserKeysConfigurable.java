/*
 * Copyright 2000-2005 JetBrains s.r.o.
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
package com.intellij.j2meplugin.module.settings.general;

import com.intellij.j2meplugin.J2MEBundle;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.TableUtil;
import com.intellij.ui.table.TableView;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.ListTableModel;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * User: anna
 * Date: Sep 26, 2004
 */
public class UserKeysConfigurable {
  private final ListTableModel<UserDefinedOption> myUserDefinedOptions = new ListTableModel<UserDefinedOption>(PARAMETER_COLUMNS);
  private final TableView myTable = new TableView(myUserDefinedOptions);
  private JButton myAddButton;
  private JButton myRemoveButton;
  private JButton myMoveUpButton;
  private JButton myMoveDownButton;
  private JPanel myTablePlace;
  private JPanel myWholePanel;
  private final HashSet<UserDefinedOption> myOptions;

  public UserKeysConfigurable(HashSet<UserDefinedOption> userDefinedOptions) {
    myOptions = userDefinedOptions;
    myUserDefinedOptions.setSortable(false);
    myUserDefinedOptions.setItems(new ArrayList<UserDefinedOption>(myOptions));

    myRemoveButton.setEnabled(false);
    myMoveUpButton.setEnabled(false);
    myMoveDownButton.setEnabled(false);
    myTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        if (myTable.getSelectedRow() == -1) {
          myRemoveButton.setEnabled(false);
          myMoveUpButton.setEnabled(false);
          myMoveDownButton.setEnabled(false);
        }
        else {
          myRemoveButton.setEnabled(true);
          if (myTable.getSelectedRow() != 0) {
            myMoveUpButton.setEnabled(true);
          }
          else {
            myMoveUpButton.setEnabled(false);
          }
          if (myTable.getSelectedRow() != myUserDefinedOptions.getItems().size() - 1) {
            myMoveDownButton.setEnabled(true);
          }
          else {
            myMoveDownButton.setEnabled(false);
          }
        }
      }
    });

    myAddButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        myTable.stopEditing();
        ArrayList<UserDefinedOption> options = new ArrayList<UserDefinedOption>(myUserDefinedOptions.getItems());
        options.add(new UserDefinedOption("", ""));
        myUserDefinedOptions.setItems(options);
        TableUtil.selectRows(myTable, new int []{myTable.getRowCount() - 1});
      }
    });

    myRemoveButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        TableUtil.removeSelectedItems(myTable);
      }
    });


    myMoveDownButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        TableUtil.moveSelectedItemsDown(myTable);
      }
    });

    myMoveUpButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        TableUtil.moveSelectedItemsUp(myTable);
      }
    });
    myTablePlace.setLayout(new BorderLayout());
    myTablePlace.add(ScrollPaneFactory.createScrollPane(myTable), BorderLayout.CENTER);

  }

  public JPanel getUserKeysPanel() {
    JPanel panel = new JPanel(new BorderLayout());
    panel.add(myWholePanel, BorderLayout.CENTER);
    return panel;
  }

  public ListTableModel<UserDefinedOption> getUserDefinedOptions() {
    return (ListTableModel<UserDefinedOption>)myTable.getModel();
  }

  public void setUserDefinedOptions(ArrayList<UserDefinedOption> userDefinedOptions) {
    myUserDefinedOptions.setItems(userDefinedOptions);
  }

  public TableView getTable() {
    return myTable;
  }

  private static final ColumnInfo[] PARAMETER_COLUMNS = new ColumnInfo[]{
    new ColumnInfo<UserDefinedOption, String>(J2MEBundle.message("module.settings.user.defined.key")) {
      public String valueOf(final UserDefinedOption userOption) {
        return userOption.getKey();
      }

      public void setValue(final UserDefinedOption userOption, final String name) {
        userOption.setKey(name);
      }

      public boolean isCellEditable(final UserDefinedOption userOption) {
        return true;
      }
    },
    new ColumnInfo<UserDefinedOption, String>(J2MEBundle.message("module.settings.user.defined.value")) {
      public String valueOf(final UserDefinedOption userOption) {
        return userOption.getValue();
      }

      public void setValue(final UserDefinedOption userOption, final String value) {
        userOption.setValue(value);
      }

      public boolean isCellEditable(final UserDefinedOption userOption) {
        return true;
      }
    }
  };
}
