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

import com.intellij.j2meplugin.J2MEBundle;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.FieldPanel;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

/**
 * User: anna
 * Date: Oct 12, 2004
 */
public class MobileExplodedPanel {


  private final JPanel myExplodedPanel;
  private final JLabel myExplanation;
  private final JCheckBox myUseExplodedDir;
  private final FieldPanel myExplodedDirectory;

  private String myExplodedPath;
  private boolean myUseExplodedDirectory;
  private boolean myExcludeFromContent;
  private final JCheckBox myExcludeFromModuleContent;
  private boolean myModified = false;

  //private JPanel myRemoteDeployPanel;

  public MobileExplodedPanel(boolean useExplodedDir, boolean excludeFromContent, String explodedDir) {


    myExplodedPanel = new JPanel(new GridBagLayout());
    myExplodedPanel.setBorder(new TitledBorder(J2MEBundle.message("exploded.directory.settings")));
    myExplanation = new JLabel(J2MEBundle.message("exploded.directory.explanation"));
    myUseExplodedDir = new JCheckBox(J2MEBundle.message("exploded.directory.setup"));
    myExplodedDirectory = new FieldPanel();
    myExcludeFromModuleContent = new JCheckBox(J2MEBundle.message("exploded.directory.excluded"));
    myExplodedPanel.add(myExplanation,
                        new GridBagConstraints(0, GridBagConstraints.RELATIVE, 1, 1, 1.0, 0.0, GridBagConstraints.NORTHWEST,
                                               GridBagConstraints.HORIZONTAL, new Insets(5, 0, 5, 0), 0, 0));
    myExplodedPanel.add(myUseExplodedDir,
                        new GridBagConstraints(0, GridBagConstraints.RELATIVE, 1, 1, 1.0, 0.0, GridBagConstraints.NORTHWEST,
                                               GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    myExplodedPanel.add(myExplodedDirectory,
                        new GridBagConstraints(0, GridBagConstraints.RELATIVE, 1, 1, 1.0, 0.0, GridBagConstraints.NORTHWEST,
                                               GridBagConstraints.HORIZONTAL, new Insets(5, 13, 0, 0), 0, 0));
    myExplodedPanel.add(myExcludeFromModuleContent,
                        new GridBagConstraints(0, GridBagConstraints.RELATIVE, 1, 1, 1.0, 1.0, GridBagConstraints.NORTHWEST,
                                               GridBagConstraints.NONE, new Insets(0, 10, 0, 0), 0, 0));

    /*   myRemoteDeployPanel = new MobileRemoteDeploymentPanel().getComponent();
       myRemoteDeployPanel.setBorder(new TitledBorder("Deployment to remote server"));
       myExplodedPanel.add(myRemoteDeployPanel, new GridBagConstraints(0, GridBagConstraints.RELATIVE, 1, 1, 1.0, 1.0, GridBagConstraints.NORTHWEST,
                                               GridBagConstraints.NONE, new Insets(0, 10, 0, 0), 0, 0));
   */
    myExplodedPath = explodedDir;
    myUseExplodedDirectory = useExplodedDir;
    myExcludeFromContent = excludeFromContent;
  }

  public String getExplodedDir() {
    return myExplodedPath;
  }

  public boolean isExcludeFromContent() {
    return myExcludeFromContent;
  }

  public boolean isPathEnabled() {
    return myUseExplodedDirectory;
  }

  public JComponent getComponent() {

   myUseExplodedDir.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (myUseExplodedDir.isSelected()) {
          if (myExplodedDirectory.getText() == null || myExplodedDirectory.getText().equals("")) {
            myExplodedDirectory.setText(myExplodedPath);
          }
          myExplodedDirectory.setEnabled(true);
          myExcludeFromModuleContent.setEnabled(true);
        }
        else {
          myExplodedDirectory.setEnabled(false);
          myExcludeFromModuleContent.setEnabled(false);
        }
        myModified = true;
      }
    });
    final LocalFileSystem lfs = LocalFileSystem.getInstance();
    myExplodedDirectory.setBrowseButtonActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        final FileChooserDescriptor descriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor();
        descriptor.setTitle(J2MEBundle.message("exploded.directory.chooser.title"));
        descriptor.setDescription(J2MEBundle.message("exploded.directory.chooser"));
        VirtualFile toSelect = null;
        if (myExplodedDirectory.getText() != null){
          toSelect = lfs.findFileByPath(myExplodedDirectory.getText().replace(File.separatorChar, '/'));
        }
        VirtualFile[] files = FileChooser.chooseFiles(myExplodedPanel, descriptor, toSelect);
        if (files.length != 0) {
          myExplodedDirectory.setText(FileUtil.toSystemDependentName(files[0].getPath()));
        }
      }
    });
    myExplodedDirectory.createComponent();
    myExplodedDirectory.getTextField().getDocument().addDocumentListener(new DocumentAdapter() {
      protected void textChanged(DocumentEvent e) {
        myModified = true;
      }
    });

    myExcludeFromModuleContent.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        myModified = true;
      }
    });
    return myExplodedPanel;
  }

  public JComponent getPreferredFocusedComponent() {
    return myUseExplodedDir;
  }

  public void reset(){
    if (myUseExplodedDirectory) {
      myExplodedDirectory.setText(myExplodedPath);
      myExcludeFromModuleContent.setSelected(myExcludeFromContent);
    }
    myExplodedDirectory.setEnabled(myUseExplodedDirectory);
    myExcludeFromModuleContent.setEnabled(myUseExplodedDirectory);
    myUseExplodedDir.setSelected(myUseExplodedDirectory);
    myModified = false;
  }

  public void apply(){
    myUseExplodedDirectory = myUseExplodedDir.isSelected();
    if (myUseExplodedDirectory){
      myExplodedPath = myExplodedDirectory.getText();
    } else {
      myExplodedPath = null;
    }
    myExcludeFromContent = myExcludeFromModuleContent.isSelected();
    myModified = false;
  }

  public boolean isModified(){
    return myModified;
  }
}
