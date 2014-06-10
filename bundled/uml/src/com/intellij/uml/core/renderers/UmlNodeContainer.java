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

import com.intellij.openapi.graph.builder.GraphBuilder;
import com.intellij.openapi.graph.builder.renderer.GradientFilledPanel;
import com.intellij.openapi.ui.VerticalFlowLayout;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.SimpleColoredComponent;
import com.intellij.uml.UmlEdge;
import com.intellij.uml.UmlNode;
import com.intellij.uml.UmlProvider;
import com.intellij.uml.Utils;
import com.intellij.uml.utils.UmlUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * @author Konstantin Bulenkov
 */
public class UmlNodeContainer {
  private GradientFilledPanel myHeader;
  private final JPanel myBody;
  private final UmlProvider myProvider;
  private final UmlNode myNode;

  public UmlNodeContainer(UmlNode node, GraphBuilder<UmlNode, UmlEdge> builder, Point point) {
    myNode = node;
    myProvider = Utils.getProvider(builder);
    initHeader();

    myBody = new JPanel(new VerticalFlowLayout(VerticalFlowLayout.TOP, 0, 0, true, false));
    myBody.setBorder(new EmptyBorder(0,0,0,0));
    myBody.add(new UmlNodeBodyComponent(myNode.getIdentifyingElement(), builder, point));
  }

  public JPanel getHeader() {
    return myHeader;
  }

  public JPanel getBody() {
    return myBody;
  }

  private JComponent createHeaderLabel() {
    Object element = myNode.getIdentifyingElement();
    final SimpleColoredComponent label = new SimpleColoredComponent();
    label.setIcon(myNode.getIcon());
    
    myProvider.getElementManager().getPresentableName(element).appendToComponent(label);
    label.setAlignmentX(Component.LEFT_ALIGNMENT);
    label.setBorder(IdeBorderFactory.createEmptyBorder(3, 3, 3, 3));
    label.setOpaque(false);
    label.setIconOpaque(false);
    return label;
  }

  private void initHeader() {
    final JComponent headerLabel = createHeaderLabel();
    myHeader = new GradientFilledPanel(myProvider.getColorManager().getNodeHeaderColor(null)) {
      @Override
      public void paint(final Graphics g) {
        setGradientColor(myProvider.getColorManager().getNodeHeaderColor(null));
        super.paint(g);
      }
    };
    myHeader.setLayout(new BorderLayout());
    myHeader.add(headerLabel, BorderLayout.CENTER);
    headerLabel.setForeground(UmlUtils.getElementColor(myNode.getIdentifyingElement()));
    myHeader.setFocusable(false);
  }
}
