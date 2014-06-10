package com.intellij.seam.el;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.jsp.el.impl.ELResolveUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SeamELInjectorUtil {
  private SeamELInjectorUtil() {
  }

  @NotNull
  public static List<TextRange> getELTextRanges(final PsiElement element) {
    return ELResolveUtil.getELTextRanges(element, "#{", "}");
  }
}
