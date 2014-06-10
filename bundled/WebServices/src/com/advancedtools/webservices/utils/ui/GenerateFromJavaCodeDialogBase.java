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

package com.advancedtools.webservices.utils.ui;

import com.advancedtools.webservices.utils.DeployUtils;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.util.containers.ArrayListSet;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * @author Maxim Mossienko
 * @author Konstantin Bulenkov
 */
public abstract class GenerateFromJavaCodeDialogBase extends GenerateDialogBase {
  private HashMap<PsiMethod,String> myMethod2DeploymentProblemsMap;

  public GenerateFromJavaCodeDialogBase(Project _project, PsiClass clazz) {
    super(_project);

    setCurrentClass(clazz);
    startTrackingCurrentClassOrFile();
  }

  @Override
  protected void init() {
    super.init();
    initRenderers();
  }

  private void initRenderers() {
    getMethodsTable().setDefaultRenderer(
      PsiMethod.class,
      new MethodTableCellRenderer() {
        protected String getProblemByMethod(PsiMethod method) {
          return myMethod2DeploymentProblemsMap.get(method);
        }

        protected boolean getDeploymentStatus(PsiMethod method) {
          return false;
        }

        protected boolean supportsDeployment() {
          return false;
        }
      }
    );
  }

  protected abstract JTable getMethodsTable();

  public void setCurrentClass(PsiClass aClass) {
    super.setCurrentClass(aClass);
    fillMethodsTable();
  }

  private final Set<PsiClass> visited = new ArrayListSet<PsiClass>();
  private final Set<String> unresolved = new ArrayListSet<String>();

  public Set<PsiClass> getVisited() {
    return visited;
  }

  public Set<String> getUnresolved() {
    return unresolved;
  }

  private boolean requestedOnce;
  protected void fillMethodsTable() {
    if (getMethodsTable() == null) {
      if (!requestedOnce) {
        ApplicationManager.getApplication().invokeLater(new Runnable() {
          public void run() {
            fillMethodsTable();
          }
        });
        requestedOnce = true;
      }
      return;
    }

    PsiClass clazz = getCurrentClass();
    final PsiMethod[] methods = clazz != null ? clazz.getMethods(): PsiMethod.EMPTY_ARRAY;

    myMethod2DeploymentProblemsMap = new HashMap<PsiMethod, String>(methods.length);
    final List<PsiMethod> methodsAllowedForDeployment = new ArrayList<PsiMethod>(methods.length);
    final List<PsiMethod> methodsNotAllowedForDeployment = new ArrayList<PsiMethod>(methods.length);

    DeployUtils.processClassMethods(
      methods,
      new DeployUtils.DeploymentProcessor() {
        public void processMethod(PsiMethod method, String problem, List<String> nonelementaryTypes) {
          //if (method != null && method.getReturnType() != null) {
          //  DeployUtils.findReferencedTypesForType(method.getReturnType().getDeepComponentType(), visited, unresolved);
          //}
          //if (method != null) {
          //  for (PsiParameter param : method.getParameterList().getParameters()) {
          //    DeployUtils.findReferencedTypesForType(param.getType(), visited, unresolved);
          //  }
          //}
          myMethod2DeploymentProblemsMap.put(method, problem);
          if (problem == null) methodsAllowedForDeployment.add(method);
          else methodsNotAllowedForDeployment.add(method);
        }
      }
    );

    TableModel dataModel = createMethodDeploymentTable(methodsAllowedForDeployment, methodsNotAllowedForDeployment);

    getMethodsTable().setModel(dataModel);
  }

  protected MethodDeploymentTableModel createMethodDeploymentTable(List<PsiMethod> methodsAllowedForDeployment, List<PsiMethod> methodsNotAllowedForDeployment) {
    return new MethodDeploymentTableModel(methodsAllowedForDeployment, methodsNotAllowedForDeployment);
  }

  protected ValidationResult doValidate(ValidationData _data) {
    PsiClass clazz = getCurrentClass();
    String s = clazz != null ? DeployUtils.checkAccessibleClassPrerequisites(myProject, clazz) : NO_CLASS_IN_SELECTED_TEXT_EDITOR;
    initiateValidation(1000); // Class may change

    if (s != null) {
      return new ValidationResult(s, null);
    }

    return null;
  }

  public PsiMethod[] getSelectedMethods() {
    TableModel tableModel = getMethodsTable().getModel();
    if (!(tableModel instanceof MethodDeploymentTableModel)) return PsiMethod.EMPTY_ARRAY;
    return ((MethodDeploymentTableModel) tableModel).getSelectedMethods();
  }

  protected class MyValidationData extends ValidationData {
    public PsiMethod[] selectedMethods;

    protected void doAcquire() {
      selectedMethods = getSelectedMethods();
    }
  }

  protected MyValidationData createValidationData() {
    return new MyValidationData();
  }
}
