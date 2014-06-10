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

package org.jetbrains.plugins.grails.references.controller;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.util.ArrayUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.grails.references.manager.GrailsImplicitVariableManager;
import org.jetbrains.plugins.groovy.GroovyIcons;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrExpression;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrReferenceExpression;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ilyas
 */
public class ControllerRefExprReference implements PsiPolyVariantReference {

  public static final String[] CONTROLLER_PROPERTY_NAMES = {"application", "params", "request", "response", "session"};
  public static final String[] CONTROLLER_METHOD_NAMES = {"redirect", "bindData", "chain", "render"};

  @NotNull
  private final GrReferenceExpression myRefExpr;
  public final String CONTROLLER_SUFFIX = "Controller";

  public ControllerRefExprReference(@NotNull GrReferenceExpression refExpr) {
    myRefExpr = refExpr;
  }

  @NotNull
  public ResolveResult[] multiResolve(boolean incompleteCode) {
    return new ResolveResult[0];
  }

  @NotNull
  public GrReferenceExpression getElement() {
    return myRefExpr;
  }

  public TextRange getRangeInElement() {
    return getElement().getRangeInElement();
  }

  @Nullable
  public PsiElement resolve() {
    if (isNotUnderControllerClass()) return null;

    GrailsImplicitVariableManager manager = GrailsImplicitVariableManager.getInstance(getElement().getProject());
    if (manager != null) {
      PsiElement implicitVariable = manager.getImplicitVariable(getElement().getReferenceName(), getElement().getContainingFile());
      if (implicitVariable != null) return implicitVariable;
    }
    return null;
  }

  public String getCanonicalText() {
    return myRefExpr.getCanonicalText();
  }

  public PsiElement handleElementRename(String newElementName) throws IncorrectOperationException {
    return null;
  }

  public PsiElement bindToElement(@NotNull PsiElement element) throws IncorrectOperationException {
    return null;
  }

  public boolean isReferenceTo(PsiElement element) {
    return false;
  }

  public static LookupElement[] getControllerPageLookupMembers() {
    List<LookupElement> pageProperties = new ArrayList<LookupElement>();
    for (String propName : CONTROLLER_PROPERTY_NAMES) {
      pageProperties.add(LookupElementBuilder.create(propName).setIcon(GroovyIcons.PROPERTY));
    }
    for (String methodName : CONTROLLER_METHOD_NAMES) {
      pageProperties.add(LookupElementBuilder.create(methodName).setIcon(GroovyIcons.METHOD));
    }
    int length = CONTROLLER_PROPERTY_NAMES.length + CONTROLLER_METHOD_NAMES.length;
    return pageProperties.toArray(new LookupElement[length]);
  }


  public Object[] getVariants() {

    if (isNotUnderControllerClass()) return ArrayUtil.EMPTY_OBJECT_ARRAY;

    GrExpression qualifier = myRefExpr.getQualifierExpression();
    if (qualifier == null) {
      return getControllerPageLookupMembers();
    }
    return ArrayUtil.EMPTY_OBJECT_ARRAY;
  }

  private boolean isNotUnderControllerClass() {
    PsiElement parent = myRefExpr.getParent();
    while (parent != null && !(parent instanceof PsiFile) && !(parent instanceof PsiClass)) {
      parent = parent.getParent();
    }
    if (!(parent instanceof PsiClass)) return true;
    PsiClass aClass = (PsiClass) parent;
    String name = aClass.getName();
    return name == null ||
        !(parent.getParent() instanceof PsiFile) ||
        !name.endsWith(CONTROLLER_SUFFIX);
  }

  public boolean isSoft() {
    return false;
  }
}
