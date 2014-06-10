/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.model.values;

import com.intellij.psi.PsiType;
import com.intellij.spring.model.xml.beans.ListOrSet;
import com.intellij.spring.model.xml.beans.SpringValue;
import com.intellij.util.xml.GenericDomValue;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author Dmitry Avdeev
 */
public class ListOrSetValueConverter extends PropertyValueConverter {

  @NotNull
  public List<? extends PsiType> getValueTypes(final GenericDomValue element) {
    if (element instanceof SpringValue) {
      final List<? extends PsiType> psiTypes = ((SpringValue)element).getRequiredTypes();
      if (!psiTypes.isEmpty())
      return psiTypes;
    }
    final ListOrSet parent = (ListOrSet)element.getParent();
    assert parent != null;
    return parent.getRequiredTypes();
  }
}
