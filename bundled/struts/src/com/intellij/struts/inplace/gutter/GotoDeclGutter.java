/*
 * Copyright 2000-2006 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.intellij.struts.inplace.gutter;

import com.intellij.codeInsight.navigation.NavigationUtil;
import com.intellij.ide.util.PsiElementListCellRenderer;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.util.Iconable;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlTag;
import com.intellij.struts.StrutsManager;
import com.intellij.struts.StrutsModel;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.DomManager;
import com.intellij.util.xml.DomUtil;
import com.intellij.util.xml.ElementPresentation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.MouseEvent;

/**
 * Provides "Go To Declaration" action in gutter mark.
 *
 * @author davdeev
 * @author Yann Cébron
 */
public abstract class GotoDeclGutter extends GutterIconRendererBase {

  private final PsiElement myElement;
  private final String myTooltip;
  private final AnAction myClickAction;

  private static final DomElementListCellRenderer DOM_ELEMENT_LIST_CELL_RENDERER = new DomElementListCellRenderer();

  protected GotoDeclGutter(@NotNull final PsiElement element, @NotNull final Icon icon, final String tooltip) {
    super(icon);
    myElement = element;
    myTooltip = tooltip;
    myClickAction = new AnAction() {

      public void actionPerformed(final AnActionEvent e) {
        final DomElement[] elements = getDestinations(myElement);
        if (elements == null || elements.length == 0) {

        } else if (elements.length == 1) {
          // only one navigation target
          final PsiElement element = elements[0].getXmlTag();
          if (element instanceof Navigatable && ((Navigatable) element).canNavigateToSource()) {
            ((Navigatable) element).navigate(true);
          }
        } else {
          // show popup for selecting navigation target from list
          final JBPopup gotoDeclarationPopup = NavigationUtil.getPsiElementPopup(
            DomUtil.getElementTags(elements),
            DOM_ELEMENT_LIST_CELL_RENDERER,
            "Goto " + elements[0].getPresentation().getTypeName() + " Declaration");
          gotoDeclarationPopup.show(new RelativePoint((MouseEvent) e.getInputEvent()));
        }
      }
    };
  }

  @Nullable
  protected abstract DomElement[] getDestinations(@NotNull PsiElement element);

  @Nullable
  public String getTooltipText() {
    return myTooltip;
  }

  @Nullable
  public AnAction getClickAction() {
    return myClickAction;
  }

  public boolean isNavigateAction() {
    return true;
  }

  /**
   * Renderer for DOM elements in multiple targets popup.
   */
  private static class DomElementListCellRenderer extends PsiElementListCellRenderer {

    /**
     * Gets the presentation text for the given element.
     *
     * @param psiElement Element from list.
     *
     * @return DomElement's presentation name.
     */
    public String getElementText(final PsiElement psiElement) {
      return getDomElementPresentation(psiElement).getElementName();
    }

    /**
     * Show corresponding Struts Module prefix or name of containing file.
     *
     * @param psiElement Element from list.
     * @param s          Present container text.
     *
     * @return Container text.
     */
    protected String getContainerText(final PsiElement psiElement,
                                      final String s) {
      final StrutsModel model = StrutsManager.getInstance().getStrutsModel(psiElement);
      if (model != null) {
        return '[' + model.getModulePrefix() + ']';
      } else {
        return '(' + psiElement.getContainingFile().getName() + ')';
      }
    }

    /**
     * Show corresponding icon for element.
     *
     * @param psiElement Element from list.
     *
     * @return DomElement's presentation icon.
     */
    protected Icon getIcon(final PsiElement psiElement) {
      return getDomElementPresentation(psiElement).getIcon();
    }

    protected int getIconFlags() {
      return Iconable.ICON_FLAG_CLOSED;
    }

    /**
     * Gets the DOM presentation for the given PsiElement.
     *
     * @param psiElement PsiElement to get presentation for.
     *
     * @return ElementPresentation.
     */
    @NotNull
    private static ElementPresentation getDomElementPresentation(final PsiElement psiElement) {
      final DomElement domElement = DomManager.getDomManager(psiElement.getProject())
        .getDomElement((XmlTag) psiElement);
      assert domElement != null; // we only pass XmlTag from DOM-mapped config files
      return domElement.getPresentation();
    }
  }

}