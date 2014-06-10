/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.velocity.inspections;

import com.intellij.codeInsight.template.Expression;
import com.intellij.codeInsight.template.Template;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.Function;
import com.intellij.util.IncorrectOperationException;
import com.intellij.velocity.VelocityBundle;
import com.intellij.velocity.psi.PsiUtil;
import com.intellij.velocity.psi.VtlLanguage;
import com.intellij.velocity.psi.directives.VtlMacroCall;
import com.intellij.velocity.psi.files.VtlFile;
import com.intellij.velocity.psi.reference.VtlReferenceExpression;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author Alexey Chmutov
 */
public abstract class DefineMacroLibraryRefIntention extends DefineInCommentIntention {
    public DefineMacroLibraryRefIntention(@NotNull String text) {
        super(text, VelocityBundle.message("add.macro.library.ref.fix.name"));
    }

    @Override
    protected boolean isAvailable(@NotNull VtlReferenceExpression ref) {
        return ref.getParent() instanceof VtlMacroCall && Util.canSetVelocityProperties(ref.getContainingFile());
    }

    protected void prepareTemplate(@NotNull Template template, @NotNull final PsiElement element, String relativePath, @NotNull final PsiFile fileToInsertComment) {
        assert element instanceof VtlReferenceExpression;
        final List<String> allFiles = Util.collectFilePaths(element, new Function<PsiFile, String>() {
            public String fun(@NotNull final PsiFile psiFile) {
                PsiFile file = psiFile.getViewProvider().getPsi(VtlLanguage.INSTANCE);
                if (file instanceof VtlFile) {
                    VtlFile vtlFile = (VtlFile) file;
                    if (vtlFile.getNumberOfMacros(((VtlReferenceExpression)element).getReferenceName()) > 0) {
                        return PsiUtil.getRelativePath(fileToInsertComment, vtlFile);
                    }
                }
                return null;
            }
        });

        template.addTextSegment("#* @vtlmacrolibrary path=\"");
        final Expression pathExpression = new StringCollectionExpression(allFiles);
        template.addVariable("PATH", pathExpression, pathExpression, true);
        final String fileRef = relativePath != null ? " file=\"" + relativePath + "\"" : "";
        template.addTextSegment("\"" + fileRef + " *#\n");
        template.addEndVariable();
    }

    public static class Local extends DefineMacroLibraryRefIntention {
        public Local() {
            super(VelocityBundle.message("add.macro.library.ref.fix.name.local"));
        }

        public void invoke(@NotNull final Project project, final Editor editor, final PsiFile file) throws IncorrectOperationException {
            defineInComment(editor, file, file, false);
        }
    }

    public static class LocalExternal extends DefineMacroLibraryRefIntention {
        public LocalExternal() {
            super(VelocityBundle.message("add.macro.library.ref.fix.name.external"));
        }

        public void invoke(@NotNull final Project project, final Editor editor, final PsiFile file) throws IncorrectOperationException {
            chooseTargetFile(file, editor, true);
        }
    }

    public static class ModuleWide extends DefineMacroLibraryRefIntention {
        public ModuleWide() {
            super(VelocityBundle.message("add.macro.library.ref.fix.name.module.wide"));
        }

        public void invoke(@NotNull final Project project, final Editor editor, final PsiFile file) throws IncorrectOperationException {
            chooseTargetFile(file, editor, false);
        }
    }
}