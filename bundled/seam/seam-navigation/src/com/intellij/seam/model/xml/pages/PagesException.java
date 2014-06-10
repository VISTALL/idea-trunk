package com.intellij.seam.model.xml.pages;


import com.intellij.psi.PsiClass;
import com.intellij.util.xml.*;
import org.jetbrains.annotations.NotNull;

public interface PagesException extends RedirectOwner, SeamPagesDomElement {
  @NotNull
  @Attribute("class")
  @ExtendClass(value = "java.lang.Exception", instantiatable = false)
  GenericAttributeValue<PsiClass> getClazz();

  @NotNull
  EndConversation getEndConversation();

  @NotNull
  HttpError getHttpError();
}
