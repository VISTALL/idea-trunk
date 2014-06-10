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
import com.intellij.openapi.graph.base.Node;
import com.intellij.openapi.graph.builder.GraphBuilder;
import com.intellij.openapi.graph.builder.util.GraphViewUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.InputValidator;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiPackage;
import com.intellij.uml.actions.UmlAction;
import com.intellij.uml.model.UmlEdge;
import com.intellij.uml.model.UmlNode;
import com.intellij.uml.utils.UmlBundle;
import com.intellij.uml.utils.UmlUtils;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;

/**
 * @author Konstantin Bulenkov
 */
public abstract class CreateActionBase extends UmlAction {
  public abstract String getTitle();
  public abstract String getPrompt();
  @Nullable
  public abstract PsiClass create(PsiDirectory dir, String className);

  public Icon getDialogIcon() {
    return Messages.getQuestionIcon();
  }
    
  public void actionPerformed(final AnActionEvent e) {
    final GraphBuilder<UmlNode,UmlEdge> builder = getBuilder(e);

    if (builder == null) return;

    final InputValidator validator = getValidator(builder.getProject());
    PsiPackage psiPackage = null;
    final List<Node> nodes = GraphViewUtil.getSelectedNodes(builder.getGraph());

    if (nodes.size() == 1) {
      UmlNode node = builder.getNodeObject(nodes.get(0));
      if (node != null && node.getIdentifyingElement() instanceof PsiPackage) {
        psiPackage = (PsiPackage)node.getIdentifyingElement();
      }
    }

    if (psiPackage == null) {
      psiPackage = UmlUtils.getPackage(UmlUtils.getDataModel(builder).getInitialElement());
    }
    String prefix = (psiPackage == null) ? "" : psiPackage.getQualifiedName();
    if (prefix.length() > 0) prefix += ".";

    final String fqn = Messages.showInputDialog(builder.getProject(), getPrompt(), getTitle(), getDialogIcon(), prefix, validator);

    if (fqn == null) return;

    int dot = fqn.lastIndexOf('.');
    String className = dot > 0 ? fqn.substring(dot + 1) : fqn;
    String packageName = dot > 0 ? fqn.substring(0, dot) : "";
    final PsiPackage aPackage = JavaPsiFacade.getInstance(builder.getProject()).findPackage(packageName);
    
    if (aPackage != null) {
      PsiClass cls = create(aPackage.getDirectories()[0], className);
      if (cls == null) {
        Messages.showErrorDialog(builder.getProject(),
                                 UmlBundle.message("cant.create.element", fqn),
                                 UmlBundle.message("error"));
      } else {
        UmlUtils.getDataModel(builder).addElement(cls);
        builder.updateView();
        UmlUtils.updateGraph(builder, true, false);
      }
    }
  }

  protected InputValidator getValidator(Project project) {
    return new ClassNameValidator(project);
  }
}
