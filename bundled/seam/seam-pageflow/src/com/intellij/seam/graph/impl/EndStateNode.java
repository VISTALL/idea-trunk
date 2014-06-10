package com.intellij.seam.graph.impl;

import com.intellij.seam.graph.PageflowNodeType;
import com.intellij.seam.model.xml.pageflow.EndState;
import com.intellij.seam.PageflowIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * User: Sergey.Vasiliev
 */
public class EndStateNode extends PageflowBasicNode<EndState> {

  public EndStateNode(String name, EndState identifyingElement) {
    super(identifyingElement, name);
  }

  @NotNull
  public PageflowNodeType getNodeType() {
    return PageflowNodeType.END_STATE;
  }

  public Icon getIcon() {
    return PageflowIcons.PAGEFLOW_END_STATE;
  }
}
