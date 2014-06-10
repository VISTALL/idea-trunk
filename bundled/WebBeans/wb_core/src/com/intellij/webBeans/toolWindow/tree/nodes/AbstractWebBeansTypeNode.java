package com.intellij.webBeans.toolWindow.tree.nodes;

import com.intellij.openapi.module.Module;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractWebBeansTypeNode extends AbstractWebBeansNode {
  private Module myModule;
  private WebBeansNodeTypes myType;
  private String myNodeName;

  protected AbstractWebBeansTypeNode(WebBeansModuleNode webBeansModuleNode, @NotNull Module module, @NotNull WebBeansNodeTypes type,
                                     String nodeName) {
    super(webBeansModuleNode);
    myModule = module;
    myType = type;
    myNodeName = nodeName;
  }

  @Override
  protected void doUpdate() {
    setPlainText(myNodeName);
  }

  public Module getModule() {
    return myModule;
  }

  public WebBeansNodeTypes getType() {
    return myType;
  }

  @Override
  public Object[] getEqualityObjects() {
    return new Object[]{myType};
  }
}
