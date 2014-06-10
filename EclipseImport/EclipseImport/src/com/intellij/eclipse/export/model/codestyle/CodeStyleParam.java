/*
 * Created on 21.06.2005
 */
package com.intellij.eclipse.export.model.codestyle;

/**
 * @author Sergey.Grigorchuk
 */
public class CodeStyleParam extends AbstractCodeStyleParam {

  private String value;

  /**
   * @param name
   * @param value
   */
  CodeStyleParam(String name, String value) {
    super(name);
    this.value = value;
  }

  /**
   * @param name
   */
  public CodeStyleParam(String name) {
    super(name);
  }

  /**
   * @return Returns the value.
   */
  public String getValue() {
    return value;
  }

  /**
   * @param value The value to set.
   */
  public void setValue(String value) {
    this.value = value;
  }
}
