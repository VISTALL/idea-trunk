/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.model.values;

import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiType;
import com.intellij.spring.model.xml.beans.SpringEntry;
import com.intellij.util.xml.GenericDomValue;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

/**
 * @author Dmitry Avdeev
 */
public class EntryKeyConverter extends PropertyValueConverter {

  @NotNull
  public List<? extends PsiType> getValueTypes(final GenericDomValue domValue) {
    SpringEntry entry = (SpringEntry)domValue.getParent();
    assert entry != null;
    final PsiClass psiClass = entry.getRequiredKeyClass();
    return psiClass == null ? Collections.<PsiType>emptyList() : Collections.singletonList(JavaPsiFacade.getInstance(psiClass.getProject()).getElementFactory().createType(psiClass));
  }
}
