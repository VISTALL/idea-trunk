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

import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.graph.builder.GraphBuilder;
import com.intellij.openapi.graph.view.NodeCellEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.FocusCommand;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.psi.PsiElement;
import com.intellij.uml.core.renderers.UmlNodeBodyComponent;
import com.intellij.uml.renderers.UmlPsiClassContainer;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * @author Konstantin Bulenkov
 */
public class UmlNodeEditorManager implements DataProvider {
  private static final UmlNodeEditorManager instance = new UmlNodeEditorManager();
  private UmlPsiClassNodeCellEditor currentCellEditor;
  private JList currentContainer;
  private UmlNodeCellEditor currentNewCellEditor;

  private UmlNodeEditorManager(){}

  public static UmlNodeEditorManager getInstance() {
    return instance;
  }

  public NodeCellEditor getCurrentCellEditor() {
    return currentCellEditor == null ? currentNewCellEditor : currentCellEditor;
  }

  public void setCurrentCellEditor(final AbstractCellEditor editor, final GraphBuilder builder) {
    if (editor instanceof UmlPsiClassNodeCellEditor) {
      currentCellEditor = (UmlPsiClassNodeCellEditor)editor;
      currentNewCellEditor = null;
    } else if (editor instanceof UmlNodeCellEditor) {
      currentCellEditor = null;
      currentNewCellEditor = (UmlNodeCellEditor)editor;
    } else {
      currentCellEditor = null;
      currentNewCellEditor = null;
      currentContainer = null;
    }

    currentContainer = getCurrentContainer();
    final Project project = getProject();
    if (currentContainer != null && project != null) {
      if (builder != null) {
        currentContainer.addKeyListener(new KeyAdapter() {
          @Override
          public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
              stopEditing(builder);
            }
          }
        });
      }

      currentContainer.setSelectedIndex(0);
      IdeFocusManager.getInstance(project).requestFocus(new UmlFocusCommand(currentContainer), true);
    }
  }

  @Nullable
  private Project getProject() {
    if (currentContainer instanceof UmlNodeBodyComponent) {
      return ((UmlNodeBodyComponent)currentContainer).getProject();
    } else if (currentContainer instanceof UmlPsiClassContainer) {
      return ((UmlPsiClassContainer)currentContainer).getProject();
    }
    return null;
  }

  @Nullable
  private JList getCurrentContainer() {
    if (currentCellEditor != null) {
      return findComponent(currentCellEditor.getEditor(), UmlPsiClassContainer.class);
    }
    if (currentNewCellEditor != null) {
      return findComponent(currentNewCellEditor.getEditor(), UmlNodeBodyComponent.class);
    }
    return null;
  }

  public Object getData(@NonNls final String dataId) {
    JList editor = currentContainer; //synchronization
    if (editor == null) return null;
    if (dataId.equals(DataKeys.PSI_ELEMENT.getName())) {
      Object value = editor.getSelectedValue();    
      if (value instanceof PsiElement) {
        if (((PsiElement)value).isPhysical()) {
          return value;
        }
      }
    }
    return null;
  }

  @Nullable
  private static <T extends JComponent> T findComponent(JComponent parent, Class<T> cls) {
    if (parent == null || parent.getClass() == cls) return (T)parent;
    for (Component component : parent.getComponents()) {
      if (component instanceof JComponent) {
        T comp = findComponent((JComponent)component, cls);
        if (comp != null) return comp;
      }
    }
    return null;
  }

  public void stopEditing(GraphBuilder builder) {
    if (currentCellEditor != null) {
      currentCellEditor.stopCellEditing();
    }
    if (currentNewCellEditor != null) {
      currentNewCellEditor.stopCellEditing();
    }
    final JComponent canvas = builder.getView().getCanvasComponent();
    IdeFocusManager.getInstance(builder.getProject()).requestFocus(new UmlFocusCommand(canvas), true);
  }

  private static class UmlFocusCommand extends FocusCommand.ByComponent {
    public UmlFocusCommand(@Nullable Component toFocus) {
      super(toFocus);
    }

    @Override
    public boolean isExpired() {
      return false;
    }
  }
}