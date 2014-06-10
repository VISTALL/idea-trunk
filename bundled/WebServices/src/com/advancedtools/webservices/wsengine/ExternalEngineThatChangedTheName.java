package com.advancedtools.webservices.wsengine;

import org.jetbrains.annotations.NotNull;

/**
 * @author Maxim
 */
public interface ExternalEngineThatChangedTheName {
  boolean isYourOldName(@NotNull String name);
}
