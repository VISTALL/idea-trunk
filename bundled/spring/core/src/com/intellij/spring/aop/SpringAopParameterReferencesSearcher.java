/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.spring.aop;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.util.Computable;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.LocalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.xml.XmlFile;
import com.intellij.spring.SpringModel;
import com.intellij.spring.model.SpringUtils;
import com.intellij.util.Processor;
import com.intellij.util.QueryExecutor;
import gnu.trove.THashSet;

import java.util.Set;

/**
 * @author peter
 */
public class SpringAopParameterReferencesSearcher implements QueryExecutor<PsiReference, ReferencesSearch.SearchParameters> {
  public boolean execute(final ReferencesSearch.SearchParameters queryParameters, final Processor<PsiReference> consumer) {
    //noinspection AutoUnboxing
    return ApplicationManager.getApplication().runReadAction(new Computable<Boolean>() {
      public Boolean compute() {
        final SearchScope scope = queryParameters.getScope();
        if (scope instanceof MyLocalSearchScope) {
          return true; //recursive call
        }

        final PsiElement element = queryParameters.getElementToSearch();
        if (element instanceof PsiParameter) {
          final Module module = ModuleUtil.findModuleForPsiElement(element);
          if (module != null) {
            final Set<XmlFile> visited = new THashSet<XmlFile>();
            for (final SpringModel model : SpringUtils.getNonEmptySpringModels(module)) {
              for (final XmlFile xmlFile : model.getConfigFiles()) {
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
