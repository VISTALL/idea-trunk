package com.advancedtools.webservices.references;

import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiElement;
import com.intellij.openapi.util.TextRange;
import com.intellij.util.IncorrectOperationException;
import com.advancedtools.webservices.environmentFacade.EnvironmentFacade;
import org.jetbrains.annotations.NotNull;

/**
 * @author maxim
 */
abstract public class BaseRangedReference implements PsiReference {
  private final PsiElement myElement;
  private final TextRange myRange;

  public BaseRangedReference(PsiElement psiElement,int index, int endIndex) {
    myElement = psiElement;
    myRange = new TextRange(index, endIndex);
  }

  public PsiElement getElement() {
    return myElement;
  }

  public TextRange getRangeInElement() {
    return myRange;
  }

  public String getCanonicalText() {
    return myRange.substring(myElement.getText());
  }

  public PsiElement handleElementRename(String string) throws IncorrectOperationException {
    return EnvironmentFacade.getInstance().handleContentChange(
      myElement,
      myRange,
      string
    );
  }

  public PsiElement bindToElement(@NotNull PsiElement psiElement) throws IncorrectOperationException {
    throw new IncorrectOperationException();
  }

  public boolean isReferenceTo(PsiElement psiElement) {
    return myElement.getManager().areElementsEquivalent(resolve(),psiElement);
  }
}
