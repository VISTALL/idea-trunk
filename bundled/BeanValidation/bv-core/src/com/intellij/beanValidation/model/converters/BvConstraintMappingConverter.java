/*
 * Copyright 2000-2009 JetBrains s.r.o.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.intellij.beanValidation.model.converters;

import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.ElementManipulators;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FilePathReferenceProvider;
import com.intellij.psi.xml.XmlElement;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlDocument;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.xml.ConvertContext;
import com.intellij.util.xml.CustomReferenceConverter;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.ResolvingConverter;
import com.intellij.beanValidation.constants.BvCommonConstants;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;

/**
 * @author Konstantin Bulenkov
 */
public class BvConstraintMappingConverter extends ResolvingConverter<XmlFile> implements CustomReferenceConverter {

  public String toString(@Nullable final XmlFile xml, final ConvertContext context) {
    if (xml == null) {
      return null;
    }

    final VirtualFile file = xml.getVirtualFile();
    if (file == null) {
      return null;
    }

    final VirtualFile root = getRootForFile(file, context);
    if (root == null) {
      return null;
    }

    return VfsUtil.getRelativePath(file, root, '/');
  }

  @Nullable
  private static VirtualFile getRootForFile(final VirtualFile file, final ConvertContext context) {
    final ProjectFileIndex projectFileIndex = ProjectRootManager.getInstance(context.getPsiManager().getProject()).getFileIndex();
    VirtualFile root = projectFileIndex.getSourceRootForFile(file);

    if (root == null) {
      root = projectFileIndex.getContentRootForFile(file);
    }

    return root;
  }

  public XmlFile fromString(@Nullable @NonNls final String value, final ConvertContext context) {
      if (value == null) {
        return null;
      }

      final XmlElement xmlElement = context.getReferenceXmlElement();
      if (xmlElement == null) {
        return null;
      }

      final PsiReference[] references = createReferences((GenericDomValue) context.getInvocationElement(),
                                                         xmlElement,
                                                         context);
      if (references.length == 0) {
        return null;
      }

      final PsiElement element = references[references.length - 1].resolve();
      return element instanceof XmlFile ? (XmlFile) element : null;
    }

    @NotNull
    public Collection<? extends XmlFile> getVariants(final ConvertContext context) {
      return Collections.emptyList();
    }

    public PsiElement resolve(final XmlFile psiFile, final ConvertContext context) {
      // recursive self-inclusion
      if (context.getFile().equals(psiFile)) {
        return null;
      }

      return isFileAccepted(psiFile) ? super.resolve(psiFile, context) : null;
    }

    @NotNull
    public PsiReference[] createReferences(@NotNull final GenericDomValue genericDomValue,
                                           @NotNull final PsiElement element,
                                           @NotNull final ConvertContext context) {
      final String s = genericDomValue.getStringValue();
      if (s == null) {
        return PsiReference.EMPTY_ARRAY;
      }

      final int offset = ElementManipulators.getOffsetInElement(element);
      return new FilePathReferenceProvider() {
        protected boolean isPsiElementAccepted(final PsiElement element) {
          return super.isPsiElementAccepted(element) &&
                 (!(element instanceof PsiFile) || isFileAccepted((PsiFile) element));
        }
      }.getReferencesByElement(element, s, offset, true);
    }

    private static boolean isFileAccepted(@NotNull final PsiFile file) {
      if (file instanceof XmlFile) {
        final XmlFile xmlFile = (XmlFile) file;
        final XmlDocument document = xmlFile.getDocument();
        if (document != null) {
          final XmlTag rootTag = document.getRootTag();
          if (rootTag != null) {
            return BvCommonConstants.BEAN_VALIDATION_CONSTRAINT_MAPPINGS_ROOT_TAG.equals(rootTag.getName());
          }
        }
      }

      return false;
    }

    public String getErrorMessage(@Nullable final String value, final ConvertContext context) {
      // check if user tries to include current file
      if (Comparing.equal(context.getFile().getName(), value)) {
        return "Recursive inclusion of current file";
      }

      // TODO check for cyclic include

      return "Cannot resolve file ''" + value + "'' (not included in file sets?)";
    }
}
