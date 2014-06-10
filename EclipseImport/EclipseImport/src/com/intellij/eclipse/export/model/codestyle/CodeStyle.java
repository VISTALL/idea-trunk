/*
 * Created on 21.06.2005
 */
package com.intellij.eclipse.export.model.codestyle;

import java.util.StringTokenizer;

/**
 * @author Sergey.Grigorchuk
 */
public class CodeStyle extends CodeStyleContainerParam {

  /**
   *
   */
  public CodeStyle() {
    super("root");
  }

  public AbstractCodeStyleParam getOrCreateParam(String pathName, String value) {
    StringTokenizer st = new StringTokenizer(pathName, "/\\.");
    AbstractCodeStyleParam prev = this;
    while (st.hasMoreTokens()) {
      String name = st.nextToken();
      CodeStyleContainerParam containerParam = (CodeStyleContainerParam)prev;
      AbstractCodeStyleParam child = containerParam.getChild(name);
      if (child == null) {
        if (st.hasMoreElements())
          child = new CodeStyleContainerParam(name);
        else
          child = new CodeStyleParam(name, value);
        containerParam.addChild(child);
      }
      prev = child;
    }
    return prev;
  }
}
