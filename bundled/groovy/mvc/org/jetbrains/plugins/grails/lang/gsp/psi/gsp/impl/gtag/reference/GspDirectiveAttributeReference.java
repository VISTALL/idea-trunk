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

package org.jetbrains.plugins.grails.lang.gsp.psi.gsp.impl.gtag.reference;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.grails.lang.gsp.psi.gsp.api.directive.GspDirectiveAttribute;

/**
 * @author ilyas
 */
public class GspDirectiveAttributeReference implements PsiReference {

  @NotNull
  private final ASTNode myNameElement;
  public final String[] DIRECTIVE_ATTRIBUTES = new String[]{"import", "contentType"};

  public GspDirectiveAttributeReference(@NotNull ASTNode nameElement) {
    myNameElement = nameElement;
  }

  @NotNull
  public ASTNode getNameElement() {
    return myNameElement;
  }

  public PsiElement getElement() {
    if (myNameElement.getPsi().getParent() instanceof GspDirectiveAttribute) {
      return myNameElement.getPsi().getParent();
    } else {
      return myNameElement.getPsi();
    }
  }

  public TextRange getRangeInElement() {
    final ASTNode nameElement = getNameElement();
    final PsiElement element = getElement();
    if (element == myNameElement) return new TextRange(0, myNameElement.getTextLength());
    final int elementLength = element.getTextLength();
    int diffFromEnd = 0;
    ASTNode astNode = element.getNode();
    assert astNode != null;
    for (ASTNode node = astNode.getLastChildNode(); node != nameElement && node != null; node = node.getTreePrev()) {
      diffFromEnd += node.getTextLength();
    }
    final int nameEnd = elementLength - diffFromEnd;
    int begin = nameEnd - nameElement.getTextLength();
    if (begin > nameEnd) return new TextRange(begin, begin);
    return new TextRange(begin, nameEnd);
  }

  @Nullable
  public PsiElement resolve() {
    return getElement().getParent();
  }

  public String getCanonicalText() {
    return getNameElement().getText();
  }

  public PsiElement handleElementRename(String newElementName) throws IncorrectOperationException {
    return null;
  }

  public PsiElement bindToElement(@NotNull PsiElement element) throws IncorrectOperationException {
    return null;
  }

  public boolean isReferenceTo(PsiElement element) {
    return false;
  }

  public Object[] getVariants() {
    return DIRECTIVE_ATTRIBUTES;
  }

  public boolean isSoft() {
    return false;
  }
}
