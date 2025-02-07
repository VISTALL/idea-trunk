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

package org.jetbrains.plugins.groovy.lang.completion.filters.classdef;

import com.intellij.psi.PsiElement;
import com.intellij.psi.filters.ElementFilter;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.typedef.*;
import org.jetbrains.plugins.groovy.lang.psi.util.PsiUtil;

/**
 * @author ilyas
 */
public class ExtendsFilter implements ElementFilter {
  public boolean isAcceptable(Object element, PsiElement context) {
    if (context.getParent() == null || context.getParent() instanceof GrExtendsClause) {
      return false;
    }
    PsiElement elem = context.getParent();
    if (elem instanceof GrTypeDefinitionBody) { //inner class
      elem = PsiUtil.skipWhitespaces(context.getPrevSibling(), false);
    }
    else {
      elem = PsiUtil.skipWhitespaces(elem.getPrevSibling(), false);
    }
    if (!(elem instanceof GrInterfaceDefinition || elem instanceof GrClassDefinition)) {
      return false;
    }
    PsiElement[] children = elem.getChildren();
    for (PsiElement child : children) {
      if (child instanceof GrImplementsClause || child instanceof GrExtendsClause || child instanceof GrTypeDefinitionBody) {
        return false;
      }
    }
    return true;
  }

  public boolean isClassAcceptable(Class hintClass) {
    return true;
  }

  @NonNls
  public String toString() {
    return "'extends' keyword filter";
  }

}
