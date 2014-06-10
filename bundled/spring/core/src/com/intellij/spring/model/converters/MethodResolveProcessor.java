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

package com.intellij.spring.model.converters;

import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.ElementClassHint;
import com.intellij.psi.scope.NameHint;
import com.intellij.psi.scope.PsiScopeProcessor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Dmitry Avdeev
 */
public class MethodResolveProcessor implements PsiScopeProcessor, ElementClassHint {

  private final NameHint myNameHint;
  private final List<PsiMethod> myMethods = new ArrayList<PsiMethod>();

  public MethodResolveProcessor() {
    myNameHint = null;
  }

  public MethodResolveProcessor(final String name) {
    myNameHint = new NameHint() {
      public String getName(ResolveState state) {
        return name;
      }
    };
  }

  public PsiMethod[] getMethods() {
    return myMethods.toArray(new PsiMethod[myMethods.size()]);
  }

  public boolean execute(PsiElement element, ResolveState state) {
    myMethods.add((PsiMethod)element);
    return true;
  }

  public <T> T getHint(Key<T> hintKey) {
    if (hintKey == ElementClassHint.KEY) {
      return (T)this;
    }
    if (hintKey == NameHint.KEY) {
      return (T)myNameHint;
    }
    return null;
  }

  public void handleEvent(Event event, Object associated) {
  }

  public boolean shouldProcess(DeclaractionKind kind) {
    return kind == DeclaractionKind.METHOD;
  }
}
