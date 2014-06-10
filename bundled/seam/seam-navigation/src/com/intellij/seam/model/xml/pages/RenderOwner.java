package com.intellij.seam.model.xml.pages;

import org.jetbrains.annotations.NotNull;

/**
 * User: Sergey.Vasiliev
 */
public interface RenderOwner extends SeamPagesDomElement {
  @NotNull
  Render getRender();
}
