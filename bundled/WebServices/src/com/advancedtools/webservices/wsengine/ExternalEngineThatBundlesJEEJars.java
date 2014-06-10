package com.advancedtools.webservices.wsengine;

import org.jetbrains.annotations.NotNull;

/**
 * @author Maxim
 */
public interface ExternalEngineThatBundlesJEEJars {
  @NotNull
  String[] getJEEJarNames(@NotNull ExternalEngine.LibraryDescriptorContext context);
}