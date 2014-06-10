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

import com.intellij.featureStatistics.FeatureUsageTracker;
import com.intellij.ide.actions.GotoActionBase;
import com.intellij.ide.util.gotoByName.ChooseByNamePopup;
import com.intellij.ide.util.gotoByName.ChooseByNamePopupComponent;
import com.intellij.ide.util.gotoByName.GotoClassModel2;
import com.intellij.navigation.ChooseByNameRegistry;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.graph.builder.GraphBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.uml.model.UmlEdge;
import com.intellij.uml.model.UmlNode;
import com.intellij.uml.utils.UmlBundle;
import com.intellij.uml.utils.UmlUtils;

/**
 * @author Konstantin Bulenkov
 */
public class UmlAddClassAction extends GotoActionBase {
  public void gotoActionPerformed(final AnActionEvent e) {
    final Project project = e.getData(PlatformDataKeys.PROJECT);
    final GraphBuilder<UmlNode,UmlEdge> builder = UmlAction.getBuilder(e);

    if (project == null || builder == null) return;

    FeatureUsageTracker.getInstance().triggerFeatureUsed("navigation.popup.class");
    PsiDocumentManager.getInstance(project).commitAllDocuments();

    final ChooseByNamePopup popup = ChooseByNamePopup.createPopup(project, new UmlAddClassModel(project), getPsiContext(e));    

    popup.invoke(new ChooseByNamePopupComponent.Callback() {
      public void onClose() {
        if (UmlAddClassAction.class.equals(myInAction)) myInAction = null;
      }

      public void elementChosen(Object element) {
        if (builder == null || !(element instanceof PsiElement)) return;
        UmlUtils.getDataModel(builder).addElement((PsiElement)element);
        //UmlUtils.updateGraph(builder, false, false);
      }
    }, ModalityState.current(), true);
  }

  protected boolean hasContributors() {
    return ChooseByNameRegistry.getInstance().getClassModelContributors().length > 0;
  }

  private static class UmlAddClassModel extends GotoClassModel2 {
    public UmlAddClassModel(Project project) {
      super(project);
    }

    @Override
    public String getPromptText() {
      return UmlBundle.message("prompt.enter.class.name.to.add");
    }
  }
}
