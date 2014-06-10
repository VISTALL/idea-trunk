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

package com.advancedtools.webservices.jaxb;

import com.advancedtools.webservices.WSBundle;
import com.advancedtools.webservices.WebServicesPluginSettings;
import com.advancedtools.webservices.actions.WebServicePlatformUtils;
import com.advancedtools.webservices.utils.LibUtils;
import com.advancedtools.webservices.utils.ui.GenerateDialogBase;
import com.advancedtools.webservices.utils.ui.GenerateFromJavaCodeDialogBase;
import com.advancedtools.webservices.utils.ui.MethodDeploymentTableModel;
import com.advancedtools.webservices.utils.ui.MyDialogWrapper;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;

/**
 * @by maxim
 */
class GenerateJAXBSchemasFromJavaDialog extends GenerateFromJavaCodeDialogBase {
  private JPanel myPanel;
  private JLabel className;
  private JLabel statusText;
  private JLabel status;
  private JLabel classNameText;
  private JCheckBox includeFollowingMethods;
  private JTable methodsTable;
  private JScrollPane myMethodsScrollPane;

  public GenerateJAXBSchemasFromJavaDialog(Project _project, PsiClass clazz, @Nullable final GenerateJAXBSchemasFromJavaDialog previousDialog) {
    super(_project, clazz);

    setTitle(WSBundle.message("generate.xml.schema.from.java.using.jaxb.dialog.title"));

    includeFollowingMethods.setSelected(true);
    includeFollowingMethods.setMnemonic('i');
    includeFollowingMethods.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        final boolean selected = includeFollowingMethods.isSelected();

        myMethodsScrollPane.setVisible(selected);
        myMethodsScrollPane.getParent().invalidate();
        myMethodsScrollPane.getParent().validate();
        pack();
      }
    });

    if (previousDialog != null) {
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          setCurrentClass(previousDialog.getCurrentClass());
          methodsTable.setModel(previousDialog.getMethodsTable().getModel());
          includeFollowingMethods.setSelected(previousDialog.includeFollowingMethods.isSelected());
        }
      });
    } else {
      includeFollowingMethods.setSelected(false);
    }
    
    init();
  }

  protected JTable getMethodsTable() {
    return methodsTable;
  }

  protected ValidationResult doValidate(ValidationData _data) {
    MyDialogWrapper.ValidationResult validationResult = checkJWSDPPathSet(
      this,
      LibUtils.getModuleFromClass(getCurrentClass())
    );
    if (validationResult != null) return validationResult;

    final String path = WebServicesPluginSettings.getInstance().getEngineManager().getExternalEngineByName(JaxbMappingEngine.JAXB_2_ENGINE).getBasePath();
    if (path == null && LibUtils.getLibUrlsForToolRunning("com.sun.tools.internal.jxc.SchemaGenerator", myProject).length == 0) {
      return new ValidationResult("Please, specify JWSDP/Glassfish Path in plugin settings", null);
    }

    validationResult = super.doValidate(_data);

    if (validationResult == null) {
      final MyValidationData validationData = (MyValidationData) _data;

      if (validationData.includeTypesOfMethods && validationData.selectedMethods.length == 0) {
        return new ValidationResult("No methods selected", null);
      }
    }

    return validationResult;
  }

  static ValidationResult checkJWSDPPathSet(GenerateDialogBase dialog, Module module) {
    if (WebServicesPluginSettings.getInstance().getJwsdpPath() == null &&
      !WebServicePlatformUtils.isJdk1_6SetUpForModule(module)
      ) {
      return dialog.new ValidationResult("Please, specify JWSDP/Glassfish Path in plugin settings or use JDK 6", null, 2000);
    }
    return null;
  }

  protected MyValidationData createValidationData() {
    return new MyValidationData();
  }

  class MyValidationData extends GenerateFromJavaCodeDialogBase.MyValidationData {
    boolean includeTypesOfMethods;

    protected void doAcquire() {
      includeTypesOfMethods = toIncludeTypesOfMethods();
      super.doAcquire();
    }
  }

  protected MethodDeploymentTableModel createMethodDeploymentTable(List<PsiMethod> methodsAllowedForDeployment, List<PsiMethod> methodsNotAllowedForDeployment) {
    return new MethodDeploymentTableModel(methodsAllowedForDeployment, methodsNotAllowedForDeployment) {
      public String getColumnName(int column) {
        if (column == 0) return "Add to JAXB Generation";
        return "Parameter / return types of following method";
      }
    };
  }

  protected JComponent getClassName() {
    return className;
  }

  protected JComponent createCenterPanel() {
    return myPanel;
  }

  boolean toIncludeTypesOfMethods() {
    return includeFollowingMethods.isSelected();
  }

  protected JLabel getStatusTextField() {
    return statusText;
  }

  protected JLabel getStatusField() {
    return status;
  }

  @NotNull
  protected String getHelpId() {
    return "GenerateJAXBSchemasFromJava.html";
  }
}
