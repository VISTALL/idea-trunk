package com.advancedtools.webservices.inspections.fixes;

import org.jetbrains.annotations.NotNull;
import com.intellij.psi.PsiElement;

/**
 * @by Konstantin Bulenkov
 */
public class RemoveElementFix extends BaseRemoveElementFix {
  private final String myName;
  public RemoveElementFix(@NotNull final PsiElement element, @NotNull final String name) {
    super(element);
    myName = name;
  }

  @NotNull
  public String getName() {
    return myName;
  }
}
