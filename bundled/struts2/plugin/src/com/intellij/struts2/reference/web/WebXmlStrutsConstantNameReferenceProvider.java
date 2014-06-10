/*
 * Copyright 2009 The authors
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.intellij.struts2.reference.web;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.impl.source.resolve.reference.PsiReferenceProviderBase;
import com.intellij.psi.xml.XmlTag;
import com.intellij.struts2.model.constant.StrutsConstant;
import com.intellij.struts2.model.constant.StrutsConstantManager;
import com.intellij.util.Function;
import com.intellij.util.ProcessingContext;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Resolves S2 constant names for {@code <param-name>} in {@code web.xml}.
 *
 * @author Yann C&eacute;bron
 */
public class WebXmlStrutsConstantNameReferenceProvider extends PsiReferenceProviderBase {

  @NotNull
  @Override
  public PsiReference[] getReferencesByElement(@NotNull final PsiElement psiElement,
                                               @NotNull final ProcessingContext processingContext) {
    return new PsiReference[]{new StrutsConstantNameReference((XmlTag) psiElement)};
  }


  private static class StrutsConstantNameReference extends PsiReferenceBase<XmlTag> {

    StrutsConstantNameReference(@NotNull final XmlTag xmlTag) {
      super(xmlTag);
    }

    public PsiElement resolve() {
      return myElement;
    }

    @NotNull
    public Object[] getVariants() {
      final Module module = ModuleUtil.findModuleForPsiElement(myElement);
      if (module == null) {
        return EMPTY_ARRAY;
      }

      final StrutsConstantManager constantManager = StrutsConstantManager.getInstance(myElement.getProject());
      final List<StrutsConstant> constants = constantManager.getConstants(module);

      return ContainerUtil.map2Array(constants, new Function<StrutsConstant, Object>() {
        public Object fun(final StrutsConstant strutsConstant) {
          return strutsConstant.getName();
        }
      });
    }

  }

}