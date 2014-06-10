package com.advancedtools.webservices.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

/**
 * @author maxim
 */
public interface ExternalProcessHandler {
  @NotNull String getName();
  @NotNull String[] getCommands();
  @Nullable File getLaunchDir();

  void setLaunchDir(@Nullable File launchDir);

  @Nullable InvokeExternalCodeUtil.OutputConsumer getOutputConsumer();
  void setOutputConsumer(@Nullable InvokeExternalCodeUtil.OutputConsumer outputConsumer);

  String describeExecution();
}
