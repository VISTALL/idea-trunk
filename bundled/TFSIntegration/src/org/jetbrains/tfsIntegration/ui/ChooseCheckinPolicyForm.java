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

package org.jetbrains.tfsIntegration.ui;

import com.intellij.ui.CollectionListModel;
import com.intellij.util.EventDispatcher;
import org.jetbrains.tfsIntegration.checkin.PolicyBase;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.EventListener;
import java.util.List;

public class ChooseCheckinPolicyForm {
  public interface Listener extends EventListener {
    void stateChanged();

    void close();
  }

  private JList myPoliciesList;
  private JPanel myContentPane;
  private JTextArea myDescriptionArea;

  private final EventDispatcher<Listener> myEventDispatcher = EventDispatcher.create(Listener.class);

  public ChooseCheckinPolicyForm(List<PolicyBase> policies) {
    myPoliciesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    myPoliciesList.setModel(new CollectionListModel(policies));
    myPoliciesList.setCellRenderer(new DefaultListCellRenderer() {
      @Override
      public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        JLabel label = (JLabel)super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        label.setText(((PolicyBase)value).getPolicyType().getName());
        return label;
      }
    });


    myPoliciesList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        PolicyBase policy = getSelectedPolicy();
        myDescriptionArea.setText(policy != null ? policy.getPolicyType().getDescription() : null);
        myEventDispatcher.getMulticaster().stateChanged();
      }
    });

    myPoliciesList.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
          if (getSelectedPolicy() != null) {
            myEventDispatcher.getMulticaster().close();
          }
        }
      }
    });

    myDescriptionArea.setWrapStyleWord(true);
  }

  public PolicyBase getSelectedPolicy() {
    return (PolicyBase)myPoliciesList.getSelectedValue();
  }

  public void addListener(Listener listener) {
    myEventDispatcher.addListener(listener);
  }

  public JComponent getContentPane() {
    return myContentPane;
  }


}
