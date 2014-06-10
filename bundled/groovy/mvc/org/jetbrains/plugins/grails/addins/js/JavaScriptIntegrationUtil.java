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

import com.intellij.lang.javascript.JSElementType;
import com.intellij.lang.javascript.psi.JSElement;
import com.intellij.lang.javascript.psi.impl.JSEmbeddedContentImpl;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.plugins.grails.addins.GrailsIntegrationUtil;
import org.jetbrains.plugins.grails.lang.gsp.psi.groovy.api.GspOuterHtmlElement;
import org.jetbrains.plugins.grails.lang.gsp.psi.gsp.api.gtag.GspGrailsTag;

/**
 * @author ilyas
 */
public class JavaScriptIntegrationUtil {

  public static final String JS_GRAILS_TAG_NAME = "g:javascript";

  private JavaScriptIntegrationUtil() {
  }

  public static boolean isJSEmbeddedContent(final PsiElement element) {
    return element instanceof JSEmbeddedContentImpl;
  }

  public static boolean isJSElementType(IElementType type) {
    return GrailsIntegrationUtil.isJsSupportEnabled() && type instanceof JSElementType;
  }

  public static boolean isJSElement(PsiElement element) {
    return GrailsIntegrationUtil.isJsSupportEnabled() && element instanceof JSElement;
  }

  public static boolean isJavaScriptInjection(PsiElement inj, PsiElement parent) {
    return GrailsIntegrationUtil.isJsSupportEnabled() &&
        inj instanceof GspOuterHtmlElement &&
        parent instanceof GspGrailsTag &&
        JS_GRAILS_TAG_NAME.equals(((GspGrailsTag) parent).getName());
  }
}
