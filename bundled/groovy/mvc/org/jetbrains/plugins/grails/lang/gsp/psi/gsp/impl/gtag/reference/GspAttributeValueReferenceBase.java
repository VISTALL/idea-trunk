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

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

/**
 * @author ilyas
 */
public abstract class GspAttributeValueReferenceBase implements PsiReference {
  protected PsiElement myElement;
  protected TextRange myRange;

  public GspAttributeValueReferenceBase(final PsiElement element) {
    this(element, 1);
  }

  public GspAttributeValueReferenceBase(final PsiElement element, int offset) {
    this(element, new TextRange(offset, element.getTextLength() - offset));
  }

  public GspAttributeValueReferenceBase(final PsiElement element, TextRange range) {
    myElement = element;
    myRange = range;
  }

  public PsiElement getElement() {
    return myElement;
  }

  public TextRange getRangeInElement() {
    return myRange;
  }

  public String getCanonicalText() {
    final String s = myElement.getText();
    if (myRange.getStartOffset() < s.length() && myRange.getEndOffset() <= s.length()) {
      return myRange.substring(s);
    }
    return "";
  }

  public PsiElement handleElementRename(String newElementName) throws IncorrectOperationException {
    return null;
  }

  public PsiElement bindToElement(@NotNull PsiElement element) throws IncorrectOperationException {
    return null;
  }

  public boolean isReferenceTo(PsiElement element) {
    return myElement.getManager().areElementsEquivalent(element, resolve());
  }
}