package com.intellij.seam.model.xml.pages;


import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface Redirect extends PagesViewIdOwner, SeamPagesDomElement {

  @NotNull
  Message getMessage();

  @NotNull
  List<Param> getParams();

  Param addParam();
}
