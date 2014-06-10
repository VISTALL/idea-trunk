package com.intellij.seam.model.converters;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.seam.utils.SeamCommonUtils;
import com.intellij.seam.utils.beans.ContextVariable;
import com.intellij.util.xml.ConvertContext;
import com.intellij.util.xml.CustomReferenceConverter;
import com.intellij.util.xml.GenericDomValue;
import org.jetbrains.annotations.NotNull;

/**
 * User: Sergey.Vasiliev
 */
public class SeamAnnoReferenceConverter implements CustomReferenceConverter<String> {

  @NotNull
  public PsiReference[] createReferences(final GenericDomValue<String> genericDomValue,
                                         final PsiElement element,
                                         final ConvertContext context) {
    return new PsiReference[]{new PsiReferenceBase<PsiElement>(element) {

      public PsiElement resolve() {
        final String name = genericDomValue.getStringValue();
        if (StringUtil.isEmptyOrSpaces(name)) return null;

        final Module module = context.getModule();

        for (ContextVariable contextVariable : SeamCommonUtils.getSeamContextVariablesWithDependencies(module, true, false)) {
           if (name.equals(contextVariable.getName())) {
             return contextVariable.getModelElement().getIdentifyingPsiElement();
           }
        }
        return getElement().getParent().getParent();
      }

      public boolean isSoft() {
        return true;
      }

      public Object[] getVariants() {
        return EMPTY_ARRAY;
      }
    }};
  }
}

