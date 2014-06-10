package com.intellij.seam.model.xml.pageflow;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * User: Sergey.Vasiliev
 */
public interface ExceptionHandlerOwner extends SeamPageflowDomElement {
  @NotNull
  List<ExceptionHandler> getExceptionHandlers();

  ExceptionHandler addExceptionHandler();
}
