/*
 * Created on 21.06.2005
 */
package com.intellij.eclipse.export.model.codestyle;

/**
 * @author Sergey.Grigorchuk
 */
public abstract class AbstractCodeStyleParam {

  private String name;

  /**
   * @param name
   */
  protected AbstractCodeStyleParam(String name) {
    super();
    this.name = name;
  }

  /**
   * @return Returns the name.
   */
  public String getName() {
    return name;
  }
}
