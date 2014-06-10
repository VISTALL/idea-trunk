/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.struts;

import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.util.Iconable;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.ElementPresentationUtil;
import com.intellij.psi.jsp.JspFile;
import com.intellij.psi.xml.XmlFile;
import com.intellij.struts.dom.Forward;
import com.intellij.struts.dom.StrutsConfig;
import com.intellij.struts.dom.Action;
import com.intellij.struts.dom.tiles.TilesDefinitions;
import com.intellij.struts.dom.validator.FormValidation;
import com.intellij.struts.util.PsiClassUtil;
import com.intellij.ui.LayeredIcon;
import com.intellij.ui.RowIcon;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.DomIconProvider;
import com.intellij.util.xml.DomManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author peter
 */
public class StrutsIconProvider extends DomIconProvider implements DumbAware {
  // IconProvider -------------------------------------------------------------
  // original code posted by Sascha Weinreuter

  private boolean active;

  public Icon getIcon(@NotNull DomElement element, int flags) {
    if (element instanceof Forward) {
      return element.getParent() instanceof Action ? StrutsIcons.FORWARD_ICON : StrutsIcons.GLOBAL_FORWARD_ICON;
    }
    return null;
  }

  @Nullable
  public Icon getIcon(@NotNull final PsiElement element, final int flags) {

    if (element instanceof JspFile) {
      return null;
    }
    if (!(element instanceof PsiClass || element instanceof XmlFile)) {
      return null;
    }
    // IconProvider queries non-physical PSI as well (e.g. completion items)
    if (!element.isPhysical()) {
      return null;
    }

    // for getting the original icon from IDEA
    if (active) {
      return null;
    }

    active = true;

    try {
      Icon strutsIcon = null;
      Icon icon = null;

      // handle XML files
      if (element instanceof XmlFile) {
        final XmlFile xmlFile = (XmlFile) element;
        final DomManager domManager = DomManager.getDomManager(xmlFile.getProject());

        if (domManager.getFileElement(xmlFile, StrutsConfig.class) != null) {
          strutsIcon = StrutsIcons.ACTION_SMALL_ICON;
        } else if (domManager.getFileElement(xmlFile, TilesDefinitions.class) != null) {
          strutsIcon = StrutsIcons.TILES_SMALL_ICON;
        } else if (domManager.getFileElement(xmlFile, FormValidation.class) != null) {
          strutsIcon = StrutsIcons.VALIDATOR_SMALL_ICON;
        }
      }
      // handle JAVA classes
      else if (element instanceof PsiClass) {
        final PsiClass psiClass = (PsiClass) element;

        if (PsiClassUtil.isSuper(psiClass, "org.apache.struts.action.Action")) {
          strutsIcon = StrutsIcons.ACTION_SMALL_ICON;
        } else if (PsiClassUtil.isSuper(psiClass, "org.apache.struts.action.ActionForm")) {
          strutsIcon = StrutsIcons.FORMBEAN_SMALL_ICON;
        } else if (PsiClassUtil.isSuper(psiClass, "org.apache.struts.tiles.Controller")) {
          strutsIcon = StrutsIcons.TILES_SMALL_ICON;
        }
      }

      // match? build new layered icon
      if (strutsIcon != null) {
        icon = new LayeredIcon(2);
        final Icon original = element.getIcon(flags & ~Iconable.ICON_FLAG_VISIBILITY);
        ((LayeredIcon) icon).setIcon(original, 0);
        ((LayeredIcon) icon).setIcon(strutsIcon, 1, StrutsIcons.OVERLAY_ICON_OFFSET_X, StrutsIcons.OVERLAY_ICON_OFFSET_Y);
        if (element instanceof PsiClass) {
          RowIcon rowIcon = new RowIcon(2);
          rowIcon.setIcon(icon, 0);
          icon = ElementPresentationUtil.addVisibilityIcon((PsiClass) element, flags, rowIcon);
        }

      }

      return icon;
    } finally {
      active = false;
    }

  }

}
