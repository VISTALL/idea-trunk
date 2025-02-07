package org.jetbrains.plugins.clojure.psi.api;

import org.jetbrains.plugins.clojure.psi.ClojurePsiElement;
import org.jetbrains.plugins.clojure.psi.api.symbols.ClSymbol;
import org.jetbrains.annotations.Nullable;
import com.intellij.psi.PsiElement;

/**
 * @author ilyas
 */
public interface ClList extends ClojurePsiElement, ClBraced, ClListLike {
  @Nullable
  String getPresentableText();

  @Nullable
  ClSymbol getFirstSymbol();

  @Nullable
  PsiElement getSecondNonLeafElement();

  @Nullable
  String getHeadText();

}
