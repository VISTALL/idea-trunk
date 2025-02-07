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
package org.jetbrains.plugins.groovy.lang.psi.impl.synthetic;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.light.LightIdentifier;

/**
 * @author ven
 */
public class JavaIdentifier extends LightIdentifier {
  private final PsiElement myElement;

  public JavaIdentifier(PsiManager manager, PsiElement element) {
    super(manager, element.getText());
    myElement = element;
  }

  public TextRange getTextRange() {
    return myElement.getTextRange();
  }

  public PsiFile getContainingFile() {
    return myElement.getContainingFile();
  }

  @Override
  public int getStartOffsetInParent() {
    return myElement.getStartOffsetInParent();
  }

  @Override
  public int getTextOffset() {
    return myElement.getTextOffset();
  }

  @Override
  public PsiElement getParent() {
    return myElement.getParent();
  }

  @Override
  public PsiElement getPrevSibling() {
    return myElement.getPrevSibling();
  }

  @Override
  public PsiElement getNextSibling() {
    return myElement.getNextSibling();
  }

  
}
