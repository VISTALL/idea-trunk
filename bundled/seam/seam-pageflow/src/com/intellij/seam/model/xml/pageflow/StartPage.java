package com.intellij.seam.model.xml.pageflow;


import com.intellij.openapi.paths.PathReference;
import com.intellij.util.xml.Attribute;
import com.intellij.util.xml.GenericAttributeValue;
import org.jetbrains.annotations.NotNull;

/**
 * http://jboss.com/products/seam/pageflow:start-pageElemType interface.
 */
public interface StartPage extends SeamPageflowDomElement, PageElements {

  @NotNull
  @Attribute("redirect")
  GenericAttributeValue<Boolean> getRedirectAttr();


  @NotNull
  GenericAttributeValue<Enabled> getSwitch();


  @NotNull
  GenericAttributeValue<PathReference> getNoConversationViewId();


  @NotNull
  GenericAttributeValue<Integer> getTimeout();

  @NotNull
  GenericAttributeValue<Enabled> getBack();
}
