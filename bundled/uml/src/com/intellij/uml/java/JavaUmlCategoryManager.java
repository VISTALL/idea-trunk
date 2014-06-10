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

package com.intellij.uml.java;

import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import com.intellij.uml.AbstractUmlNodeContentManager;
import com.intellij.uml.UmlCategory;
import com.intellij.uml.utils.UmlIcons;

/**
 * @author Konstantin Bulenkov
 */
public class JavaUmlCategoryManager extends AbstractUmlNodeContentManager {
  private static final UmlCategory FIELDS = new UmlCategory("Fields", UmlIcons.FIELD_ICON);
  private static final UmlCategory CONSTRUCTORS = new UmlCategory("Constructors", UmlIcons.CONSTRUCTOR_ICON);
  private static final UmlCategory METHODS = new UmlCategory("Methods", UmlIcons.METHOD_ICON);
  private static final UmlCategory PROPERTIES = new UmlCategory("Properties", UmlIcons.PROPERTY);  

  private final static UmlCategory[] CATEGORIES = {FIELDS, CONSTRUCTORS, METHODS, PROPERTIES};

  public UmlCategory[] getContentCategories() {
    return CATEGORIES;
  }

  public boolean isInCategory(Object element, UmlCategory category) {
    if (FIELDS.equals(category)) {
      return element instanceof PsiField;
    }
    if (CONSTRUCTORS.equals(category)) {
      return element instanceof PsiMethod && ((PsiMethod)element).isConstructor();
    }
    if (METHODS.equals(category)) {
      return element instanceof PsiMethod && !((PsiMethod)element).isConstructor();
    }

    if (PROPERTIES.equals(category)) {
      if (element instanceof PsiField) {
        //TODO Make properties stuff
      }
    }
    return false;
  }
}
