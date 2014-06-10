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
import com.intellij.j2meplugin.module.settings.MobileApplicationType;
import com.intellij.j2meplugin.module.settings.MobileModuleSettings;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.UnnamedConfigurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.FieldPanel;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * User: anna
 * Date: Oct 29, 2004
 */
public class MobileBuildPanel implements UnnamedConfigurable {
  private final JPanel myBuildSettingsPanel = new JPanel(new GridBagLayout());
  private final JLabel myJarPathLabel = new JLabel(J2MEBundle.message("build.settings.jar.file.label"));
  private final TextFieldWithBrowseButton myJarUrl = new TextFieldWithBrowseButton();
  private final TextFieldWithBrowseButton myDescriptorUrl = new TextFieldWithBrowseButton();
  private final JLabel myDescriptorLabel = new JLabel();

  private final JCheckBox myUseUserManifest = new JCheckBox(J2MEBundle.message("build.settings.manifest.label"));
  private final FieldPanel myUserManifestPath = new FieldPanel();

  private final MobileApplicationType myMobileApplicationType;
  private final Project myProject;
  private final MobileModuleSettings mySettings;
  private boolean myModified = false;

  public MobileBuildPanel(MobileApplicationType mobileApplicationType, Project project, MobileModuleSettings settings) {
    myMobileApplicationType = mobileApplicationType;
    myProject = project;
    mySettings = settings;
  }

  public JComponent createComponent() {
    if (myMobileApplicationType != null && mySettings != null) {
      myDescriptorLabel.setText(J2MEBundle.message("file.label", myMobileApplicationType.getExtension().toUpperCase()));
      myDescriptorUrl.addBrowseFolderListener(J2MEBundle.message("build.settings.file.url.title", myMobileApplicationType.getExtension()),
                                              J2MEBundle.message("build.settings.file.url", myMobileApplicationType.getExtension()),
                                              myProject,
                                              new FileChooserDescriptor(true,
                                                                        true,
                                                                        true,
                                                                        true,
                                                                        false,
                                                                        false));
      myDescriptorUrl.setText(mySettings.getMobileDescriptionPath());
      final DocumentAdapter modifier = new DocumentAdapter() {
        protected void textChanged(DocumentEvent e) {
          myModified = true;
        }
      };
      myDescriptorUrl.getTextField().getDocument().addDocumentListener(modifier);

      myJarUrl.addBrowseFolderListener(J2MEBundle.message("build.settings.jar.utl.title"),
                                       J2MEBundle.message("build.settings.jar.url"),
                                       myProject,
                                       new FileChooserDescriptor(true,
                                                                 true, //todo if folder then create file with specified name
                                                                 true,
                                                                 true,
                                                                 false,
                                                                 false));
      myJarUrl.setText(mySettings.getJarURL());
      myJarUrl.getTextField().getDocument().addDocumentListener(modifier);

      myUseUserManifest.setSelected(mySettings.isUseUserManifest());
      myUserManifestPath.setEnabled(mySettings.isUseUserManifest());
      myUserManifestPath.setText(mySettings.getUserManifestPath() != null ? mySettings.getUserManifestPath() : "");
      myUseUserManifest.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          myUserManifestPath.setEnabled(myUseUserManifest.isSelected());
          myModified = true;
        }
      });
      myUserManifestPath.setBrowseButtonActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          final FileChooserDescriptor descriptor = FileChooserDescriptorFactory.createSingleFileNoJarsDescriptor();
          descriptor.setTitle(J2MEBundle.message("build.settings.manifest.title"));
          descriptor.setDescription(J2MEBundle.message("build.settings.manifes.choose"));
          VirtualFile[] files = FileChooser.chooseFiles(myBuildSettingsPanel, descriptor);
          if (files.length != 0) {
            myUserManifestPath.setText(FileUtil.toSystemDependentName(files[0].getPath()));
          }
        }
      });
      myUserManifestPath.createComponent();
      myUserManifestPath.getTextField().getDocument().addDocumentListener(modifier);
    }
    myBuildSettingsPanel.add(myJarPathLabel,
                             new GridBagConstraints(0, GridBagConstraints.RELATIVE, 1, 1, 0.0, 0.0, GridBagConstraints.EAST,
                                                    GridBagConstraints.NONE, new Insets(5, 0, 5, 0), 0, 0));
    myBuildSettingsPanel.add(myJarUrl,
                             new GridBagConstraints(1, GridBagConstraints.RELATIVE, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER,
                                                    GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 0), 0, 0));

    myBuildSettingsPanel.add(myDescriptorLabel,
                             new GridBagConstraints(0, GridBagConstraints.RELATIVE, 1, 1, 0.0, 0.0, GridBagConstraints.EAST,
                                                    GridBagConstraints.NONE, new Insets(5, 0, 5, 0), 0, 0));
    myBuildSettingsPanel.add(myDescriptorUrl,
                             new GridBagConstraints(1, GridBagConstraints.RELATIVE, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER,
                                                    GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 0), 0, 0));
    myBuildSettingsPanel.add(myUseUserManifest,
                             new GridBagConstraints(0, GridBagConstraints.RELATIVE, 2, 1, 1.0, 0.0, GridBagConstraints.WEST,
                                                    GridBagConstraints.NONE, new Insets(5, 0, 0, 0), 0, 0));
    myBuildSettingsPanel.add(myUserManifestPath,
                             new GridBagConstraints(1, GridBagConstraints.RELATIVE, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER,
                                                    GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 0), 0, 0));
    myBuildSettingsPanel.setBorder(new TitledBorder(J2MEBundle.message("mobile.module.packages.title")));

    return myBuildSettingsPanel;
  }

  public boolean isModified() {
    return myModified;
  }

  public void apply() throws ConfigurationException {
    if (myDescriptorUrl.getText() == null || myDescriptorUrl.getText().length() == 0) {
      throw new ConfigurationException(J2MEBundle.message("build.settings.descriptor.not.specified", myMobileApplicationType.getExtension().toUpperCase()));
    }
    if (!myDescriptorUrl.getText().endsWith(myMobileApplicationType.getExtension())) {
      throw new ConfigurationException(J2MEBundle.message("build.settings.mistyped.descriptor", myMobileApplicationType.getExtension().toUpperCase()));
    }
    if (myJarUrl.getText() == null || myJarUrl.getText().length() == 0) {
      throw new ConfigurationException(J2MEBundle.message("build.settings.jar.not.specified"));
    }
    if (myUseUserManifest.isSelected() &&
        (myUserManifestPath.getText() == null || myUserManifestPath.getText().length() == 0)) {
      throw new ConfigurationException(J2MEBundle.message("build.settings.manifest.not.specified"));
    }

    mySettings.setMobileDescriptionPath(myDescriptorUrl.getText());
    mySettings.setJarURL(myJarUrl.getText());
    mySettings.setUseUserManifest(myUseUserManifest.isSelected());
    if (myUseUserManifest.isSelected()) {
      mySettings.setUserManifestPath(myUserManifestPath.getText());
    }
    myModified = false;

  }

  public void reset() {
    myDescriptorUrl.setText(mySettings.getMobileDescriptionPath());
    myJarUrl.setText(mySettings.getJarURL());
    myUseUserManifest.setSelected(mySettings.isUseUserManifest());
    myUserManifestPath.setText(mySettings.getUserManifestPath() != null ? mySettings.getUserManifestPath() : "");
    myModified = false;
  }

  public void disposeUIResources() {
  }

  public JComponent getPreferredFocusedComponent() {
    return myJarUrl.getTextField();
  }
}
