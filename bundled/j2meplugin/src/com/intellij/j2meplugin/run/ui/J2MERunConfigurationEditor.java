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
package com.intellij.j2meplugin.run.ui;

import com.intellij.execution.impl.CheckableRunConfigurationEditor;
import com.intellij.j2meplugin.J2MEBundle;
import com.intellij.j2meplugin.emulator.Emulator;
import com.intellij.j2meplugin.emulator.EmulatorType;
import com.intellij.j2meplugin.emulator.MobileSdk;
import com.intellij.j2meplugin.module.J2MEModuleProperties;
import com.intellij.j2meplugin.module.J2MEModuleType;
import com.intellij.j2meplugin.module.MobileModuleUtil;
import com.intellij.j2meplugin.module.settings.MobileApplicationType;
import com.intellij.j2meplugin.module.settings.MobileModuleSettings;
import com.intellij.j2meplugin.module.settings.general.UserDefinedOption;
import com.intellij.j2meplugin.module.settings.general.UserKeysConfigurable;
import com.intellij.j2meplugin.run.J2MERunConfiguration;
import com.intellij.j2meplugin.run.ui.editors.EmulatorEditor;
import com.intellij.j2meplugin.util.J2MEClassBrowser;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.RawCommandLineEditor;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * User: anna
 * Date: Aug 16, 2004
 */
public class J2MERunConfigurationEditor extends SettingsEditor<J2MERunConfiguration> implements CheckableRunConfigurationEditor<J2MERunConfiguration>{
  private EditorPanel myEditor;
  private final Project myProject;

  private final J2MERunConfiguration myJ2MERunConfiguration;
  private static final Logger LOG = Logger.getInstance("#" + J2MERunConfigurationEditor.class.getName());

  public J2MERunConfigurationEditor(Project project, J2MERunConfiguration j2MERunConfiguration) {
    myProject = project;
    myJ2MERunConfiguration = j2MERunConfiguration;
  }

  public void resetEditorFrom(J2MERunConfiguration j2merc) {
    myEditor.myModule.getComponent().setSelectedItem(j2merc.getModule());
    if (myEditor.getEmulatorRunOptions() != null) {
      myEditor.getEmulatorRunOptions().reset();
    }
    myEditor.myJad.setText(j2merc.JAD_NAME);
    myEditor.myClass.setText(j2merc.MAIN_CLASS_NAME);
    myEditor.myUseClasses.setSelected(j2merc.IS_CLASSES);
    myEditor.myUseJad.setSelected(!j2merc.IS_CLASSES);
    myEditor.myUserKeysConfigurable.setUserDefinedOptions(j2merc.userParameters);
    if (myEditor.myOTASettings != null) {
      myEditor.myOTASettings.resetEditorFrom(j2merc);
    }
    myEditor.myUseOTA.setSelected(j2merc.IS_OTA);
    myEditor.myProgramParameters.getComponent().setText(j2merc.COMMAND_LINE_PARAMETERS);
    myEditor.changeConfigurationTargetSelection();
  }

  public void applyEditorTo(J2MERunConfiguration j2merc) throws ConfigurationException {
    j2merc.setModule(myEditor.getModule());
    if (myEditor.getEmulatorRunOptions() != null) {
      myEditor.getEmulatorRunOptions().apply();
    }
    j2merc.JAD_NAME = myEditor.myJad.getText();
    j2merc.MAIN_CLASS_NAME = myEditor.myClass.getText();
    j2merc.IS_CLASSES = myEditor.myUseClasses.isSelected();
    if (myEditor.myUserKeysConfigurable != null) {
      myEditor.myUserKeysConfigurable.getTable().stopEditing();
      j2merc.userParameters = new ArrayList<UserDefinedOption>(myEditor.myUserKeysConfigurable.getUserDefinedOptions().getItems());
    }
    if (myEditor.myOTASettings != null) myEditor.myOTASettings.applyEditorTo(j2merc);
    j2merc.IS_OTA = myEditor.myUseOTA.isSelected();
    j2merc.COMMAND_LINE_PARAMETERS = myEditor.myProgramParameters.getComponent().getText();
  }

  @NotNull
  public JComponent createEditor() {
    myEditor = new EditorPanel();
    return myEditor.getComponent();
  }

  public void disposeEditor() {}

  public void checkEditorData(final J2MERunConfiguration j2merc) {
    try {
      j2merc.setModule(myEditor.getModule());
      if (myEditor.getEmulatorRunOptions() != null) {
        myEditor.getEmulatorRunOptions().apply();
      }
      j2merc.JAD_NAME = myEditor.myJad.getText();
      j2merc.MAIN_CLASS_NAME = myEditor.myClass.getText();
      j2merc.IS_CLASSES = myEditor.myUseClasses.isSelected();
      if (myEditor.myOTASettings != null) myEditor.myOTASettings.applyEditorTo(j2merc);
      j2merc.IS_OTA = myEditor.myUseOTA.isSelected();
      j2merc.COMMAND_LINE_PARAMETERS = myEditor.myProgramParameters.getComponent().getText();
    }
    catch (ConfigurationException e) {
      //can't be
    }
  }

  private class EditorPanel {
    private JPanel myWholePanel;

    private LabeledComponent<JComboBox> myModule;
    private final DefaultComboBoxModel myModuleModel = new DefaultComboBoxModel();

    private JPanel myChoosenEmulatorSettingsPlace;
    private LabeledComponent<RawCommandLineEditor> myProgramParameters;
    private JPanel myConfigurationPanel;

    private JRadioButton myUseClasses;
    private JRadioButton myUseJad;
    private JRadioButton myUseOTA;

    private TextFieldWithBrowseButton myClass;
    private JLabel myClassLabel;
    private JPanel myClassPanel;

    private TextFieldWithBrowseButton myJad;
    private JLabel myJadLabel;
    private JPanel myJadPanel;

    private JPanel myOTAPanel;
    private JPanel myUserOptionsPanel;

    private UserKeysConfigurable myUserKeysConfigurable;

    private OTASettingsConfigurable myOTASettings;

    public EmulatorEditor getEmulatorRunOptions() {
      if (myEmulatorEditor == null) {
        fillEmulatorEditor(getModule());
      }
      return myEmulatorEditor;
    }

    private EmulatorEditor myEmulatorEditor;

    public EditorPanel() {
      myConfigurationPanel.setVisible(false);
      myProgramParameters.setComponent(new RawCommandLineEditor());
      myProgramParameters.getComponent().setDialogCaption(myProgramParameters.getRawText());
      final Module[] modules = ModuleManager.getInstance(myProject).getModules();
      for (Module module : modules) {
        if (module.getModuleType() == J2MEModuleType.getInstance()) {
          myModuleModel.addElement(module);
        }
      }
      myModule.getComponent().setModel(myModuleModel);
      myModule.getComponent().setRenderer(new DefaultListCellRenderer() {
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
          final Component component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
          if (value != null) {
            setText(((Module)value).getName());
            setIcon(((Module)value).getModuleType().getNodeIcon(false));
          }
          return component;
        }
      });
      myModule.getComponent().addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          moduleChanged();
        }
      });

      myChoosenEmulatorSettingsPlace.setLayout(new BorderLayout());

      myOTAPanel.setVisible(false);
      myUserOptionsPanel.setLayout(new BorderLayout());
      myUserOptionsPanel.setVisible(false);

      myUseOTA.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          changeConfigurationTargetSelection();
        }
      });
      myUseJad.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          changeConfigurationTargetSelection();
        }
      });
      myUseClasses.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          changeConfigurationTargetSelection();
        }
      });

      myClass.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          if (getModule() == null) {
            return;
          }
          final J2MEClassBrowser j2MEClassBrowser = new J2MEClassBrowser(getModule());
          j2MEClassBrowser.show();
          j2MEClassBrowser.setField(myClass);
        }

      });


      myJad.addBrowseFolderListener(J2MEBundle.message("run.configuration.browse.file"),
                                    J2MEBundle.message("run.configuration.file.to.start"),
                                    myProject,
                                    new FileChooserDescriptor(true,
                                                              false,
                                                              false,
                                                              false,
                                                              false,
                                                              false));


      myUseJad.setSelected(true);
      myClassPanel.setVisible(false);
    }

    private void createKeysConfigurable() {
      myUserKeysConfigurable = new UserKeysConfigurable(new HashSet<UserDefinedOption>());
      myUserOptionsPanel.removeAll();
      myUserOptionsPanel.add(myUserKeysConfigurable.getUserKeysPanel(), BorderLayout.CENTER);
    }

    public JComponent getComponent() {
      fillEmulatorEditor(myJ2MERunConfiguration.getModule());
      createKeysConfigurable();
      JPanel myBorder = new JPanel(new BorderLayout());
      myBorder.add(myWholePanel, BorderLayout.CENTER);
      return myBorder;
    }

    private Module getModule() {
      return (Module)myModule.getComponent().getSelectedItem();
    }

    public void moduleChanged() {
      myConfigurationPanel.setVisible(false);
      fillEmulatorEditor(getModule());
      if (getModule() == null) return;
      final Sdk projectJdk = ModuleRootManager.getInstance(getModule()).getSdk();
      if (projectJdk == null || !MobileSdk.checkCorrectness(projectJdk, getModule())) return;
      myConfigurationPanel.setVisible(true);

      final String[] otaCommands = ((Emulator)projectJdk.getSdkAdditionalData()).getOTACommands(projectJdk.getHomePath());
      LOG.assertTrue(otaCommands != null);
      if (otaCommands.length > 0) {
        myOTASettings = new OTASettingsConfigurable(projectJdk, getModule());
        myOTASettings.setCommands(otaCommands);
        Disposer.register(J2MERunConfigurationEditor.this, myOTASettings);
        myUseOTA.setVisible(true);
      }
      else {
        myUseOTA.setVisible(false);
      }
      final EmulatorType emulatorType = MobileSdk.getEmulatorType(projectJdk, getModule());
      LOG.assertTrue(emulatorType != null);
      final MobileApplicationType mobileApplicationType = MobileModuleUtil.getMobileApplicationTypeByName(emulatorType.getApplicationType());
      LOG.assertTrue(mobileApplicationType != null);
      final String extension = mobileApplicationType.getExtension();
      myUseJad.setText(StringUtil.capitalize(extension));
      myJadLabel.setText(J2MEBundle.message("file.label", StringUtil.capitalize(extension)));
      myClassLabel.setText(StringUtil.capitalize(J2MEBundle.message("klass.label", mobileApplicationType.getPresentableClassName())));
      final MobileModuleSettings moduleSettings = MobileModuleSettings.getInstance(getModule());
      LOG.assertTrue(moduleSettings != null);
      myJad.setText(moduleSettings.getMobileDescriptionPath());
      myUseJad.setSelected(true);
      changeConfigurationTargetSelection();
    }

    private void changeConfigurationTargetSelection(){
      if (getModule() == null) return;
      myClassPanel.setVisible(myUseClasses.isSelected());
      myJadPanel.setVisible(myUseJad.isSelected());
      myUserOptionsPanel.setVisible(false);
      myOTAPanel.setVisible(false);
      if (myUseClasses.isSelected()){
        if (J2MEModuleProperties.getInstance(getModule()).getMobileApplicationType().isUserParametersEnable()) {
          myUserOptionsPanel.setVisible(true);
        }
        else {
          myUserOptionsPanel.setVisible(false);
        }
      }
      if (myUseJad.isSelected()) {
        myJadPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), myUseJad.getText().toUpperCase()));
      }
      if (myUseOTA.isSelected()) {
        myOTAPanel.removeAll();
        myOTAPanel.add(myOTASettings.createEditor(), BorderLayout.CENTER);
        myOTAPanel.setVisible(true);
      }
    }

    private void fillEmulatorEditor(final Module module) {
      if (module != null) {
        final Sdk projectJdk = ModuleRootManager.getInstance(module).getSdk();
        if (projectJdk != null && MobileSdk.checkCorrectness(projectJdk, getModule())) {
          myChoosenEmulatorSettingsPlace.removeAll();
          final Emulator emulator = (Emulator)projectJdk.getSdkAdditionalData();
          final EmulatorType emulatorType = emulator.getEmulatorType();
          LOG.assertTrue(emulatorType != null);
          myEmulatorEditor = new EmulatorEditor(myJ2MERunConfiguration, emulatorType.getAvailableSkins(projectJdk.getHomePath()), projectJdk);
          final JComponent emulatorEditor = myEmulatorEditor.createComponent();
          if (myEmulatorEditor.isVisible()) {
            myChoosenEmulatorSettingsPlace.add(emulatorEditor, BorderLayout.CENTER);
            myChoosenEmulatorSettingsPlace.setVisible(true);
          }
          else {
            myChoosenEmulatorSettingsPlace.setVisible(false);
          }
          myChoosenEmulatorSettingsPlace.updateUI();
        }
      } else {
        myChoosenEmulatorSettingsPlace.setVisible(false);
      }
    }
  }
}
