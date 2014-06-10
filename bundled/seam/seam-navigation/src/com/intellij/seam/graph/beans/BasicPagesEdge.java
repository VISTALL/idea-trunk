package com.intellij.seam.graph.beans;

import com.intellij.seam.model.xml.pages.PagesViewIdOwner;
import com.intellij.util.xml.DomElement;
import org.jetbrains.annotations.NotNull;

/**
 * User: Sergey.Vasiliev
 */
public abstract class BasicPagesEdge<T extends DomElement> {
  private final BasicPagesNode mySource;
  private final BasicPagesNode myTarget;
  private final PagesViewIdOwner myViewId;
  private final T myParentElement;

  public BasicPagesNode getSource() {
    return mySource;
  }

  public BasicPagesNode getTarget() {
    return myTarget;
  }

  public BasicPagesEdge(@NotNull final BasicPagesNode source, @NotNull final BasicPagesNode target, final PagesViewIdOwner
                   redirect, @NotNull final T parentElement) {
    mySource = source;
    myTarget = target;
    myViewId = redirect.createStableCopy();
    myParentElement = parentElement.<T>createStableCopy();
  }

  public String getName() {
    return "";
  }

  public PagesViewIdOwner getViewId() {
    return myViewId;
  }

  public T getParentElement() {
    return myParentElement;
  }

  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    final BasicPagesEdge that = (BasicPagesEdge)o;

    if (!myViewId.equals(that.myViewId)) return false;
    if (!mySource.equals(that.mySource)) return false;
    if (!myTarget.equals(that.myTarget)) return false;

    return true;
  }

  public int hashCode() {
    int result;
    result = mySource.hashCode();
    result = 31 * result + myTarget.hashCode();
    result = 31 * result + myViewId.hashCode();
    return result;
  }
}

