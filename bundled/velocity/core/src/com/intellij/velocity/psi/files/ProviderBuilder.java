package com.intellij.velocity.psi.files;

import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.lang.properties.psi.PropertiesFile;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReference;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.PsiModificationTracker;
import com.intellij.velocity.VtlFileIndex;
import com.intellij.velocity.VtlGlobalMacroProvider;
import com.intellij.velocity.VtlGlobalVariableProvider;
import com.intellij.velocity.psi.PsiUtil;
import com.intellij.velocity.psi.VtlImplicitVariable;
import com.intellij.velocity.psi.VtlMacro;
import com.intellij.velocity.psi.VtlVariable;
import gnu.trove.THashMap;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * @author Alexey Chmutov
 */
class ProviderBuilder {
  private final VtlFile myFile;

  public ProviderBuilder(@NotNull VtlFile file) {
    myFile = file;
  }

  CachedValueProvider<Collection<VtlVariable>> createGlobalVarsProvider() {
    return new CachedValueProvider<Collection<VtlVariable>>() {
      public Result<Collection<VtlVariable>> compute() {
        final Collection<VtlVariable> result = new ArrayList<VtlVariable>();
        for (final VtlGlobalVariableProvider provider : Extensions.getExtensions(VtlGlobalVariableProvider.EP_NAME)) {
          result.addAll(provider.getGlobalVariables(myFile));
        }
        return Result.create(result, myFile);
      }
    };
  }

  CachedValueProvider<Map<String, Set<VtlMacro>>> createAllMacrosProvider() {
    return new CachedValueProvider<Map<String, Set<VtlMacro>>>() {
      public CachedValueProvider.Result<Map<String, Set<VtlMacro>>> compute() {
        final Map<String, Set<VtlMacro>> result = new THashMap<String, Set<VtlMacro>>();
        myFile.accept(new PsiRecursiveElementVisitor() {
          @Override
          public void visitElement(PsiElement element) {
            super.visitElement(element);
            if (element instanceof VtlMacro) {
              registerMacro((VtlMacro)element, result);
            }
          }
        });
        return CachedValueProvider.Result.create(result, myFile);
      }
    };
  }

  private static void registerMacro(VtlMacro macro, Map<String, Set<VtlMacro>> result) {
    String macroName = macro.getName();
    if (macroName == null) {
      return;
    }
    Set<VtlMacro> sameNameMacros = result.get(macroName);
    if (sameNameMacros == null) {
      sameNameMacros = new THashSet<VtlMacro>();
      result.put(macroName, sameNameMacros);
    }
    sameNameMacros.add(macro);
  }

  CachedValueProvider<Map<String, VtlImplicitVariable>> createImplicitVarsProvider() {
    return new CachedValueProvider<Map<String, VtlImplicitVariable>>() {
      public CachedValueProvider.Result<Map<String, VtlImplicitVariable>> compute() {
        final Map<String, VtlImplicitVariable> result = new THashMap<String, VtlImplicitVariable>();
        Properties defaultVariables = myFile.getViewProvider().getUserData(FileTemplateManager.DEFAULT_TEMPLATE_PROPERTIES);
        if (defaultVariables != null) {
          for (Map.Entry<Object, Object> entry : defaultVariables.entrySet()) {
            VtlImplicitVariable var = VtlImplicitVariable.getOrCreate(result, myFile, null, (String)entry.getKey(), null);
            var.setType((String)entry.getValue());
          }
        }
        myFile.accept(new PsiRecursiveElementVisitor() {
          @Override
          public void visitComment(PsiComment comment) {
            final String text = comment.getText();
            String[] nameAndTypeAndScopeFilePath = VtlFile.findVariableNameAndTypeAndScopeFilePath(text);
            if (nameAndTypeAndScopeFilePath == null) {
              return;
            }
            VtlFile scopeFile = findVtlFile(comment, text, nameAndTypeAndScopeFilePath[2]);
            if (nameAndTypeAndScopeFilePath[2] != null && scopeFile == null) {
              return;
            }
            VtlImplicitVariable var = VtlImplicitVariable.getOrCreate(result, myFile, comment, nameAndTypeAndScopeFilePath[0], scopeFile);
            var.setType(nameAndTypeAndScopeFilePath[1]);
          }
        });
        return CachedValueProvider.Result.create(result, myFile);
      }
    };
  }

  public CachedValueProvider<Collection<VtlMacro>> createGlobalMacrosProvider() {
    return new CachedValueProvider<Collection<VtlMacro>>() {
      public Result<Collection<VtlMacro>> compute() {
        final Collection<VtlMacro> result = new ArrayList<VtlMacro>();
        for (final VtlGlobalMacroProvider provider : Extensions.getExtensions(VtlGlobalMacroProvider.EP_NAME)) {
          result.addAll(provider.getGlobalMacros(myFile));
        }
        return Result.create(result, myFile);
      }
    };
  }

  public CachedValueProvider<Collection<VtlFileProxy>> createMacroLibrariesProvider() {
    return new CachedValueProvider<Collection<VtlFileProxy>>() {
      public CachedValueProvider.Result<Collection<VtlFileProxy>> compute() {
        final Collection<VtlFileProxy> result = new THashSet<VtlFileProxy>();
        myFile.accept(new PsiRecursiveElementVisitor() {
          @Override
          public void visitComment(PsiComment comment) {
            final String text = comment.getText();
            String[] pathAndScopeFilePath = VtlFile.findMacroLibraryPathAndScopeFilePath(text);
            if (pathAndScopeFilePath == null) {
              return;
            }
            VtlFile libraryFile = findVtlFile(comment, text, pathAndScopeFilePath[0]);
            if (libraryFile == null) {
              return;
            }
            VtlFile scopeFile = findVtlFile(comment, text, pathAndScopeFilePath[1]);
            if (pathAndScopeFilePath[1] == null || scopeFile != null) {
              result.add(new VtlFileProxy(libraryFile, scopeFile));
            }
          }
        });
        return CachedValueProvider.Result.create(result, myFile);
      }
    };
  }

  @Nullable
  private static VtlFile findVtlFile(PsiComment comment, String text, @Nullable String filePath) {
    return findFile(comment, text, filePath, VtlFile.class);
  }

  @Nullable
  private static <T extends PsiFile> T findFile(PsiComment comment, String text, @Nullable String filePath, Class<T> fileClass) {
    if (filePath == null) {
      return null;
    }
    final FileReference[] fileReferences = PsiUtil.getFileReferences(filePath, comment, text.indexOf(filePath), false);
    return PsiUtil.findFile(fileReferences, fileClass);
  }

  public CachedValueProvider<VelocityPropertiesProvider> createVelocityPropertiesProvider() {
    return new CachedValueProvider<VelocityPropertiesProvider>() {
      public CachedValueProvider.Result<VelocityPropertiesProvider> compute() {
        final Set dependencies = new HashSet(3);
        final Ref<VelocityPropertiesProvider> result = new Ref<VelocityPropertiesProvider>();

        PsiRecursiveElementVisitor visitor = new PsiRecursiveElementVisitor() {

          @Override
          public void visitFile(PsiFile file) {
            dependencies.add(file);
            super.visitFile(file);
          }

          @Override
          public void visitComment(PsiComment comment) {
            if (result.get() != null) {
              return;
            }
            final String text = comment.getText();
            String[] velocityPropertiesPathAndScopeFilePath = VtlFile.findVelocityPropertiesPathAndScopeFilePath(text);
            if (velocityPropertiesPathAndScopeFilePath == null) {
              return;
            }
            VtlFile scopeFile = findVtlFile(comment, text, velocityPropertiesPathAndScopeFilePath[2]);
            if (velocityPropertiesPathAndScopeFilePath[2] != null && (scopeFile == null || !isOriginalEquivalent(myFile, scopeFile))) {
              return;
            }
            PropertiesFile velocityPropertiesFile =
                findFile(comment, text, velocityPropertiesPathAndScopeFilePath[0], PropertiesFile.class);
            if (velocityPropertiesFile == null) {
              return;
            }
            dependencies.add(velocityPropertiesFile);
            VirtualFile runtimeRoot = findRuntimeRoot(comment.getContainingFile(), velocityPropertiesPathAndScopeFilePath[1]);
            result.set(new VelocityPropertiesProvider(velocityPropertiesFile, runtimeRoot));
          }
        };
        myFile.accept(visitor);
        if (result.get() == null) {
          final Collection<VtlFile> implicitlyIncludedFiles = VtlFileIndex.getImplicitlyIncludedFiles(myFile);
          if (implicitlyIncludedFiles.size() == 0) {
            dependencies.add(PsiModificationTracker.OUT_OF_CODE_BLOCK_MODIFICATION_COUNT);
          }
          for (VtlFile implicitlyIncludedFile : implicitlyIncludedFiles) {
            implicitlyIncludedFile.accept(visitor);
            if (result.get() != null) {
              break;
            }
          }
        }
        return CachedValueProvider.Result.create(result.get(), dependencies.toArray());
      }
    };
  }

  @Nullable
  private VirtualFile findRuntimeRoot(@NotNull PsiFile contextFile, @Nullable String path) {
    final PsiDirectory parent = contextFile.getParent();
    if (path == null || path.length() == 0 || parent == null) return null;
    final VirtualFile context = parent.getVirtualFile();
    if (path.charAt(0) != '/') {
      return context.findFileByRelativePath(path);
    }
    final Module module = ModuleUtil.findModuleForPsiElement(contextFile);
    if (module == null) return null;
    ModuleRootManager moduleRootManager = ModuleRootManager.getInstance(module);
    if (moduleRootManager == null) return null;
    for (VirtualFile root : moduleRootManager.getContentRoots()) {
      if (VfsUtil.isAncestor(root, context, false)) {
        return path.length() == 1 ? root : root.findFileByRelativePath(path.substring(1));
      }
    }
    return null;
  }

  private boolean isOriginalEquivalent(@NotNull VtlFile file1, @NotNull VtlFile file2) {
    return file1.getOriginalFile().isEquivalentTo(file2.getOriginalFile());
  }
}
