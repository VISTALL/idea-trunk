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

package com.intellij.uml.java;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiUtil;
import com.intellij.ui.SimpleColoredText;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.uml.AbstractUmlElementManager;
import com.intellij.uml.presentation.UmlColorManager;
import com.intellij.uml.utils.UmlUtils;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Konstantin Bulenkov
 */
public class JavaUmlElementManager extends AbstractUmlElementManager<PsiElement> {
  public PsiElement findInDataContext(DataContext context) {
    return DataKeys.PSI_ELEMENT.getData(context);
  }

  public boolean isAcceptableAsNode(Object element) {
    if (element instanceof PsiClass) {
      return !(element instanceof PsiAnonymousClass);
    }
    return element instanceof PsiPackage;
  }

  public PsiElement[] getNodeElements(PsiElement parent) {
    if (parent instanceof PsiClass) {
      final PsiClass psiClass = (PsiClass)parent;
      final List<PsiElement> elements = new ArrayList<PsiElement>();
      elements.addAll(Arrays.asList(psiClass.getFields()));
      elements.addAll(Arrays.asList(psiClass.getMethods()));
      return elements.isEmpty() ? PsiElement.EMPTY_ARRAY : elements.toArray(new PsiElement[elements.size()]);
    }
    return PsiElement.EMPTY_ARRAY;
  }

  public boolean canCollapse(PsiElement element) {
    return false;
  }

  public boolean isContainerFor(PsiElement parent, PsiElement child) {
    if (parent instanceof PsiPackage && child instanceof PsiQualifiedNamedElement) {
      PsiQualifiedNamedElement psiQualifiedNamedElement = (PsiQualifiedNamedElement)child;
      PsiPackage psiPackage = (PsiPackage)parent;
      final String fqn = psiQualifiedNamedElement.getQualifiedName();
      return fqn != null && fqn.startsWith(psiPackage.getQualifiedName());
    }
    return false;
  }

  public String getElementTitle(PsiElement element) {
    return element instanceof PsiClass ? ((PsiClass)element).getName() : "Package " + ((PsiPackage)element).getName();
  }

  public SimpleColoredText getPresentableName(Object element) {
    if (element instanceof PsiMethod) {
      return getMethodPresentableName((PsiMethod)element);
    } else if (element instanceof PsiField) {
      return getFieldPresentableName((PsiField)element);
    } else if (element instanceof PsiClass) {
      return getClassPresentableName((PsiClass)element);
    } else if (element instanceof PsiPackage) {
      return getPackagePresentableName((PsiPackage)element);
    }
    return null;
  }

  private SimpleColoredText getPackagePresentableName(PsiPackage psiPackage) {
    final SimpleColoredText text = new SimpleColoredText();
    text.append(psiPackage.getQualifiedName(), new SimpleTextAttributes(SimpleTextAttributes.STYLE_BOLD, getFGColor()));
    return text;
  }

  private SimpleColoredText getClassPresentableName(PsiClass psiClass) {
    int style = SimpleTextAttributes.STYLE_BOLD;
    if (psiClass.isDeprecated()) style |= SimpleTextAttributes.STYLE_STRIKEOUT;
    if (!psiClass.isPhysical()) style |= SimpleTextAttributes.STYLE_ITALIC;

    String label = psiClass.getName();

    final PsiTypeParameterList classParams = psiClass.getTypeParameterList();
    if (classParams != null && classParams.getText() != null) {
      final String params = classParams.getText();
      label += params.length() > 7 ? "<...>" : params;
    }

    final SimpleColoredText text = new SimpleColoredText();
    text.append(label, new SimpleTextAttributes(style, getFGColor()));
    return text;
  }

  private SimpleColoredText getMethodPresentableName(PsiMethod method) {
    int style = SimpleTextAttributes.STYLE_PLAIN;
    if (method.isDeprecated()) style |= SimpleTextAttributes.STYLE_STRIKEOUT;
    if (!method.isPhysical()) style |= SimpleTextAttributes.STYLE_ITALIC;
    final SimpleColoredText text = new SimpleColoredText();
    text.append(getMethodSignature(method), new SimpleTextAttributes(style, getFGColor()));
    return text;
  }

  private static SimpleColoredText getFieldPresentableName(@NotNull PsiField field) {
    int style = SimpleTextAttributes.STYLE_PLAIN;
    if (field.isDeprecated()) style |= SimpleTextAttributes.STYLE_STRIKEOUT;
    if (!field.isPhysical()) style |= SimpleTextAttributes.STYLE_ITALIC;
    return new SimpleColoredText(field.getName(), new SimpleTextAttributes(style, getFGColor()));
  }

  private static Color getFGColor() {
    return UmlColorManager.getInstance().getNodeForeground(false);
  }


  public SimpleColoredText getPresentableType(Object element) {
    PsiType type = null;
    if (element instanceof PsiField) {
      type = ((PsiField)element).getType();
    } else if (element instanceof PsiMethod) {
      type = ((PsiMethod)element).getReturnType();
    }
    if (type == null) return null;
    final PsiClass psiClass = PsiUtil.resolveClassInType(type);
    int style = SimpleTextAttributes.STYLE_PLAIN;

    if (psiClass != null && psiClass.isDeprecated()) {
      style |= SimpleTextAttributes.STYLE_STRIKEOUT;
    }

    return new SimpleColoredText(type.getPresentableText(), new SimpleTextAttributes(style, getFGColor()));
  }

  public String getElementDescription(PsiElement element) {
    if (element instanceof PsiClass) {
      return "<html><b>" + ((PsiClass)element).getQualifiedName() + "</b></html>";
    } else if (element instanceof PsiPackage) {
      return UmlUtils.getInfo((PsiPackage)element).toString();
    }
    return "unknown";
  }

  private static String getMethodSignature(@NotNull PsiMethod method) {
    StringBuilder signature = new StringBuilder(method.getName());
    signature.append("(");
    for (PsiParameter param : method.getParameterList().getParameters()) {
      signature.append(param.getType().getPresentableText());
      signature.append(", ");
    }
    if (method.getParameterList().getParameters().length != 0) {
      signature.delete(signature.length()-2,signature.length());
    }
    signature.append(")");
    return signature.toString();
  }
}
