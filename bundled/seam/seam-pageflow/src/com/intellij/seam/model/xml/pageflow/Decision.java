package com.intellij.seam.model.xml.pageflow;


import com.intellij.util.xml.GenericAttributeValue;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface Decision extends PageflowNamedElement, EventsOwner, ExceptionHandlerOwner, PageflowTransitionHolder {

  @NotNull
  GenericAttributeValue<String> getExpression();

  @NotNull
  List<Delegation> getHandlers();

  Delegation addHandler();
}
