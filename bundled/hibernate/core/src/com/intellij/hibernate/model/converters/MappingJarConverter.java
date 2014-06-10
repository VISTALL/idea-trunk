package com.intellij.hibernate.model.converters;

import com.intellij.ide.highlighter.ArchiveFileType;
import com.intellij.psi.PsiFile;

/**
 * @author Gregory.Shrago
 */
public class MappingJarConverter extends MappingElementConverterBase {

  protected boolean isFileAccepted(final PsiFile file) {
    return file != null && file.getFileType() instanceof ArchiveFileType;
  }
}