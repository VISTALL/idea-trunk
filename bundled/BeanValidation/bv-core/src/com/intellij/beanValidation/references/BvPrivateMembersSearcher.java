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

package com.intellij.beanValidation.references;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.util.Computable;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.LocalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.Processor;
import com.intellij.util.QueryExecutor;
import com.intellij.beanValidation.utils.BVUtils;
import gnu.trove.THashSet;

import java.util.Set;

/**
 * @author Konstantin Bulenkov
 */
public class BvPrivateMembersSearcher implements QueryExecutor<PsiReference, ReferencesSearch.SearchParameters> {
  public boolean execute(final ReferencesSearch.SearchParameters queryParameters, final Processor<PsiReference> consumer) {
    //noinspection AutoUnboxing
    return ApplicationManager.getApplication().runReadAction(new Computable<Boolean>() {
      public Boolean compute() {
        final SearchScope scope = queryParameters.getScope();
        if (scope instanceof MyLocalSearchScope) {
          return true; //recursive call
        }

        final PsiElement element = queryParameters.getElementToSearch();
        if (element instanceof PsiField || element instanceof PsiMethod) {
          final Module module = ModuleUtil.findModuleForPsiElement(element);
          if (!BVUtils.isBeanValidationFacetDefined(module)) return true;
          if (module != null) {
            final Set<XmlFile> visited = new THashSet<XmlFile>();
            for (final XmlFile xmlFile : BVUtils.getConstraintFiles(module)) {
              if (!visited.contains(xmlFile)) {
                visited.add(xmlFile);
                final LocalSearchScope localScope = (LocalSearchScope)new LocalSearchScope(xmlFile).intersectWith(scope);
                if (localScope.getScope().length > 0) {
                  if (!ReferencesSearch.search(element, new MyLocalSearchScope(localScope), true).forEach(consumer)) return false;
                }
              }
            }
          }
        }
        return true;
      }
    });
  }
  
  private static class MyLocalSearchScope extends LocalSearchScope {
    public MyLocalSearchScope(final LocalSearchScope scope) {
      super(scope.getScope(), scope.getDisplayName(), scope.isIgnoreInjectedPsi());
    }
  }
}