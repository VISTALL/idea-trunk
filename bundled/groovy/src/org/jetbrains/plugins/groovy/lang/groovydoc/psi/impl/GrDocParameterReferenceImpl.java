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

package org.jetbrains.plugins.groovy.lang.groovydoc.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.util.ArrayUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.groovy.lang.groovydoc.lexer.GroovyDocTokenTypes;
import org.jetbrains.plugins.groovy.lang.groovydoc.psi.api.GrDocParameterReference;
import org.jetbrains.plugins.groovy.lang.groovydoc.psi.api.GrDocTagValueToken;
import org.jetbrains.plugins.groovy.lang.psi.GroovyPsiElementFactory;
import org.jetbrains.plugins.groovy.lang.psi.api.GroovyResolveResult;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.params.GrParameter;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.typedef.members.GrMethod;
import org.jetbrains.plugins.groovy.lang.psi.api.types.GrTypeParameter;
import org.jetbrains.plugins.groovy.lang.psi.api.types.GrTypeParameterListOwner;
import org.jetbrains.plugins.groovy.lang.psi.impl.GroovyResolveResultImpl;

import java.util.ArrayList;

/**
 * @author ilyas
 */
public class GrDocParameterReferenceImpl extends GroovyDocPsiElementImpl implements GrDocParameterReference {

  public GrDocParameterReferenceImpl(@NotNull ASTNode node) {
    super(node);
  }

  public String toString() {
    return "GrDocParameterReference";
  }

  public PsiReference getReference() {
    return this;
  }

  @NotNull
  public ResolveResult[] multiResolve(boolean incompleteCode) {
    final String name = getName();
    if (name == null) return ResolveResult.EMPTY_ARRAY;
    ArrayList<GroovyResolveResult> candidates = new ArrayList<GroovyResolveResult>();

    final PsiElement owner = GrDocCommentUtil.findDocOwner(this);
    if (owner instanceof GrMethod) {
      final GrMethod method = (GrMethod)owner;
      final GrParameter[] parameters = method.getParameters();

      for (GrParameter parameter : parameters) {
        if (name.equals(parameter.getName())) {
          candidates.add(new GroovyResolveResultImpl(parameter, true));
        }
      }
      return candidates.toArray(new ResolveResult[candidates.size()]);
    }
    else {
      final PsiElement firstChild = getFirstChild();
      if (owner instanceof GrTypeParameterListOwner && firstChild != null) {
        final ASTNode node = firstChild.getNode();
        if (node != null && GroovyDocTokenTypes.mGDOC_TAG_VALUE_LT.equals(node.getElementType())) {
          final PsiTypeParameter[] typeParameters = ((PsiTypeParameterListOwner)owner).getTypeParameters();
          for (PsiTypeParameter typeParameter : typeParameters) {
            if (name.equals(typeParameter.getName())) {
              candidates.add(new GroovyResolveResultImpl(typeParameter, true));
            }
          }
        }
      }
    }
    return ResolveResult.EMPTY_ARRAY;
  }

  public PsiElement getElement() {
    return this;
  }

  public TextRange getRangeInElement() {
    return new TextRange(0, getTextLength());
  }

  @Override
  public String getName() {
    return getReferenceNameElement().getText();
  }

  @Nullable
  public PsiElement resolve() {
    final ResolveResult[] results = multiResolve(false);
    if (results.length != 1) return null;
    return results[0].getElement();
  }

  public String getCanonicalText() {
    return getName();
  }

  public PsiElement handleElementRename(String newElementName) throws IncorrectOperationException {
    PsiElement nameElement = getReferenceNameElement();
    ASTNode node = nameElement.getNode();
    ASTNode newNameNode = GroovyPsiElementFactory.getInstance(getProject()).createDocMemberReferenceNameFromText(newElementName).getNode();
    assert newNameNode != null && node != null;
    node.getTreeParent().replaceChild(node, newNameNode);
    return this;
  }

  public PsiElement bindToElement(@NotNull PsiElement element) throws IncorrectOperationException {
    if (isReferenceTo(element)) return this;
    return null;
  }

  public boolean isReferenceTo(PsiElement element) {
    if (!(element instanceof GrParameter || element instanceof GrTypeParameter)) return false;
    return getManager().areElementsEquivalent(element, resolve());
  }

  public Object[] getVariants() {
    final PsiElement owner = GrDocCommentUtil.findDocOwner(this);
    final PsiElement firstChild = getFirstChild();
    if (owner instanceof GrTypeParameterListOwner && firstChild != null) {
      final ASTNode node = firstChild.getNode();
      if (node != null && GroovyDocTokenTypes.mGDOC_TAG_VALUE_LT.equals(node.getElementType())) {
        return ((PsiTypeParameterListOwner)owner).getTypeParameters();
      }
    }
    if (owner instanceof PsiMethod) {
      return ((PsiMethod)owner).getParameterList().getParameters();
    }
    return ArrayUtil.EMPTY_OBJECT_ARRAY;
  }

  public boolean isSoft() {
    return false;
  }

  @NotNull
  public GrDocTagValueToken getReferenceNameElement() {
    GrDocTagValueToken token = findChildByClass(GrDocTagValueToken.class);
    assert token != null;
    return token;
  }
}
