package org.jetbrains.plugins.grails.lang.gsp;

import com.intellij.lang.Language;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.FileViewProviderFactory;
import com.intellij.psi.PsiManager;

/**
 * @author ilyas
 */
public class GspFileviewProviderFactory implements FileViewProviderFactory {
  public FileViewProvider createFileViewProvider(VirtualFile file, Language language, PsiManager manager, boolean physical) {
    return new GspFileViewProvider(manager, file, physical);
  }
}
