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

package com.intellij.uml.editors;

import com.intellij.openapi.graph.builder.GraphBuilder;
import com.intellij.openapi.graph.view.Graph2DView;
import com.intellij.openapi.graph.view.NodeCellEditor;
import com.intellij.openapi.graph.view.NodeRealizer;
import com.intellij.uml.UmlEdge;
import com.intellij.uml.UmlNode;
import com.intellij.uml.presentation.UmlPresentationModel;
import com.intellij.uml.utils.UmlUtils;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * @author Konstantin Bulenkov
 */
public class UmlNodeCellEditor  extends AbstractCellEditor implements NodeCellEditor {
  private final GraphBuilder<UmlNode, UmlEdge> myBuilder;
  private JComponent myEditor;
  private Object myValue;

  public UmlNodeCellEditor(final GraphBuilder<UmlNode, UmlEdge> builder) {
    super();
    myBuilder = builder;
  }

  public Object getCellEditorValue() {
    return myValue;
  }

  public JComponent getEditor() {
    return myEditor;
  }

  public JComponent getNodeCellEditorComponent(final Graph2DView view,
                                               final NodeRealizer context, final Object value, final boolean isSelected) {
    if (myEditor == null) {
      initEditor(context);
    }
    myValue = value;
    return myEditor;
  }

  private void initEditor(NodeRealizer realizer) {
    final JPanel innerPanel = new JPanel(new BorderLayout());
    innerPanel.setBorder(new LineBorder(Color.ORANGE));
    innerPanel.setFocusable(false);
    Point p = UmlUtils.getNodeCoordinatesOnScreen(realizer.getNode(), myBuilder.getView());
    ((UmlPresentationModel)myBuilder.getGraphPresentationModel()).getRenderer().tuneNode(realizer, innerPanel, p);
    myEditor = innerPanel;
    UmlNodeEditorManager.getInstance().setCurrentCellEditor(this, myBuilder);
  }

  @Override
  public boolean stopCellEditing() {
    UmlNodeEditorManager.getInstance().setCurrentCellEditor(null, myBuilder);
    return super.stopCellEditing();
  }

  @Override
  public void cancelCellEditing() {
    UmlNodeEditorManager.getInstance().setCurrentCellEditor(null, myBuilder);
    super.cancelCellEditing();
  }
}
