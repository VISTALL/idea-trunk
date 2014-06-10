/*
 * Copyright 2000-2008 JetBrains s.r.o.
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

package com.intellij.uml.actions.create;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.graph.builder.GraphBuilder;
import com.intellij.openapi.graph.builder.util.GraphViewUtil;
import com.intellij.openapi.graph.base.Node;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.pom.java.LanguageLevel;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiUtil;
import com.intellij.uml.actions.UmlAction;
import com.intellij.uml.model.UmlEdge;
import com.intellij.uml.model.UmlNode;
import com.intellij.uml.utils.UmlUtils;
import com.intellij.uml.utils.UmlPsiUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NonNls;

/**
 * @author Konstantin Bulenkov
 */
public abstract class CreateNewMethodBase extends UmlAction {
  private static final Logger LOG = Logger.getInstance("#com.intellij.uml.actions.create.CreateNewMethodBase");
  private Project project;

  public abstract boolean isConstructor();

  @Override
  public void update(final AnActionEvent e) {
    final GraphBuilder<UmlNode, UmlEdge> builder = getBuilder(e);
    boolean enabled = (builder != null && GraphViewUtil.getSelectedNodes(builder.getGraph()).size() == 1);
    if (enabled) {
      final Node node = GraphViewUtil.getSelectedNodes(builder.getGraph()).get(0);
      final UmlNode umlNode = builder.getNodeObject(node);
      if (umlNode == null) {
        enabled = false;
      } else {
        enabled = umlNode.getIdentifyingElement() instanceof PsiClass;
        if (enabled && isConstructor()) {
          PsiClass myClass = (PsiClass)umlNode.getIdentifyingElement();
          enabled = !myClass.isInterface();
        }
      }
    }
    e.getPresentation().setEnabled(enabled);
  }

  public void actionPerformed(final AnActionEvent e) {
    final GraphBuilder<UmlNode,UmlEdge> builder = getBuilder(e);
    PsiElement element = DataKeys.PSI_ELEMENT.getData(e.getDataContext());

    if (!(element instanceof PsiClass) || builder == null) return;

    final PsiClass myClass = (PsiClass)element;
    if (! prepareClassForWrite(myClass)) return;
    
    project = myClass.getProject();
    final PsiElementFactory factory = JavaPsiFacade.getInstance(project).getElementFactory();
    try {
      final PsiMethod method = isConstructor() ?
                               factory.createConstructor()
                               :
                               factory.createMethodFromText(myClass.isInterface() ? "void method();" : "public void method() {}", 
                                                            myClass,
                                                            LanguageLevel.HIGHEST);
      final CreateNewMethodDialog dialog = new CreateNewMethodDialog(project, method, myClass);
      dialog.show();
      if (dialog.getExitCode() == DialogWrapper.OK_EXIT_CODE) {
        if (!isConstructor() && isReturnStatementRequired(method, myClass)) {
          insertReturnStatement(method);
        }
        if (method.hasModifierProperty(PsiModifier.ABSTRACT)) {
          UmlPsiUtil.runWriteActionInCommandProcessor(new Runnable() {
            public void run() {
              try {
                final PsiCodeBlock body = method.getBody();
                if (body != null) body.delete();
                final PsiModifierList modifierList = myClass.getModifierList();
                if (modifierList != null) modifierList.setModifierProperty(PsiModifier.ABSTRACT, true);
              }
              catch (IncorrectOperationException e1) {
                LOG.error(e1);
              }
            }
          });
        }
        UmlPsiUtil.runWriteActionInCommandProcessor(new Runnable() {
          public void run() {
            final PsiMethod[] methods = myClass.getMethods();
            final PsiMethod[] constructors = myClass.getConstructors();
            final PsiField[] fields = myClass.getFields();
            PsiElement anchor;

            if (methods.length == 0 && fields.length == 0) {
              anchor = myClass.getLBrace();
            } else if (methods.length != 0) {
              if (isConstructor()) {
                if (constructors.length != 0) {
                anchor = constructors[constructors.length - 1];
                } else {
                  anchor = fields.length == 0 ? myClass.getLBrace() : fields[fields.length - 1];
                }
              } else {
                anchor = methods[methods.length - 1];
              }
            } else {
              anchor = fields[fields.length - 1];
            }

            try {
              myClass.addAfter(method, anchor);
            } catch (IncorrectOperationException e1) {
              LOG.error(e1);
            }
          }
        });
      }
    } catch (IncorrectOperationException e1) {
      LOG.error(e1);
    }
    UmlPsiUtil.reformat(myClass);
    UmlUtils.updateGraph(builder, true, false);
  }

  void insertReturnStatement(final PsiMethod method) {
    UmlPsiUtil.runWriteActionInCommandProcessor(new Runnable() {
      public void run() {

        final PsiType type = method.getReturnType();
        if (type == null) return;
        if (PsiType.VOID.equals(type)) return;
        @NonNls String returnStatement = "return null;";
        if (type.equals(PsiType.BOOLEAN)) {
          returnStatement = "return false;";
        } else if (type instanceof PsiPrimitiveType && !type.equals(PsiType.NULL)) {
          returnStatement = "return 0;";
        }
        final String code = returnStatement;
        UmlPsiUtil.runWriteActionInCommandProcessor(new Runnable() {
          public void run() {
            try {
              final PsiStatement returnCode = JavaPsiFacade.getInstance(project).getElementFactory().createStatementFromText(code, method);
              final PsiCodeBlock body = method.getBody();
              if (body == null) {
                PsiUtil.setModifierProperty(method, PsiModifier.ABSTRACT, true);
              } else {
                body.add(returnCode);
              }
            } catch (Exception e) {
              LOG.error(e);
            }
          }
        });
      }
    });
  }

  static boolean isReturnStatementRequired(PsiMethod method, PsiClass container) {
    if (container.isInterface()) return false;
    final PsiType returnType = method.getReturnType();
    if (returnType == null || returnType.equals(PsiType.VOID) || returnType.equals(PsiType.NULL)) return false;
    final PsiModifierList modifiers = method.getModifierList();
    if (modifiers.hasModifierProperty(PsiModifier.ABSTRACT)) return false;
    return true;
  }
}
