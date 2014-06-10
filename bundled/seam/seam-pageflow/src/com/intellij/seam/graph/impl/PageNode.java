package com.intellij.seam.graph.impl;

import com.intellij.seam.graph.PageflowNodeType;
import com.intellij.seam.model.xml.pageflow.PageElements;
import com.intellij.seam.model.xml.pageflow.StartPage;
import com.intellij.seam.PageflowIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * User: Sergey.Vasiliev
 */
public class PageNode extends PageflowBasicNode<PageElements> {

  public PageNode(String name, PageElements identifyingElement) {
    super(identifyingElement, name);
  }

  @NotNull
  public PageflowNodeType getNodeType() {
    return getIdentifyingElement() instanceof StartPage ? PageflowNodeType.START_PAGE : PageflowNodeType.PAGE;
  }

  public Icon getIcon() {
    return getIdentifyingElement() instanceof StartPage ? PageflowIcons.PAGEFLOW_START_PAGE : PageflowIcons.PAGEFLOW_PAGE;
  }
}
