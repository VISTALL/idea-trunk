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

package org.jetbrains.plugins.grails.projectView;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.grails.util.GrailsUtils;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.GrField;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.typedef.GrTypeDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

/**
 * User: Dmitry.Krasilschikov
 * Date: 09.02.2009
 */
public class GrailsAction {
  private final Module myModule;
  private final GrField myField;

  public GrailsAction(Module module, GrField field) {
    myModule = module;
    myField = field;
  }

  @Nullable
  public static GrailsAction fromField(final GrField field) {
    final GrTypeDefinition grTypeDefinition = PsiTreeUtil.getParentOfType(field, GrTypeDefinition.class);
    if (grTypeDefinition == null) return null;
    GrailsController controller = GrailsController.fromClass(grTypeDefinition);
    if (controller == null) return null;
    return new GrailsAction(controller.getModule(), field);
  }

  @NotNull
  public List<GrailsView> getViews() {
    final GrTypeDefinition grTypeDefinition = PsiTreeUtil.getParentOfType(myField, GrTypeDefinition.class, false);
    assert grTypeDefinition != null;

    final String categoryName = GrailsUtils.getCategoryName(grTypeDefinition.getContainingFile(), myModule);
    if (categoryName == null) return Collections.emptyList();

    final VirtualFile grailsViewsDirectory = GrailsUtils.findViewsDirectory(myModule);
    //This is because view tree does'n stuctured by packages
    int i = categoryName.lastIndexOf('.');
    i = (i == -1 ? 0 : i+1);
    final VirtualFile viewDir = grailsViewsDirectory.findFileByRelativePath(categoryName.substring(i, categoryName.length()));

    final List<GrailsView> views = new ArrayList<GrailsView>();
    if (viewDir == null) return Collections.emptyList();

    for (VirtualFile file : viewDir.getChildren()) {
      if (myField.getName().equals(file.getNameWithoutExtension())) {
        views.add(new GrailsView(file));
      }
    }
    return views;
  }

  public GrField getField() {
    return myField;
  }
}
