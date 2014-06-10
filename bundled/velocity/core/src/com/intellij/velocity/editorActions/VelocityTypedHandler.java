/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.velocity.editorActions;

import com.intellij.codeInsight.editorActions.TypedHandlerDelegate;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import static com.intellij.velocity.editorActions.EditorUtil.*;
import com.intellij.velocity.psi.VtlCompositeElementTypes;

/**
 * @author Alexey Chmutov
 */
public class VelocityTypedHandler extends TypedHandlerDelegate {

    public Result beforeCharTyped(final char c, final Project project, final Editor editor, final PsiFile file, final FileType fileType) {
        if (isNotVtlFile(file, editor)) {
            return Result.CONTINUE;
        }

        final Document document = editor.getDocument();
        final PsiDocumentManager documentManager = PsiDocumentManager.getInstance(project);

        if (c == '(') {
            final int offset = editor.getCaretModel().getOffset();
            documentManager.commitDocument(document);
            char charAtOffset = getCharAt(document, offset);
            if (charAtOffset == '(') {
                typeInStringAndMoveCaret(editor, offset + 1, "(");
                return Result.STOP;
            }
            PsiElement token = file.findElementAt(offset - 1);
            if (token == null) {
                return Result.CONTINUE;
            }
            String tokenText = token.getText().trim();
            if ("#foreach".equals(tokenText) || "#if".equals(tokenText) || "#macro".equals(tokenText)) {
                typeInStringAndMoveCaret(editor, offset + 1, "()\n#end ");
                return Result.STOP;
            }
        } else if(c == '}') {
            final int offset = editor.getCaretModel().getOffset();
            documentManager.commitDocument(document);
            if (getCharAt(document, offset) == '}') {
                editor.getCaretModel().moveToOffset(offset + 1);
                return Result.STOP;
            }
        }

        return Result.CONTINUE;
    }

    public Result charTyped(final char c, final Project project, final Editor editor, final PsiFile file) {
        if (isNotVtlFile(file, editor)) {
            return Result.CONTINUE;
        }

        final PsiDocumentManager documentManager = PsiDocumentManager.getInstance(project);
        final Document document = editor.getDocument();

        if (c == '{') {
            int offset = editor.getCaretModel().getOffset();
            documentManager.commitDocument(document);
            PsiElement element = file.findElementAt(offset - 1);
            if (element != null) {
                final String text = element.getText().trim();
                if ("${".equals(text) || "$!{".equals(text) || "#{".equals(text)) {
                    char charAtOffset = getCharAt(document, offset);
                    String textToInsert = charAtOffset == '{' ? "}$" : "}";
                    PsiElement parent = element.getParent();
                    if (getElementType(parent) == VtlCompositeElementTypes.INTERPOLATION) {
                        offset = parent.getTextOffset() + parent.getTextLength();
                    }
                    document.insertString(offset, textToInsert);
                    return Result.STOP;
                }
            }
        }
        return Result.CONTINUE;
    }

}
