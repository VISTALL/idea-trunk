package com.advancedtools.webservices.references;

import com.intellij.psi.PsiElement;
import com.intellij.util.ArrayUtil;
import com.advancedtools.webservices.environmentFacade.EnvironmentFacade;
import org.jetbrains.annotations.Nullable;

/**
 * @author maxim
*/
class CheckedFQClassReference extends BaseRangedReference {
  public CheckedFQClassReference(PsiElement element) {
    super(element, 1, element.getTextLength() - 1);
  }

  @Nullable
  public PsiElement resolve() {
    return EnvironmentFacade.getInstance().findClass(getCanonicalText(), getElement().getProject(), null);
  }

  public Object[] getVariants() {
    return ArrayUtil.EMPTY_OBJECT_ARRAY;
  }

  public boolean isSoft() {
    return false;
  }
}
