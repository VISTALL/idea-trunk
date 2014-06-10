package com.intellij.seam.model.xml.pageflow;

import com.intellij.seam.model.xml.converters.PageflowTransitionTargetConverter;
import com.intellij.util.xml.Convert;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

public interface Transition extends PageflowNamedElement, ActionsOwner, ExceptionHandlerOwner, SeamPageflowDomElement {

  @NotNull
  @Required
  @Convert(value = PageflowTransitionTargetConverter.class)
  GenericAttributeValue<PageflowNamedElement> getTo();
}
