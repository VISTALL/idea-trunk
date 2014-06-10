package com.intellij.seam.graph.beans;

import com.intellij.seam.model.xml.pages.Page;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * User: Sergey.Vasiliev
 */
public class PageNode extends BasicPagesNode<Page> {
  public PageNode(@NotNull final Page identifyingElement, @Nullable final String name) {
    super(identifyingElement, name);
  }
}
