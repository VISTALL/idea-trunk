package com.intellij.webBeans.toolWindow.tree.nodes;

import com.intellij.util.containers.HashSet;

import java.util.Set;

public class WebBeansNodesConfig {
  @SuppressWarnings({"SetReplaceableByEnumSet"}) private Set<WebBeansNodeTypes> myShowNodeTypes = new HashSet<WebBeansNodeTypes>();

  public WebBeansNodesConfig() {
    myShowNodeTypes.add(WebBeansNodeTypes.BINDING);
    myShowNodeTypes.add(WebBeansNodeTypes.DEPLOYMENT);
    myShowNodeTypes.add(WebBeansNodeTypes.SCOPE);
  }

  synchronized public boolean isShow(WebBeansNodeTypes nodeType) {
    return myShowNodeTypes.contains(nodeType);
  }

  synchronized public void show(WebBeansNodeTypes nodeType) {
    myShowNodeTypes.add(nodeType);
  }

  synchronized public void hide(WebBeansNodeTypes nodeType) {
    myShowNodeTypes.remove(nodeType);
  }

  synchronized public void hideAll(WebBeansNodeTypes nodeType) {
    myShowNodeTypes.clear();
  }
}
