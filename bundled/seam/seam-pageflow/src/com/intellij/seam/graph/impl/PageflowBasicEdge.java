package com.intellij.seam.graph.impl;

import com.intellij.seam.graph.PageflowEdge;
import com.intellij.seam.graph.PageflowNode;
import com.intellij.seam.model.xml.pageflow.Transition;
import org.jetbrains.annotations.NotNull;

/**
 * User: Sergey.Vasiliev
 */
public class PageflowBasicEdge implements PageflowEdge {
  private final PageflowNode mySource;
  private final PageflowNode myTarget;
  private final String myName;
  private final Transition myTransition;
  private final boolean myDuplicated;

  public PageflowNode getSource() {
    return mySource;
  }

  public PageflowBasicEdge(final PageflowNode source, final PageflowNode target, final Transition transition, boolean duplicated) {
    mySource = source;
    myTarget = target;
    myName = transition.getName().getStringValue();
    myTransition = transition;
    myDuplicated = duplicated;
  }

  public PageflowNode getTarget() {
    return myTarget;
  }

  public String getName() {
    return myName == null? "" : myName;
  }

  @NotNull
  public Transition getIdentifyingElement() {
    return myTransition;
  }

  public boolean isDuplicated() {
    return myDuplicated;
  }

  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    final PageflowBasicEdge that = (PageflowBasicEdge)o;

    if (mySource != null ? !mySource.equals(that.mySource) : that.mySource != null) return false;
    if (myTarget != null ? !myTarget.equals(that.myTarget) : that.myTarget != null) return false;
    if (myTransition != null ? !myTransition.equals(that.myTransition) : that.myTransition != null) return false;

    return true;
  }

  public int hashCode() {
    int result;
    result = (mySource != null ? mySource.hashCode() : 0);
    result = 31 * result + (myTarget != null ? myTarget.hashCode() : 0);
    result = 31 * result + (myTransition != null ? myTransition.hashCode() : 0);
    return result;
  }
}
