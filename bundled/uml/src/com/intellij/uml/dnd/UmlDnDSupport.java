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

package com.intellij.uml.dnd;

import com.intellij.ide.projectView.impl.AbstractProjectViewPSIPane;
import com.intellij.ide.projectView.impl.AbstractProjectViewPane;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.graph.builder.GraphBuilder;
import com.intellij.openapi.graph.base.Node;
import com.intellij.openapi.graph.view.Graph2DView;
import com.intellij.openapi.project.Project;
import com.intellij.uml.*;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import java.awt.dnd.*;
import java.awt.datatransfer.Transferable;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;

/**
 * @author Konstantin Bulenkov
 */
public class UmlDnDSupport implements DropTargetListener {
  private final GraphBuilder<UmlNode, UmlEdge> myBuilder;
  private UmlDnDProvider myProvider;
  final UmlDataModel myDataModel;

  public UmlDnDSupport(GraphBuilder<UmlNode, UmlEdge> builder) {
    myBuilder = builder;
    final UmlExtras extras = Utils.getProvider(builder).getExtras();
    myProvider = extras == null ? null : extras.getDnDProvider();
    myDataModel = Utils.getDataModel(builder);
  }

  public void dragEnter(DropTargetDragEvent dtde) {
    final Object[] values;
    if (myProvider == null || (values = getValues(dtde.getTransferable())).length == 0) {
      dtde.rejectDrag();
      return;
    }
    for (Object value : values) {
      if (! myProvider.isAcceptedForDnD(value, myBuilder.getProject())) {
        dtde.rejectDrag();
        return;
      }
    }
    dtde.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
  }

  private Object[] getValues(Transferable transferable) {
    List<Object> values = new ArrayList<Object>();
    try {
      final Object transferData = transferable.getTransferData(AbstractProjectViewPane.FLAVORS[0]);
      if (transferData instanceof AbstractProjectViewPSIPane.TransferableWrapper) {
        final TreeNode[] treeNodes = ((AbstractProjectViewPSIPane.TransferableWrapper)transferData).getTreeNodes();
        for (TreeNode treeNode : treeNodes) {
          if (treeNode instanceof DefaultMutableTreeNode) {
            final Object userObject = ((DefaultMutableTreeNode)treeNode).getUserObject();
            if (userObject instanceof AbstractTreeNode)
            values.add(((AbstractTreeNode)userObject).getValue());
          }
        }
      }
    } catch (Exception e) {//
    }
    return values.toArray(new Object[values.size()]);
  }

  public void dragOver(DropTargetDragEvent dtde) {
  }

  public void dropActionChanged(DropTargetDragEvent dtde) {
  }

  public void dragExit(DropTargetEvent dte) {
  }

  public void drop(DropTargetDropEvent dtde) {
    boolean setCoord = false;
    final Object[] values = getValues(dtde.getTransferable());
    for (Object value : values) {
      final Project project = myBuilder.getProject();
      if (myProvider.isAcceptedForDnD(value, project)) {
        final Object element = myProvider.wrapToModelObject(value, project);
        if (element != null) {
          final UmlNode umlNode = myDataModel.addElement(element);
          if (!setCoord && umlNode != null) {
            final Point loc = dtde.getLocation();
            final Node node = myBuilder.getNode(umlNode);
            if (node == null) continue;
            final Graph2DView view = myBuilder.getView();
            myBuilder.getGraph().getRealizer(node).setLocation(view.toWorldCoordX((int)loc.getX()), view.toWorldCoordY((int)loc.getY()));
            setCoord = true;
          }
        }
      }
    }
    myBuilder.updateGraph();
  }
}
