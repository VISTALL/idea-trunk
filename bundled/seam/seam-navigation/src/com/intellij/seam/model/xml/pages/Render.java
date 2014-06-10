package com.intellij.seam.model.xml.pages;


import org.jetbrains.annotations.NotNull;

public interface Render extends PagesViewIdOwner, SeamPagesDomElement {

  @NotNull
  Message getMessage();
}
