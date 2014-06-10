package com.intellij.seam.graph.impl;

import com.intellij.seam.PageflowIcons;
import com.intellij.seam.graph.PageflowNodeType;
import com.intellij.seam.model.xml.pageflow.ProcessState;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * User: Sergey.Vasiliev
 */
public class ProcessStateNode extends PageflowBasicNode<ProcessState> {

  public ProcessStateNode(String name, ProcessState identifyingElement) {
    super(identifyingElement, name);
  }

  @NotNull
  public PageflowNodeType getNodeType() {
    return PageflowNodeType.PROCESS_STATE;
  }

  public Icon getIcon() {
    return PageflowIcons.PAGEFLOW_PROCESS_STATE;
  }
}