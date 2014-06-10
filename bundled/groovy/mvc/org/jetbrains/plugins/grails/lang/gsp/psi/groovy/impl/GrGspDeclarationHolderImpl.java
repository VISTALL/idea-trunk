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

package org.jetbrains.plugins.grails.lang.gsp.psi.groovy.impl;

import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.grails.lang.gsp.lexer.core.GspTokenTypes;
import org.jetbrains.plugins.grails.lang.gsp.psi.groovy.GspLazyElement;
import org.jetbrains.plugins.grails.lang.gsp.psi.groovy.api.GrGspClass;
import org.jetbrains.plugins.grails.lang.gsp.psi.groovy.api.GrGspDeclarationHolder;
import org.jetbrains.plugins.groovy.lang.psi.GroovyElementVisitor;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.GrField;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.GrVariable;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.GrVariableDeclaration;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.typedef.members.GrMethod;
import org.jetbrains.plugins.groovy.lang.resolve.ResolveUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ilyas
 */
public class GrGspDeclarationHolderImpl extends GspLazyElement implements GrGspDeclarationHolder {

  public GrGspDeclarationHolderImpl(CharSequence text) {
    super(GspTokenTypes.GROOVY_DECLARATION, text);
  }

  public String toString() {
    return "Groovy class level declaration element";
  }

  public void accept(GroovyElementVisitor visitor) {
  }

  public GrField[] getFields() {
    GrVariableDeclaration[] declarations = findChildrenByClass(GrVariableDeclaration.class);
    if (declarations.length == 0) return GrField.EMPTY_ARRAY;
    List<GrField> result = new ArrayList<GrField>();
    for (GrVariableDeclaration declaration : declarations) {
      GrVariable[] variables = declaration.getVariables();
      for (GrVariable variable : variables) {
        if (variable instanceof GrField) {
          result.add((GrField) variable);
        }
      }
    }
    return result.toArray(new GrField[result.size()]);
  }


  public GrMethod[] getMethods() {
    return findChildrenByClass(GrMethod.class);
  }

  public boolean processDeclarations(@NotNull PsiScopeProcessor processor, @NotNull ResolveState state, PsiElement lastParent, @NotNull PsiElement place) {
    GrGspClass clazz = PsiTreeUtil.getParentOfType(this, GrGspClass.class);
    if (clazz != null) {
      if (!clazz.processDeclarations(processor, state, this, place)) return false;
      ResolveUtil.treeWalkUp(clazz, processor, false);
    }

    return false; //do not attempt any further resolving
  }
}
