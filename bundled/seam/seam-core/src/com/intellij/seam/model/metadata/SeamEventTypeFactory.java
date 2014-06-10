package com.intellij.seam.model.metadata;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleServiceManager;
import org.jetbrains.annotations.NotNull;

public abstract class SeamEventTypeFactory implements Disposable {

  public static SeamEventTypeFactory getInstance(final Module module) {
      return ModuleServiceManager.getService(module, SeamEventTypeFactory.class);
  }

  @NotNull
  public abstract SeamEventType getOrCreateEventType(final String eventType);
}
