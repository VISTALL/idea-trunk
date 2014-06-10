package com.intellij.seam.model.xml.pages;


import com.intellij.util.xml.GenericAttributeValue;
import org.jetbrains.annotations.NotNull;

public interface ResumeProcess extends SeamPagesDomElement {

  @NotNull
  GenericAttributeValue<String> getProcessId();
}
