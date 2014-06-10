/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.impl.model.beans;

import com.intellij.psi.PsiType;
import com.intellij.spring.model.xml.beans.SpringEntry;
import com.intellij.spring.model.xml.beans.SpringKey;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

/**
 * @author Dmitry Avdeev
 */
@SuppressWarnings({"AbstractClassNeverImplemented"})
public abstract class SpringKeyImpl extends SpringValueHolderImpl implements SpringKey {
  
  @NotNull
  public List<? extends PsiType> getRequiredTypes() {
    final SpringEntry entry = (SpringEntry)getParent();
    assert entry != null;
    final PsiType type = entry.getRequiredKeyType();
    return type == null ? Collections.<PsiType>emptyList() : Collections.singletonList(type);
  }
}
