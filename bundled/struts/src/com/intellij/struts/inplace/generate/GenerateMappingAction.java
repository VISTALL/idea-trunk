/*
 * Copyright 2000-2007 JetBrains s.r.o.
 *
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

package com.intellij.struts.inplace.generate;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.ui.actions.generate.GenerateDomElementAction;

import javax.swing.*;

/**
 * @author Dmitry Avdeev
 */
@SuppressWarnings({"ComponentNotRegistered"})
public class GenerateMappingAction<T extends DomElement> extends GenerateDomElementAction {

  public GenerateMappingAction(final GenerateMappingProvider<T> provider, final Icon icon) {
    super(provider);
    getTemplatePresentation().setIcon(icon);
  }

  protected boolean isValidForFile(final Project project, final Editor editor, final PsiFile file) {
    return ((GenerateMappingProvider)myProvider).getParentDomElement(project, editor, file) != null;
  }
}
