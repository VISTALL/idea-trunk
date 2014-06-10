package com.intellij.seam.el.typedHandler;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.source.jsp.el.typeHandler.ELTypedHandler;
import com.intellij.seam.facet.SeamFacet;

public abstract class BasicSeamElTypedHandler extends ELTypedHandler {

  protected static boolean isSeamFacetDetected(final PsiFile file) {
    final Module module = ModuleUtil.findModuleForPsiElement(file);

    return module != null && SeamFacet.getInstance(module) != null;
  }

  @Override
  protected boolean isElStarted(final char c) {
    return c == '#';
  }
}
