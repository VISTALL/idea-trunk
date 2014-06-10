/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.velocity.editorActions;

import com.intellij.codeInsight.editorActions.BackspaceHandlerDelegate;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import static com.intellij.velocity.editorActions.EditorUtil.*;
import com.intellij.velocity.psi.VtlCompositeElementTypes;
import com.intellij.velocity.psi.files.VtlFileViewProvider;


/**
 * @author Alexey Chmutov
 */
public class VelocityBackspaceHandler extends BackspaceHandlerDelegate {
    private int pairedBraceOffset;

    public void beforeCharDeleted(final char c, final PsiFile file, final Editor editor) {
        pairedBraceOffset = -1;
        if (c != '{') {
            return;
        }

        final int offset = editor.getCaretModel().getOffset() - 1;
        if (!(file.getViewProvider() instanceof VtlFileViewProvider) || offset == 0) {
            return;
        }

        final Document document = editor.getDocument();
        PsiDocumentManager.getInstance(file.getProject()).commitDocument(document);
        final PsiElement element = file.findElementAt(offset);
        if (element == null) {
            return;
        }
        PsiElement parent = element.getParent();
        if (getElementType(parent) == VtlCompositeElementTypes.INTERPOLATION) {
            pairedBraceOffset = parent.getTextOffset() + parent.getTextLength() - 1;
        } else {
            final String text = element.getText().trim();
            if ("${".startsWith(text) || "#{".startsWith(text)
                    || "${}".startsWith(text) || "#{}".startsWith(text)) {
                pairedBraceOffset = offset + 1;
            }
        }
        if(pairedBraceOffset != -1 && '}' != getCharAt(document, pairedBraceOffset)) {
            pairedBraceOffset = -1;
        }

    }

    public boolean charDeleted(final char c, final PsiFile file, final Editor editor) {
        if (pairedBraceOffset != -1) {
            final int offset = editor.getCaretModel().getOffset();
            final Document doc = editor.getDocument();
            if (pairedBraceOffset - offset == 1) {
                doc.deleteString(pairedBraceOffset - 2, pairedBraceOffset);
            } else {
                doc.deleteString(pairedBraceOffset - 1, pairedBraceOffset);
            }
            pairedBraceOffset = -1;
            return true;
        }
        return false;
    }
}