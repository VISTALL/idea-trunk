package com.intellij.seam.graph;

import com.intellij.util.xml.DomElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public interface PageflowNode<T extends DomElement> {
  @Nullable
  String getName();

  @NotNull
  PageflowNodeType getNodeType();

  Icon getIcon();

  @NotNull
  T getIdentifyingElement();
}
