package com.intellij.seam.model.xml.pages;

import org.jetbrains.annotations.NotNull;

/**
 * User: Sergey.Vasiliev
 */
public interface TaskOwner extends SeamPagesDomElement {
  @NotNull
  StartTask getStartTask();

  @NotNull
  BeginTask getBeginTask();

  @NotNull
  EndTask getEndTask();
}
