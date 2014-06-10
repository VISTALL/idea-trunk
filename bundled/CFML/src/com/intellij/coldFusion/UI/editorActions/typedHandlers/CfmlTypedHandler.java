package com.intellij.coldFusion.UI.editorActions.typedHandlers;

import com.intellij.codeInsight.editorActions.TypedHandlerDelegate;
import com.intellij.codeInsight.highlighting.BraceMatchingUtil;
import com.intellij.coldFusion.UI.editorActions.matchers.CfmlBraceMatcher;
import com.intellij.coldFusion.UI.editorActions.utils.CfmlEditorUtil;
import com.intellij.coldFusion.model.CfmlLanguage;
import com.intellij.coldFusion.model.CfmlUtil;
import com.intellij.coldFusion.model.files.CfmlFile;
import com.intellij.coldFusion.model.files.CfmlFileType;
import com.intellij.coldFusion.model.psi.CfmlTag;
import com.intellij.coldFusion.model.psi.tokens.CfmlTokenTypes;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.highlighter.HighlighterIterator;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;

/**
 * Created by Lera Nikolaenko
 * Date: 13.10.2008
 */
public class CfmlTypedHandler extends TypedHandlerDelegate {
    public Result beforeCharTyped(final char c, final Project project, final Editor editor, final PsiFile file, final FileType fileType) {
        PsiFile cfmlFile = file.getViewProvider().getPsi(CfmlLanguage.INSTANCE);

        if (isNotCfmlFile(cfmlFile, editor)) {
            return Result.CONTINUE;
        }
        int offset = editor.getCaretModel().getOffset();

        if (c == '{') {
            CfmlBraceMatcher braceMatcher = (CfmlBraceMatcher) BraceMatchingUtil.getBraceMatcher(CfmlFileType.INSTANCE, CfmlLanguage.INSTANCE);
            HighlighterIterator iterator = ((EditorEx) editor).getHighlighter().createIterator(offset);
            if (!braceMatcher.isLBraceToken(iterator, editor.getDocument().getCharsSequence(), fileType)) {
                DocumentUtils.typeInStringAndMoveCaret(editor, offset, "}");
                // return Result.STOP;
            }
            return Result.CONTINUE;
        }
        if (c == '#') {
            if (CfmlEditorUtil.countSharpsBalance(editor) == 0) {
                char charAtOffset = DocumentUtils.getCharAt(editor.getDocument(), offset);
                if (charAtOffset == '#') {
                    editor.getCaretModel().moveToOffset(offset + 1);
                    return Result.STOP;
                }
                DocumentUtils.typeInStringAndMoveCaret(editor, offset, "#");
            }
        } else if (c == '>') {
            if (((EditorEx) editor).getHighlighter().createIterator(editor.getCaretModel().getOffset()).getTokenType() == CfmlTokenTypes.COMMENT ||
                    ((EditorEx) editor).getHighlighter().createIterator(editor.getCaretModel().getOffset()).getTokenType().getLanguage() != CfmlLanguage.INSTANCE) {
                return Result.CONTINUE;
            }
            insertCloseTagIfNeeded(editor, cfmlFile, project);
            return Result.STOP;
        }
        return Result.CONTINUE;
    }

    public static boolean insertCloseTagIfNeeded(Editor editor, PsiFile file, Project project) {
        final Document document = editor.getDocument();
        final PsiDocumentManager documentManager = PsiDocumentManager.getInstance(project);

        int offset = editor.getCaretModel().getOffset();
        documentManager.commitDocument(document);
        char charAtOffset = DocumentUtils.getCharAt(document, offset);

        if (charAtOffset != '>') {
            DocumentUtils.typeInStringAndMoveCaret(editor, offset, ">");
        }
        editor.getCaretModel().moveToOffset(++offset);
        if (DocumentUtils.getCharAt(document, offset - 2) == '/') {
            return false;
        }
        HighlighterIterator iterator = ((EditorEx) editor).getHighlighter().createIterator(offset - 2);

        while (!iterator.atEnd() && !iterator.getTokenType().equals(CfmlTokenTypes.CF_TAG_NAME)) {
            if (CfmlUtil.isControlToken(iterator.getTokenType())) {
                return false;
            }
            iterator.retreat();
        }
        if (!iterator.atEnd()) {
            iterator.retreat();
            if (!iterator.atEnd() && iterator.getTokenType().equals(CfmlTokenTypes.LSLASH_ANGLEBRACKET)) {
                return false;
            }
            iterator.advance();
        }
        if (iterator.atEnd()) {
          return false;
        }
        String tagName = document.getCharsSequence().subSequence(iterator.getStart(), iterator.getEnd()).toString();
        if (CfmlUtil.isSingleCfmlTag(tagName) || CfmlUtil.isUserDefined(tagName)) {
            return false;
        }
        PsiElement tagElement = file.findElementAt(iterator.getStart());
        while (tagElement != null && !(tagElement instanceof CfmlTag)) {
            tagElement = tagElement.getParent();
        }
        if (tagElement == null) {
            return false;
        }
        boolean doInsertion = false;
        if (tagElement.getLastChild() instanceof PsiErrorElement) {
            doInsertion = true;
        } else {
            iterator = ((EditorEx) editor).getHighlighter().createIterator(0);
            while (iterator.getStart() < offset) {
                if (iterator.getTokenType() == CfmlTokenTypes.CF_TAG_NAME) {
                    String currentTagName = document.getCharsSequence().subSequence(iterator.getStart(), iterator.getEnd()).toString();
                    if (tagName.equals(currentTagName)) {
                        PsiElement currentTagElement = file.findElementAt(iterator.getStart());
                        currentTagElement = PsiTreeUtil.getParentOfType(currentTagElement, CfmlTag.class);
                        if (currentTagElement.getLastChild() instanceof PsiErrorElement) {
                            doInsertion = true;
                            break;
                        }
                    }
                }
                iterator.advance();
            }
        }
        if (doInsertion) {
            DocumentUtils.typeInStringAndMoveCaret(editor, offset, "</" + ((CfmlTag) tagElement).getTagName() + ">");
            return true;
        }
        return false;
    }


    public Result charTyped(final char c, final Project project, final Editor editor, final PsiFile file) {
        if (isNotCfmlFile(file, editor)) {
            return Result.CONTINUE;
        }
        return Result.CONTINUE;
    }

    static boolean isNotCfmlFile(final PsiFile file, final Editor editor) {
        return !(file instanceof CfmlFile)
                || editor.getCaretModel().getOffset() == 0;
    }
}
