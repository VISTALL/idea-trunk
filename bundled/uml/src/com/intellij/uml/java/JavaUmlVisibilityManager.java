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

import com.intellij.psi.PsiModifier;
import com.intellij.psi.PsiModifierListOwner;
import com.intellij.uml.AbstractUmlVisibilityManager;
import com.intellij.uml.VisibilityLevel;
import com.intellij.util.ArrayUtil;

import java.util.Comparator;

/**
 * @author Konstantin Bulenkov
 */
public class JavaUmlVisibilityManager extends AbstractUmlVisibilityManager {
  private static final VisibilityLevel[] levels = {
    new VisibilityLevel(PsiModifier.PUBLIC),
    new VisibilityLevel(PsiModifier.PACKAGE_LOCAL, "package"),
    new VisibilityLevel(PsiModifier.PROTECTED),
    new VisibilityLevel(PsiModifier.PRIVATE, "All")
  };

  private static final Comparator<VisibilityLevel> COMPARATOR = new Comparator<VisibilityLevel>() {
    public int compare(VisibilityLevel o1, VisibilityLevel o2) {
      final int ind1 = ArrayUtil.indexOf(levels, o1);
      final int ind2 = ArrayUtil.indexOf(levels, o2);
      return ind1 == ind2 ? 0 : ind1 < 0 ? 1 : ind1 - ind2; 
    }
  };


  public VisibilityLevel[] getVisibilityLevels() {
    return levels;
  }

  public VisibilityLevel getVisibilityLevel(Object element) {
    if (element instanceof PsiModifierListOwner) {
      PsiModifierListOwner modifierList = (PsiModifierListOwner)element;
      if (modifierList.hasModifierProperty(PsiModifier.PUBLIC)) return levels[0];
      if (modifierList.hasModifierProperty(PsiModifier.PACKAGE_LOCAL)) return levels[1];
      if (modifierList.hasModifierProperty(PsiModifier.PROTECTED)) return levels[2];
      if (modifierList.hasModifierProperty(PsiModifier.PRIVATE)) return levels[3];
    }
    return null;
  }

  public Comparator<VisibilityLevel> getComparator() {
    return COMPARATOR;
  }
}
