package com.advancedtools.webservices.references;

import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiElement;
import com.intellij.openapi.util.TextRange;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
* User: Maxim.Mossienko
* Date: Feb 18, 2008
* Time: 8:32:34 PM
* To change this template use File | Settings | File Templates.
*/
class DelegatingClassReferenceThatReferencesAnyMethod implements PsiReference, ClassReferenceThatReferencesAnyMethod {
  private final PsiReference wrappedReference;

  DelegatingClassReferenceThatReferencesAnyMethod(PsiReference _wrappedReference) {
    wrappedReference = _wrappedReference;
  }

  public PsiElement getElement() {
    return wrappedReference.getElement();
  }

  public TextRange getRangeInElement() {
    return wrappedReference.getRangeInElement();
  }

  public PsiElement resolve() {
    return wrappedReference.resolve();
  }

  public String getCanonicalText() {
    return wrappedReference.getCanonicalText();
  }

  public PsiElement handleElementRename(final String newElementName) throws IncorrectOperationException {
    return wrappedReference.handleElementRename(newElementName);
  }

  public PsiElement bindToElement(@NotNull final PsiElement element) throws IncorrectOperationException {
    return wrappedReference.bindToElement(element);
  }

  public boolean isReferenceTo(final PsiElement element) {
    return wrappedReference.isReferenceTo(element);
  }

  public Object[] getVariants() {
    return wrappedReference.getVariants();
  }

  public boolean isSoft() {
    return wrappedReference.isSoft();
  }
}
