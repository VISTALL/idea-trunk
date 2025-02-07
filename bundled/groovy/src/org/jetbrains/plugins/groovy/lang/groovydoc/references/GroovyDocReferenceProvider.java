/*
 * Copyright 2000-2008 JetBrains s.r.o.
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

package org.jetbrains.plugins.groovy.lang.groovydoc.references;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.psi.filters.ElementFilter;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.groovy.lang.groovydoc.psi.api.GroovyDocPsiElement;

/**
 * @author ilyas
 */
public class GroovyDocReferenceProvider extends PsiReferenceProvider {
  @NotNull
  public PsiReference[] getReferencesByElement(PsiElement element) {
    return new PsiReference[0];
  }

  @NotNull
  public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
    //todo review
    return getReferencesByElement(element);
  }

  public static class GroovyDocReferenceFilter implements ElementFilter {
    public boolean isAcceptable(Object element, PsiElement context) {
      return context instanceof GroovyDocPsiElement;
    }

    public boolean isClassAcceptable(Class hintClass) {
      return true;
    }
  }

}
