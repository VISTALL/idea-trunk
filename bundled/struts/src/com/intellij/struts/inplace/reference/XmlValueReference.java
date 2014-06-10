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

package com.intellij.struts.inplace.reference;

import com.intellij.codeInsight.daemon.EmptyResolveMessageProvider;
import com.intellij.codeInsight.daemon.QuickFixProvider;
import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.codeInsight.daemon.impl.quickfix.QuickFixAction;
import com.intellij.javaee.web.WebUtil;
import com.intellij.javaee.web.facet.WebFacet;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Iconable;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.ElementManipulators;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.ElementPresentationManager;
import com.intellij.util.xml.highlighting.ResolvingElementQuickFix;
import com.intellij.codeInspection.LocalQuickFixProvider;
import com.intellij.codeInspection.LocalQuickFix;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 */
public abstract class XmlValueReference implements PsiReference, QuickFixProvider, LocalQuickFixProvider, EmptyResolveMessageProvider {

  protected final XmlAttributeValue myValue;
  private final BaseReferenceProvider myProvider;

  public void setSoft(boolean soft) {
    mySoft = soft;
  }

  protected boolean mySoft;

  public void setRange(final TextRange range) {
    myRange = range;
  }

  private TextRange myRange;

  /**
   * may affect getRangeInElement() and getUnresolvedMessage()
   */
  protected int errorType;

  private final static int ERROR_NO = 0;
  private final static int ERROR_EMPTY = 1;
  private final static int ERROR_DEFAULT = 2;

  public XmlValueReference(XmlAttributeValue attribute, BaseReferenceProvider provider) {
    this(attribute, provider, null);
  }


  public XmlValueReference(XmlAttributeValue attribute, BaseReferenceProvider provider, TextRange range) {
    myRange = range == null ? new TextRange(1, attribute.getValue().length() + 1) : range;
    myValue = attribute;
    myProvider = provider;
    mySoft = provider.isSoft();
  }

  @NotNull
  public Project getProject() {
    return myValue.getProject();
  }

  @Nullable
  public WebFacet getWebFacet() {
    return WebUtil.getWebFacet(myValue);
  }

  public PsiElement handleElementRename(String newElementName) throws IncorrectOperationException {
    return ElementManipulators.getManipulator(myValue).handleContentChange(myValue, getRangeInElement(), newElementName);
  }

  @Nullable
  protected static Object[] getItems(Collection<? extends DomElement> elements) {
    if (elements == null) {
      return null;
    }
    return ElementPresentationManager.getInstance().createVariants(elements, Iconable.ICON_FLAG_VISIBILITY);
  }

  @NotNull
  public String getValue() {
    String s = myValue.getValue();
    if (myRange == null) {
      return s;
    }
    else {
      return s.substring(myRange.getStartOffset() - 1, myRange.getEndOffset() - 1);
    }
  }

  public String getCanonicalText() {
    return myProvider.getCanonicalName() + " " + getValue();
  }

  public PsiElement bindToElement(@NotNull PsiElement psiElement) throws IncorrectOperationException {
    return null;
  }

  public PsiElement getElement() {
    return myValue;
  }

  public Object[] getVariants() {
    return doGetVariants();
  }

  public TextRange getRangeInElement() {
    switch (errorType) {
      case ERROR_EMPTY:
        return new TextRange(1, getValue().length() + 1);
      default:
    }

    if (myRange == null) {
      return new TextRange(1, getValue().length() + 1);
    }
    else {
      return myRange;
    }
  }

  /**
   * Base resolver
   *
   * @return null if the attribute is empty
   */
  public PsiElement resolve() {
    if (myValue.getValue().trim().length() == 0) {
      errorType = ERROR_EMPTY;
      return null;
    }
    else {
      errorType = ERROR_NO;
      PsiElement result = doResolve();
      if (result == null && errorType == ERROR_NO) {
        errorType = ERROR_DEFAULT;
      }
      return result;
    }
  }

  @Nullable
  protected abstract PsiElement doResolve();

  @Nullable
  protected abstract Object[] doGetVariants();

  public boolean isReferenceTo(PsiElement psielement) {
    return psielement.getManager().areElementsEquivalent(psielement, resolve());
  }

  public boolean isSoft() {
    return mySoft;
  }

  public String getUnresolvedMessagePattern() {
    switch (errorType) {
      case ERROR_EMPTY:
        return "Wrong attribute value";
      default:
        return "Cannot resolve " + getCanonicalText();
    }
  }

  public void registerQuickfix(final HighlightInfo info, final PsiReference reference) {
    final Class<? extends DomElement> domClass = myProvider.getDomClass();
    if (domClass != null) {
      final String text = getValue().trim();
      final DomElement scope = getScope();
      if (text.length() > 0 && scope != null) {
        final ResolvingElementQuickFix fix = createResolvingFix(getScope());
        QuickFixAction.registerQuickFixAction(info, fix);        
      }
    }
  }

  @Nullable
  protected ResolvingElementQuickFix createResolvingFix(final DomElement scope) {
    final Class<? extends DomElement> domClass = myProvider.getDomClass();
    if (domClass != null) {
      final String text = getValue().trim();
      if (text.length() > 0 && scope != null) {
        return ResolvingElementQuickFix.createFix(text, domClass, scope);
      }
    }
    return null;
  }

  public LocalQuickFix[] getQuickFixes() {
    final ResolvingElementQuickFix quickFix = createResolvingFix(getScope());
    return quickFix == null ? LocalQuickFix.EMPTY_ARRAY : new LocalQuickFix[] { quickFix };
  }

  @Nullable
  protected DomElement getScope() {
    return null;
  }
}
