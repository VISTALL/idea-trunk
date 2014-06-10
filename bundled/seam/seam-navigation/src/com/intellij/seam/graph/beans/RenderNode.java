package com.intellij.seam.graph.beans;

import com.intellij.seam.model.xml.pages.Render;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * User: Sergey.Vasiliev
 */
public class RenderNode extends BasicPagesNode<Render> {

  public RenderNode(@NotNull final Render identifyingElement, @Nullable final String name) {
    super(identifyingElement, name);
  }
}

