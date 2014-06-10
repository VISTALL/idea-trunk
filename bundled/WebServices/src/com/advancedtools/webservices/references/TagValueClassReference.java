package com.advancedtools.webservices.references;

import com.intellij.psi.*;
import com.intellij.psi.xml.XmlTag;
import com.intellij.openapi.util.TextRange;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.ArrayUtil;
import com.advancedtools.webservices.environmentFacade.EnvironmentFacade;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;

/**
 * @author Maxim
*/
class TagValueClassReference implements PsiReference {
  private final XmlTag myElement;
  private TextRange myRange;

  public TagValueClassReference(XmlTag element) {
    myElement = element;
    String text = element.getValue().getText();
    String trimmedText = text.trim();
    int start = element.getValue().getTextRange().getStartOffset() - element.getTextRange().getStartOffset() + text.indexOf(trimmedText);
    if (trimmedText.endsWith("[]")) {
      trimmedText = trimmedText.substring(0, trimmedText.length() - 2);
      trimmedText = trimmedText.trim();
    }
    myRange = new TextRange(start, start + trimmedText.length());
  }

  public PsiElement getElement() {
    return myElement;
  }

  public TextRange getRangeInElement() {
    return myRange;
  }

  public String getCanonicalText() {
    final String trimmedText = myElement.getValue().getTrimmedText();
    return trimmedText.substring(0, myRange.getLength() < trimmedText.length() ? myRange.getLength() : trimmedText.length());
  }

  public PsiElement handleElementRename(String string) throws IncorrectOperationException {
    PsiElement psiElement = EnvironmentFacade.getInstance().handleContentChange(myElement, myRange, string);
    myRange = new TextRange(myRange.getStartOffset(), myRange.getStartOffset() + string.length());
    return psiElement;
  }

  public PsiElement bindToElement(@NotNull PsiElement element) throws IncorrectOperationException {
    return handleElementRename(((PsiClass)element).getQualifiedName());
  }

  public boolean isReferenceTo(PsiElement element) {
    return myElement.getManager().areElementsEquivalent(resolve(), element);
  }

  @Nullable
  public PsiElement resolve() {
    return EnvironmentFacade.getInstance().findClass(getCanonicalText(), myElement.getProject(), null);
  }

  public Object[] getVariants() {
    return ArrayUtil.EMPTY_OBJECT_ARRAY;
  }

  public boolean isSoft() {
    return false;
  }
}
