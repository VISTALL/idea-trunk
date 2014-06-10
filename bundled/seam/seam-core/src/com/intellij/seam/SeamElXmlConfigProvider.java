/*
 * Copyright 2000-2009 JetBrains s.r.o.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.intellij.seam;

import com.intellij.seam.el.SeamELInjectorUtil;
import com.intellij.seam.el.SeamELContextProvider;
import com.intellij.seam.model.xml.SeamDomModelManager;
import com.intellij.seam.model.xml.PageflowDomModelManager;
import com.intellij.seam.model.xml.PagesDomModelManager;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlText;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiLanguageInjectionHost;
import com.intellij.psi.impl.source.jsp.el.ELLanguage;
import com.intellij.psi.impl.source.jsp.el.ELContextProvider;
import com.intellij.lang.injection.MultiHostRegistrar;
import com.intellij.lang.injection.MultiHostInjector;
import com.intellij.openapi.util.TextRange;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Arrays;

/**
* @author peter
*/
public class SeamElXmlConfigProvider implements MultiHostInjector {
  private final SeamDomModelManager mySeamDomModelManager;
  private final PageflowDomModelManager myPageflowManager;
  private final PagesDomModelManager myPagesDomModelManager;

  public SeamElXmlConfigProvider(SeamDomModelManager seamDomModelManager, PageflowDomModelManager pageflowManager,
                                 PagesDomModelManager pagesDomModelManager) {
    mySeamDomModelManager = seamDomModelManager;
    myPageflowManager = pageflowManager;
    myPagesDomModelManager = pagesDomModelManager;
  }

  private boolean isElContainerFile(@NotNull final XmlFile xmlFile) {
    return mySeamDomModelManager.isSeamComponents(xmlFile) || myPageflowManager.isPageflow(xmlFile) || myPagesDomModelManager.isPages(xmlFile);
  }

  public void getLanguagesToInject(@NotNull final MultiHostRegistrar registrar, @NotNull final PsiElement host) {
    final PsiElement originalElement = host.getOriginalElement();
    // operate only in seam xml config files
    final PsiFile psiFile = originalElement.getContainingFile();
    if (psiFile instanceof XmlFile && isElContainerFile((XmlFile)psiFile)) {
      for (TextRange textRange : SeamELInjectorUtil.getELTextRanges(originalElement)) {
        registrar.startInjecting(ELLanguage.INSTANCE)
          .addPlace(null, null, (PsiLanguageInjectionHost)originalElement, textRange)
          .doneInjecting();
      }
      originalElement.putUserData(ELContextProvider.ourContextProviderKey, new SeamELContextProvider(originalElement));
    }
  }

  @NotNull
  public List<? extends Class<? extends PsiElement>> elementsToInjectIn() {
    return Arrays.asList(XmlAttributeValue.class, XmlText.class);
  }
}
