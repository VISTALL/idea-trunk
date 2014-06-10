/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.velocity;

import com.intellij.openapi.fileTypes.FileTypeConsumer;
import com.intellij.openapi.fileTypes.FileTypeFactory;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.velocity.psi.files.VtlFileType;
import org.jetbrains.annotations.NotNull;

/**
 * @author Alexey Chmutov
 */
public class VelocityApplicationComponent extends FileTypeFactory {
  public void createFileTypes(final @NotNull FileTypeConsumer consumer) {
    consumer.consume(VtlFileType.INSTANCE, StringUtil.join(VtlFileType.INSTANCE.getExtensions(), ";"));
  }
}
