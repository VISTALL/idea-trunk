package com.intellij.velocity;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.roots.ModuleFileIndex;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileFilter;
import com.intellij.openapi.project.DumbService;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.containers.HashMap;
import com.intellij.util.indexing.*;
import com.intellij.util.io.BooleanDataDescriptor;
import com.intellij.util.io.KeyDescriptor;
import com.intellij.velocity.psi.files.VtlFile;
import com.intellij.velocity.psi.files.VtlFileType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * @author Alexey Chmutov
 */
public class VtlFileIndex extends ScalarIndexExtension<Boolean> {
    private static final ID<Boolean, Void> NAME = ID.create("VtlFileIndex");
    @NonNls
    public static final String IMPLICIT_INCLUDE_MARKER = "#* @implicitly included *#\n";

    public ID<Boolean, Void> getName() {
        return NAME;
    }

    @NotNull
    public static Collection<VtlFile> getImplicitlyIncludedFiles(final PsiFile targetFile) {
        final Module module = ModuleUtil.findModuleForPsiElement(targetFile);
        if (module == null || DumbService.getInstance(module.getProject()).isDumb()) {
            return Collections.emptyList();
        }
        final Collection<VirtualFile> files =
                FileBasedIndex.getInstance().getContainingFiles(NAME, Boolean.TRUE, GlobalSearchScope.moduleScope(module));
        List<VtlFile> result = new ArrayList<VtlFile>(files.size());
        for (final VirtualFile virtualFile : files) {
            final PsiFile psiFile = targetFile.getManager().findFile(virtualFile);
            if (psiFile instanceof VtlFile) {
                if(psiFile.equals(targetFile)) {
                    return Collections.emptyList();
                }
                result.add((VtlFile) psiFile);
            }
        }
        return result;
    }


    public DataIndexer<Boolean, Void, FileContent> getIndexer() {
        return new DataIndexer<Boolean, Void, FileContent>() {
            @NotNull
            public Map<Boolean, Void> map(final FileContent inputData) {
                final CharSequence text = inputData.getContentAsText();
                final int markerLength = IMPLICIT_INCLUDE_MARKER.length();
                if (markerLength > text.length()
                        || !IMPLICIT_INCLUDE_MARKER.equals(text.subSequence(0, markerLength).toString())) {
                    return Collections.emptyMap();
                }
                final HashMap<Boolean, Void> map = new HashMap<Boolean, Void>();
                map.put(Boolean.TRUE, null);
                return map;
            }
        };
    }

    public KeyDescriptor<Boolean> getKeyDescriptor() {
        return BooleanDataDescriptor.INSTANCE;
    }

    public FileBasedIndex.InputFilter getInputFilter() {
        return new FileBasedIndex.InputFilter() {
            public boolean acceptInput(final VirtualFile file) {
                return VtlFileType.INSTANCE == file.getFileType();
            }
        };
    }

    public boolean dependsOnFileContent() {
        return true;
    }

    public int getVersion() {
        return 0;
    }

    private static class ModuleSourceVirtualFileFilter implements VirtualFileFilter {
        private final ModuleFileIndex myIndex;

        public ModuleSourceVirtualFileFilter(final Module module) {
            myIndex = ModuleRootManager.getInstance(module).getFileIndex();
        }

        public boolean accept(final VirtualFile file) {
            return myIndex.isInSourceContent(file);
        }
    }
}
