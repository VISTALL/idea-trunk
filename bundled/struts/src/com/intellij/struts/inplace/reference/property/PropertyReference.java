/*
 * Copyright 2000-2006 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.intellij.struts.inplace.reference.property;

import com.intellij.codeInsight.lookup.LookupValueFactory;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PropertyUtil;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlTag;
import com.intellij.struts.core.PsiBeanProperty;
import com.intellij.struts.core.PsiBeanPropertyCache;
import com.intellij.struts.inplace.reference.XmlValueReference;
import com.intellij.struts.inplace.reference.BaseReferenceProvider;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

/**
 * @author davdeev
 */
abstract class PropertyReference extends XmlValueReference implements PsiPolyVariantReference {

  private final PropertyReferenceSet myReferenceSet;
  private final int myIndex;
  private boolean myIndexed;

  public PropertyReference(PropertyReferenceSet set, int index, TextRange range, BaseReferenceProvider provider) {
    super(set.getValue(), provider, range);
    myReferenceSet = set;
    myIndex = index;
  }

  @NotNull
  protected abstract PsiBeanProperty[] getPropertiesForTag(final boolean forVariants);

  @Nullable
  private PsiElement getContext() {
    PropertyReference ref = getContextReference();
    if (ref == null) {
      return null;
    }
    else {
      final ResolveResult[] resolveResults = ref.multiResolve(false);
      return resolveResults.length == 0 ? null : resolveResults[0].getElement();
    }
  }

  @Nullable
  private PropertyReference getContextReference() {
    return myIndex <= 0 ? null : myReferenceSet.getReferences()[myIndex - 1];
  }

  @NotNull
  public String getValue() {
    myIndexed = false;
    String val = super.getValue();
    int pos = val.indexOf('(');
    int pos1 = val.indexOf(')');
    if (pos != -1 && pos1 != -1 && pos1 > pos) {
      return val.substring(0, pos);
    }
    pos = val.indexOf('[');
    pos1 = val.indexOf(']');
    if (pos != -1 && pos1 != -1 && pos1 > pos) {
      myIndexed = true;
      return val.substring(0, pos);
    }
    return val;
  }

  @Nullable
  protected PsiElement doResolve() {
    ResolveResult[] result = multiResolve(false);
    if (result.length == 1) {
      return result[0].getElement();
    }
    else {
      return null;
    }
  }

  protected Object[] doGetVariants() {
    PsiBeanProperty[] props = getProperties(true);
    ArrayList<Object> result = new ArrayList<Object>();
    for (PsiBeanProperty p : props) {
      Object item = LookupValueFactory.createLookupValueWithHint(p.getName(), p.getIcon(), p.getType());
      result.add(item);
    }
    return result.toArray();
  }

  private ResolveResult[] last;

  public PsiElement handleElementRename(String string) throws IncorrectOperationException {

    if (last != null && last.length > 0) {
      if (last[0].getElement()instanceof PsiField || last[0].getElement()instanceof PsiMethod) {
        String field = PropertyUtil.getPropertyName(string);
        if (field != null) {
          string = field;
        }
      }
    }
    return super.handleElementRename(string);
  }

  public boolean isReferenceTo(PsiElement element) {
    ResolveResult[] result = multiResolve(false);
    for (ResolveResult aResult : result) {
      PsiElement el = aResult.getElement();
      if (element.getManager().areElementsEquivalent(element, el)) {
        return true;
      }
    }
    return false;
  }

  @NotNull
  private PsiBeanProperty[] getProperties(final boolean forVariants) {

    PsiElement context = getContext();
    if (context == null) { // the first property
      XmlTag tag = PsiTreeUtil.getParentOfType(myValue, XmlTag.class);
      if (tag != null) {
        return getPropertiesForTag(forVariants);
      }
    }
    else {
      PsiType type = null;
      if (context instanceof PsiMethod) {
        type = ((PsiMethod)context).getReturnType();
      }
      else if (context instanceof PsiField) {
        type = ((PsiField)context).getType();
      }
      if (type != null) {
        if (type instanceof PsiArrayType) {
          PsiReference ref = getContextReference();
          if (ref instanceof PropertyReference && ((PropertyReference)ref).myIndexed) {
            type = ((PsiArrayType)type).getComponentType();
          }
        }

        if (type instanceof PsiClassType) return getProperties(((PsiClassType)type).resolve());
      }
    }
    return PsiBeanProperty.EMPTY_ARRAY;
  }

  protected PsiBeanProperty[] getProperties(String className) {
    XmlAttributeValue attributeValue = myReferenceSet.getValue();
    PsiClass clazz = JavaPsiFacade.getInstance(attributeValue.getProject()).findClass(className, GlobalSearchScope.allScope(getProject()));
    return getProperties(clazz);
  }

  @NotNull
  private PsiBeanProperty[] getProperties(PsiClass clazz) {
    return PsiBeanPropertyCache.getInstance(getProject()).getBeanProperties(clazz);
  }

  @NotNull
  public ResolveResult[] multiResolve(final boolean incompleteCode) {

    ArrayList<ResolveResult> result = new ArrayList<ResolveResult>();
    String val = getValue();

    PsiBeanProperty[] props = getProperties(false);
    for (PsiBeanProperty prop : props) {
      if (prop.getName().equals(val)) {
        PsiElement[] els = prop.getPsiElements();
        if (els != null) {
          for (PsiElement el : els) {
            result.add(new PsiElementResolveResult(el));
          }
        }
        else {
          result.add(new PsiElementResolveResult(myValue));
        }
        break;
      }
    }
    last = result.toArray(new ResolveResult[result.size()]);
    return last;
  }
}
