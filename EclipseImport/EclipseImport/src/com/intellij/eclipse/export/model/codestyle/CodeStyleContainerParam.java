/*
 * Created on 21.06.2005
 */
package com.intellij.eclipse.export.model.codestyle;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Sergey.Grigorchuk
 */
public class CodeStyleContainerParam extends AbstractCodeStyleParam {

  private Map children = new HashMap();

  /**
   * @param name
   */
  CodeStyleContainerParam(String name) {
    super(name);
  }

  public void addChild(AbstractCodeStyleParam child) {
    children.put(child.getName(), child);
  }

  public void removeChild(AbstractCodeStyleParam child) {
    children.remove(child);
  }

  public AbstractCodeStyleParam[] getChildren() {
    return (AbstractCodeStyleParam[])children.values()
      .toArray(new AbstractCodeStyleParam[children.size()]);
  }

  public AbstractCodeStyleParam getChild(String name) {
    return (AbstractCodeStyleParam)children.get(name);
  }
}
