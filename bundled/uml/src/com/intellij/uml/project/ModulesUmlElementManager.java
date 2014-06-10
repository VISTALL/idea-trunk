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

package com.intellij.uml.project;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.module.Module;
import com.intellij.ui.SimpleColoredText;
import com.intellij.uml.AbstractUmlElementManager;
import com.intellij.uml.presentation.UmlColorManager;

import java.awt.*;

/**
 * @author Konstantin Bulenkov
 */
public class ModulesUmlElementManager extends AbstractUmlElementManager<ModuleItem> {
  public ModuleItem findInDataContext(DataContext context) {
    final Module module = DataKeys.MODULE_CONTEXT.getData(context);
    return module != null ? new ModuleItem(module) : null;
  }

  public boolean isAcceptableAsNode(Object element) {
    return element instanceof ModuleItem;
  }

  public ModuleItem[] getNodeElements(ModuleItem module) {
    return ModuleItem.EMPTY_ARRAY;
  }

  public boolean canCollapse(ModuleItem element) {
    return false;
  }

  public boolean isContainerFor(ModuleItem parent, ModuleItem child) {
    return false;
  }

  public String getElementTitle(ModuleItem element) {
    return element.toString();
  }

  public SimpleColoredText getPresentableName(Object element) {
    if (element instanceof ModuleItem) {
      return new SimpleColoredText(((ModuleItem)element).getName(), DEFAULT_TITLE_ATTR);
    }
    return null;
  }

  private static Color getFGColor() {
    return UmlColorManager.getInstance().getNodeForeground(false);
  }


  public SimpleColoredText getPresentableType(Object element) {
    return null;
  }

  public String getElementDescription(ModuleItem element) {
    return element.getName(); //TODO beter description
  }
}
