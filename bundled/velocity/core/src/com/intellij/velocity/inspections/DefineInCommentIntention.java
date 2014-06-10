/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.velocity.inspections;

import com.intellij.codeInsight.CodeInsightUtilBase;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.PopupStep;
import com.intellij.openapi.ui.popup.util.BaseListPopupStep;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.velocity.Icons;
import com.intellij.velocity.VelocityBundle;
import com.intellij.velocity.VtlFileIndex;
import com.intellij.velocity.psi.PsiUtil;
import com.intellij.velocity.psi.files.VtlFile;
import com.intellij.velocity.psi.files.VtlFileViewProvider;
import com.intellij.velocity.psi.reference.VtlReferenceExpression;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Collection;
import java.io.IOException;

/**
 * @author Alexey Chmutov
 */
public abstract class DefineInCommentIntention implements IntentionAction {
    private final String myText;
    private final String myFamilyName;
    public static final String VELOCITY_IMPLICIT_VM = "velocity_implicit.vm";

    public DefineInCommentIntention(@NotNull String text, @NotNull String familyName) {
        myText = text;
        myFamilyName = familyName;
    }

    @NotNull
    public String getText() {
        return myText;
    }

    @NotNull
    public String getFamilyName() {
        return myFamilyName;
    }

    public final boolean isAvailable(@NotNull final Project project, final Editor editor, final PsiFile file) {
      return file.getViewProvider() instanceof VtlFileViewProvider &&
             getReferenceElement(editor, file) != null &&
             ModuleUtil.findModuleForPsiElement(file) != null;
    }

    @Nullable
    protected PsiElement getReferenceElement(@NotNull final Editor editor, @NotNull final PsiFile file) {
        final VtlReferenceExpression ref = Util.findReferenceExpression(editor, file);
        return ref != null && ref.multiResolve(false).length == 0 && isAvailable(ref) ? ref : null;
    }

    protected boolean isAvailable(@NotNull VtlReferenceExpression ref) {
        return true;
    }

    protected void defineInComment(final Editor editor, final PsiFile fileWithVarReference, final PsiFile fileToInsertComment, final boolean addFileReference) {
        final PsiElement ref = getReferenceElement(editor, fileWithVarReference);
        assert ref != null;
        final Project project = fileWithVarReference.getProject();
        if (!CodeInsightUtilBase.prepareFileForWrite(fileToInsertComment)) {
            return;
        }

        final PsiDocumentManager documentManager = PsiDocumentManager.getInstance(project);
        final Document documentToInsertComment = documentManager.getDocument(fileToInsertComment);
        assert documentToInsertComment != null;
        new WriteCommandAction(project) {
            protected void run(Result result) throws Throwable {
                Editor editor = FileEditorManager.getInstance(project).openTextEditor(new OpenFileDescriptor(project, fileToInsertComment.getViewProvider().getVirtualFile(), 0), true);
                assert editor != null;
                assert documentToInsertComment == editor.getDocument();
                int insertionIndex = documentToInsertComment.getText().startsWith(VtlFileIndex.IMPLICIT_INCLUDE_MARKER) ? VtlFileIndex.IMPLICIT_INCLUDE_MARKER.length() : 0;
                editor.getCaretModel().moveToOffset(insertionIndex);
                TemplateManager manager = TemplateManager.getInstance(project);
                final Template template = manager.createTemplate("", "");
                final String relativePath = addFileReference ? PsiUtil.getRelativePath(fileToInsertComment, fileWithVarReference) : null;
                prepareTemplate(template, ref, relativePath, fileToInsertComment);
                manager.startTemplate(editor, template);
            }
        }.execute();
    }

    protected abstract void prepareTemplate(@NotNull Template template, @NotNull PsiElement element, @Nullable String relativePath, @NotNull PsiFile fileToInsertComment);

    protected void chooseTargetFile(final PsiFile file, final Editor editor, final boolean addFileReference) {
        final Collection<VtlFile> implicitlyIncludedFiles = VtlFileIndex.getImplicitlyIncludedFiles(file);
        if (implicitlyIncludedFiles.size() == 1) {
            defineInComment(editor, file, implicitlyIncludedFiles.iterator().next(), addFileReference);
            return;
        }

        if (implicitlyIncludedFiles.size() < 1) {
            final VtlFile newTargetFile = new WriteCommandAction<VtlFile>(file.getProject()) {
                protected void run(Result<VtlFile> result) throws Throwable {
                    final VirtualFile virtualFile = createVelocityImplicitVmFile();
                    if(virtualFile == null) {
                        return;
                    }
                    VfsUtil.saveText(virtualFile, VtlFileIndex.IMPLICIT_INCLUDE_MARKER);
                    final PsiFile psiFile = file.getManager().findFile(virtualFile);
                    if (psiFile instanceof VtlFile) {
                        result.setResult((VtlFile) psiFile);
                    }
                }

                @Nullable
                private VirtualFile createVelocityImplicitVmFile() throws IOException {
                    final Module module = ModuleUtil.findModuleForPsiElement(file);
                    final VirtualFile[] roots = ModuleRootManager.getInstance(module).getSourceRoots();
                    if(roots.length > 0) {
                        return roots[0].createChildData(this, VELOCITY_IMPLICIT_VM);
                    }
                    final PsiDirectory psiDirectory = file.getContainingDirectory();
                    return psiDirectory == null ? null : psiDirectory.getVirtualFile().createChildData(this, VELOCITY_IMPLICIT_VM);
                }
            }.execute().getResultObject();
            if (newTargetFile != null) {
                defineInComment(editor, file, newTargetFile, addFileReference);
            }
            return;
        }

        final BaseListPopupStep<VtlFile> step = new BaseListPopupStep<VtlFile>(VelocityBundle.message("choose.external.definitions.file"),
                implicitlyIncludedFiles.toArray(new VtlFile[implicitlyIncludedFiles.size()])) {
            @NotNull
            @Override
            public String getTextFor(final VtlFile value) {
                return value.getViewProvider().getVirtualFile().getName();
            }

            @Override
            public PopupStep onChosen(final VtlFile selectedValue, final boolean finalChoice) {
                if (finalChoice) {
                    defineInComment(editor, file, selectedValue, addFileReference);
                }
                return super.onChosen(selectedValue, finalChoice);
            }

            @Override
            public boolean isSpeedSearchEnabled() {
                return true;
            }

            @Override
            public Icon getIconFor(final VtlFile aValue) {
                return Icons.VTL_ICON;
            }
        };
        JBPopupFactory.getInstance().createListPopup(step).showInBestPositionFor(editor);
    }

    public boolean startInWriteAction() {
        return true;
    }
}