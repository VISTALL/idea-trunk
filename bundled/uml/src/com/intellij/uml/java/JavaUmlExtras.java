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

package com.intellij.uml.java;

import com.intellij.psi.PsiElement;
import com.intellij.uml.UmlDnDProvider;
import com.intellij.uml.UmlElementsProvider;
import com.intellij.uml.UmlExtras;
import com.intellij.uml.java.providers.PsiClassImplementations;
import com.intellij.uml.java.providers.PsiClassParents;

/**
 * @author Konstantin Bulenkov
 */
public class JavaUmlExtras extends UmlExtras<PsiElement> {
  private UmlElementsProvider[] providers = {
    new PsiClassImplementations(),
    new PsiClassParents()
  };

  private JavaUmlDnDSupport dndSupport = new JavaUmlDnDSupport();
  @Override
  public UmlElementsProvider<PsiElement>[] getElementsProviders() {
    return providers;
  }

  @Override
  public UmlDnDProvider<PsiElement> getDnDProvider() {
    return dndSupport;
  }
}
