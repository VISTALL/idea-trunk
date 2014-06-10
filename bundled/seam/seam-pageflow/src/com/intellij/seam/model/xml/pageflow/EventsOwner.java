package com.intellij.seam.model.xml.pageflow;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface EventsOwner extends SeamPageflowDomElement {
  @NotNull
  List<Event> getEvents();

  Event addEvent();
}
