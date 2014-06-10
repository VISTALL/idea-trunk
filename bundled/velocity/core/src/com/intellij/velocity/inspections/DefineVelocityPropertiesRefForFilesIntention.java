/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.velocity.inspections;

import com.intellij.codeInsight.template.Expression;
import com.intellij.codeInsight.template.Template;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReference;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.Function;
import com.intellij.velocity.VelocityBundle;
import static com.intellij.velocity.inspections.Util.*;
import com.intellij.velocity.psi.reference.VtlFileReferenceSet;
import com.intellij.velocity.psi.files.VelocityPropertiesProvider;
import com.intellij.lang.properties.PropertiesLanguage;
import com.intellij.lang.properties.psi.PropertiesFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * @author Alexey Chmutov
 */
public abstract class DefineVelocityPropertiesRefForFilesIntention extends DefineInCommentIntention {
  public DefineVelocityPropertiesRefForFilesIntention(@NotNull String text) {
    super(text, VelocityBundle.message("add.velocity.properties.ref.fix.name"));
  }

  @Override
  @Nullable
  protected PsiElement getReferenceElement(@NotNull final Editor editor, @NotNull final PsiFile file) {
    FileReference ref = findReferenceExpression(editor, file, FileReference.class);
    return ref != null && ref.resolve() == null && canSetVelocityProperties(file) ? ref.getElement() : null;
  }

  protected void prepareTemplate(@NotNull Template template,
                                 @NotNull final PsiElement element,
                                 String relativePath,
                                 @NotNull final PsiFile fileToInsertComment) {
    final List<String> allFiles = computeFilePaths(element, fileToInsertComment);
    template.addTextSegment("#* @velocityproperties path=");
    final Expression pathExpression = new StringCollectionExpression(allFiles);
    template.addVariable("PATH", pathExpression, pathExpression, true);
    final String fileRef = relativePath != null ? " file=\"" + relativePath + "\"" : "";
    template.addTextSegment(fileRef + " *#\n");
    template.addEndVariable();
  }

  private static List<String> computeFilePaths(final PsiElement element, final PsiFile fileToInsertComment) {
    final VtlFileReferenceSet refSet = findVtlFileReferenceSet(element);
    if (refSet == null) {
      return Collections.emptyList();
    }
    final PsiFile[] referencedFiles =
        findReferencedFiles(ModuleUtil.findModuleForPsiElement(element), refSet.getLastReference().getCanonicalText());

    if (referencedFiles.length == 0) {
      return Collections.emptyList();
    }

    return collectFilePaths(element, new Function<PsiFile, String>() {
      public String fun(@NotNull final PsiFile psiFile) {
        PsiFile file = psiFile.getViewProvider().getPsi(PropertiesLanguage.INSTANCE);
        if (file instanceof PropertiesFile) {
          PropertiesFile propFile = (PropertiesFile)file;
          VelocityPropertiesProvider velocityProperties = new VelocityPropertiesProvider(propFile);
          for (PsiFile referencedFile : referencedFiles) {
            String referencedFilePath = referencedFile.getViewProvider().getVirtualFile().getPath();
            String filePath = computeFilePath(velocityProperties, referencedFilePath, refSet.getPathString(), fileToInsertComment);
            if (filePath != null) return filePath;
          }
        }
        return null;
      }
    });
  }

  public static class Local extends DefineVelocityPropertiesRefForFilesIntention {
    public Local() {
      super(VelocityBundle.message("add.velocity.properties.ref.fix.name.local"));
    }

    public void invoke(@NotNull final Project project, final Editor editor, final PsiFile file) throws IncorrectOperationException {
      defineInComment(editor, file, file, false);
    }
  }

  public static class LocalExternal extends DefineVelocityPropertiesRefForFilesIntention {
    public LocalExternal() {
      super(VelocityBundle.message("add.velocity.properties.ref.fix.name.external"));
    }

    public void invoke(@NotNull final Project project, final Editor editor, final PsiFile file) throws IncorrectOperationException {
      chooseTargetFile(file, editor, true);
    }
  }

  public static class ModuleWide extends DefineVelocityPropertiesRefForFilesIntention {
    public ModuleWide() {
      super(VelocityBundle.message("add.velocity.properties.ref.fix.name.module.wide"));
    }

    public void invoke(@NotNull final Project project, final Editor editor, final PsiFile file) throws IncorrectOperationException {
      chooseTargetFile(file, editor, false);
    }
  }
}