package com.intellij.webBeans.model.xml;

import com.intellij.util.xml.GenericDomValue;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface WebBeans extends WebBeansDomElement {

  @NotNull
  List<WebBeansComponent> getComponents();

  @NotNull
  List<WebBeansDeploy> getDeploys();

  @NotNull
  List<WebBeansInterceptor> getInterceptors();

  @NotNull
  List<Interceptors> getInterceptorses();

  @NotNull
  List<Decorators> getDecoratorses();

  @NotNull
  List<GenericDomValue<String>> getEjbLookups();
}
