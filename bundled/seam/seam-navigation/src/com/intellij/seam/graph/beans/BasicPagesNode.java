package com.intellij.seam.graph.beans;

import com.intellij.util.xml.DomElement;
import com.intellij.seam.PagesIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * User: Sergey.Vasiliev
 */
public abstract class BasicPagesNode<T extends DomElement> {

  private final T myIdentifyingElement;
  private final String myName;

  protected BasicPagesNode(@NotNull final T identifyingElement, @Nullable final String name) {
    myIdentifyingElement = identifyingElement;
    myName = name;
  }

  public String getName() {
    return myName;
  }

  @NotNull
  public T getIdentifyingElement() {
    return myIdentifyingElement;
  }

  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    final BasicPagesNode pagesNode = (BasicPagesNode)o;

    if (!myIdentifyingElement.equals(pagesNode.myIdentifyingElement)) return false;

    return true;
  }

  public int hashCode() {
    int result;
    result = myIdentifyingElement.hashCode();
    result = 31 * result + (myName != null ? myName.hashCode() : 0);
    return result;
  }

  public Icon getIcon() {
    return PagesIcons.PAGE;
  }
}
