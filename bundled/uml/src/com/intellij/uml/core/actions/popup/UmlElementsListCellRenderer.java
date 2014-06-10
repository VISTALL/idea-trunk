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

import com.intellij.ide.ui.UISettings;
import com.intellij.ide.util.ModuleRendererFactory;
import com.intellij.navigation.ItemPresentation;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.editor.markup.EffectType;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.vcs.FileStatus;
import com.intellij.openapi.vcs.FileStatusManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.problems.WolfTheProblemSolver;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.ui.*;
import com.intellij.uml.UmlProvider;
import com.intellij.util.IconUtil;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.Comparator;

/**
 * @author Konstantin Bulenkov
 */
public abstract class UmlElementsListCellRenderer extends JPanel implements ListCellRenderer {
  private final UmlProvider myUmlProvider;

  protected UmlElementsListCellRenderer(UmlProvider umlProvider) {
    super(new BorderLayout());
    myUmlProvider = umlProvider;
  }

  private class LeftRenderer extends ColoredListCellRenderer {
    private final String myModuleName;

    public LeftRenderer(final String moduleName) {
      myModuleName = moduleName;
    }

    protected void customizeCellRenderer(
      JList list,
      Object value,
      int index,
      boolean selected,
      boolean hasFocus
      ) {
      Color bgColor = UIUtil.getListBackground();
      if (value instanceof PsiElement) {
        PsiElement element = (PsiElement)value;
        String name = getElementText(value);
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

            final FileColorManager colorManager = FileColorManager.getInstance(psiFile.getProject());
            if (colorManager.isEnabled()) {
              if (psiFile.isValid()) {
                final Color fileBgColor = colorManager.getFileColor(psiFile);
                bgColor = fileBgColor == null ? bgColor : fileBgColor;
              }
            }
          }
        }

        TextAttributes attributes = null;

        if (value instanceof NavigationItem) {
          TextAttributesKey attributesKey = null;
          final ItemPresentation presentation = ((NavigationItem)value).getPresentation();
          if (presentation != null) attributesKey = presentation.getTextAttributesKey();

          if (attributesKey != null) {
            attributes = EditorColorsManager.getInstance().getGlobalScheme().getAttributes(attributesKey);
          }
        }

        SimpleTextAttributes nameAttributes;
        if (isProblemFile) {
          attributes = TextAttributes.merge(new TextAttributes(color, null, Color.red, EffectType.WAVE_UNDERSCORE, Font.PLAIN),attributes);
        }

        nameAttributes = attributes != null ? SimpleTextAttributes.fromTextAttributes(attributes):null;

        if (nameAttributes == null)  nameAttributes = new SimpleTextAttributes(Font.PLAIN, color);

        assert name != null: "Null name for PSI element " + element;
        append(name, nameAttributes);
        setIcon(UmlElementsListCellRenderer.this.getIcon(element));

        String containerText = getContainerText(element, name + (myModuleName != null ? myModuleName + "        " : ""));
        if (containerText != null) {
          append(" " + containerText, new SimpleTextAttributes(Font.PLAIN, Color.GRAY));
        }
      }
      else {
        setIcon(IconUtil.getEmptyIcon(false));
        append(value == null ? "" : value.toString(), new SimpleTextAttributes(Font.PLAIN, list.getForeground()));
      }
      setPaintFocusBorder(false);
      setBackground(selected ? UIUtil.getListSelectionBackground() : bgColor);
    }
  }

  public Component getListCellRendererComponent(JList list,
                                                Object value,
                                                int index,
                                                boolean isSelected,
                                                boolean cellHasFocus) {
    removeAll();
    String moduleName = null;
    DefaultListCellRenderer rightRenderer = getRightCellRenderer();
    final Component leftCellRendererComponent =
      new LeftRenderer(moduleName).getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
    if (rightRenderer != null) {
      final Component rightCellRendererComponent =
        rightRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
      rightCellRendererComponent.setBackground(isSelected ? UIUtil.getListSelectionBackground() : leftCellRendererComponent.getBackground());
      add(rightCellRendererComponent, BorderLayout.EAST);
      moduleName = rightRenderer.getText();
      final JPanel spacer = new JPanel();
      spacer.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
      spacer.setBackground(isSelected ? UIUtil.getListSelectionBackground() : leftCellRendererComponent.getBackground());
      add(spacer, BorderLayout.CENTER);
    }
    add(leftCellRendererComponent, BorderLayout.WEST);
    setBackground(isSelected ? UIUtil.getListSelectionBackground() : leftCellRendererComponent.getBackground());
    return this;
  }

  @Nullable
  protected DefaultListCellRenderer getRightCellRenderer() {
    if (UISettings.getInstance().SHOW_ICONS_IN_QUICK_NAVIGATION) {
      return ModuleRendererFactory.getInstance().getModuleRenderer();
    }
    return null;
  }

  public String getElementText(Object element) {
    final String text = myUmlProvider.getElementManager().getElementTitle(element);
    return text == null ? "" : text;
  }

  @Nullable
  protected String getContainerText(Object element, final String name) {
    return null;
  }

  protected abstract int getIconFlags();

  protected Icon getIcon(PsiElement element) {
    return element.getIcon(getIconFlags());
  }

  public Comparator getComparator() {
    return new Comparator() {
      public int compare(Object o1, Object o2) {
        return getText(o1).compareTo(getText(o2));
      }

      private String getText(Object element) {
        String elementText = getElementText(element);
        String containerText = getContainerText(element, elementText);
        return containerText != null ? elementText + " " + containerText : elementText;
      }
    };
  }

  public void installSpeedSearch(JList list) {
    new ListSpeedSearch(list) {
      protected String getElementText(Object o) {
        return UmlElementsListCellRenderer.this.getElementText(o);
      }
    };
  }
}
