package com.intellij.spring.impl.model.beans;

import com.intellij.spring.model.xml.beans.SpringDomAttribute;

/**
 * @author Dmitry Avdeev
 */
@SuppressWarnings({"AbstractClassNeverImplemented"})
public abstract class SpringDomAttributeImpl implements SpringDomAttribute {

  public String getAttributeKey() {
    return getKey().getValue();
  }

  public String getAttributeValue() {
    return getValue().getValue();
  }
}
