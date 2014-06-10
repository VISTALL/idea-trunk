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

package com.intellij.uml.renderers;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.ActionPopupMenu;
import com.intellij.openapi.graph.builder.GraphBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.FileStatus;
import com.intellij.openapi.vcs.FileStatusManager;
import com.intellij.openapi.vcs.changes.PsiChangeTracker;
import com.intellij.openapi.vcs.changes.PsiElementFilter;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.uml.components.ClassItemComponent;
import com.intellij.uml.components.UmlClassProperty;
import com.intellij.uml.model.UmlEdge;
import com.intellij.uml.model.UmlNode;
import com.intellij.uml.presentation.UmlColorManager;
import com.intellij.uml.presentation.UmlDiagramPresentation;
import com.intellij.uml.presentation.VisibilityLevel;
import com.intellij.uml.utils.PsiUtils;
import com.intellij.uml.utils.UmlUtils;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.*;
import java.util.List;

/**
 * @author Konstantin Bulenkov
 */
public class UmlPsiClassContainer extends JList implements ListCellRenderer {
  private final Project project;
  private final GraphBuilder<UmlNode, UmlEdge> myBuilder;
  private final Point basePoint;
  private final Map<PsiElement, Color> colors = new HashMap<PsiElement, Color>();
  private static final Comparator<PsiNamedElement> ELEMENTS_COMPARATOR = new Comparator<PsiNamedElement>() {
    public int compare(PsiNamedElement f1, PsiNamedElement f2) {
      final String name = f1.getName();
      if (f1.isPhysical() == f2.isPhysical()) {
        return name == null ? -1 : name.compareTo(f2.getName());
      } else {
        return f1.isPhysical() ? -1 : 1;
      }
    }
  };

  private static final PsiElementFilter<PsiMethod> METHOD_FILTER = new PsiElementFilter<PsiMethod>(PsiMethod.class) {
    @Override
    public boolean accept(PsiMethod element) {
      return ! PsiUtils.isAnonymousClass(element.getContainingClass());
    }
  };

  private static final PsiElementFilter<PsiField> FIELD_FILTER = new PsiElementFilter<PsiField>(PsiField.class) {
    @Override
    public boolean accept(PsiField element) {
      return ! PsiUtils.isAnonymousClass(element.getContainingClass());
    }
  };

  public UmlPsiClassContainer(PsiClass psiClass, GraphBuilder<UmlNode, UmlEdge> builder, Point point) {
    super(getElements(psiClass, UmlUtils.getPresentation(builder)));
    project = psiClass.getProject();
    myBuilder = builder;
    basePoint = point;
    setCellRenderer(this);
    setBackground(UmlColorManager.getInstance().getNodeBackground(false));

    if (UmlUtils.getPresentation(builder).isColorManagerEnabled()) {
      initColors(psiClass, UmlUtils.getPresentation(builder));
    }

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

  private void initColors(PsiClass psiClass, UmlDiagramPresentation presentation) {
    final PsiFile psiFile = psiClass.getContainingFile();
    VirtualFile file;
    if (psiFile == null || (file = psiFile.getVirtualFile()) == null) return;

    final FileStatus status = FileStatusManager.getInstance(project).getStatus(file);
    if (status != FileStatus.NOT_CHANGED) {
      if (presentation.isFieldsVisible()) {
        final Map<PsiField, FileStatus> fields = PsiChangeTracker.getElementsChanged(psiFile, FIELD_FILTER);
        for (PsiField field : fields.keySet()) {
          colors.put(field, fields.get(field).getColor());
        }
      }
      if (presentation.isConstructorsVisible() || presentation.isMethodsVisible()) {
        final Map<PsiMethod, FileStatus> methods = PsiChangeTracker.getElementsChanged(psiFile, METHOD_FILTER);
        for (PsiMethod method : methods.keySet()) {
          colors.put(method, methods.get(method).getColor());
        }
      }
    }
  }

  public Component getListCellRendererComponent(final JList list,
                                                final Object value,
                                                final int index,
                                                final boolean isSelected,
                                                final boolean cellHasFocus) {
    final Color color = getElementColor(value);
    if (value instanceof PsiField) {
      return new ClassItemComponent((PsiField)value, isSelected, color);
    } else if (value instanceof PsiMethod) {
      return new ClassItemComponent((PsiMethod)value, isSelected, color);
    } else if (value instanceof UmlClassProperty) {
      return new ClassItemComponent((UmlClassProperty)value, isSelected, color);
    } else if (value == null) {
      return new LineSeparator();
    } else {
      return new DefaultListCellRenderer();
    }      
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

  private static Object[] getElements(PsiClass clazz, UmlDiagramPresentation presentation) {
    final List<Object> elements = new ArrayList<Object>();
    final VisibilityLevel visibility = presentation.getVisibilityLevel();
    Map<PsiField, FileStatus> fieldsModified = null;
    Map<PsiMethod, FileStatus> methodsModified = null;
    final boolean vcsEnabled = presentation.isVcsFilterEnabled();
    if (vcsEnabled) {
      final PsiFile file = clazz.getContainingFile();
      fieldsModified = PsiChangeTracker.getElementsChanged(file, FIELD_FILTER);
      methodsModified = PsiChangeTracker.getElementsChanged(file, METHOD_FILTER);
    }

    if (presentation.isFieldsVisible()) {
      List<PsiField> fields = PsiUtils.getFields(clazz, visibility);
      if (vcsEnabled) {
        fields = new ArrayList<PsiField>(fieldsModified.keySet());
      }
      if (presentation.isPropertiesVisible()) {
        PsiUtils.removePropertiesFromFields(clazz, fields);
      }

      if (!fields.isEmpty()) {
        Collections.sort(fields, ELEMENTS_COMPARATOR);
        elements.addAll(fields);
        elements.add(null); //add a separator
      }
    }

    if (presentation.isPropertiesVisible()) {
      final List<UmlClassProperty> properties = PsiUtils.getProperties(clazz, visibility);
      elements.addAll(properties);
      if (!properties.isEmpty()) {
        elements.add(null); //add a separator
      }
    }

    if (presentation.isConstructorsVisible()) {
      List<PsiMethod> constructors = PsiUtils.getConstructors(clazz, visibility);
      if (vcsEnabled) {
        constructors = PsiUtils.getConstructors(methodsModified.keySet(), visibility);
      }
      if (! constructors.isEmpty()) {
        Collections.sort(constructors, ELEMENTS_COMPARATOR);
        elements.addAll(constructors);
        elements.add(null); //add a separator
      }
    }

    if (presentation.isMethodsVisible()) {
      List<PsiMethod> methods = PsiUtils.getMethods(clazz, visibility);
      if (vcsEnabled) {
        methods = PsiUtils.getMethods(methodsModified.keySet(), visibility);
      }
      if (presentation.isPropertiesVisible()) {
        PsiUtils.removePropertiesFromMethods(clazz, methods);
      }
      Collections.sort(methods, ELEMENTS_COMPARATOR);
      elements.addAll(methods);
    }

    if (! elements.isEmpty() && elements.get(elements.size() - 1) == null) {
      elements.remove(elements.size() - 1);
    }
    return elements.toArray(new Object[elements.size()]);
  }

  private static class LineSeparator extends JPanel {
    public LineSeparator() {
      setBackground(Color.GRAY);
      setPreferredSize(new Dimension(-1, 1));
    }
  }
}
