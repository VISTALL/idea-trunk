/*
 * Copyright 2000-2007 JetBrains s.r.o.
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

package org.jetbrains.plugins.grails.actions;

import com.intellij.ide.IdeView;
import com.intellij.openapi.actionSystem.*;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.grails.GrailsBundle;
import org.jetbrains.plugins.grails.GrailsIcons;
import org.jetbrains.plugins.grails.util.GrailsUtils;
import org.jetbrains.plugins.groovy.actions.GroovyTemplatesFactory;
import org.jetbrains.plugins.groovy.actions.NewGroovyActionBase;

/**
 * @author ilyas
 */
public class NewGspAction extends NewGroovyActionBase {
  private static final String POINT_GSP = ".gsp";

  public NewGspAction() {
    super(GrailsBundle.message("gsp.menu.action.text"),
            GrailsBundle.message("gsp.menu.action.description"),
            GrailsIcons.GSP_FILE_TYPE);
  }

  protected String getActionName(PsiDirectory directory, String newName) {
    return null;
  }

  protected String getDialogPrompt() {
    return GrailsBundle.message("gsp.dlg.prompt");
  }

  protected String getDialogTitle() {
    return GrailsBundle.message("gsp.dlg.title");
  }

  protected String getCommandName() {
    return GrailsBundle.message("gsp.command.name");
  }

  @Override
  protected boolean isAvailable(DataContext dataContext) {
    return super.isAvailable(dataContext) && isInWebAppOrGrailsViewsDirectory(dataContext);
  }

  public void update(final AnActionEvent e) {
    final Presentation presentation = e.getPresentation();

    super.update(e);

    if (presentation.isEnabled()) {
      final IdeView view = e.getData(DataKeys.IDE_VIEW);
      if (view != null) {
        for (PsiDirectory dir : view.getDirectories()) {
          if (GrailsUtils.isUnderGrailsViewsDirectory(dir)) {
            presentation.setWeight(Presentation.HIGHER_WEIGHT);
            return;
          }
        }
      }
    }
  }

  private static boolean isInWebAppOrGrailsViewsDirectory(final DataContext dataContext) {
    final IdeView view = DataKeys.IDE_VIEW.getData(dataContext);

    if (!GrailsUtils.hasGrailsSupport(DataKeys.MODULE.getData(dataContext))) {
      return false;
    }

    if (view != null) {
      for (PsiDirectory dir : view.getDirectories()) {
        if (GrailsUtils.isUnderWebAppDirectory(dir) || GrailsUtils.isUnderGrailsViewsDirectory(dir)) {
          return true;
        }
      }
    }

    return false;
  }

  protected final void checkBeforeCreate(String newName, PsiDirectory directory) throws IncorrectOperationException {
    boolean isCorrectName = newName != null && newName.matches("(\\w+\\.)*\\w+");
    if (!isCorrectName) {
      throw new IncorrectOperationException(GrailsBundle.message("0.is.not.gsp.name", newName));
    }
  }

  @NotNull
  protected PsiElement[] doCreate(String newName, PsiDirectory directory) throws Exception {
    String templateName = "GroovyServerPage.gsp";
    if (newName.endsWith(POINT_GSP)) newName = newName.substring(0, newName.length() - POINT_GSP.length());
    PsiFile psiFile = GroovyTemplatesFactory.createFromTemplate(directory, newName, newName + POINT_GSP, templateName);
    return new PsiElement[]{psiFile};
  }

}
