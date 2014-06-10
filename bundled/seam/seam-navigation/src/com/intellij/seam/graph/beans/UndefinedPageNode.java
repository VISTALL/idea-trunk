package com.intellij.seam.graph.beans;

import com.intellij.seam.model.xml.pages.PagesViewIdOwner;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * User: Sergey.Vasiliev
 */
public class UndefinedPageNode extends BasicPagesNode<PagesViewIdOwner>{
  public UndefinedPageNode(@NotNull final PagesViewIdOwner identifyingElement, @Nullable final String name) {
    super(identifyingElement, name);
  }
}
