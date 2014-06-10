/*
 * Copyright 2000-2008 JetBrains s.r.o.
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

package org.jetbrains.tfsIntegration.ui;

import com.intellij.ui.DocumentAdapter;
import com.intellij.util.EventDispatcher;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.EventListener;

public class MergeNameForm {
  public interface Listener extends EventListener{
    void selectedPathChanged();
  }

  private final EventDispatcher<Listener> myEventDispatcher = EventDispatcher.create(Listener.class);

  private final String myYoursPath;
  private final String myTheirsPath;

  private JRadioButton myYoursRadioButton;
  private JRadioButton myTheirsRadioButton;
  private JRadioButton myUseCustomRadioButton;
  private JTextField myCustomPathTextField;
  private JPanel myContentPanel;
  private JLabel myErrorLabel;

  public MergeNameForm(final String yoursName, final String theirsName) {
    myYoursPath = yoursName;
    myTheirsPath = theirsName;

    myYoursRadioButton.setText(myYoursRadioButton.getText() + ": " + myYoursPath);
    myYoursRadioButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        update();
      }
    });

    myTheirsRadioButton.setText(myTheirsRadioButton.getText() + ": " + myTheirsPath);
    myTheirsRadioButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        update();
      }
    });
    myCustomPathTextField.setText(myYoursPath);
    myUseCustomRadioButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        update();
      }
    });

    myCustomPathTextField.setText(myYoursPath);
    
    myCustomPathTextField.getDocument().addDocumentListener(new DocumentAdapter() {
      protected void textChanged(final DocumentEvent e) {
        myEventDispatcher.getMulticaster().selectedPathChanged();
      }
    });

    myErrorLabel.setText(" ");
  }

  private void update() {
    myCustomPathTextField.setEnabled(myUseCustomRadioButton.isSelected());
    myEventDispatcher.getMulticaster().selectedPathChanged();
  }

  public JComponent getPanel() {
    return myContentPanel;
  }

  @Nullable
  public String getSelectedPath() {
    if (myYoursRadioButton.isSelected()) {
      return myYoursPath;
    }
    else if (myTheirsRadioButton.isSelected()) {
      return myTheirsPath;
    }
    else if (myUseCustomRadioButton.isSelected()) {
      return myCustomPathTextField.getText();
    }
    throw new IllegalStateException("Unexpected state");
  }

  public void addListener(Listener listener) {
    myEventDispatcher.addListener(listener);
  }

  public void removeListener(Listener listener) {
    myEventDispatcher.removeListener(listener);
  }

  public void setErrorText(final String errorMessage) {
    myErrorLabel.setText(errorMessage);
  }


}
