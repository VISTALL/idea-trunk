package com.intellij.spring.model.actions.patterns.frameworks.util;

import com.intellij.util.xmlb.annotations.Attribute;
import com.intellij.util.xmlb.annotations.Tag;

@Tag("docLink")
public class StandardBeansDocLink {

  @Attribute("beanId")
  public String myBeanId;

  @Attribute("api")
  public String myApiLink;

  @Attribute("reference")
  public String myReferenceLink;

  public String getBeanId() {
    return myBeanId;
  }

  public String getApiLink() {
    return myApiLink;
  }

  public String getReferenceLink() {
    return myReferenceLink;
  }
}

