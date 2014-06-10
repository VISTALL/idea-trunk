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

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.ActionPopupMenu;
import com.intellij.openapi.graph.builder.GraphBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.uml.*;
import com.intellij.uml.editors.UmlNodeEditorManager;
import com.intellij.uml.presentation.UmlColorManager;
import com.intellij.uml.presentation.UmlPresentationModel;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.*;
import java.util.List;

/**
 * @author Konstantin Bulenkov
 */
public class UmlNodeBodyComponent extends JList implements ListCellRenderer {
  private final Project project;
  private final GraphBuilder<UmlNode, UmlEdge> myBuilder;
  private final UmlProvider myProvider;
  private final Point basePoint;
  private final Map<PsiElement, Color> colors = new HashMap<PsiElement, Color>();

  public UmlNodeBodyComponent(Object element, GraphBuilder<UmlNode, UmlEdge> builder, Point point) {
    super(getElements(element, builder));
    project = builder.getProject();
    myBuilder = builder;
    myProvider = Utils.getProvider(myBuilder);
    basePoint = point;
    setCellRenderer(this);
    setBackground(UmlColorManager.getInstance().getNodeBackground(false));

    addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(final ListSelectionEvent e) {
        if (getSelectedValue() == null) {
          final int index = getSelectedIndex();
          setSelectedIndex(getSelectedIndex() + ((e.getLastIndex() == index) ? 1 : -1));
        }
      }
    });

    addMouseMotionListener(new MouseMotionAdapter() {
      boolean myIsEngaged = false;
      public void mouseMoved(MouseEvent e) {
        if (myIsEngaged && ! getComponentPopupMenu().isVisible()) {
          Point point = e.getPoint();
          int index = locationToIndex(point);
          setSelectedIndex(index);
        } else {
          myIsEngaged = true;
        }
      }
    });

    final ActionGroup group = (ActionGroup)ActionManager.getInstance().getAction("Uml.NodeCellEditorPopup");
    final ActionPopupMenu menu = ActionManager.getInstance().createActionPopupMenu(ActionPlaces.STRUCTURE_VIEW_POPUP, group);
    setComponentPopupMenu(menu.getComponent());
  }

  public Component getListCellRendererComponent(final JList list,
                                                final Object value,
                                                final int index,
                                                final boolean isSelected,
                                                final boolean cellHasFocus) {
    return value == null ?
           new LineSeparator() : new UmlNodeElementComponent(value, isSelected, getElementColor(value), myProvider);
  }

  public Color getElementColor(Object element) {
    if (element instanceof PsiElement) {
      final PsiElement key = (PsiElement)element;
      final Color color = colors.get(key);
      if (color != null) return color;
      if (!key.isPhysical()) return Color.GRAY;
    }
    return UmlColorManager.getInstance().getDefaultNodeElementColor();
  }

  @Override
  public Point getPopupLocation(final MouseEvent event) {
    Point p = myBuilder.getView().getCanvasComponent().getLocationOnScreen();
    return new Point(basePoint.x - p.x + event.getX(), basePoint.y - p.y + event.getY());
  }

  public Project getProject() {
    return project;
  }

  private static Object[] getElements(Object element, GraphBuilder<UmlNode, UmlEdge> builder) {
    final ArrayList<Object> elements = new ArrayList<Object>();
    final UmlProvider umlProvider = Utils.getProvider(builder);
    final UmlVisibilityManager visibilityManager = umlProvider.getVisibilityManager();
    final VisibilityLevel visibility = visibilityManager.getCurrentVisibilityLevel();
    final Comparator<VisibilityLevel> comparator = visibilityManager.getComparator();
    final List<UmlCategory> enabled = Arrays.asList(getEnabledCategories(builder));

    for (UmlCategory category : umlProvider.getNodeContentManager().getContentCategories()) {
      if (! enabled.contains(category)) continue;
      final Object[] nodeElements = Utils.getNodeElementsForCategory(umlProvider, element, category);
      if (nodeElements.length > 0) {
        boolean added = false;
        for (Object nodeElement : nodeElements) {
          final VisibilityLevel level = visibilityManager.getVisibilityLevel(nodeElement);
          if (comparator.compare(level, visibility) <= 0) {
            elements.add(nodeElement);
            added = true;
          }
        }
        if (added) elements.add(null);
      }
    }

    if (! elements.isEmpty() && elements.get(elements.size() - 1) == null) {
      elements.remove(elements.size() - 1);
    }
    return elements.toArray(new Object[elements.size()]);
  }

  public void setEditorMode() {
    this.addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
          UmlNodeEditorManager.getInstance().stopEditing(myBuilder);
        }
      }
    });
  }

  private static class LineSeparator extends JPanel {
    public LineSeparator() {
      setBackground(Color.GRAY);
      setPreferredSize(new Dimension(-1, 1));
    }
  }

  public static UmlCategory[] getEnabledCategories(GraphBuilder<UmlNode, UmlEdge> builder) {
    return ((UmlPresentationModel)builder.getGraphPresentationModel()).getPresentation().getEnabledCategories();
  }
}
