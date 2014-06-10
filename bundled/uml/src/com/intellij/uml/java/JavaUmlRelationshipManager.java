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

import com.intellij.uml.UmlRelationshipManager;
import com.intellij.uml.UmlRelationshipInfo;
import com.intellij.uml.UmlCategory;
import com.intellij.psi.PsiElement;
import com.intellij.uml.utils.UmlIcons;
import org.jetbrains.annotations.Nullable;

/**
 * @author Konstantin Bulenkov
 */
public class JavaUmlRelationshipManager implements UmlRelationshipManager<PsiElement> {
  private static final UmlCategory[] CATEGORIES = {new UmlCategory("Dependencies", UmlIcons.DEPENDENCY_ICON)};

  @Nullable
  public UmlRelationshipInfo getDependencyInfo(PsiElement e1, PsiElement e2, UmlCategory category) {
    return null;
  }

  public UmlCategory[] getContentCategories() {
    return CATEGORIES;
  }
}
