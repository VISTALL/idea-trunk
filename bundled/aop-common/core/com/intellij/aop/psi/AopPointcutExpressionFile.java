/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop.psi;

import com.intellij.aop.LocalAopModel;
import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.util.Key;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.PsiFileImpl;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.xml.XmlElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.TestOnly;

/**
 * @author peter
 */
public final class AopPointcutExpressionFile extends PsiFileBase {
  public static final Key<LocalAopModel> LOCAL_AOP_MODEL = Key.create("LocalAopModel");

  public AopPointcutExpressionFile(FileViewProvider fileView) {
    super(fileView, AopPointcutExpressionLanguage.getInstance());
  }

  public boolean processDeclarations(@NotNull final PsiScopeProcessor processor,
                                     @NotNull final ResolveState state,
                                     final PsiElement lastParent,
                                     @NotNull final PsiElement place) {
    final PsiMethod method = getAopModel().getPointcutMethod();
    if (method != null) {
      for (final PsiParameter parameter : method.getParameterList().getParameters()) {
        if (!processor.execute(parameter, state)) return false;
      }
    }

    final PsiElement element = getContext();
    if (element instanceof XmlElement) {
      JavaPsiFacade facade = JavaPsiFacade.getInstance(getProject());
      PsiPackage aPackage = facade.findPackage("");
      if (aPackage != null && !aPackage.processDeclarations(processor, state, lastParent, place)) return false;

      aPackage = facade.findPackage("java.lang");
      if (aPackage != null && !aPackage.processDeclarations(processor, state, lastParent, place)) return false;
    }

    return super.processDeclarations(processor, state, lastParent, place);
  }

  public String toString() {
    return "AopPointcutExpressionFile:" + getName();
  }

  @NotNull
  public FileType getFileType() {
    return AopPointcutExpressionFileType.INSTANCE;
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    visitor.visitFile(this);
  }

  @Nullable
  public PsiPointcutExpression getPointcutExpression() {
    return findChildByClass(PsiPointcutExpression.class);
  }

  @SuppressWarnings({"CloneDoesntDeclareCloneNotSupportedException", "CloneDoesntCallSuperClone"})
  protected PsiFileImpl clone() {
    final PsiFileImpl file = super.clone();
    file.putUserData(LOCAL_AOP_MODEL, getUserData(LOCAL_AOP_MODEL));
    return file;
  }

  @NotNull
  public LocalAopModel getAopModel() {
    final LocalAopModel data = getUserData(LOCAL_AOP_MODEL);
    if (data != null) return data;

    final PsiElement element = getContext() == null ? this : getContext();
    return element.getUserData(LOCAL_AOP_MODEL);
  }

  @TestOnly
  public void setAopModel(final LocalAopModel model) {
    putUserData(LOCAL_AOP_MODEL, model);
  }
}
