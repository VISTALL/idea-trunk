package com.intellij.webBeans.model.xml;

import com.intellij.util.xml.GenericDomValue;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface WebBeansComponent extends WebBeansDomElement {

  @NotNull
  List<GenericDomValue<String>> getClasses();

  @NotNull
  List<GenericDomValue<String>> getDeployments();

  @NotNull
  List<GenericDomValue<String>> getBindings();

  @NotNull
  List<GenericDomValue<String>> getScopes();

  @NotNull
  List<GenericDomValue<String>> getNameds();

  @NotNull
  List<GenericDomValue<String>> getStereotypes();

  @NotNull
  List<GenericDomValue<String>> getProducerMethods();

  @NotNull
  List<GenericDomValue<String>> getProducerTypes();

  @NotNull
  List<GenericDomValue<String>> getRemotes();

  @NotNull
  List<GenericDomValue<String>> getBoundTos();

  @NotNull
  List<GenericDomValue<String>> getTopics();

  @NotNull
  List<GenericDomValue<String>> getInterceptorBindings();
}
