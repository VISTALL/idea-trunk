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

package com.intellij.uml.components;

import com.intellij.openapi.util.Iconable;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiUtil;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.SimpleColoredComponent;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.uml.presentation.UmlColorManager;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

/**
 * @author Konstantin Bulenkov
 */
public class ClassItemComponent extends JPanel {
  private SimpleColoredComponent myLeft;
  private SimpleColoredComponent myRight;
  private Color myColor;

  public ClassItemComponent(PsiMethod method, boolean selected, Color color) {
    super(new BorderLayout());
    myColor = color;
    initMethod(method, selected);
    init(selected);
  }

  public ClassItemComponent(PsiField field, boolean selected, Color color) {
    super(new BorderLayout());
    myColor = color;
    initField(field, selected);
    init(selected);
  }

  public ClassItemComponent(UmlClassProperty property, boolean selected, Color color) {
    super(new BorderLayout());
    myColor = color;
    initProperty(property, selected);
    init(selected);
  }

  private void initProperty(UmlClassProperty property, boolean selected) {
    final Color fgColor = selected ? UmlColorManager.getInstance().getNodeForeground(selected) : myColor;
    myLeft = new SimpleColoredComponent();
    myLeft.setIcon(property.getIcon());
    int style = Color.BLACK.equals(myColor) ? SimpleTextAttributes.STYLE_PLAIN : SimpleTextAttributes.STYLE_BOLD;
    if (property.isDeprecated()) style |= SimpleTextAttributes.STYLE_STRIKEOUT;
    myLeft.append(property.getName(), new SimpleTextAttributes(style, fgColor));

    initType(property.getType(), selected);
  }

  private void initMethod(final PsiMethod method, boolean selected) {
    final Color fgColor = selected ? UmlColorManager.getInstance().getNodeForeground(selected) : myColor;
    myLeft = new SimpleColoredComponent();
    myLeft.setIcon(method.getIcon(Iconable.ICON_FLAG_VISIBILITY));
    int style = Color.BLACK.equals(myColor) ? SimpleTextAttributes.STYLE_PLAIN : SimpleTextAttributes.STYLE_BOLD;
    if (method.isDeprecated()) style |= SimpleTextAttributes.STYLE_STRIKEOUT;
    if (!method.isPhysical()) style |= SimpleTextAttributes.STYLE_ITALIC;
    myLeft.append(getMethodSignature(method), new SimpleTextAttributes(style, fgColor));

    initType(method.getReturnType(), selected);
  }

  private void initField(final PsiField field, boolean selected) {
    final Color fgColor = selected ? UmlColorManager.getInstance().getNodeForeground(selected) : myColor;
    myLeft = new SimpleColoredComponent();
    myLeft.setIcon(field.getIcon(Iconable.ICON_FLAG_VISIBILITY));
    int style = Color.BLACK.equals(myColor) ? SimpleTextAttributes.STYLE_PLAIN : SimpleTextAttributes.STYLE_BOLD;
    if (field.isDeprecated()) style |= SimpleTextAttributes.STYLE_STRIKEOUT;
    if (!field.isPhysical()) style |= SimpleTextAttributes.STYLE_ITALIC;
    String name = field.getName();
    myLeft.append(name, new SimpleTextAttributes(style, fgColor));

    initType(field.getType(), selected);
  }

  private void initType(final PsiType type, boolean selected) {
    final Color fgColor = selected ? UmlColorManager.getInstance().getNodeForeground(selected) : myColor;
    myRight = new SimpleColoredComponent();
    if (type != null) {
      final PsiClass psiClass = PsiUtil.resolveClassInType(type);
      int style = SimpleTextAttributes.STYLE_PLAIN;

      if (psiClass != null && psiClass.isDeprecated()) {
        style |= SimpleTextAttributes.STYLE_STRIKEOUT;
      }
      myRight.append(type.getPresentableText(), new SimpleTextAttributes(style, fgColor) );
    }
  }

  private void init(boolean selected) {
    setBorder(IdeBorderFactory.createEmptyBorder(1, 2, 1, 2));
    add(myLeft, BorderLayout.WEST);
    add(myRight, BorderLayout.EAST);
    Dimension preferredSize = getPreferredSize();
    setPreferredSize(new Dimension((int)preferredSize.getWidth() + 20, (int)preferredSize.getHeight()));
    setBackground(UmlColorManager.getInstance().getNodeBackground(selected));
    myLeft.setForeground(UmlColorManager.getInstance().getNodeForeground(selected));
    myRight.setForeground(UmlColorManager.getInstance().getNodeForeground(selected));
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
