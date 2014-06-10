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
package com.intellij.j2meplugin.emulator.ui;

import com.intellij.j2meplugin.J2MEBundle;
import com.intellij.j2meplugin.emulator.Emulator;
import com.intellij.j2meplugin.emulator.EmulatorType;
import com.intellij.j2meplugin.emulator.EmulatorUtil;
import com.intellij.j2meplugin.emulator.MobileSdk;
import com.intellij.j2meplugin.emulator.midp.MIDPEmulatorType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.projectRoots.*;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.Disposer;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * User: anna
 * Date: Sep 22, 2004
 */
public class MobileSdkConfigurable implements AdditionalDataConfigurable {
  private JPanel myWholePanel;

  private JComboBox myInheritedJavaSDK;
  private final DefaultComboBoxModel myJavaSdkList = new DefaultComboBoxModel();
  private Sdk myMobileJdk;

  private final SdkModel mySdkModel;
  private final SdkModificator mySdkModificator;

  private JPanel myMIDPPanel;
  private JTextField myPreverifyOptions;
  private EmulatorType myEmulatorType;
  private MobileApiSettingsEditor myApiEditor;

  public MobileSdkConfigurable(SdkModel sdkModel, SdkModificator sdkModificator) {
    mySdkModel = sdkModel;
    mySdkModificator = sdkModificator;
    myInheritedJavaSDK.setModel(myJavaSdkList);
    myInheritedJavaSDK.setRenderer(new DefaultListCellRenderer(){
      public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        final Component listCellRendererComponent = super.getListCellRendererComponent(list, value, index, isSelected,
                                                                                       cellHasFocus);
        if (value instanceof Sdk) {
          setText(((Sdk)value).getName());
        }
        return listCellRendererComponent;
      }
    });
    myMIDPPanel.setLayout(new BorderLayout());
    restoreCurrentJavaSdks();
  }

  private void restoreCurrentJavaSdks() {
    myJavaSdkList.removeAllElements();
    Sdk[] allJdks = mySdkModel.getSdks();
    for (int i = 0; allJdks != null && i < allJdks.length; i++) {
      if (Comparing.equal(allJdks[i].getSdkType(),JavaSdk.getInstance())) {
        myJavaSdkList.addElement(allJdks[i]);
      }
    }
  }

  public JComponent createComponent() {
    return myWholePanel;
  }

  public boolean isModified() {
    boolean isModified;
    Emulator emulator = null;
    if (myMobileJdk != null &&
        myMobileJdk.getSdkAdditionalData() != null &&
        myJavaSdkList.getSelectedItem() != null) {
      emulator = (Emulator)myMobileJdk.getSdkAdditionalData();
      isModified = !Comparing.equal(emulator.getJavaSdkName(), ((Sdk)myJavaSdkList.getSelectedItem()).getName());
    } else {
      isModified = true;
    }
    if (emulator != null) {
      if (myPreverifyOptions.getText() != null) {
        isModified |= !Arrays.equals(emulator.getPreverifyOptions(), getStringArrayWithoutEmptyStrings(myPreverifyOptions.getText().split(" ")));
      }
    }
    if (myApiEditor != null) {
      isModified |= myApiEditor.isModified();
    }
    return isModified;
  }

  private static String[] getStringArrayWithoutEmptyStrings(final String[] array) {
    ArrayList<String> result = new ArrayList<String>();
    for (int i = 0; array != null && i < array.length; i++) {
      if (array[i].length() != 0) {
        result.add(array[i]);
      }
    }
    return result.toArray(new String[result.size()]);
  }

  public void apply() throws ConfigurationException {
    if (myInheritedJavaSDK.getSelectedIndex() == -1) {
      //not to remove mobile sdk when the last javaSdk was deleted
      restoreCurrentJavaSdks();
      throw new ConfigurationException(J2MEBundle.message("jdk.no.javasdk.specified"));
    }

    Sdk jdk = (Sdk)myJavaSdkList.getSelectedItem();
    if (jdk == null) {
      //not to remove mobile sdk when the last javaSdk was deleted
      restoreCurrentJavaSdks();
      throw new ConfigurationException(J2MEBundle.message("jdk.no.javasdk.specified"));
    }
    final String[] preverifyOptions = myPreverifyOptions.getText() != null ? myPreverifyOptions.getText().split(" ") : null;
    Emulator emulator = new Emulator(myEmulatorType, getStringArrayWithoutEmptyStrings(preverifyOptions), jdk.getName(), myMobileJdk.getHomePath());
    if (myApiEditor != null) {
      myApiEditor.applyEditorTo(emulator);
    }
    final SdkModificator modificator = myMobileJdk.getSdkModificator();
    modificator.setVersionString(jdk.getVersionString());
    modificator.setSdkAdditionalData(emulator);
    ApplicationManager.getApplication().runWriteAction(new Runnable() {
      public void run() {
        modificator.commitChanges();
      }
    });
  }

  public void reset() {
    if (MobileSdk.checkCorrectness(myMobileJdk, null)) {
      final Emulator emulator = (Emulator)myMobileJdk.getSdkAdditionalData();
      myJavaSdkList.setSelectedItem(emulator.getJavaSdk(mySdkModel));
      myEmulatorType = emulator.getEmulatorType();
      final String homePath = myMobileJdk.getHomePath();
      if (myEmulatorType == null){
        myEmulatorType = EmulatorUtil.getValidEmulatorType(homePath);
        if (myEmulatorType instanceof MIDPEmulatorType){
          reconfigureMIDPPanel(homePath);
        }
      }
      emulator.setHome(homePath);
      if (myApiEditor != null) {
        myMIDPPanel.setVisible(true);
        myApiEditor.resetEditorFrom(emulator);
      }
      else {
        myMIDPPanel.setVisible(false);
      }
      String[] preverifyOptions = emulator.getPreverifyOptions();
      if (preverifyOptions != null && preverifyOptions.length > 0) {
        String options = "";
        for (String preverifyOption : preverifyOptions) {
          options += preverifyOption + " ";
        }
        myPreverifyOptions.setText(options);
      }
      else {
        myPreverifyOptions.setText("");
      }
    }
  }

  public void disposeUIResources() {
    if (myApiEditor != null){
      Disposer.dispose(myApiEditor);      
    }
  }


  public void setSdk(Sdk sdk) {
    myMobileJdk = sdk;
    homePathChanged(sdk.getHomePath());
  }

  public void homePathChanged(final String homePath) {
    if (myApiEditor != null) {
      Disposer.dispose(myApiEditor);
      myApiEditor = null;
    }
    myEmulatorType = EmulatorUtil.getValidEmulatorType(homePath);
    if (!(myEmulatorType instanceof MIDPEmulatorType)) {
      myMIDPPanel.setVisible(false);
    }
    else {
      myMIDPPanel.setVisible(true);
      reconfigureMIDPPanel(homePath);
      myApiEditor.resetEditorFrom(new Emulator(myEmulatorType, null, null, homePath));
    }
  }

  private void reconfigureMIDPPanel(final String homePath) {
    myMIDPPanel.removeAll();
    myApiEditor = ((MIDPEmulatorType)myEmulatorType).getApiEditor(homePath, myMobileJdk, mySdkModificator);
    myMIDPPanel.add(myApiEditor.createEditor(), BorderLayout.CENTER);
  }

  public void addJavaSdk(Sdk sdk) {
    myJavaSdkList.addElement(sdk);
  }

  public void removeJavaSdk(Sdk sdk) {
    myJavaSdkList.removeElement(sdk);
  }

  public void updateJavaSdkList(Sdk sdk, String previousName) {
    final Sdk[] sdks = mySdkModel.getSdks();
    for (Sdk currentSdk : sdks) {
      if (Comparing.equal(currentSdk.getSdkType(), MobileSdk.getInstance())){
        final Emulator emulator = ((Emulator)currentSdk.getSdkAdditionalData());
        if (Comparing.equal(emulator.getJavaSdkName(), previousName)){
          emulator.setJavaSdk(sdk.getName());
        }
      }
    }
    restoreCurrentJavaSdks();    
  }
}
