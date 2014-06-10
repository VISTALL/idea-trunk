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
package com.intellij.j2meplugin.run.ui.editors;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.j2meplugin.emulator.Emulator;
import com.intellij.j2meplugin.emulator.EmulatorType;
import com.intellij.j2meplugin.run.J2MERunConfiguration;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.UnnamedConfigurable;
import com.intellij.openapi.projectRoots.Sdk;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

/**
 * User: anna
 * Date: Oct 17, 2004
 */
public class EmulatorEditor implements UnnamedConfigurable {
  private JPanel myDeviceOptionsPanel;
  private JButton myPreferencesButton;
  private final DefaultComboBoxModel myDevicesList = new DefaultComboBoxModel();
  private JComboBox myDevices;
  private JButton myUtilButton;
  private JPanel myDevicesPanel;
  private boolean myModified = false;
  private final Sdk myProjectJdk;
  private final J2MERunConfiguration myConfiguration;
  private final String[] myDeviceNames;
  private static final Logger LOG = Logger.getInstance("#" + EmulatorEditor.class.getName());

  public EmulatorEditor(J2MERunConfiguration j2merc, String[] devices, Sdk projectJdk) {
    myConfiguration = j2merc;
    myProjectJdk = projectJdk;
    myDeviceNames = devices;
    final EmulatorType emulatorType = getEmulatorType();
    myPreferencesButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        final String prefPath = emulatorType.getPrefPath(myProjectJdk.getHomePath());
        GeneralCommandLine generalCommandLine = new GeneralCommandLine();
        generalCommandLine.setExePath(prefPath);
        generalCommandLine.setWorkDirectory(myProjectJdk.getHomePath() + File.separator + "bin");    //todo
        startExternalProcess(generalCommandLine);
      }
    });

    myUtilButton.addActionListener(new ActionListener() {  // todo may be remove to other place
      public void actionPerformed(ActionEvent e) {
        final String utilPath = emulatorType.getUtilPath(myProjectJdk.getHomePath());

        GeneralCommandLine generalCommandLine = new GeneralCommandLine();
        generalCommandLine.setExePath(utilPath);
        startExternalProcess(generalCommandLine);
      }
    });

  }

  private static void startExternalProcess(final GeneralCommandLine generalCommandLine) {
    try {
      OSProcessHandler osProcessHandler = new OSProcessHandler(generalCommandLine.createProcess(),
                                                               generalCommandLine.getCommandLineString());
      osProcessHandler.startNotify();
      osProcessHandler.waitFor();
    }
    catch (ExecutionException e) {
      LOG.info(e);
    }
  }

  public boolean isVisible() {
    return myDevicesPanel.isVisible() ||
           myPreferencesButton.isVisible() ||
           myUtilButton.isVisible();
  }

  public JComponent createComponent() {
    if (myDeviceNames != null) {
      myDevicesPanel.setVisible(true);
      myDevicesList.removeAllElements();
      for (String deviceName : myDeviceNames) {
        myDevicesList.addElement(deviceName);
      }
      //if not set -> get default value
      //myDevices.setSelectedItem(myDevicesList.getSize() > 0 ? myDevicesList.getElementAt(0) : null);
      myDevices.setModel(myDevicesList);
      myDevices.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          myModified = true;
        }
      });
    }
    else {
      myDevicesPanel.setVisible(false);
    }
    final EmulatorType emulatorType = getEmulatorType();
    if (emulatorType.getPrefPath(myProjectJdk.getHomePath()) == null) {
      myPreferencesButton.setVisible(false);
    }
    if (emulatorType.getUtilPath(myProjectJdk.getHomePath()) == null) {
      myUtilButton.setVisible(false);
    }
    return myDeviceOptionsPanel;
  }

  public boolean isModified() {
    return myModified;
  }

  public void apply() throws ConfigurationException {
    if (myDevicesPanel.isVisible()) {
      final EmulatorType emulatorType = getEmulatorType();
      /*DeviceSpecificOption device = emulatorType.getDeviceSpecificOptions().get(EmulatorType.DEVICE);
      myConfiguration.addDeviceOption(device, (String)myDevices.getSelectedItem());*/
      myConfiguration.TARGET_DEVICE_NAME = (String)myDevices.getSelectedItem();
    }
    myModified = false;
  }

  public void reset() {
    final EmulatorType emulatorType = getEmulatorType();
    final String device = emulatorType.getDeviceOption();
    if (device != null) {
      myDevicesPanel.setVisible(true);
      myDevices.setSelectedItem(myConfiguration.TARGET_DEVICE_NAME);
    }
    else {
      myDevicesPanel.setVisible(false);
    }
    myModified = false;
  }

  @NotNull
  private EmulatorType getEmulatorType() {
    final EmulatorType emulatorType = ((Emulator)myProjectJdk.getSdkAdditionalData()).getEmulatorType();
    LOG.assertTrue(emulatorType != null);
    return emulatorType;
  }

  public void disposeUIResources() {}
}
