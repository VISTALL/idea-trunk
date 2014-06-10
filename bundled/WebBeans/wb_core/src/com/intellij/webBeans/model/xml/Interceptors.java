package com.intellij.webBeans.model.xml;

import com.intellij.util.xml.GenericDomValue;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface Interceptors extends WebBeansDomElement {

  @NotNull
  List<GenericDomValue<String>> getInterceptors();
}
