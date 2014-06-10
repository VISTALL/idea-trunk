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

package org.jetbrains.android.newProject;

import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.lexer.JavaLexer;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.roots.libraries.LibraryTablesRegistrar;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.pom.java.LanguageLevel;
import com.intellij.psi.JavaTokenType;
import com.intellij.ui.DocumentAdapter;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.android.sdk.AndroidPlatformChooser;
import org.jetbrains.android.sdk.AndroidPlatform;
import org.jetbrains.android.util.AndroidBundle;
import org.jetbrains.android.util.AndroidUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by IntelliJ IDEA.
 * User: Eugene.Kudelevsky
 * Date: Jun 26, 2009
 * Time: 7:43:13 PM
 * To change this template use File | Settings | File Templates.
 */
public class AndroidModuleWizardStep extends ModuleWizardStep {
  private final AndroidPlatformChooser myPlatformChooser;
  private final AndroidModuleBuilder myModuleBuilder;
  private final LibraryTable.ModifiableModel myModel;

  private JPanel myPanel;
  private JPanel mySdkPanel;
  private JTextField myApplicationNameField;
  private JTextField myPackageNameField;
  private JTextField myActivityNameField;
  private JLabel myErrorLabel;
  private JCheckBox myHelloAndroidCheckBox;
  private JPanel myActivtiyPanel;

  public AndroidModuleWizardStep(@NotNull AndroidModuleBuilder moduleBuilder) {
    super();
    myModel = LibraryTablesRegistrar.getInstance().getLibraryTable().getModifiableModel();
    myPlatformChooser = new AndroidPlatformChooser(myModel, null);
    myPlatformChooser.rebuildPlatforms();
    myApplicationNameField.setText(moduleBuilder.getName());
    mySdkPanel.setLayout(new BorderLayout(1, 1));
    mySdkPanel.add(myPlatformChooser.getComponent());
    myModuleBuilder = moduleBuilder;
    myHelloAndroidCheckBox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        UIUtil.setEnabled(myActivtiyPanel, myHelloAndroidCheckBox.isSelected(), true);
      }
    });
    myPackageNameField.getDocument().addDocumentListener(new DocumentAdapter() {
      @Override
      protected void textChanged(DocumentEvent e) {
        String message = validatePackageName();
        myErrorLabel.setText(message);
      }
    });
    myActivityNameField.getDocument().addDocumentListener(new DocumentAdapter() {
      @Override
      protected void textChanged(DocumentEvent e) {
        String message = validateActivityName();
        myErrorLabel.setText(message);
      }
    });
  }

  public static boolean isValidPackageName(@NotNull String name) {
    int index = 0;
    while (true) {
      int index1 = name.indexOf('.', index);
      if (index1 < 0) index1 = name.length();
      if (!isIdentifier(name.substring(index, index1))) return false;
      if (index1 == name.length()) return true;
      index = index1 + 1;
    }
  }

  private static boolean isIdentifier(@NotNull String candidate) {
    ApplicationManager.getApplication().assertReadAccessAllowed();
    Lexer lexer = new JavaLexer(LanguageLevel.JDK_1_3);
    lexer.start(candidate);
    if (lexer.getTokenType() != JavaTokenType.IDENTIFIER) return false;
    lexer.advance();
    return lexer.getTokenType() == null;
  }

  private String validatePackageName() {
    String candidate = myPackageNameField.getText().trim();
    if (candidate.length() == 0) {
      return AndroidBundle.message("specify.package.name.error");
    }
    if (!isValidPackageName(candidate)) {
      return AndroidBundle.message("not.valid.package.name.error", candidate);
    }
    if (!AndroidUtils.contains2Ids(candidate)) {
      return AndroidBundle.message("package.name.must.contain.2.ids.error");
    }
    return "";
  }

  private String validateActivityName() {
    String candidate = myActivityNameField.getText().trim();
    if (!isIdentifier(candidate)) {
      return AndroidBundle.message("not.valid.acvitiy.name.error", candidate);
    }
    return "";
  }

  public JComponent getComponent() {
    myApplicationNameField.setText(myModuleBuilder.getName());
    return myPanel;
  }

  @Override
  public boolean validate() throws ConfigurationException {
    if (myPlatformChooser.getSelectedPlatform() == null) {
      throw new ConfigurationException(AndroidBundle.message("select.platform.error"));
    }
    String message = validatePackageName();
    if (message.length() > 0) {
      throw new ConfigurationException(message);
    }
    message = validateActivityName();
    if (message.length() > 0) {
      throw new ConfigurationException(message);
    }
    return true;
  }

  public void updateDataModel() {
    AndroidPlatform selectedPlatform = myPlatformChooser.getSelectedPlatform();
    assert selectedPlatform != null;
    myModuleBuilder.setPlatform(selectedPlatform);
    myModuleBuilder.setActivityName(myHelloAndroidCheckBox.isSelected() ? myActivityNameField.getText().trim() : "");
    myModuleBuilder.setPackageName(myPackageNameField.getText().trim());
    myModuleBuilder.setApplicationName(myApplicationNameField.getText().trim());
  }

  @Override
  public void onStepLeaving() {
    myPlatformChooser.apply();
    ApplicationManager.getApplication().runWriteAction(new Runnable() {
      public void run() {
        myModel.commit();
      }
    });
  }

  @Override
  public String getHelpId() {
    return "reference.dialogs.new.project.fromScratch.android"; 
  }

  @Override
  public void disposeUIResources() {
    Disposer.dispose(myPlatformChooser);
  }
}
