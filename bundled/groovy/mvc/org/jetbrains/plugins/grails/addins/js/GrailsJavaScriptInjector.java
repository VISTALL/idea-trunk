/*
 * Copyright 2000-2007 JetBrains s.r.o.
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

package org.jetbrains.plugins.grails.addins.js;

import com.intellij.lang.Language;
import com.intellij.lang.injection.MultiHostInjector;
import com.intellij.lang.injection.MultiHostRegistrar;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.grails.lang.gsp.psi.groovy.api.GspOuterHtmlElement;
import org.jetbrains.plugins.grails.lang.gsp.psi.gsp.api.gtag.GspGrailsTag;

import java.util.Arrays;
import java.util.List;

/**
 * @author ilyas
 */
public class GrailsJavaScriptInjector implements MultiHostInjector {

  private static MultiHostRegistrar processJavaSctiptTag(GspOuterHtmlElement host, MultiHostRegistrar registrar) {
    return registrar.addPlace(null, null, host, new TextRange(0, host.getTextLength()));
  }

  public void getLanguagesToInject(@NotNull MultiHostRegistrar registrar, @NotNull PsiElement tag) {
    if (!(tag instanceof GspGrailsTag)) return;
    boolean hasJsInjection = false;
    for (PsiElement child : tag.getChildren()) {
      if (JavaScriptIntegrationUtil.isJavaScriptInjection(child, tag)) {
        hasJsInjection = true;
        break;
      }
    }
    if (!hasJsInjection) return;

    LanguageFileType jsFileType = JavaScriptSupportLoader.JAVASCRIPT;
    Language language = jsFileType.getLanguage();
    registrar = registrar.startInjecting(language);
    PsiElement host = tag.getFirstChild();
    while (host != null) {
      if (JavaScriptIntegrationUtil.isJavaScriptInjection(host, tag)) {
        registrar = processJavaSctiptTag(((GspOuterHtmlElement) host), registrar);
      }
      host = host.getNextSibling();
    }
    registrar.doneInjecting();
  }

  @NotNull
  public List<? extends Class<? extends PsiElement>> elementsToInjectIn() {
    return Arrays.asList(GspGrailsTag.class);
  }
}
