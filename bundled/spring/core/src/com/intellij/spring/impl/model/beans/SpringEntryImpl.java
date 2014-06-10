/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.impl.model.beans;

import com.intellij.psi.*;
import com.intellij.spring.model.xml.beans.SpringBeanPointer;
import com.intellij.spring.model.xml.beans.SpringEntry;
import com.intellij.spring.model.xml.beans.SpringMap;
import com.intellij.spring.model.xml.beans.TypeHolder;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * @author Dmitry Avdeev
 */
@SuppressWarnings({"AbstractClassNeverImplemented"})
public abstract class SpringEntryImpl extends SpringInjectionImpl implements SpringEntry {

  @NotNull
  public List<? extends PsiType> getRequiredTypes() {
    SpringMap map = (SpringMap)getParent();
    final List<PsiType> psiTypes = TypedCollectionImpl.getRequiredTypes(map);
    if (!psiTypes.isEmpty()) {
      return psiTypes;
    }
    assert map != null;
    return Collections.singletonList(getRequiredTypeFromGenerics(map, 1));
  }

  @Nullable
  public PsiClass getRequiredKeyClass() {
    SpringMap map = (SpringMap)getParent();
    assert map != null;
    final PsiClass psiClass = map.getKeyType().getValue();
    if (psiClass != null) {
      return psiClass;
    }
    final PsiType keyType = getRequiredTypeFromGenerics(map, 0);
    return keyType instanceof PsiClassType ? ((PsiClassType)keyType).resolve() : null;
  }

  @Nullable
  public PsiType getRequiredKeyType() {
    SpringMap map = (SpringMap)getParent();
    assert map != null;
    final PsiClass psiClass = map.getKeyType().getValue();
    if (psiClass != null) {
      return JavaPsiFacade.getInstance(psiClass.getProject()).getElementFactory().createType(psiClass);
    }
    return getRequiredTypeFromGenerics(map, 0);
  }

  @Nullable
  private static PsiType getRequiredTypeFromGenerics(@NotNull SpringMap map, int index) {
    final DomElement parent = map.getParent();
    if (parent instanceof TypeHolder) {
      final List<? extends PsiType> types = ((TypeHolder)parent).getRequiredTypes();
      for (PsiType type : types) {
        if (type instanceof PsiClassType) {
          final PsiClassType.ClassResolveResult resolveResult = ((PsiClassType)type).resolveGenerics();
          final PsiClass psiClass = resolveResult.getElement();
          final PsiSubstitutor substitutor = resolveResult.getSubstitutor();
          if (psiClass != null && substitutor != null) {
            final PsiTypeParameter[] typeParameters = psiClass.getTypeParameters();
            if (typeParameters.length == 2) {
              return substitutor.substitute(typeParameters[index]);
            }
          }
        }
      }
    }
    return null;
  }

  @NotNull
  public GenericAttributeValue<SpringBeanPointer> getRefAttr() {
    return getValueRef();        
  }

}