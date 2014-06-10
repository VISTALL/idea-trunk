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
package org.jetbrains.idea.tomcat;

import com.intellij.javaee.appServerIntegrations.ApplicationServerPersistentDataEditor;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.DocumentAdapter;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.io.File;
import java.awt.*;

public class TomcatDataEditor extends ApplicationServerPersistentDataEditor<TomcatPersistentData> {
  private JPanel myPanel;
  private TextFieldWithBrowseButton myHomeDir;
  private TextFieldWithBrowseButton myBaseDir;
  private JLabel myVersionLabel;
  private JLabel myErrorLabel;
  private JPanel myInfoPanel;
  private boolean myShouldSyncBase = true;
  private boolean myIsSyncing = false;

  public TomcatDataEditor() {
    myErrorLabel.setIcon(IconLoader.getIcon("/runConfigurations/configurationWarning.png"));
    updateInfoPanel(true);


    initChooser(myHomeDir, TomcatBundle.message("chooser.title.tomcat.home.directory"), TomcatBundle.message("chooser.description.tomcat.home.directory"));
    initChooser(myBaseDir, TomcatBundle.message("chooser.title.tomcat.base.directory"), TomcatBundle.message("chooser.description.tomcat.base.directory"));

    myHomeDir.getTextField().getDocument().addDocumentListener(new DocumentAdapter() {
      public void textChanged(DocumentEvent event) {
        update();
      }
    });

    myBaseDir.getTextField().getDocument().addDocumentListener(new DocumentAdapter() {
      public void textChanged(DocumentEvent event) {
        if (!myIsSyncing) myShouldSyncBase = false;
        updateErrorLabel();
      }
    });
  }

  private void updateInfoPanel(final boolean valid) {
    ((CardLayout)myInfoPanel.getLayout()).show(myInfoPanel, valid ? "valid" : "error");
  }

  private static void initChooser(TextFieldWithBrowseButton field, String title, String description) {
    field.getTextField().setEditable(true);
    field.addBrowseFolderListener(title, description, null, new FileChooserDescriptor(false, true, false, false, false, false));
  }

  private void update() {
    myVersionLabel.setText(getVersion());

    if (myShouldSyncBase) {
      syncBaseDirField();
    }

    updateErrorLabel();
  }

  private void syncBaseDirField() {
    myIsSyncing = true;
    try {
      myBaseDir.setText(myHomeDir.getText());
    }
    finally {
      myIsSyncing = false;
    }
  }

  private void updateErrorLabel() {
    boolean hasError = false;
    try {
      checkDirectories();
    }
    catch (ConfigurationException e) {
      myErrorLabel.setText(e.getMessage());
      hasError = true;
    }
    updateInfoPanel(!hasError);
  }

  private String getVersion() {
    String homeDir = myHomeDir.getText();
    if (homeDir.length() == 0 || !new File(homeDir, "bin").isDirectory()) {
      return "";
    }
    @NonNls final String pathToServletJar = homeDir + File.separator + "common" + File.separator + "lib" + File.separator + "servlet.jar";
    File jar40 = new File(pathToServletJar);
    if (jar40.exists()) {
      return TomcatPersistentData.VERSION40;
    }
    @NonNls final String pathToServletApiJar = homeDir + File.separator + "lib" + File.separator + "servlet-api.jar";
    File jar60 = new File(pathToServletApiJar);
    if (jar60.exists()) {
      return TomcatPersistentData.VERSION60;
    }
    return TomcatPersistentData.VERSION50;
  }

  public void resetEditorFrom(TomcatPersistentData data) {
    myHomeDir.setText(data.CATALINA_HOME.replace('/', File.separatorChar));
    myShouldSyncBase = data.CATALINA_BASE.length() == 0;
    if (myShouldSyncBase) {
      syncBaseDirField();
    }
    else {
      myBaseDir.setText(data.CATALINA_BASE.replace('/', File.separatorChar));
    }
    update();
  }

  public void applyEditorTo(TomcatPersistentData data) throws ConfigurationException {
    String version = myVersionLabel.getText();
    if (version.length() == 0) {
      version = TomcatPersistentData.VERSION50;
    }

    File home = new File(myHomeDir.getText()).getAbsoluteFile();
    File base = new File(myBaseDir.getText()).getAbsoluteFile();
    data.CATALINA_HOME = home.getAbsolutePath().replace(File.separatorChar, '/');
    data.CATALINA_BASE = base.getAbsolutePath().replace(File.separatorChar, '/');
    if (data.CATALINA_BASE.equals(data.CATALINA_HOME)) {
      data.CATALINA_BASE = "";
      myShouldSyncBase = true;
    }
    else {
      myShouldSyncBase = false;
    }

    data.VERSION = version;
  }

  private void checkDirectories() throws ConfigurationException {
    if (StringUtil.isEmptyOrSpaces(myHomeDir.getText())) {
      throw new ConfigurationException(TomcatBundle.message("error.message.tomcat.home.path.should.not.be.empty"));
    }
    File home = new File(myHomeDir.getText()).getAbsoluteFile();

    checkIsDirectory(home);
    checkIsDirectory(new File(home, TomcatConstants.CATALINA_CONFIG_DIRECTORY_NAME));
    checkIsDirectory(new File(home, TomcatConstants.CATALINA_BIN_DIRECTORY_NAME));
    if (TomcatPersistentData.VERSION60.equals(myVersionLabel.getText())) {
      checkIsDirectory(new File(home, TomcatConstants.CATALINA_LIB_DIRECTORY_NAME));
    }
    else {
      File common = new File(home, TomcatConstants.CATALINA_COMMON_DIRECTORY_NAME);
      checkIsDirectory(common);
      checkIsDirectory(new File(common, TomcatConstants.CATALINA_LIB_DIRECTORY_NAME));
    }

    if (StringUtil.isEmptyOrSpaces(myBaseDir.getText())) {
      throw new ConfigurationException(TomcatBundle.message("error.message.tomcat.base.directory.path.should.not.be.empty"));
    }
    File base = new File(myBaseDir.getText()).getAbsoluteFile();
    checkIsDirectory(base);
    checkIsDirectory(new File(base, TomcatConstants.CATALINA_CONFIG_DIRECTORY_NAME));
  }

  private static void checkIsDirectory(File file) throws ConfigurationException {
    if (!file.isDirectory()) {
      throw new ConfigurationException(TomcatBundle.message("message.text.cant.find.directory", file.getAbsolutePath()));
    }
  }

  @NotNull
  public JComponent createEditor() {
    return myPanel;
  }

  public void disposeEditor() {
  }

  protected JComponent createCenterPanel() {
    return myPanel;
  }
}