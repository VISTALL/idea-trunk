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
package com.intellij.j2meplugin.module.settings.ui;

import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.j2meplugin.J2MEBundle;
import com.intellij.j2meplugin.module.J2MEModuleBuilder;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.FieldPanel;
import org.jetbrains.annotations.NonNls;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

/**
 * User: anna
 * Date: Sep 17, 2004
 */
public class J2MEModuleResourcesStep extends ModuleWizardStep {
  private final J2MEModuleBuilder myModuleBuilder;
  private JPanel myWholePanel;
  private JLabel myExplanation;
  private JCheckBox mySetupResourseFolder;
  private FieldPanel myResourcesDirectory;
  private String myDefaultResourceDirectoryPath;
  private final Icon myIcon;
  private final String myHelpId;

  public J2MEModuleResourcesStep(WizardContext wizardContext,
                                 J2MEModuleBuilder moduleBuilder,
                                 Icon wizardIcon,
                                 @NonNls String s) {

    myModuleBuilder = moduleBuilder;
    myIcon = wizardIcon;
    myHelpId = s;
    myExplanation.setText("");
  }

  public JComponent getPreferredFocusedComponent() {
    return mySetupResourseFolder.isSelected()? (JComponent)myResourcesDirectory.getTextField() : (JComponent)mySetupResourseFolder;
  }

  public JComponent getComponent() {
    @NonNls final String res = "res";
    myDefaultResourceDirectoryPath = myModuleBuilder.getModuleFileDirectory() != null ? myModuleBuilder.getModuleFileDirectory().replace(
      '/', File.separatorChar) +
                               File.separator +
                               res :
                                     "";
    mySetupResourseFolder.setSelected(myModuleBuilder.getResourcesDirPath() != null);
    setUpResourceDirectoryPanel();
    mySetupResourseFolder.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setUpResourceDirectoryPanel();
      }
    });
    myResourcesDirectory.setBrowseButtonActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        final FileChooserDescriptor descriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor();
        descriptor.setTitle(J2MEBundle.message("resource.directory.chooser.title"));
        descriptor.setDescription(J2MEBundle.message("resource.directory.chooser"));
        VirtualFile initial = LocalFileSystem.getInstance().findFileByPath(
          FileUtil.toSystemIndependentName(myModuleBuilder.getModuleFileDirectory()));
        VirtualFile[] files = FileChooser.chooseFiles(myWholePanel, descriptor, initial);
        if (files.length != 0) {
          myResourcesDirectory.setText(FileUtil.toSystemDependentName(files[0].getPath()));
        }
      }
    });
    myResourcesDirectory.createComponent();
    myWholePanel.setBorder(BorderFactory.createEtchedBorder());
    return myWholePanel;
  }

  private void setUpResourceDirectoryPanel() {
    if (mySetupResourseFolder.isSelected()) {
      myResourcesDirectory.setEnabled(true);
      if (myModuleBuilder.isDefaultRDirectoryModified()) {
        myResourcesDirectory.setText(myModuleBuilder.getResourcesDirPath());
      }
      else {
        myResourcesDirectory.setText(myDefaultResourceDirectoryPath);
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            myResourcesDirectory.getTextField().selectAll();
            myResourcesDirectory.getTextField().requestFocus();
          }
        });
      }
    }
    else {
      myResourcesDirectory.setEnabled(false);
    }
  }

  public void updateDataModel() {
    if (mySetupResourseFolder.isSelected()) {
      myModuleBuilder.setResourcesDirPath(myResourcesDirectory.getText());
      myModuleBuilder.setDefaultRDirectoryModified(!myDefaultResourceDirectoryPath.equals(myResourcesDirectory.getText()));
    }
    else {
      myModuleBuilder.setResourcesDirPath(null);
    }
  }

  public Icon getIcon() {
    return myIcon;
  }

  public String getHelpId() {
    return myHelpId;
  }

  public void onStepLeaving() {
    updateDataModel();
  }
}
