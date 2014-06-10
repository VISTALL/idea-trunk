package com.intellij.seam.el;

import com.intellij.lang.injection.MultiHostInjector;
import com.intellij.lang.injection.MultiHostRegistrar;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.jsp.el.ELContextProvider;
import com.intellij.psi.impl.source.jsp.el.ELLanguage;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtil;
import com.intellij.seam.facet.SeamFacet;
import com.intellij.seam.utils.SeamCommonUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class SeamClassELInjector implements MultiHostInjector {

  public void getLanguagesToInject(@NotNull final MultiHostRegistrar registrar, @NotNull final PsiElement host) {
    final PsiElement originalElement = PsiUtil.getOriginalElement(host, PsiLiteral.class);
    PsiClass psiClass = PsiTreeUtil.getParentOfType(originalElement, PsiClass.class);

    if (psiClass != null) {
      final Module module = ModuleUtil.findModuleForPsiElement(psiClass);
      if (module != null && SeamFacet.getInstance(module) != null) {
        if (SeamCommonUtils.isSeamClass(psiClass) || SeamCommonUtils.isAbstractSeamComponent(psiClass)) {
          for (TextRange textRange : SeamELInjectorUtil.getELTextRanges(host)) {
            registrar.startInjecting(ELLanguage.INSTANCE).addPlace(null, null, (PsiLanguageInjectionHost)host, textRange).doneInjecting();

            host.putUserData(ELContextProvider.ourContextProviderKey, new SeamELContextProvider(host));
          }
        }
      }
    }
  }

  @NotNull
  public List<? extends Class<? extends PsiElement>> elementsToInjectIn() {
    return Arrays.asList(PsiLiteral.class);
  }
}
