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

package org.jetbrains.plugins.grails.projectView.v2.nodes.leafs;

import com.intellij.ide.projectView.ViewSettings;
import com.intellij.openapi.module.Module;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.groovy.mvc.projectView.NodeId;
import org.jetbrains.plugins.groovy.mvc.projectView.FileNode;

/**
 * User: Dmitry.Krasilschikov
 * Date: 22.04.2009
 */

public class ViewNode extends FileNode {
  public ViewNode(@NotNull final Module module, @NotNull final PsiFile file, final ViewSettings viewSettings, final String locationMark) {
    super(module, file, locationMark, viewSettings);
  }

  @Override
  protected String getTestPresentationImpl(@NotNull final NodeId nodeId, @NotNull final PsiElement psiElement) {
    return "Layout: " + ((PsiFile)psiElement).getName();
  }

  @NotNull
  @Override
  public SortInfo getSortInformation() {
    return SortInfo.VIEW;
  }
}