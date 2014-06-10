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

package com.intellij.uml.core.renderers;

import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.SimpleColoredComponent;
import com.intellij.ui.SimpleColoredText;
import com.intellij.uml.UmlElementManager;
import com.intellij.uml.UmlProvider;
import com.intellij.uml.presentation.UmlColorManager;

import javax.swing.*;
import java.awt.*;

/**
 * @author Konstantin Bulenkov
 */
public class UmlNodeElementComponent extends JPanel {
  private SimpleColoredComponent myLeft= new SimpleColoredComponent();
  private SimpleColoredComponent myRight = new SimpleColoredComponent();

  public UmlNodeElementComponent(Object element, boolean selected, Color color, UmlProvider provider) {
    super(new BorderLayout());
    final UmlElementManager<?> mgr = provider.getElementManager();
    final Color fgColor = selected ? UmlColorManager.getInstance().getNodeForeground(selected) : color;
    myLeft.setIcon(mgr.getNodeElementIcon(element));
    myLeft.setForeground(fgColor);
    final SimpleColoredText text = mgr.getPresentableName(element);
    if (text != null) text.appendToComponent(myLeft);

    final SimpleColoredText typeText = mgr.getPresentableType(element);
    myRight.setForeground(fgColor);
    if (typeText != null) {
      typeText.appendToComponent(myRight);
    }

    init(selected);
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
}
