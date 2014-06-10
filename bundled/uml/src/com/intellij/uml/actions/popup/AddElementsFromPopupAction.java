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

package com.intellij.uml.actions.popup;

import com.intellij.ide.util.PsiElementListCellRenderer;
import com.intellij.navigation.ItemPresentation;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.editor.markup.EffectType;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.graph.GraphUtil;
import com.intellij.openapi.graph.base.Node;
import com.intellij.openapi.graph.builder.GraphBuilder;
import com.intellij.openapi.graph.builder.util.GraphViewUtil;
import com.intellij.openapi.graph.view.Graph2D;
import com.intellij.openapi.graph.view.Graph2DView;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.PopupChooserBuilder;
import com.intellij.openapi.vcs.FileStatus;
import com.intellij.openapi.vcs.FileStatusManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.problems.WolfTheProblemSolver;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiUtil;
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.uml.actions.UmlAction;
import com.intellij.uml.actions.UmlDataKeys;
import com.intellij.uml.model.UmlClassDiagramDataModel;
import com.intellij.uml.model.UmlEdge;
import com.intellij.uml.model.UmlNode;
import com.intellij.uml.utils.UmlPsiUtil;
import com.intellij.uml.utils.UmlUtils;
import com.intellij.util.IconUtil;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * @author Konstantin Bulenkov
 */
public abstract class AddElementsFromPopupAction extends UmlAction {
  @Nullable
  public abstract PsiElement[] getElements(final PsiClass psiClass);

  @NotNull
  public abstract String getPopupTitle(final GraphBuilder<UmlNode, UmlEdge> builder);

  @Override
  public void update(AnActionEvent e) {    
    boolean enabled = getSelectedClass(getBuilder(e)) != null;
    e.getPresentation().setEnabled(enabled);    
  }

  @Nullable
  public static UmlNode getSelectedNode(final GraphBuilder<UmlNode, UmlEdge> builder) {
    if (builder == null) return null;
    List<Node> nodes = GraphViewUtil.getSelectedNodes(builder.getGraph());
    if (nodes.size() != 1) return null;
    Node node = nodes.get(0);
    return builder.getNodeObject(node);
  }

  @Nullable
  public static PsiClass getSelectedClass(final GraphBuilder<UmlNode, UmlEdge> builder) {
    final UmlNode node = getSelectedNode(builder);
    if (node == null) return null;
    final PsiElement element = node.getIdentifyingElement();
    return element instanceof PsiClass ? (PsiClass) element : null;
  }

  public void actionPerformed(AnActionEvent e) {
    GraphBuilder<UmlNode, UmlEdge> builder = getBuilder(e);
    UmlNode umlNode = getSelectedNode(builder);
    if (builder == null || umlNode == null) return;

    Graph2D graph = builder.getGraph();
    Graph2DView view = (Graph2DView)graph.getCurrentView();
    List<Node> nodes = GraphViewUtil.getSelectedNodes(graph);
    if (nodes.size() != 1) return;
    Node node = nodes.get(0);

    PsiElement element = umlNode.getIdentifyingElement();

    assert element instanceof PsiClass;

    PsiClass psiClass = (PsiClass)element;
    PsiClass[] elements = UmlPsiUtil.removeAnonymous(getElements(psiClass));

    if (elements == null) return;
    List<PsiClass> existen = new ArrayList<PsiClass>();
    for (UmlNode umlnode : builder.getNodeObjects()) {
      if (umlnode.getIdentifyingElement() instanceof PsiClass) {
        existen.add((PsiClass)umlnode.getIdentifyingElement());
      }
    }

    //elements = UmlPsiUtil.removeExisten(elements, existen);
    //if (elements == null) return;
    final DefaultPsiElementListCellRenderer renderer = new DefaultPsiElementListCellRenderer(existen);
    Arrays.sort(elements, renderer.getComparator());
    JList list = new JList(elements);
    list.setCellRenderer(renderer);
    MouseMotionListener[] listeners = list.getMouseMotionListeners();
    for (MouseMotionListener listener : listeners) {
      list.removeMouseMotionListener(listener);
    }
    renderer.installSpeedSearch(list);
    JBPopup popup = new PopupChooserBuilder(list)
      .setItemChoosenCallback(new SubclassFoundCallback(builder, list))
      .setMovable(true)
      .setTitle(getPopupTitle(builder))
      .createPopup();

    Point p = UmlUtils.getNodeCoordinatesOnScreen(node, view);
    popup.showInScreenCoordinates(view.getCanvasComponent(), new Point(p.x, p.y + (int)(24 * view.getZoom()))); // 24 == HEADER HEIGHT
  }

  private static class DefaultPsiElementListCellRenderer extends PsiElementListCellRenderer<PsiClass> {
    private final List<PsiClass> existen;

    public DefaultPsiElementListCellRenderer(List<PsiClass> existen) {
      this.existen = existen;
    }

    private class LeftRenderer extends ColoredListCellRenderer {
      private final String myModuleName;

      public LeftRenderer(String moduleName) {
        myModuleName = moduleName;
      }

      protected void customizeCellRenderer(JList list, Object value, int index, boolean selected, boolean hasFocus) {
        if (value instanceof PsiClass) {
          PsiClass element = (PsiClass)value;
          String name = getElementText(element);
          Color color = list.getForeground();
          PsiFile psiFile = element.getContainingFile();
          boolean isProblemFile = false;

          if (psiFile != null) {
            VirtualFile vFile = psiFile.getVirtualFile();
            if (vFile != null) {
              if (WolfTheProblemSolver.getInstance(psiFile.getProject()).isProblemFile(vFile)) {
                isProblemFile = true;
              }
              FileStatus status = FileStatusManager.getInstance(psiFile.getProject()).getStatus(vFile);
              color = status.getColor();
            }
          }

          TextAttributes attributes = null;

          TextAttributesKey attributesKey = null;
          final ItemPresentation presentation = ((NavigationItem)value).getPresentation();
          if (presentation != null) attributesKey = presentation.getTextAttributesKey();

          if (attributesKey != null) {
            attributes = EditorColorsManager.getInstance().getGlobalScheme().getAttributes(attributesKey);
          }


          SimpleTextAttributes nameAttributes;
          if (existen.contains(element)) {
            attributes = new TextAttributes(Color.GRAY, null, null, null, Font.ITALIC);
          }
          if (isProblemFile) {
            attributes = TextAttributes.merge(new TextAttributes(color, null, Color.red, EffectType.WAVE_UNDERSCORE, Font.PLAIN),attributes);
          }

          nameAttributes = attributes != null ? SimpleTextAttributes.fromTextAttributes(attributes):null;

          if (nameAttributes == null)  nameAttributes = new SimpleTextAttributes(Font.PLAIN, color);

          assert name != null: "Null name for PSI element " + element;
          append(name, nameAttributes);
          setIcon(element.getIcon(getIconFlags()));

          String containerText = getContainerText(element, name + (myModuleName != null ? myModuleName + "        " : ""));
          if (containerText != null) {
            append(" " + containerText, new SimpleTextAttributes(Font.PLAIN, Color.GRAY));
          }
        } else {
          setIcon(IconUtil.getEmptyIcon(false));
          append(value == null ? "" : value.toString(), new SimpleTextAttributes(Font.PLAIN, list.getForeground()));
        }
        setPaintFocusBorder(false);
        setBackground(selected ? UIUtil.getListSelectionBackground() : UIUtil.getListBackground());
      }
    }

    public String getElementText(final PsiClass cls) {
      final String text = cls.getName();
      return text == null ? "" : text;
    }

    @Override
    public Comparator<PsiClass> getComparator() {
      final Comparator<PsiClass> comp = super.getComparator();
      return new Comparator<PsiClass>(){
        public int compare(PsiClass c1, PsiClass c2) {
          final boolean b1 = existen.contains(c1);
          final boolean b2 = existen.contains(c2);
          if ( ((b1 && b2) || (!b1 && !b2)) ) {
            final boolean a1 = c1.isAnnotationType();
            final boolean a2 = c2.isAnnotationType();
            if ( ((a1 && a2) || (!a1 && !a2)) ) {
              final boolean i1 = c1.isInterface();
              final boolean i2 = c2.isInterface();
              if ( ((i1 && i2) || (!i1 && !i2)) ) {
                return comp.compare(c1, c2);                
              } else {
                return i1 ? 1 : -1;
              }
            } else {
              return a1 ? 1 : -1;
            }
          } else {
            return b1 ? 1 : -1;
          }
        }
      };
    }

    protected String getContainerText(final PsiClass element, final String name) {
      return null;
    }

    protected int getIconFlags() {
      return 0;
    }

    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
    removeAll();
    String moduleName = null;
    DefaultListCellRenderer rightRenderer = getRightCellRenderer();
    if (rightRenderer != null) {
      final Component rightCellRendererComponent =
        rightRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
      add(rightCellRendererComponent, BorderLayout.EAST);
      moduleName = rightRenderer.getText();
      final JPanel spacer = new JPanel();
      spacer.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
      spacer.setBackground(isSelected ? UIUtil.getListSelectionBackground() : UIUtil.getListBackground());
      add(spacer, BorderLayout.CENTER);
    }
    final Component leftCellRendererComponent =
      new LeftRenderer(moduleName).getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
    add(leftCellRendererComponent, BorderLayout.WEST);
    setBackground(isSelected ? UIUtil.getListSelectionBackground() : UIUtil.getListBackground());
    return this;
    }
  }

  private static class SubclassFoundCallback implements Runnable {
    private final GraphBuilder<UmlNode, UmlEdge> myBuilder;
    private final JList myList;


    public SubclassFoundCallback(GraphBuilder<UmlNode, UmlEdge> builder, JList list) {
      myBuilder = builder;
      myList = list;
    }

    public void run() {
      ProgressManager.getInstance().runProcessWithProgressSynchronously(new Runnable(){
        public void run() {
          Object[] selected = myList.getSelectedValues();
          final boolean checkForInners = ! UmlUtils.getPresentation(myBuilder).isShowInnerClasses();
          boolean innerSelected = false;
          boolean added = false;
          for (Object obj : selected) {
            if (obj instanceof PsiClass) {
              PsiClass psiClass = (PsiClass)obj;
              UmlClassDiagramDataModel model = (UmlClassDiagramDataModel)myBuilder.getGraphDataModel();
              if (model.findNode(psiClass) == null) {
                model.addElement(psiClass);
                added = true;
                if (checkForInners && !innerSelected) {
                  innerSelected = PsiUtil.isInnerClass(psiClass);
                }
              }
            }
          }
          final boolean updateLayout = UmlUtils.getPresentationModel(myBuilder).isPopupMode() && added;
          UmlUtils.updateGraph(myBuilder, false, updateLayout);
          if (updateLayout) {
            final JBPopup popup = myBuilder.getUserData(UmlDataKeys.UML_POPUP);
            if (popup != null) {
              GraphUtil.setBestPopupSizeForGraph(popup, myBuilder);
            }
          }
        }
      }, "Adding elements and layouting", true, myBuilder.getProject());
    }
  }
}

