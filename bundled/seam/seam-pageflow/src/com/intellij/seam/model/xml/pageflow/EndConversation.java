package com.intellij.seam.model.xml.pageflow;


import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Convert;
import com.intellij.seam.model.xml.converters.PageflowDefinitionConverter;
import org.jetbrains.annotations.NotNull;

public interface EndConversation extends SeamPageflowDomElement {

  @NotNull
  GenericAttributeValue<Boolean> getBeforeRedirect();

  @Convert(value = PageflowDefinitionConverter.class)
  GenericAttributeValue<PageflowDefinition> getCreateProcess();
}
