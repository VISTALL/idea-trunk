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

package org.jetbrains.plugins.grails.references.providers;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.psi.filters.ElementFilter;
import com.intellij.util.ProcessingContext;
import com.intellij.util.SmartList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.grails.lang.gsp.psi.groovy.api.GspGroovyFile;
import org.jetbrains.plugins.grails.references.controller.ControllerRefExprReference;
import org.jetbrains.plugins.grails.references.gsp.GspGroovyRefExprReference;
import org.jetbrains.plugins.grails.util.GrailsUtils;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrExpression;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrReferenceExpression;

import java.util.List;

/**
 * @author ilyas
 */
public class GrailsReferenceExpressionProvider extends PsiReferenceProvider {

  public static final Class SCOPE_CLASS = GrReferenceExpression.class;

  @NotNull
  private static PsiReference[] getReferencesByElement(PsiElement element) {
    if (!(element instanceof GrReferenceExpression)) return new PsiReference[0];
    List<PsiReference> result = new SmartList<PsiReference>();

    PsiFile file = element.getContainingFile();
    final GrReferenceExpression expr = (GrReferenceExpression)element;
    final GrExpression qualifier = expr.getQualifierExpression();
    if (qualifier == null) {
      VirtualFile virtualFile = file.getOriginalFile().getVirtualFile();
      if (GrailsUtils.isControllerClassFile(virtualFile, element.getProject())) {
        result.add(new ControllerRefExprReference(expr));
      }
    }

    if (file instanceof GspGroovyFile) {
      result.add(new GspGroovyRefExprReference(expr));
    }
    // Inject reference for domain class methods and fields
    if (qualifier != null) {
      //result.add(new DomainClassAwareReference(expr));
    }
    return result.toArray(new PsiReference[result.size()]);
  }

  @NotNull
  public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
    return getReferencesByElement(element);
  }

  public static class GspGroovyRefExprFilter implements ElementFilter {
    public boolean isAcceptable(Object element, PsiElement context) {
      return context instanceof GrReferenceExpression;
    }

    public boolean isClassAcceptable(Class hintClass) {
      return true;
    }
  }
}
