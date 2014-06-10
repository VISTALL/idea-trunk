package com.intellij.seam.graph.impl;

import com.intellij.seam.graph.PageflowNodeType;
import com.intellij.seam.model.xml.pageflow.StartState;
import com.intellij.seam.PageflowIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * User: Sergey.Vasiliev
 */
public class StartStateNode extends PageflowBasicNode<StartState> {

  public StartStateNode(String name, StartState identifyingElement) {
    super(identifyingElement, name);
  }

  @NotNull
  public PageflowNodeType getNodeType() {
    return PageflowNodeType.START_STATE;
  }

  public Icon getIcon() {
    return PageflowIcons.PAGEFLOW_START;
  }
}

