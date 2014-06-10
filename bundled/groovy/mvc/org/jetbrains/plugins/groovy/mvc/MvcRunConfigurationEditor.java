/*
 * Copyright 2000-2007 JetBrains s.r.o.
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

package org.jetbrains.plugins.groovy.mvc;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.RawCommandLineEditor;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.*;

public class MvcRunConfigurationEditor<T extends MvcRunConfiguration> extends SettingsEditor<T> {
  private DefaultComboBoxModel myModulesModel;
  private JComboBox myModulesBox;
  private JPanel myMainPanel;
  private RawCommandLineEditor myVMParameters;
  private JTextField myCommandLine;
  private JLabel myVMParamsLabel;
  private JPanel myExtensionPanel;
  private JCheckBox myDepsClasspath;
  private MvcFramework myFramework;

  public MvcRunConfigurationEditor() {
    myCommandLine.getDocument().addDocumentListener(new DocumentAdapter() {
      @Override
      protected void textChanged(DocumentEvent e) {
        commandLineChanged(getCommandLine());
      }
    });
  }

  public void resetEditorFrom(T configuration) {
    myFramework = configuration.getFramework();
    myVMParameters.setDialogCaption("VM Parameters");
    myVMParameters.setText(configuration.vmParams);
    myVMParamsLabel.setLabelFor(myVMParameters);

    myCommandLine.setText(configuration.cmdLine);

    myModulesModel.removeAllElements();
    for (Module module : configuration.getValidModules()) {
      myModulesModel.addElement(module);
    }
    myModulesModel.setSelectedItem(configuration.getModule());

    myDepsClasspath.setSelected(configuration.depsClasspath);

    commandLineChanged(getCommandLine());
  }

  protected void commandLineChanged(@NotNull String newText) {
    final Module module = getSelectedModule();
    final String depsClasspath = module == null ? "" : myFramework.getApplicationClassPath(module, newText.contains("test-app")).getPathsString();
    final boolean hasClasspath = StringUtil.isNotEmpty(depsClasspath);
    setCBEnabled(hasClasspath, myDepsClasspath);

    String presentable = "Add --classpath";
    if (hasClasspath) {
      presentable += ": " + (depsClasspath.length() > 100 ? depsClasspath.substring(0, 100) + "..." : depsClasspath);
    }
    myDepsClasspath.setText(presentable);
    myDepsClasspath.setToolTipText(depsClasspath);
  }

  protected static void setCBEnabled(boolean enabled, final JCheckBox checkBox) {
    final boolean wasEnabled = checkBox.isEnabled();
    checkBox.setEnabled(enabled);
    if (wasEnabled && !enabled) {
      checkBox.setSelected(false);
    } else if (!wasEnabled && enabled) {
      checkBox.setSelected(true);
    }
  }

  public void applyEditorTo(T configuration) throws ConfigurationException {
    configuration.setModule(getSelectedModule());
    configuration.vmParams = myVMParameters.getText().trim();
    configuration.cmdLine = getCommandLine();
    configuration.depsClasspath = myDepsClasspath.isSelected();
  }

  private String getCommandLine() {
    return myCommandLine.getText().trim();
  }

  private Module getSelectedModule() {
    return (Module)myModulesBox.getSelectedItem();
  }

  public void addExtension(JComponent component) {
    myExtensionPanel.add(component);
  }

  @NotNull
  public JComponent createEditor() {
    myModulesModel = new DefaultComboBoxModel();
    myModulesBox.setModel(myModulesModel);
    myModulesBox.setRenderer(new DefaultListCellRenderer() {
      public Component getListCellRendererComponent(JList list, final Object value, int index, boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        final Module module = (Module)value;
        if (module != null) {
          setIcon(module.getModuleType().getNodeIcon(false));
          setText(module.getName());
        }
        return this;
      }
    });

    return myMainPanel;
  }

  public void disposeEditor() {
  }

}
