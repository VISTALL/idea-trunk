package com.intellij.spring.impl.model.beans;

import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiType;
import com.intellij.spring.model.xml.beans.TypeHolder;
import com.intellij.spring.model.xml.beans.TypedCollection;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

/**
 * @author Dmitry Avdeev
 */
public abstract class TypedCollectionImpl implements TypedCollection, TypeHolder {

  @NotNull
  public List<? extends PsiType> getRequiredTypes() {
    return getRequiredTypes(this);
  }

  @NotNull
  public static List<PsiType> getRequiredTypes(final TypedCollection collection) {
    final PsiClass psiClass = collection.getValueType().getValue();
    return psiClass == null ? Collections.<PsiType>emptyList() : Collections.<PsiType>singletonList(
    JavaPsiFacade.getInstance(psiClass.getProject()).getElementFactory().createType(psiClass));
  }
}
