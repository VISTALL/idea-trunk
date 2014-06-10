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

package org.jetbrains.plugins.grails.lang.gsp.formatter.processors;

import com.intellij.formatting.Indent;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.formatter.xml.XmlFormattingPolicy;
import com.intellij.psi.html.HtmlTag;
import com.intellij.psi.templateLanguages.OuterLanguageElement;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlText;
import com.intellij.psi.xml.XmlTokenType;
import com.intellij.util.text.StringTokenizer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.grails.addins.js.JavaScriptIntegrationUtil;
import org.jetbrains.plugins.grails.lang.gsp.lexer.GspTokenTypesEx;
import org.jetbrains.plugins.grails.lang.gsp.psi.gsp.api.gtag.GspXmlRootTag;

/**
 * @author ilyas
 */
public abstract class GspIndentProcessor implements GspTokenTypesEx {

  /**
   * Calculates indent, based on code style, between parent block and child node
   *
   * @param parent parent
   * @param child  child node
   * @param policy
   * @return indent
   */
  @NotNull
  public static Indent getGspChildIndent(@NotNull final ASTNode parent,
                                         @NotNull final ASTNode child,
                                         XmlFormattingPolicy policy) {

    PsiElement parentPsi = parent.getPsi();
    PsiElement childPsi = child.getPsi();

    if (parentPsi instanceof GspXmlRootTag) {
      return Indent.getNoneIndent();
    }

    if (parentPsi instanceof XmlTag) {
      if (GspTokenTypesEx.GSP_GROOVY_SEPARATORS.contains(child.getElementType())) {
        return Indent.getNoneIndent();
      }
      if (childPsi instanceof XmlTag) {
        return parentPsi instanceof HtmlTag ? indentForHtmlTag(policy, (HtmlTag) parentPsi) : Indent.getNormalIndent();
      }
      if (childPsi instanceof OuterLanguageElement) {
        if (JavaScriptIntegrationUtil.isJavaScriptInjection(childPsi, parentPsi)) {
          return Indent.getNormalIndent();
        }
        return parentPsi instanceof HtmlTag ? indentForHtmlTag(policy, (HtmlTag) parentPsi) : Indent.getNormalIndent();
      }
      if (childPsi instanceof XmlText) {
        return parentPsi instanceof HtmlTag ? indentForHtmlTag(policy, (HtmlTag) parentPsi) : Indent.getNormalIndent();
      }
      if (childPsi instanceof XmlAttribute) {
        return Indent.getContinuationIndent();
      }
      if (XmlTokenType.XML_DATA_CHARACTERS == child.getElementType()) {
        return parentPsi instanceof HtmlTag ? indentForHtmlTag(policy, (HtmlTag) parentPsi) : Indent.getNormalIndent();
      }
    }

    return Indent.getNoneIndent();
  }

  public static Indent indentForHtmlTag(XmlFormattingPolicy policy, HtmlTag parent) {
    CodeStyleSettings settings = policy.getSettings();
    String tagString = settings.HTML_DO_NOT_INDENT_CHILDREN_OF;
    StringTokenizer tokenizer = new StringTokenizer(tagString, ",");
    while (tokenizer.hasMoreElements()) {
      String tagName = (String) tokenizer.nextElement();
      if (parent.getName().equals(tagName)) {
        return Indent.getNoneIndent();
      }
    }
    return Indent.getNormalIndent();
  }
}
