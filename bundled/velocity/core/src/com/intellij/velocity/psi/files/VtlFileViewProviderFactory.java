/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.velocity.psi.files;

import com.intellij.psi.FileViewProviderFactory;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.lang.Language;

/**
 * @author Alexey Chmutov
 */
public class VtlFileViewProviderFactory implements FileViewProviderFactory{
  public VtlFileViewProvider createFileViewProvider(final VirtualFile file, final Language language, final PsiManager manager, final boolean physical) {
    return new VtlFileViewProvider(manager, file, physical);
  }
}
