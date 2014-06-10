package com.intellij.velocity.inspections;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.roots.ContentIterator;
import com.intellij.openapi.roots.ModuleFileIndex;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReference;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReferenceSet;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.Function;
import com.intellij.util.SmartList;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.velocity.psi.PsiUtil;
import static com.intellij.velocity.psi.PsiUtil.getRelativePath;
import com.intellij.velocity.psi.VtlLanguage;
import com.intellij.velocity.psi.files.VelocityPropertiesProvider;
import com.intellij.velocity.psi.files.VtlFile;
import com.intellij.velocity.psi.reference.VtlFileReferenceSet;
import com.intellij.velocity.psi.reference.VtlReferenceExpression;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @author Alexey Chmutov
 */
public class Util {
  @Nullable
  static VtlReferenceExpression findReferenceExpression(@NotNull Editor editor, @NotNull PsiFile file) {
    return findReferenceExpression(editor, file, VtlReferenceExpression.class);
  }

  @Nullable
  static <T extends PsiReference> T findReferenceExpression(@NotNull Editor editor, @NotNull PsiFile file, @NotNull Class<T> refClass) {
    int offset = editor.getCaretModel().getOffset();
    final CharSequence charSequence = editor.getDocument().getCharsSequence();
    if (charSequence.length() == offset || charSequence.length() > offset && !Character.isJavaIdentifierPart(charSequence.charAt(offset))) {
      offset--;
    }
    final PsiReference reference = file.findReferenceAt(offset);
    return refClass.isInstance(reference) ? (T)reference : null;
  }

  @NotNull
  static <T> List<T> collectFilePaths(@NotNull PsiElement element, @NotNull final Function<PsiFile, T> converter) {
    final List<T> allFiles = new SmartList<T>();
    final PsiManager psiManager = element.getManager();
    ModuleFileIndex fileIndex = ModuleRootManager.getInstance(ModuleUtil.findModuleForPsiElement(element)).getFileIndex();
    fileIndex.iterateContent(new ContentIterator() {
      public boolean processFile(VirtualFile fileOrDir) {
        PsiFile psiFile = psiManager.findFile(fileOrDir);
        if (psiFile != null) {
          ContainerUtil.addIfNotNull(converter.fun(psiFile), allFiles);
        }
        return true;
      }
    });
    return allFiles;
  }

  static boolean canSetVelocityProperties(@Nullable PsiFile file) {
    if (file == null) {
      return false;
    }
    VtlFile vtlFile = (VtlFile)file.getViewProvider().getPsi(VtlLanguage.INSTANCE);
    return vtlFile != null && vtlFile.getVelocityProperties() == null;

  }

  @Nullable
  static VtlFileReferenceSet findVtlFileReferenceSet(@NotNull PsiElement element) {
    for (PsiReference ref : element.getReferences()) {
      if (!(ref instanceof FileReference)) {
        continue;
      }
      FileReferenceSet refSet = ((FileReference)ref).getFileReferenceSet();
      if (refSet instanceof VtlFileReferenceSet && refSet.getLastReference() != null) {
        return (VtlFileReferenceSet)refSet;
      }
    }
    return null;
  }

  @NotNull
  static PsiFile[] findReferencedFiles(@Nullable final Module module, @NotNull String nameFile) {
    if (module == null) return PsiFile.EMPTY_ARRAY;
    return FilenameIndex.getFilesByName(module.getProject(), nameFile, new GlobalSearchScope(module.getProject()) {
      private final VirtualFile[] myContentRoots = ModuleRootManager.getInstance(module).getContentRoots();

      public boolean contains(VirtualFile file) {
        for (VirtualFile contentRoot : myContentRoots) {
          if (VfsUtil.isAncestor(contentRoot, file, false)) {
            return true;
          }
        }
        return false;
      }

      public int compare(VirtualFile file1, VirtualFile file2) {
        return 0;
      }

      public boolean isSearchInModuleContent(@NotNull Module aModule) {
        return aModule == module;
      }

      public boolean isSearchInLibraries() {
        return false;
      }
    });
  }

  static String computeFilePath(VelocityPropertiesProvider velocityProperties,
                                String referencedFilePath,
                                String pathString,
                                PsiFile fileToInsertComment) {
    for (String path : velocityProperties.getResourceLoaderPathList()) {
      String relativePath = ".".equals(path) ? pathString : path + "/" + pathString;
      if (!referencedFilePath.endsWith(relativePath)) {
        continue;
      }
      int len = referencedFilePath.length() - relativePath.length();
      String vtlRootPath = getRelativePath(PsiUtil.getPath(fileToInsertComment), referencedFilePath.substring(0, len));
      if (vtlRootPath != null) {
        String propFilePath = getRelativePath(fileToInsertComment, velocityProperties.getPropertiesFile());
        if (propFilePath != null) {
          return "\"" + propFilePath + "\" runtime_root=\"" + vtlRootPath + "\"";
        }
      }
    }
    return null;
  }

}
