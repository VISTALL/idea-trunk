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

package com.intellij.uml.core.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.PopupChooserBuilder;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.ui.popup.PopupFactoryImpl;
import com.intellij.uml.UmlProvider;
import com.intellij.uml.utils.UmlBundle;

import javax.swing.*;

/**
 * @author Konstantin Bulenkov
 */
public abstract class ShowUmlBase extends AnAction {

  protected ShowUmlBase() {
    setInjectedContext(true);
  }

  @Override
  public void update(AnActionEvent e) {
    final UmlProvider umlProvider = UmlProvider.findProvider(e.getDataContext());
    final boolean enabled = umlProvider != null
                            && umlProvider.getElementManager().isAcceptableAsNode(umlProvider.getElementManager().findInDataContext(e.getDataContext()));
    e.getPresentation().setVisible(enabled && checkIdAndPlace(umlProvider.getID(), e.getPlace())); // TODO: remove checkIdAndPlace(...)
    e.getPresentation().setEnabled(enabled);
  }

  private static boolean checkIdAndPlace(String id, String place) {
    return !"JAVA".equals(id) || "unknown".equals(place) || "MainMenu".equals(place);
  }

  public void actionPerformed(final AnActionEvent e) {
    final DataContext context = e.getDataContext();
    final UmlProvider[] providers = UmlProvider.findProviders(context);
    final Project project = DataKeys.PROJECT.getData(context);
    if (providers.length == 0 || project == null) return;
    if (providers.length > 1) {
      String[] names = new String[providers.length];
      for (int i = 0; i < names.length; i++) {
        names[i] = providers[i].getID();
      }
      final JList jList = new JList(names);
      final PopupChooserBuilder popupBuilder = JBPopupFactory.getInstance().createListPopupBuilder(jList);
      final JBPopup popup = popupBuilder
        .setTitle(UmlBundle.message("select.uml.provider"))
        .setResizable(false)
        .setMovable(false)
        .setItemChoosenCallback(new Runnable() {
          public void run() {
            final int index = jList.getSelectedIndex();
            if (0 <= index && index < providers.length) {
              showUnderProgress(context, providers[index], project);
            }
          }
        }).createPopup();
        popup.showInBestPositionFor(context);
    } else {
      showUnderProgress(context, providers[0], project);
    }
  }

  static boolean isSupportedForElement(DataContext data) {
    final UmlProvider umlProvider = UmlProvider.findProvider(data);
    return umlProvider != null && umlProvider.getElementManager().isAcceptableAsNode(umlProvider.getElementManager().findInDataContext(data));
  }

  protected abstract Runnable show(Object element, UmlProvider provider, Project project, RelativePoint popupLocation);

  private void showUnderProgress(final DataContext context, final UmlProvider provider, final Project project) {
    final Object element = provider.getElementManager().findInDataContext(context);
    final RelativePoint point = PopupFactoryImpl.getInstance().guessBestPopupLocation(context);
    final Runnable[] postbackAction = new Runnable[1];
    ProgressManager.getInstance().runProcessWithProgressSynchronously(new Runnable() {
      public void run() {
        ProgressManager.getInstance().getProgressIndicator().setIndeterminate(true);
        postbackAction[0] = show(element, provider, project, point);
      }
    }, UmlBundle.message("building.uml.class.diagram"), true, project);
    if (postbackAction[0] != null) {
      postbackAction[0].run();
    }
  }
}
