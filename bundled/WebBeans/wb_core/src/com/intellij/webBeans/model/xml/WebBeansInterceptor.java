package com.intellij.webBeans.model.xml;

import com.intellij.util.xml.GenericDomValue;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface WebBeansInterceptor extends WebBeansDomElement {

  @NotNull
  List<GenericDomValue<String>> getClasses();

  @NotNull
  List<GenericDomValue<String>> getBindings();
}
