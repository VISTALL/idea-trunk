/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.velocity.inspections;

import com.intellij.codeInsight.template.Expression;
import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.impl.MacroCallNode;
import com.intellij.codeInsight.template.macro.CompleteSmartMacro;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import com.intellij.velocity.VelocityBundle;
import com.intellij.velocity.psi.directives.VtlMacroCall;
import com.intellij.velocity.psi.reference.VtlReferenceExpression;
import org.jetbrains.annotations.NotNull;

/**
 * @author Alexey Chmutov
 */
public abstract class DefineImplicitVariableIntention extends DefineInCommentIntention {
    public DefineImplicitVariableIntention(@NotNull String text) {
        super(text, VelocityBundle.message("add.implicit.variable.fix.name"));
    }

    protected void prepareTemplate(@NotNull Template template, @NotNull final PsiElement element, String relativePath, @NotNull PsiFile fileToInsertComment) {
        final String varName = element.getText();
        assert varName != null;

        final Expression typeExpression = new MacroCallNode(new CompleteSmartMacro());

        template.addTextSegment("#* @vtlvariable name=\"" + varName + "\" type=\"");
        template.addVariable("PATH", typeExpression, typeExpression, true);

        final String fileRef = relativePath != null ? " file=\"" + relativePath + "\"" : "";
        template.addTextSegment("\"" + fileRef + " *#\n");
        template.addEndVariable();
    }

    @Override
    protected boolean isAvailable(@NotNull VtlReferenceExpression ref) {
        return !(ref.getParent() instanceof VtlMacroCall) && !ref.hasQualifier();
    }

    public static class Local extends DefineImplicitVariableIntention {
        public Local() {
            super(VelocityBundle.message("add.implicit.variable.fix.name.local"));
        }

        public void invoke(@NotNull final Project project, final Editor editor, final PsiFile file) throws IncorrectOperationException {
            defineInComment(editor, file, file, false);
        }
    }

    public static class LocalExternal extends DefineImplicitVariableIntention {
        public LocalExternal() {
            super(VelocityBundle.message("add.implicit.variable.fix.name.external"));
        }

        public void invoke(@NotNull final Project project, final Editor editor, final PsiFile file) throws IncorrectOperationException {
            chooseTargetFile(file, editor, true);
        }
    }

    public static class ModuleWide extends DefineImplicitVariableIntention {
        public ModuleWide() {
            super(VelocityBundle.message("add.implicit.variable.fix.name.module.wide"));
        }

        public void invoke(@NotNull final Project project, final Editor editor, final PsiFile file) throws IncorrectOperationException {
            chooseTargetFile(file, editor, false);
        }
    }
}