package com.intellij.seam.graph.impl;

import com.intellij.seam.graph.PageflowNodeType;
import com.intellij.seam.model.xml.pageflow.Decision;
import com.intellij.seam.PageflowIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * User: Sergey.Vasiliev
 */
public class DecisionNode extends PageflowBasicNode<Decision> {

  public DecisionNode(String name, Decision identifyingElement) {
    super(identifyingElement, name);
  }

  @NotNull
  public PageflowNodeType getNodeType() {
    return PageflowNodeType.DECISIION;
  }

  public Icon getIcon() {
    return PageflowIcons.PAGEFLOW_DECISION;
  }
}
