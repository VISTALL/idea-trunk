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

package com.intellij.uml.actions;

import com.intellij.openapi.graph.GraphUtil;
import com.intellij.openapi.graph.base.Edge;
import com.intellij.openapi.graph.base.Node;
import com.intellij.openapi.graph.builder.GraphBuilder;
import com.intellij.openapi.graph.builder.util.GraphViewUtil;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.psi.*;
import com.intellij.refactoring.util.RefactoringUtil;
import com.intellij.uml.model.UmlClassDiagramDataModel;
import com.intellij.uml.model.UmlEdge;
import com.intellij.uml.model.UmlNode;
import com.intellij.uml.model.UmlRelationship;
import com.intellij.uml.utils.UmlBundle;
import com.intellij.uml.utils.UmlPsiUtil;
import com.intellij.uml.utils.UmlUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.List;

/**
 * @author Konstantin Bulenkov
 */
public class DeleteSelectionWrapper extends GraphActionWrapper {
  public DeleteSelectionWrapper(@NotNull AbstractAction action, @NotNull GraphBuilder<UmlNode, UmlEdge> builder) {
    super(action, builder);
  }

  public void actionPerformed(final ActionEvent e) {
    final List<Node> selectedNodes = GraphViewUtil.getSelectedNodes(getBuilder().getGraph());
    final List<Edge> selectedEdges = GraphViewUtil.getSelectedEdges(getBuilder().getGraph());
    if (selectedEdges.size() > 0 && selectedNodes.size() == 0) {
      for (Edge selectedEdge : selectedEdges) {
        final UmlEdge edge = getBuilder().getEdgeObject(selectedEdge);

        if (edge == null) return;

        final PsiElement source = edge.getSource().getIdentifyingElement();
        final PsiElement target = edge.getTarget().getIdentifyingElement();
        final UmlRelationship relationship = UmlUtils.getRelationship(edge);
        if (source instanceof PsiClass && target instanceof PsiClass && relationship != null) {
          final PsiClass src = (PsiClass)source;
          final PsiClass trg = (PsiClass)target;
          if (UmlAction.prepareClassForWrite(src)) {
            //int exitcode = Messages.showYesNoDialog(src.getProject(),
            //                                        getMessage(src, trg, relationship),
            //                                        UmlBundle.message("remove.relationship.link"),
            //                                        Messages.getQuestionIcon());
            //if (exitcode == DialogWrapper.OK_EXIT_CODE) {
            UmlPsiUtil.runWriteActionInCommandProcessor(new Runnable() {
              public void run() {
                try {
                  final PsiReferenceList extendsList = src.getExtendsList();
                  final PsiReferenceList implementsList = src.getImplementsList();
                  if (relationship == UmlRelationship.GENERALIZATION) {
                    RefactoringUtil.removeFromReferenceList(extendsList, trg);
                  } else if (relationship == UmlRelationship.REALIZATION) {
                    if (src.isInterface()) {
                      RefactoringUtil.removeFromReferenceList(extendsList, trg);

                    } else {
                      RefactoringUtil.removeFromReferenceList(implementsList, trg);
                    }
                  } else if (relationship == UmlRelationship.ANNOTATION) {
                    final PsiModifierList list = src.getModifierList();
                    if (list != null) {
                      for (PsiAnnotation annotation : list.getAnnotations()) {
                        if (annotation.isPhysical() &&
                            annotation.isValid() &&
                            UmlUtils.isEqual(annotation.getQualifiedName(), trg.getQualifiedName())) {
                          annotation.delete();
                        }
                      }
                    }
                  }
                } catch (Exception ex) {//
                }
              }
            });
            //}
          }
        }
      }
    } else {
      for (Node node : selectedNodes) {
        UmlNode umlNode = getBuilder().getNodeObject(node);
        if (umlNode == null) continue;
        PsiElement element = umlNode.getIdentifyingElement();
        ((UmlClassDiagramDataModel)getBuilder().getGraphDataModel()).removeElement(element);
      }
    }
    final boolean updateLayout = UmlUtils.getPresentationModel(getBuilder()).isPopupMode() && !selectedNodes.isEmpty();
    UmlUtils.updateGraph(getBuilder(), updateLayout, updateLayout);
    if (updateLayout) {
      final JBPopup popup = getBuilder().getUserData(UmlDataKeys.UML_POPUP);
      if (popup != null) {
        getBuilder().getView().updateView();
        GraphUtil.setBestPopupSizeForGraph(popup, getBuilder());
      }
    }
  }

  public static String getMessage(final PsiClass source, final PsiClass target, final UmlRelationship relationship) {
    if (relationship == UmlRelationship.ANNOTATION) {
      return UmlBundle.message("remove.annotation.from.class", target.getName(), source.getName());
    } else {
      return UmlBundle.message("this.will.remove.relationship.link.between.classes", source.getQualifiedName());
    }
  }
}
