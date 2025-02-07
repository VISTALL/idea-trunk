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
package org.jetbrains.plugins.groovy.lang.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.groovy.debugger.fragments.GroovyCodeFragment;
import org.jetbrains.plugins.groovy.lang.groovydoc.psi.api.GrDocComment;
import org.jetbrains.plugins.groovy.lang.lexer.GroovyTokenTypes;
import org.jetbrains.plugins.groovy.lang.psi.GrReferenceElement;
import org.jetbrains.plugins.groovy.lang.psi.GroovyFileBase;
import org.jetbrains.plugins.groovy.lang.psi.GroovyPsiElementFactory;
import org.jetbrains.plugins.groovy.lang.psi.api.toplevel.imports.GrImportStatement;
import org.jetbrains.plugins.groovy.lang.psi.api.types.GrCodeReferenceElement;
import org.jetbrains.plugins.groovy.lang.psi.api.types.GrTypeArgumentList;
import org.jetbrains.plugins.groovy.lang.psi.api.types.GrTypeElement;
import org.jetbrains.plugins.groovy.lang.psi.util.PsiUtil;

/**
 * @author ven
 */
public abstract class GrReferenceElementImpl extends GroovyPsiElementImpl implements GrReferenceElement {
  public GrReferenceElementImpl(@NotNull ASTNode node) {
    super(node);
  }

  public PsiReference getReference() {
    return this;
  }

  public String getReferenceName() {
    PsiElement nameElement = getReferenceNameElement();
    if (nameElement != null) {
      return nameElement.getText();
    }
    return null;
  }

  @Nullable
  public PsiElement getReferenceNameElement() {
    return findChildByType(GroovyTokenTypes.mIDENT);
  }

  public PsiElement getElement() {
    return this;
  }

  public TextRange getRangeInElement() {
    final PsiElement refNameElement = getReferenceNameElement();
    if (refNameElement != null) {
      final int offsetInParent = refNameElement.getStartOffsetInParent();
      return new TextRange(offsetInParent, offsetInParent + refNameElement.getTextLength());
    }
    return new TextRange(0, getTextLength());
  }

  public PsiElement handleElementRename(String newElementName) throws IncorrectOperationException {
    PsiElement nameElement = getReferenceNameElement();
    if (nameElement != null) {
      ASTNode node = nameElement.getNode();
      ASTNode newNameNode = GroovyPsiElementFactory.getInstance(getProject()).createReferenceNameFromText(newElementName).getNode();
      assert newNameNode != null && node != null;
      node.getTreeParent().replaceChild(node, newNameNode);
    }

    return this;
  }

  public PsiElement bindToElement(@NotNull PsiElement element) throws IncorrectOperationException {
    if (isReferenceTo(element)) return this;

    if (element instanceof PsiClass) {
      final String newName = ((PsiClass) element).getName();
      handleElementRename(newName);
      if (isReferenceTo(element)) return this;

      final GroovyFileBase file = (GroovyFileBase) getContainingFile();
      final PsiClass clazz = (PsiClass) element;
      final String qName = clazz.getQualifiedName();
      if (qName != null) {
        if (mayInsertImport()) {
          final GrImportStatement added = file.addImportForClass(clazz);
          if (!bindsCorrectly(element)) {
            file.removeImport(added);
            return bindWithQualifiedRef(qName);
          }

          return this;
        } else {
          return bindWithQualifiedRef(qName);
        }
      }
    } else if (element instanceof PsiMember) {
      PsiMember member = (PsiMember)element;
      if (!isPhysical()) {
        // don't qualify reference: the isReferenceTo() check fails anyway, whether we have a static import for this member or not
        return this;
      }
      final PsiClass psiClass = member.getContainingClass();
      if (psiClass == null) throw new IncorrectOperationException();

      String qName = psiClass.getQualifiedName() + "." + member.getName();
      return bindWithQualifiedRef(qName);
    }
    else if (element instanceof PsiPackage) {
      final String qName = ((PsiPackage) element).getQualifiedName();
      return bindWithQualifiedRef(qName);
    }

    throw new IncorrectOperationException("Cannot bind to:" + element + " of class " + element.getClass());
  }

  private boolean mayInsertImport() {
    return PsiTreeUtil.getParentOfType(this, GrDocComment.class) == null && !(getContainingFile() instanceof GroovyCodeFragment) && PsiTreeUtil.getParentOfType(this, GrImportStatement.class) == null;
  }

  private PsiElement bindWithQualifiedRef(String qName) {
    final GrTypeArgumentList list = getTypeArgumentList();
    final String typeArgs = (list != null) ? list.getText() : "";
    final String text = qName + typeArgs;
    final GrCodeReferenceElement qualifiedRef = GroovyPsiElementFactory.getInstance(getProject()).createTypeOrPackageReference(text);
    getNode().getTreeParent().replaceChild(getNode(), qualifiedRef.getNode());
    PsiUtil.shortenReference(qualifiedRef);
    return qualifiedRef;
  }

  protected boolean bindsCorrectly(PsiElement element) {
    return isReferenceTo(element);
  }

  @NotNull
  public PsiType[] getTypeArguments() {
    final GrTypeArgumentList typeArgsList = getTypeArgumentList();
    if (typeArgsList == null) return PsiType.EMPTY_ARRAY;

    final GrTypeElement[] args = typeArgsList.getTypeArgumentElements();
    if (args.length == 0) return PsiType.EMPTY_ARRAY;
    PsiType[] result = new PsiType[args.length];
    for (int i = 0; i < result.length; i++) {
      result[i] = args[i].getType();
    }

    return result;
  }

  @Nullable
  public GrTypeArgumentList getTypeArgumentList() {
    return findChildByClass(GrTypeArgumentList.class);
  }
}
