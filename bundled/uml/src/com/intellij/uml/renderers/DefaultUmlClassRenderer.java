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

package com.intellij.uml.renderers;

import com.intellij.openapi.graph.builder.GraphBuilder;
import com.intellij.openapi.graph.builder.renderer.AbstractColoredNodeCellRenderer;
import com.intellij.openapi.graph.view.NodeRealizer;
import com.intellij.openapi.util.ModificationTracker;
import com.intellij.uml.model.UmlEdge;
import com.intellij.uml.model.UmlNode;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

/**
 * @author Konstantin Bulenkov
 */
public class DefaultUmlClassRenderer extends AbstractColoredNodeCellRenderer {
  GraphBuilder<UmlNode, UmlEdge> myBuilder;

  public DefaultUmlClassRenderer(@NotNull GraphBuilder<UmlNode, UmlEdge> builder, ModificationTracker modificationTracker) {
    super(modificationTracker);
    myBuilder = builder;
  }

  private static final Point NULL_POINT = new Point(0,0);
  public void tuneNode(final NodeRealizer realizer, final JPanel wrapper) {
    tuneNode(realizer, wrapper, NULL_POINT);
  }

  public void tuneNode(final NodeRealizer realizer, final JPanel wrapper, Point point) {
    wrapper.removeAll();
    final UmlNode umlNode = myBuilder.getNodeObject(realizer.getNode());
    if (umlNode != null) {
      UmlPsiElementContainer container = new UmlPsiElementContainer(umlNode.getIdentifyingElement(), myBuilder, point);
      wrapper.add(container.getHeader(), BorderLayout.NORTH);
      wrapper.add(container.getBody(), BorderLayout.CENTER);
    }
  }


  protected int getSelectionBorderWidth() {
    return 1;
  }
}