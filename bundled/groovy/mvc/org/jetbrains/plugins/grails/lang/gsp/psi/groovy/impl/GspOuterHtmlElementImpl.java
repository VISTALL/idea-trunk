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

package org.jetbrains.plugins.grails.lang.gsp.psi.groovy.impl;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.LiteralTextEscaper;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLanguageInjectionHost;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.impl.source.tree.injected.InjectedLanguageUtil;
import com.intellij.psi.templateLanguages.OuterLanguageElement;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.grails.lang.gsp.psi.GspPsiElementFactory;
import org.jetbrains.plugins.grails.lang.gsp.psi.groovy.api.GspOuterHtmlElement;

import java.util.List;

/**
 * @author ilyas
 */
public class GspOuterHtmlElementImpl extends LeafPsiElement implements OuterLanguageElement, GspOuterHtmlElement {

  public GspOuterHtmlElementImpl(IElementType type, CharSequence text) {
    super(type, text);
  }

  public String toString() {
    return "Outer: " + getElementType();
  }

  @Nullable
  @Deprecated
  public List<Pair<PsiElement, TextRange>> getInjectedPsi() {
    return null;
  }

  public void processInjectedPsi(@NotNull InjectedPsiVisitor visitor) {
    InjectedLanguageUtil.enumerate(this, visitor);
  }

  public PsiLanguageInjectionHost updateText(@NotNull final String text) {
    ASTNode node = getNode();
    if (node == null) return this;
    ASTNode parent = node.getTreeParent();
    GspPsiElementFactory factory = GspPsiElementFactory.getInstance(getProject());
    GspOuterHtmlElement outer = factory.createOuterHtmlElement(text);
    ASTNode outerNode = outer.getNode();
    assert outerNode != null;
    parent.replaceChild(node, outerNode);

    return outer;
  }

  /**
   * @return escapre for other language occurrences in string literal
   */
  @NotNull
  public LiteralTextEscaper createLiteralTextEscaper() {
    return new LiteralTextEscaper<GspOuterHtmlElement>(this) {

      public boolean decode(@NotNull TextRange rangeInsideHost, @NotNull StringBuilder outChars) {

        TextRange realRange = rangeInsideHost.shiftRight(getTextRange().getStartOffset());
        assert getTextRange().contains(realRange);

        int start = rangeInsideHost.getStartOffset();
        int end = rangeInsideHost.getEndOffset();
        String text = getText();
        String injection = end == text.length() ? text.substring(start) : text.substring(start, end);
        outChars.append(injection);
        return true;
      }

      /**
       * Returns offset in host (start or end)
       * @param offsetInDecoded
       * @param rangeInsideHost
       * @return
       */
      public int getOffsetInHost(int offsetInDecoded, @NotNull TextRange rangeInsideHost) {
        TextRange realRange = rangeInsideHost.shiftRight(getTextRange().getStartOffset());
        assert getTextRange().contains(realRange);
        return offsetInDecoded;
      }

      public boolean isOneLine() {
        return false;
      }
    };
  }

}
