package com.intellij.seam.model.xml.pageflow;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * User: Sergey.Vasiliev
 */
public interface ActionsOwner extends SeamPageflowDomElement {
  @NotNull
  List<Action> getActions();

  Action addAction();
}
