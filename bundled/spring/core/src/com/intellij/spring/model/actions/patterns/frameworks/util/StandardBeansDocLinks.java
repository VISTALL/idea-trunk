package com.intellij.spring.model.actions.patterns.frameworks.util;

import com.intellij.util.xmlb.annotations.AbstractCollection;
import com.intellij.util.xmlb.annotations.Tag;

public class StandardBeansDocLinks {

  @Tag("links")
  @AbstractCollection(surroundWithTag = false)
  public StandardBeansDocLink[] myLinks;

  public StandardBeansDocLink[] getDocLinks() {
    return myLinks;
  }
}
