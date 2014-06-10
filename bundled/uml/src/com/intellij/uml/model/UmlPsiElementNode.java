/*
 * Copyright 2000-2008 JetBrains s.r.o.
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

package com.intellij.uml.model;

import com.intellij.psi.*;
import com.intellij.uml.utils.UmlUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * @author Konstantin Bulenkov
 */
public class UmlPsiElementNode implements UmlNode<PsiElement> {
  private final SmartPsiElementPointer<PsiElement> myPointer;
  private PsiElement lastGoodPsiElement;

  public UmlPsiElementNode(final PsiElement psiElement) {
    myPointer = SmartPointerManager.getInstance(psiElement.getProject()).createSmartPsiElementPointer(psiElement);
    lastGoodPsiElement = psiElement;
  }

  /**
   * Don't use myPointer.getElement() !!! Use getElement() instead
   * @return PsiElement from smart pointer or link to last good PsiElement
   */
  private @NotNull PsiElement getElement() {
    final PsiElement element = myPointer.getElement();
    if (element != null) {
      lastGoodPsiElement = element;
      return element;
    } else {
      return lastGoodPsiElement;
    }
  }

  public String getName() {
    final PsiElement psiElement = getElement();
    if (psiElement instanceof PsiClass) {
      return "<html><b>" + ((PsiClass)psiElement).getQualifiedName() + "</b></html>";
    } else if (psiElement instanceof PsiPackage) {
      return UmlUtils.getInfo((PsiPackage)psiElement).toString();
    }
    return "unknown";
  }

  @NotNull
  public String getFQN() {
    final PsiElement psiElement = getElement();
    String fqn = "";
    if (psiElement instanceof PsiClass) {
      fqn = ((PsiClass)psiElement).getQualifiedName();
    } else if (psiElement instanceof PsiPackage) {
      fqn = ((PsiPackage)psiElement).getQualifiedName();
    }
    return fqn == null || fqn.length() == 0 ? "" : fqn;
  }

  public Icon getIcon() {
    return getElement().getIcon(0);
  }

  @NotNull
  public PsiElement getIdentifyingElement() {
    return getElement();
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    final UmlPsiElementNode that = (UmlPsiElementNode)o;

    final String fqn = getFQN();
    return fqn.length() > 0 && fqn.equals(that.getFQN());
  }

  @Override
  public int hashCode() {
    return (getFQN().hashCode());
  }
}
