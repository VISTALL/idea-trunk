package com.intellij.seam.graph.beans;

import com.intellij.seam.PagesIcons;
import com.intellij.seam.model.xml.pages.PagesException;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * User: Sergey.Vasiliev
 */
public class ExceptionNode extends BasicPagesNode<PagesException>{
  public ExceptionNode(@NotNull final PagesException identifyingElement, @Nullable final String name) {
    super(identifyingElement, name);
  }

  public String getName() {
    return StringUtil.getShortName(getQualifiedName());
  }

  public String getQualifiedName() {
    return super.getName();
  }

  public Icon getIcon() {
    return PagesIcons.EXCEPTION;
  }
}
