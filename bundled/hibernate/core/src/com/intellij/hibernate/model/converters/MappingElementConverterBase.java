package com.intellij.hibernate.model.converters;

import com.intellij.hibernate.HibernateMessages;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.ElementManipulators;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FilePathReferenceProvider;
import com.intellij.util.xml.ConvertContext;
import com.intellij.util.xml.CustomReferenceConverter;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.ResolvingConverter;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;

/**
 * @author Gregory.Shrago
 */
public abstract class MappingElementConverterBase extends ResolvingConverter<PsiFile> implements CustomReferenceConverter {

  public PsiFile fromString(@Nullable @NonNls String s, final ConvertContext context) {
    if (s == null) return null;
    final PsiReference[] references = createReferences((GenericDomValue)context.getInvocationElement(), context.getReferenceXmlElement(), context);
    if (references.length == 0) return null;
    final PsiElement element = references[references.length - 1].resolve();
    return element instanceof PsiFile ? (PsiFile)element : null;
  }

  public String toString(@Nullable PsiFile psiFile, final ConvertContext context) {
    if (psiFile == null) return null;
    final VirtualFile file = psiFile.getVirtualFile();
    if (file == null) return null;
    VirtualFile root = getRootForFile(file, context);
    if (root == null) return null;
    return VfsUtil.getRelativePath(file, root, '/');
  }

  @Nullable
  protected VirtualFile getRootForFile(final VirtualFile file, final ConvertContext context) {
    final ProjectFileIndex projectFileIndex = ProjectRootManager.getInstance(context.getPsiManager().getProject()).getFileIndex();
    VirtualFile root = projectFileIndex.getSourceRootForFile(file);
    if (root == null) {
      root = projectFileIndex.getContentRootForFile(file);
    }
    return root;
  }

  @NotNull
  public Collection<? extends PsiFile> getVariants(final ConvertContext context) {
    return Collections.emptyList();
  }

  public PsiElement resolve(final PsiFile o, final ConvertContext context) {
    return isFileAccepted(o) ? super.resolve(o, context) : null;
  }

  @NotNull
  public PsiReference[] createReferences(GenericDomValue genericDomValue, PsiElement element, ConvertContext context) {
    final String s = genericDomValue.getStringValue();
    if (s == null) {
      return PsiReference.EMPTY_ARRAY;
    }
    final int offset = ElementManipulators.getOffsetInElement(element);
    return new FilePathReferenceProvider() {
      protected boolean isPsiElementAccepted(final PsiElement element) {
        return super.isPsiElementAccepted(element) && (!(element instanceof PsiFile) || isFileAccepted((PsiFile)element));
      }
    }.getReferencesByElement(element, s, offset, true);
  }

  protected abstract boolean isFileAccepted(final PsiFile file);

  public String getErrorMessage(@Nullable final String s, final ConvertContext context) {
    return HibernateMessages.message("cannot.resolve.file.0");
  }

}
