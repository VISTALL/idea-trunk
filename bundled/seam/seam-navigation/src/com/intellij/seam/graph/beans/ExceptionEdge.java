package com.intellij.seam.graph.beans;

import com.intellij.seam.model.xml.pages.PagesException;
import com.intellij.seam.model.xml.pages.Redirect;
import org.jetbrains.annotations.NotNull;

/**
 * User: Sergey.Vasiliev
 */
public class ExceptionEdge extends BasicPagesEdge<PagesException> {
  public ExceptionEdge(@NotNull final BasicPagesNode source, @NotNull final BasicPagesNode target, final Redirect redirect, @NotNull final PagesException parentElement) {
    super(source, target, redirect, parentElement);
  }
}
