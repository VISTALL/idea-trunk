package com.intellij.seam.model.metadata;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.RenameableFakePsiElement;
import com.intellij.seam.SeamIcons;

import javax.swing.*;

public class SeamEventType extends RenameableFakePsiElement {
  private final String myEventType;

  public SeamEventType(final String eventType, final PsiFile containingFile) {
    super(containingFile);
    myEventType = eventType;
  }

  public String getName() {
    return myEventType;
  }

  public PsiElement getParent() {
    return getContainingFile();
  }

  public String getTypeName() {
    return "Event Type";
  }

  public Icon getIcon() {
    return SeamIcons.SEAM_COMPONENT_ICON;
  }
}
