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
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.uml.actions.UmlAction;
import com.intellij.uml.model.UmlEdge;
import com.intellij.uml.model.UmlNode;
import com.intellij.uml.utils.UmlUtils;

import javax.swing.*;
import java.awt.*;

/**
 * @author Konstantin Bulenkov
 */
public class AddEnumConstant  extends UmlAction {
  @Override
  public void update(final AnActionEvent e) {
    super.update(e);
    final GraphBuilder<UmlNode,UmlEdge> builder = getBuilder(e);

    if (builder == null) return;

    final PsiElement psiElement = DataKeys.PSI_ELEMENT.getData(e.getDataContext());
    e.getPresentation().setEnabled(psiElement instanceof PsiClass && ((PsiClass)psiElement).isEnum());
  }

  public void actionPerformed(final AnActionEvent e) {
    final GraphBuilder<UmlNode, UmlEdge> builder = getBuilder(e);
    final PsiClass psiClass = (PsiClass)DataKeys.PSI_ELEMENT.getData(e.getDataContext());
    if (builder == null || psiClass == null || !psiClass.isEnum()) return;

    if (! prepareClassForWrite(psiClass)) return;

    final JDialog dialog = new CreateNewEnumConstantDialog(psiClass);
    final JComponent view = builder.getView().getCanvasComponent();
    final Point vP = view.getLocationOnScreen();
    final Dimension dSize = dialog.getPreferredSize();
    final Dimension vSize = view.getSize();
    final Point p = new Point(vP.x + (vSize.height - dSize.height)/2 , vP.y + (vSize.width - dSize.width)/2);
    dialog.setLocation(p);
    dialog.setVisible(true);
    UmlUtils.updateGraph(builder, true, false);
  }
}