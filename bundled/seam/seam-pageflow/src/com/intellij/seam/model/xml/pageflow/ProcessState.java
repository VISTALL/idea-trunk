package com.intellij.seam.model.xml.pageflow;


import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface ProcessState extends PageflowNamedElement, EventsOwner, ExceptionHandlerOwner, PageflowTransitionHolder {

  @NotNull
  List<SubProcess> getSubProcesses();

  SubProcess addSubProcess();
}
