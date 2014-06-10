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

package com.intellij.uml.core.actions.popup;

import com.intellij.navigation.ItemPresentation;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ShortcutSet;
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
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.PopupChooserBuilder;
import com.intellij.openapi.vcs.FileStatus;
import com.intellij.openapi.vcs.FileStatusManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.problems.WolfTheProblemSolver;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.SimpleColoredText;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.uml.*;
import com.intellij.uml.core.actions.UmlAction;
import com.intellij.uml.utils.UmlUtils;
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
public class AddElementsFromPopupAction extends UmlAction {
  private final UmlElementsProvider myProvider;
  private final UmlProvider myUmlProvider;

  public AddElementsFromPopupAction(UmlElementsProvider provider, GraphBuilder builder) {
    myProvider = provider;
    myUmlProvider = Utils.getProvider(builder);
    setShortcutSet(provider.getShortcutSet());
    registerCustomShortcutSet(provider.getShortcutSet(), builder.getView().getCanvasComponent());
  }

  @Override
  protected void setShortcutSet(ShortcutSet shortcutSet) {
    super.setShortcutSet(shortcutSet);
  }

  @Nullable
  public Object[] getElements(Object element, Project project) {
    return myProvider.getElements(element, project);
  }

  @NotNull
  public String getPopupTitle(Object element, Project project) {
    return myProvider.getHeaderName(element, project);
  }

  @Override
  public void update(AnActionEvent e) {    
    boolean enabled = getSelectedNode(getBuilder(e)) != null;
    e.getPresentation().setText(myProvider.getName());
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

  public void actionPerformed(AnActionEvent e) {
    final GraphBuilder<UmlNode, UmlEdge> builder = getBuilder(e);
    final UmlNode umlNode = getSelectedNode(builder);
    if (builder == null || umlNode == null) return;
    showPopup(builder, umlNode);
  }

  private void showPopup(final GraphBuilder<UmlNode, UmlEdge> builder, final UmlNode umlNode) {
    Graph2D graph = builder.getGraph();
    Graph2DView view = (Graph2DView)graph.getCurrentView();
    List<Node> nodes = GraphViewUtil.getSelectedNodes(graph);
    if (nodes.size() != 1) return;
    Node node = nodes.get(0);
    final JBPopup[] popup = new JBPopup[1];
    final Point p = UmlUtils.getNodeCoordinatesOnScreen(node, view);
    final Object element = umlNode.getIdentifyingElement();

    final Runnable process = new Runnable() {
      public void run() {
        Object[] elements = getElements(element, builder.getProject());
        if (elements == null) return;
        List<Object> existen = new ArrayList<Object>();
        for (UmlNode umlnode : builder.getNodeObjects()) {
          existen.add(umlnode.getIdentifyingElement());
        }

        final DefaultUmlElementsListCellRenderer renderer = new DefaultUmlElementsListCellRenderer(existen);
        Arrays.sort(elements, renderer.getComparator());
        JList list = new JList(elements);
        list.setCellRenderer(renderer);
        MouseMotionListener[] listeners = list.getMouseMotionListeners();
        for (MouseMotionListener listener : listeners) {
          list.removeMouseMotionListener(listener);
        }
        renderer.installSpeedSearch(list);
        popup[0] = new PopupChooserBuilder(list)
          .setItemChoosenCallback(new SubclassFoundCallback(builder, list))
          .setMovable(true)
          .setTitle(getPopupTitle(element, builder.getProject()))
          .createPopup();
      }
    };

    if (myProvider.showProgress()) {
      ProgressManager.getInstance().runProcessWithProgressSynchronously(process, myProvider.getProgressMessage(), true, builder.getProject());
    } else {
      process.run();
    }

    if (popup[0] != null) {
      popup[0].showInScreenCoordinates(view.getCanvasComponent(), new Point(p.x, p.y + (int)(24 * view.getZoom()))); // 24 == HEADER HEIGHT
    }
  }

  private class DefaultUmlElementsListCellRenderer extends UmlElementsListCellRenderer {
    private final List existen;

    public DefaultUmlElementsListCellRenderer(List existen) {
      super(myUmlProvider);
      this.existen = existen;
    }

    private class LeftRenderer extends ColoredListCellRenderer {
      private final String myModuleName;

      public LeftRenderer(String moduleName) {
        myModuleName = moduleName;
      }

      protected void customizeCellRenderer(JList list, Object value, int index, boolean selected, boolean hasFocus) {
        if (value instanceof PsiElement) {
          PsiElement element = (PsiElement)value;
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
          setIcon(AddElementsFromPopupAction.this.myUmlProvider.getElementManager().getNodeElementIcon(value));
          final SimpleColoredText text = AddElementsFromPopupAction.this.myUmlProvider.getElementManager().getPresentableName(value);
          text.appendToComponent(this);
        }
        setPaintFocusBorder(false);
        setBackground(selected ? UIUtil.getListSelectionBackground() : UIUtil.getListBackground());
      }
    }

    @Override
    public Comparator<Object> getComparator() {
      final Comparator comp = super.getComparator();
      return new Comparator<Object>(){
        public int compare(Object c1, Object c2) {
          final boolean b1 = existen.contains(c1);
          final boolean b2 = existen.contains(c2);
          if ( ((b1 && b2) || (!b1 && !b2)) ) {
            final int result = myProvider.getComparator().compare(c1, c2);
            return result == 0 ? comp.compare(c1, c2) : result;
          } else {
            return b1 ? 1 : -1;
          }
        }
      };
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
      Object[] selected = myList.getSelectedValues();
      boolean added = false;
      for (Object obj : selected) {
        UmlDataModel model = Utils.getDataModel(myBuilder);
        model.addElement(obj);
        added = true;
      }
      final boolean updateLayout = Utils.isPopupMode(myBuilder) && added;
      Utils.updateGraph(myBuilder, false, updateLayout);
      if (updateLayout) {
        final JBPopup popup = myBuilder.getUserData(UmlDataKeys.UML_POPUP);
        if (popup != null) {
          GraphUtil.setBestPopupSizeForGraph(popup, myBuilder);
        }
      }
    }
  }
}

