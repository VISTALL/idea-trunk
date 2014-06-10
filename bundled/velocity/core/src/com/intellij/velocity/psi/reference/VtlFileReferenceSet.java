package com.intellij.velocity.psi.reference;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.velocity.psi.files.VelocityPropertiesProvider;
import com.intellij.velocity.psi.files.VtlFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Alexey Chmutov
 */
public class VtlFileReferenceSet extends SoftFileReferenceSet {

    public VtlFileReferenceSet(@NotNull String text, PsiElement element, int startInElement) {
        super(text, element, startInElement);
    }

    @NotNull
    @Override
    public Collection<PsiFileSystemItem> getDefaultContexts() {
        VtlFile file = getContainingVtlFile();
        if (file == null) {
            return super.getDefaultContexts();
        }
        final VelocityPropertiesProvider velocityProperties = file.getVelocityProperties();
        if (velocityProperties == null) {
            return super.getDefaultContexts();
        }
        return getVtlDefaultContexts(velocityProperties);
    }

    @NotNull
    private Collection<PsiFileSystemItem> getVtlDefaultContexts(@NotNull VelocityPropertiesProvider velocityProperties) {
        PsiManager manager = getElement().getManager();
        Collection<PsiFileSystemItem> vtlDefaultContexts = new ArrayList<PsiFileSystemItem>();
        for (PsiFileSystemItem defaultContext : super.getDefaultContexts()) {
            for (VirtualFile resourceLoaderPath : velocityProperties.getResourceLoaderPathListBasedOn(defaultContext.getVirtualFile())) {
                PsiDirectory resourceLoaderContext = manager.findDirectory(resourceLoaderPath);
                ContainerUtil.addIfNotNull(resourceLoaderContext, vtlDefaultContexts);
            }
        }
        return vtlDefaultContexts;
    }

    @Nullable
    private VtlFile getContainingVtlFile() {
        PsiFile psiFile = getElement().getContainingFile();
        return psiFile instanceof VtlFile ? (VtlFile) psiFile : null;
    }
}
