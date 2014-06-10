/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package org.jetbrains.plugins.grails;

import com.intellij.codeInsight.template.FileTypeBasedContextType;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.grails.fileType.GspFileType;
import org.jetbrains.plugins.grails.lang.gsp.psi.gsp.api.GspFile;

/**
 * @author peter
 */
public class GspTemplateContextType extends FileTypeBasedContextType{

  protected GspTemplateContextType() {
    super("GSP", "GSP", GspFileType.GSP_FILE_TYPE);
  }

  @Override
  public boolean isInContext(@NotNull PsiFile file, int offset) {
    return file instanceof GspFile;
  }
}