package com.intellij.seam.model.xml.pages;

import org.jetbrains.annotations.NotNull;

/**
 * User: Sergey.Vasiliev
 */
public interface RedirectOwner extends SeamPagesDomElement {
  @NotNull
  Redirect getRedirect();
}
